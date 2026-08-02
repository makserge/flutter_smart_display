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

## Adding a New Voice Command

Speech is recognized offline by Vosk (Russian model). All command phrases are hardcoded Russian strings in `app/src/main/res/values/strings.xml` — there is no localization split (no `values-ru`, `values-en`, etc.).

### Pipeline overview

```
Vosk recognizer
  -> SpeechRecognitionService (emits SpeechRecognitionState.Result(word))
  -> DashboardViewModel.initASR()  (calls processCommand(...))
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
| `data/VoiceCommand.kt` | Data class carried through app state: `type`, optional `payload: String?`, `timeStamp`. |
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
| "радио" | `INTERNET_RADIO` | Navigate to Radio screen |
| "будильник" | `ALARM` | Navigate to Alarms screen |
| "таймер" | `TIMER` | Navigate to Timers screen |
| "звонок" | `DOORBELL` | Navigate to Doorbell screen |
| "включи радио" / "включить радио" | `INTERNET_RADIO_ON` | Navigate to Radio screen (radio resumes) |
| "выключи радио" / "выключить радио" | `INTERNET_RADIO_OFF` | Navigate to Radio screen, `RadioViewModel.processVoiceCommand` pauses playback |
| "радио назад" | `INTERNET_RADIO_PREV_ITEM` | Previous radio preset |
| "радио вперёд" | `INTERNET_RADIO_NEXT_ITEM` | Next radio preset |
| "радио тише" | `INTERNET_RADIO_VOL_DOWN` | Volume down |
| "радио громче" | `INTERNET_RADIO_VOL_UP` | Volume up |

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
