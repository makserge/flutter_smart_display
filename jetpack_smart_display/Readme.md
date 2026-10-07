# Android app for Android wall panel 

Sounds for alarm and timer taken from LOS 
https://github.com/EvilScout/Lineage-OS-Sounds

Thanks for clock skins:

Analog clock
https://github.com/schorschii/FsClock-Android
https://github.com/firebirdberlin/nightdream
https://github.com/arbelkilani/Clock-view
https://github.com/chenglei1986/ClockView
https://github.com/akshay2211/JetAlarm

Digital clock
https://github.com/EscapeIndustries/DotMatrixView
https://github.com/dbore/Android-Digital-Clock
https://github.com/SinoReimu/DigitalNumber-android
https://github.com/xenione/tab-digit

Fonts from
https://github.com/keshikan/DSEG

M3U parser from
https://github.com/BjoernPetersen/m3u-parser

Weather screen based on
https://github.com/ramzan/Atmostate

MPD related code from
https://github.com/thunderace/mpd-control
https://github.com/20centaurifux/mpcw.client

ASR service from
https://github.com/alphacep/vosk-android-demo

Voice model for ASR taken from
https://github.com/janvarev/Irene-Voice-Assistant

Sounds are taken from

https://github.com/Kitt-AI/snowboy

## Building

| Tool | Version |
|---|---|
| Gradle (wrapper) | 9.8.0 |
| Android Gradle Plugin | 9.4.1 (built-in Kotlin support, no `kotlin-android` plugin) |
| Kotlin / Compose compiler plugin | 2.4.20 |
| KSP | 2.3.12 |
| Compose | BOM 2026.09.00 (ui, foundation, material 1.12.1) |
| Media3 | 1.11.1 |
| libVLC (doorbell video) | 3.7.7 (`libvlc-all`) |
| JDK | 17 or newer (the Android Studio JBR works) |
| compileSdk / targetSdk / minSdk | 37 / 37 / 26 |

All versions live in `gradle/libs.versions.toml`.

The build reads `local.properties` in the project root. Besides `sdk.dir` it needs the OpenWeatherMap key, written with quotes because it is pasted into `BuildConfig` as a Java string literal:

```properties
sdk.dir=C:/Users/<you>/AppData/Local/Android/Sdk
OWM_API_KEY="<your OpenWeatherMap API key>"
```

Build with `./gradlew :app:assembleDebug` (or `assembleRelease`). Run the JVM unit tests with `./gradlew :app:testDebugUnitTest`; they cover the matrix clock, the 12/24-hour display, the radio settings, the daily random clock, the M3U parser, time formatting and the reconnect rules of the doorbell stream.

An APK build makes one APK per ABI, `app-armeabi-v7a-<build type>.apk`, `app-arm64-v8a-…`, `app-x86-…` and `app-x86_64-…`, and the universal `app-universal-<build type>.apk` with all four (an unsigned release build, such as `assembleRelease` without a signing config, ends in `-release-unsigned.apk`); there is no `app-debug.apk` any more. libVLC's native libraries are most of the app, so a panel that installs the APK by hand can take the one of its ABI (a debug build: about 105 to 122 MB instead of 291 MB). `./gradlew :app:installDebug` installs the APK that fits the connected device. An app bundle (`bundleRelease`) is not affected: Play delivers the libraries per ABI from it.

## Settings

Settings are stored in DataStore and edited through the in-project package `ui/common/prefs`, which replaced the abandoned ComposePrefs library. Keys and stored value types are the same as before, so an update keeps all settings.

A change applies at once, without restarting the app: the parts that use a setting follow it (`utils/PreferenceObserver.kt`). This covers the MQTT broker and topics, the push-button, dimmer, light and proximity settings, message alerts, voice control and the wake word, the light sensor, the weather location, the alarm and timer volumes, the radio type and the MPD server (see Radio) and all clock settings. The alarm and timer timeouts and the alarm light settings are read when a ring starts, so a change applies from the next ring.

## Radio

The radio plays only while the radio page is the page the dashboard pager has come to rest on. Leaving the page stops it; coming back starts the saved station again. Passing over the radio page during a jump to another page does not start it.

- `RadioMediaServiceHandler` (singleton) owns playback: play a station, resume, stop, next/previous (wrapping around the playlist), volume. `RadioActiveState` holds whether the radio is on; the dashboard observes it without creating the player.
- `RadioMediaService` exposes the player as a Media3 `MediaSession` while the radio page is shown. Media3 posts the media notification and moves the service in and out of the foreground. The shared player is never released by the service.
- While the voice assistant listens after the wake word, the radio is ducked (lowered) through a transient audio-focus request and restored afterwards. Wake-word/error chimes and MQTT message sounds play without taking audio focus, so they never pause the radio.
- A dead or unreachable station (for example HTTP 404) stops the radio and shows the stopped state; pick another station.
- With the MPD radio type, `MPDPlayer` is a Media3 `SimpleBasePlayer`: the playlist is MPD's queue, the current item is MPD's current song and play/pause is MPD's play state. Commands run one at a time on one MPD thread; a status monitor on its own connection reports changes made by MPD itself or by other clients. Next/previous wrap around the queue; used before the queue is known (first use after app start or after a change of the radio settings), they start MPD and step once the queue arrives.
- Every step of the MPD client has a timeout: 5 s to connect and for the `OK MPD` greeting, 10 s for a command. A server that cannot be reached, sends no greeting (a frozen MPD or another service on the port) or rejects the password makes the radio page open Settings. A command that gets no answer stops the radio.
- The radio player is created from the radio type (internal player or MPD) and the MPD server settings on the first visit to the radio page. A later change of them applies at once: `RadioMediaServiceHandler` builds a new player (`RadioPlayerFactory`), stops the old one (an MPD server too) and releases it, and the media session moves to the new player. The radio carries on with the new player if it was playing; a change made in Settings finds it off, so the new player starts when the radio page is shown again.
- The internal station list and the MPD queue each keep their own saved station, so switching the radio type and back resumes each at its own station. The media session and its notification exist only for the internal player.
- The volume slider of the radio page (tap the page to show it) and the voice volume commands are even in loudness (`service/radio/RadioVolume.kt`). On the internal player 0 is mute and 1 to 100 % go from -40 dB to 0 dB, 0.4 dB per percent, so a voice step of 5 % is 2 dB at any volume (over the raw gain it was 6 dB near the bottom and 0.4 dB near the top). With MPD the slider is MPD's own volume percent (`setvol`), because MPD's mixers (software, ALSA with dB data, PulseAudio, PipeWire) already turn it into a loudness curve. The internal player starts at full volume, as before.

## Alarms

Alarms are scheduled as exact alarm-clock entries (`AlarmManager.setAlarmClock`) one day at a time; when an alarm goes off, the next day is booked. The previous daily `setRepeating` alarms were inexact and could be delivered hours late. On Android 12 without the exact-alarm permission they fall back to a 10-minute window.

The ringing alarm is kept as state in `AlarmHandler` (`RingingAlarm`: the alarm, when it started ringing and its timeout), so it also rings when the Alarms page has not been opened since the app started (for example after a reboot). Such an alarm uses the saved light, volume and timeout settings too; the timeout is read when the alarm goes off. `AlarmHandler` ends the ring at its deadline, also when no dashboard shows it. Saying "выключи радио" stops a ringing alarm that plays a radio station.

An alarm or a timer that goes off while Settings or the doorbell screen is open is shown when the dashboard comes back, if it is still ringing. When the dashboard is replaced while an alarm rings (see Home screen), the new dashboard rings it on for the time that is left, and the volume fade-in goes on where it would be now. A ring that ended while no dashboard existed is not played later. The alarm and timer players are released with their dashboard. When an alarm and a timer alert overlap, the newest one is shown; when it ends, the other one comes back if it is still going on.

Timer alerts work the same way: `TimerHandler` holds the alert (`RingingTimer`) and ends it after the timer timeout from Settings, and a new dashboard shows it for the time that is left. A finished timer is idle again at once, so it can be restarted while its alert sounds. Each timer has at most one countdown, and a pause keeps the exact time left.

The alarm light belongs to the ringing alarm: `AlarmWakeLight` is owned by `AlarmHandler`, so the light also works when the Alarms page was never opened, and a dashboard replacement does not switch it off and on again. While the alarm rings, the app waits for the light sensor to report a dark room once, then sends the push-button "on" command or fades the dimmer in over 30 seconds (see below). Dismissing the alarm, by hand, by voice or by the alarm timeout, ends the wait and stops the fade before the dimmer "off" command is sent. The "off" command is only sent when the alarm switched the dimmer on, so a light that was already on stays on. While the light sensor is switched off there is no reading, so the light is not switched on.

Both alarm fades are even in what is perceived, not in the raw value (`utils/FadeCurves.kt`); linear ramps seemed to jump up within seconds and then hardly change. The alarm sound fades in over 15 seconds from silence to the alarm volume. The player volume is a linear gain, but loudness follows decibels, so the level rises by the same number of decibels per 100 ms step, starting 40 dB below the alarm volume (as AOSP DeskClock does): -30 dB after a quarter of the fade, -20 dB after half, -10 dB after three quarters (the linear ramp was at -12 dB after a quarter and -6 dB after half). The fade follows the time since the ring started, so a new dashboard continues it on the curve. The volume sliders in Settings (alarm, timer, message alert, voice chime) are even in loudness as well: they share one scale (`VOLUME_SETTING_SCALE`) that moves the level in equal dB steps from -20 dB (gain 0.1) to 0 dB. The settings still store the gain, so saved volumes sound the same; their thumb just sits further right (the alarm default 0.2 at 30 %, the old middle at about three quarters). The dimmer level follows CIE lightness (L*), which looks even: 1 % after 1.4 s, 4 % after 7.5 s, 18 % after 15 s, 48 % after 22.5 s, 100 % after 30 s. It is sent as an integer percent like the voice dimmer commands, and only when it changes. A dimmer whose firmware corrects brightness itself (Tasmota with its default `LedTable 1`, ESPHome `gamma_correct`) gets the correction twice and stays dim for most of the fade. To tell: at level 50 such a dimmer looks about half bright, an uncorrected one about three quarters.

## Doorbell

A message on the doorbell topic opens the doorbell screen. It starts the camera stream once per visit and goes back after the back-timer delay from Settings, or on a tap. The doorbell page of the dashboard pager starts the stream when it is shown and stops it when it is left. A second ring while the doorbell screen fades in does not open a second one.

The doorbell page and the doorbell screen share `ui/screen/doorbell/DoorbellStreamPlayer.kt`. Only the screen in front (resumed) streams. Each visit gets an ownership token: a new visit detaches the old view before it attaches its own, and only the owner stops the stream, so the doorbell page of a dashboard that is fading out never takes the stream away from the doorbell screen fading in.

The camera plays with libVLC 3.7.7, over RTSP (always TCP), HTTP (for example MJPEG) or HLS; `rtsps://` (RTSP over TLS) is not supported by libVLC 3, and Settings says so under the stream URL. Such a URL, or an empty one, is not played. The stream is tuned for low latency (500 ms network caching, hardware decoding where the device has it, 32-bit colour for software-decoded pictures), and the picture is fitted to the shape of its view (libVLC 3 would take a square panel for portrait and shrink the picture on the doorbell page). The LibVLC instance is created once, at app start, on a background thread. Every connection attempt gets a new MediaPlayer; the old one is muted and detached at once, then stopped and released in the background, each on its own thread, so a camera that does not answer cannot freeze the screen, and a stop that never returns (libVLC 3 can wait for an RTSP camera's answer without a limit) holds up no other. While a visit lasts, the stream reconnects after an error, the end of the stream, a stream that has not opened after 15 s, or no progress for 10 s (frozen time or picture): 1 s after the first failure, then 2, 4, 8 and at most 10 s, and 1 s again after 30 s of playback. A stream that has not played since the visit started (most likely a wrong URL or camera password) is tried less often from its sixth failure in a row: after 1, 2 and 4 min, then every 5 min, and the log says once to check the URL and the password. The timings are in `DoorbellStreamPolicy.kt`.

## Home screen

The app is meant to be the panel's home screen. `MainActivity` uses the `singleTask` launch mode, so a launch from the app drawer, a notification or the system's next-alarm entry reaches the running dashboard. Android still creates a second activity for a HOME launch while the app runs in a normal task, for example when it was started from the app drawer and the home key is pressed. The new activity then closes the older one. Two dashboards would both ring alarms, show the doorbell and run every voice command.

## Sensors

Bluetooth (BLE) sensors are read from advertisements. One scan serves both the sensor list and the device picker in the sensor editor; it stops when neither needs it any more. Android stops delivering results to a scan that runs longer than 30 minutes, so a running scan is restarted every 10 minutes. Sensor readings (MQTT and BLE) are kept in an immutable `MQTTData`, so every new value is shown immediately.

The panel's own light and proximity sensors are read by `SensorService`, a `connectedDevice` foreground service that relays them to MQTT. It needs no runtime permission: on Android 14+ the install-time `CHANGE_NETWORK_STATE` permission satisfies the foreground-service prerequisite. If Android refuses the foreground start, the service keeps running as a normal service, so the sensors still report while the app is shown.

The light sensor switch and its reporting interval apply at once. While the switch is off, the sensor listener is not registered and the service reports no reading (0). An interval that is not a number falls back to the default of 5 seconds.

## MQTT

- Each panel connects with its own client id, `SmartDisplay-` followed by the device's `ANDROID_ID` (23 characters). Brokers drop an existing connection when another client uses the same id, so panels must not share one.
- On every connect and reconnect the dashboard subscribes to the doorbell, push-button status and message topics (from Settings, or their defaults) and to the topics of all MQTT sensors. A broker that lost its sessions (restart without persistence) is therefore handled.
- When the broker cannot be reached at start, the connection is retried with backoff (10 s up to 5 min). After the first successful connection, paho's automatic reconnect takes over.
- Retained messages on the doorbell and message topics are ignored: the broker replays them on every subscribe, so they are not new events. A retained push-button status is used.
- A change of the broker host, port, login or password in Settings reconnects at once. A connected client disconnects without paho's quiesce wait; a client that is still in paho's reconnect cycle is reused with the new address. An address paho cannot use (for example `tcp://host` or `host/` in the host field) is logged, and nothing connects until it is corrected.
- `DashboardMqttManager` is the only owner of the subscriptions. It follows the topic settings and the list of MQTT sensors, subscribes new topics at once and unsubscribes only topics that no dashboard setting and no sensor uses any more. The Settings screen and the sensor editor no longer subscribe on their own.

## Weather

The Weather page shows the saved forecast at once. A one-line warning above it says when the last update failed or the forecast is from an earlier day. Without saved data the page shows a spinner for at most 15 seconds, then the reason (no connection, API key rejected, server error, invalid response) and a "Retry now" button.

Updates run through WorkManager and wait for a network. Opening the page requests an update when the location changed or there was no successful update in the last 30 minutes; a failed update is retried with backoff from 30 seconds. A periodic update runs every 4 hours. Requests time out after 30 seconds, and an incomplete response never replaces saved data.

Latitude and longitude accept a decimal comma ("48,13"); an empty field means the default location. An invalid location, or 0, shows a message with an "Open settings" button.

## Clock

The clock page shows one of ten clock types, chosen in Settings (General tab, Clock). Every clock is sized from the page it is drawn in, so it fits portrait panels as well as small (800x480) and large (2560x1600) ones. The time comes from one ticker in `ClockViewModel` that runs only while the clock is shown and is aligned to the wall clock, so the seconds change on time.

- **Clock type**: the default is "Digital (Matrix)". A panel that already has a saved clock type keeps it.
- **12/24-hour format**: the digital clocks follow the system setting "Use 24-hour format", also when it changes while the clock is shown. In 12-hour format the hour is 1-12 without a leading zero; Digital Clock adds a small AM/PM and keeps the place of the hour's tens, so nothing moves at 10:00 and 1:00.
- **Random clock every day** (default on): every day at 00:00 the clock page switches to a random other clock type (`service/clock/DailyClockChanger.kt`). The day of the last switch is stored, so a panel that was off at midnight switches when the app starts again. After the setting is switched on, the first switch is at the next midnight; switched off, the clock type stays as it is. A clock type picked by hand stays until the next midnight.
- **Matrix clock**:
  - **Blink separator** (default on): the ":" is lit in the first half of every second and dark in the second half.
  - **Dot color** (default turquoise green, `#03DAC5`, the theme's teal200): the matrix uses this color instead of the shared clock colors. Unlit dots are not drawn, so they show the background.
  - The time is centred horizontally on its lit dots, so a dark tens digit before 10:00 or a leading "1" does not push it off-centre. The dot size comes from the whole matrix and never changes; the time moves sideways only at the hours that change its first lit column (0, 1, 2, 3, 4, 7, 8, 10 and 20 o'clock). Vertically it stays centred on the full digit height.
  - The dot radius and spacing settings are in dp and are maximums: on a smaller page the matrix shrinks to fit.
- **Size settings of the other clocks** follow the page or the dial instead of fixed pixels: the flip clock's "Digit size" is the share of the largest size that fits; the text sizes and spacing of Digital Clock are maximums; "Font size" of Digital Clock 2 is the share of the largest size that fits; the sliders of ClockView2, JetAlarm and the rectangular clock scale with their dial or artwork.

## Adding a New Voice Command

Speech is recognized offline by Vosk (Russian model). All command phrases are hardcoded Russian strings in `app/src/main/res/values/strings.xml` — there is no localization split (no `values-ru`, `values-en`, etc.).

### Pipeline overview

```
Vosk recognizer, fed by GuardedSpeechService (the microphone loop: an audio error, e.g. an
  audio service restart, ends the loop and the service restarts recognition instead of crashing)
  -> SpeechRecognitionService (emits SpeechRecognitionState.Result(word) into the hot,
     app-wide SpeechRecognitionHandler.speechRecognitionState flow, so restarting the
     service when ASR is switched off and on keeps voice control working)
  -> AsrController.onRecognitionState()  (ducks the radio while listening, calls processCommand(...))
  -> ProcessCommand.kt processCommand()  (parses raw text -> AsrCommand + payload)
  -> DashboardViewModel.processAsrCommand()  (when-block: AsrCommand -> action)
  -> action (MQTT publish, ViewModel call, page navigation, etc.)
```

Two different dispatch tables are involved and are easy to confuse:

- `ProcessCommand.kt` — parses the *raw recognized string* into an `AsrCommand` + payload. This is a chain of registry lookups, not a single map.
- `DashboardViewModel.processAsrCommand()` — a `when` block on `AsrCommand` that performs the actual action.

### Key files

| File | Role |
|---|---|
| `data/AsrCommand.kt` | Enum of command *categories* (e.g. `LIGHT1`, `DIMMER_LIGHT_STEP`, `TIMER`, `PAGE`). No logic — just the vocabulary `processAsrCommand` switches on. |
| `data/VoiceCommandType.kt` | Registry for page-navigation / screen-action commands. Maps phrase synonyms to a `DashboardItem`. |
| `data/VoiceCommand.kt` | Data class carried through app state: `type`, optional `payload: String?`, `timeStamp`. It is a one-shot command: `DashboardViewModel.voiceCommandState` is `null` when nothing is pending, the target page clears it after handling it (`onResetCommand` / `onCommandHandled`), and a pending command is dropped when the pager settles on a different page. |
| `data/LightCommandType.kt` | Richest registry pattern — demonstrates on/off, step (+1/-1), and "set to X" prefix commands. |
| `data/LightBrightnessType.kt`, `data/TimerDurationType.kt` | "Value registries" — resolve a stripped-prefix remainder string (e.g. "на пятьдесят процентов") to a concrete value. |
| `service/asr/ProcessCommand.kt` | `processCommand(context, command, onCommand)` — ordered chain of registry lookups; first match wins. |
| `ui/screen/dashboard/DashboardViewModel.kt` | `processAsrCommand()` — final `when` block that performs the action for each `AsrCommand`. |
| `app/src/main/res/values/strings.xml` | All command phrase strings, roughly lines 195–256, named `<feature>_<variant>_command`. |

### Existing ASR commands

All phrases are Russian, defined in `app/src/main/res/values/strings.xml`. "Registry" below is the enum that owns the phrase (see Key files above); "Action" is the resulting behavior.

**Page navigation** (`VoiceCommandType`, dispatched as `AsrCommand.PAGE`) — one phrase per page, plus a synonym pair for radio on/off:

| Phrase(s) | Registry entry | Action |
|---|---|---|
| "часы" | `CLOCK` | Navigate to Clock screen |
| "погода" | `WEATHER` | Navigate to Weather screen |
| "датчики" | `SENSORS` | Navigate to Sensors screen |
| "радио" | `INTERNET_RADIO` | Navigate to Radio screen; the saved station starts (a playing station is not restarted) |
| "будильник" | `ALARM` | Navigate to Alarms screen |
| "таймер" | `TIMER` | Navigate to Timers screen |
| "звонок" | `DOORBELL` | Navigate to Doorbell screen |
| "включи радио" / "включить радио" | `INTERNET_RADIO_ON` | Navigate to Radio screen; the saved station starts (a playing station is not restarted) |
| "выключи радио" / "выключить радио" | `INTERNET_RADIO_OFF` | Handled in `DashboardViewModel.switchRadioOff()`: stops the radio where it is and **never navigates**; also stops a ringing alarm that plays a radio station and cancels a radio command that has not reached the radio page yet |
| "радио назад" | `INTERNET_RADIO_PREV_ITEM` | Navigate to Radio screen if needed, previous station (wraps around) |
| "радио вперёд" | `INTERNET_RADIO_NEXT_ITEM` | Navigate to Radio screen if needed, next station (wraps around) |
| "радио тише" | `INTERNET_RADIO_VOL_DOWN` | Navigate to Radio screen if needed (the radio starts), volume down 5 % (2 dB on the internal player) |
| "радио громче" | `INTERNET_RADIO_VOL_UP` | Navigate to Radio screen if needed (the radio starts), volume up 5 % (2 dB on the internal player) |

**Lights** (`LightCommandType`, on/off matched via `AsrCommand.LIGHT1`/`LIGHT2`):

| Phrase(s) | Registry entry | Action |
|---|---|---|
| "включи бра" / "включить бра" | `LIGHT1` on | `sendPressButtonEvent(true)` |
| "выключи бра" / "выключить бра" | `LIGHT1` off | `sendPressButtonEvent(false)` |
| "включи свет" / "включить свет" | `LIGHT2` on | `sendProximityButtonEvent(true)` |
| "выключи свет" / "выключить свет" | `LIGHT2` off | `sendProximityButtonEvent(false)` |

**Dimmer light** (`LightCommandType.DIMMER_LIGHT`, demonstrates on/off + step + set-prefix):

| Phrase(s) | AsrCommand | Payload | Action |
|---|---|---|---|
| "включи подсветку" | `DIMMER_LIGHT` | `true` | `sendDimmerLightPowerEvent(true)` |
| "выключи подсветку" | `DIMMER_LIGHT` | `false` | `sendDimmerLightPowerEvent(false)` |
| "подсветка ярче" | `DIMMER_LIGHT_STEP` | `+1` | `processDimmerLightStepCommand(1)` |
| "подсветка темнее" | `DIMMER_LIGHT_STEP` | `-1` | `processDimmerLightStepCommand(-1)` |
| "подсветка на " + one of the values below | `DIMMER_LIGHT_SET` | remainder string | `processDimmerLightSetCommand(...)`, resolved against `LightBrightnessType` |

`LightBrightnessType` values usable after the "подсветка на" prefix: "на десять процентов" (10%) ... "на двадцать процентов" (20%), "на тридцать процентов" (30%), "на сорок процентов" (40%), "на пятьдесят процентов" (50%), "на шестьдесят процентов" (60%), "на семьдесят процентов" (70%), "на восемьдесят процентов" (80%), "на девяносто процентов" (90%), "на сто процентов" (100%).

**Timers** (prefix loop in `ProcessCommand.kt` + `TimerDurationType`):

| Phrase(s) | AsrCommand | Payload | Action |
|---|---|---|---|
| "поставь таймер" / "поставить таймер" + one of the durations below | `TIMER` | remainder string | `processAsrTimerCommand(...)`, resolved against `TimerDurationType` |

`TimerDurationType` values usable after the "поставь(ть) таймер" prefix: "на одну минуту" (1 min), "на две минуты" (2 min), "на три минуты" (3 min), "на четыре минуты" (4 min), "на пять минут" (5 min), "на шесть минут" (6 min), "на семь минут" (7 min), "на восемь минут" (8 min), "на девять минут" (9 min), "на десять минут" (10 min), "на пятнадцать минут" (15 min, default), "на двадцать минут" (20 min), "на тридцать минут" (30 min), "на сорок пять минут" (45 min), "на шестьдесят минут" (60 min), "на девяносто минут" (90 min), "на сто двадцать минут" (120 min), "на сто пятьдесят минут" (150 min), "на сто восемьдесят минут" (180 min).

### Two ways to add a command

#### A. Simple command with no payload (navigate to a page, or an existing on/off action)

Use this for something like "open weather screen" or a plain on/off toggle that fits an existing registry.

1. Add the phrase string(s) to `strings.xml`, e.g.:
   ```xml
   <string name="fan_on_command">включи вентилятор</string>
   <string name="fan_on2_command">включить вентилятор</string>
   ```
2. Add an entry to `VoiceCommandType.kt` (for page navigation) or extend an existing registry like `LightCommandType.kt` (for on/off toggles) with the new resource IDs.
3. If it's a new *category* of action (not reusing an existing `AsrCommand`), add a constant to `AsrCommand.kt`.
4. Add a `when` branch in `DashboardViewModel.processAsrCommand()` that performs the action.

#### B. New registry-style command with a variable payload (step / set-to-value)

Use this pattern (modeled on `LightCommandType` + `LightBrightnessType`) when the command needs a direction (step up/down) or a numeric/enumerated value (set to X).

1. **Add `AsrCommand` constant(s)** in `data/AsrCommand.kt` for the new action(s), e.g. `FAN_SET`, `FAN_STEP`.
2. **Add or extend a registry enum** (like `LightCommandType`) whose constructor holds:
   - lists of phrase-resource IDs per category (on/off, step-up/step-down, set-prefix)
   - the `AsrCommand` to emit for each category

   Add companion-object `match()` / `matchStep()` / `matchSetPrefix()` functions that lazily build a `phrase -> (AsrCommand, payload)` cache via `context.getString(id)`, mirroring `LightCommandType`'s `onOffCache` / `stepCache` / `setPrefixCache`.
3. **If the command takes a value** (not just a direction), add a small "value registry" enum like `LightBrightnessType` or `TimerDurationType` that maps a phrase to a concrete value (e.g. percent, duration). This is resolved *downstream in the ViewModel*, not inside `ProcessCommand.kt`.
4. **Wire the new match functions into `ProcessCommand.kt`**, inserted before the final `VoiceCommandType` fallback:
   ```kotlin
   FanCommandType.match(context, command)?.let { (asrCommand, isOn) ->
       onCommand(asrCommand, isOn)
       return
   }
   ```
   Order matters: prefix-style ("set to X") matches use `startsWith`, so they must be checked before the exact-match fallback (`VoiceCommandType.getDashboardItem`), which is always last.
5. **Add phrase strings** to `strings.xml` for every synonym referenced by the new registry entries.
6. **Add `when` branches** in `DashboardViewModel.processAsrCommand()` for each new `AsrCommand`, casting the payload to its expected type (`Boolean`, `Int`, or `String`):
   ```kotlin
   AsrCommand.FAN_SET -> processFanSetCommand(params as String)
   AsrCommand.FAN_STEP -> processFanStepCommand(params as Int)
   ```
7. **Implement the action** (e.g. an MQTT publish via `publishMQTT(topic, messagePayload)`, following the pattern in `sendDimmerLightPowerEvent()`).

### Worked example: dimmer light on/off

"включи подсветку" ("turn on the backlight"):

1. Vosk recognizes the phrase; `SpeechRecognitionService` emits `SpeechRecognitionState.Result(word = "включи подсветку")`.
2. `DashboardViewModel.initASR()` calls `processCommand(context, command, onCommand = ...)`.
3. `ProcessCommand.kt` checks `LightCommandType.match(context, command)` first — it matches `LightCommandType.DIMMER_LIGHT.onCommandIds` (`R.string.dimmer_light_on_command`) and returns `AsrCommand.DIMMER_LIGHT to true`.
4. `DashboardViewModel.processAsrCommand()` hits `AsrCommand.DIMMER_LIGHT -> sendDimmerLightPowerEvent(params as Boolean)`.
5. `sendDimmerLightPowerEvent(true)` publishes the on/off MQTT payload, and (since turning on) also republishes the last known brightness level.

### String resource conventions

- Naming: `<feature>_<variant>_command` (e.g. `dimmer_light_on_command`, `dimmer_light_on2_command` for a second synonym).
- Keep all command strings grouped together in `strings.xml` near the existing block (~lines 195–256) for discoverability.
- Every phrase referenced by a registry entry needs its own string resource — there's no runtime string composition beyond simple prefix-stripping (`command.removePrefix(prefix).trim()`).
