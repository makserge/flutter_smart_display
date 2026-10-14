package com.smsoft.smartdisplay.ui.screen.dashboard

const val APP_CHANNEL = "SmartDisplaychannnel"
// Empty: not configured, so nothing is sent or subscribed until a topic is entered in Settings.
// (They were "*/stat/*/POWER" and "*/cmnd/*/POWER": no real topic has a "*", so the button never
// worked until someone typed the topics.)
const val PUSH_BUTTON_STATUS_DEFAULT_TOPIC = ""
const val PUSH_BUTTON_COMMAND_DEFAULT_TOPIC = ""
const val PUSH_BUTTON_DEFAULT_STATE = false
const val PUSH_BUTTON_ON_PAYLOAD = "ON"
const val PUSH_BUTTON_DEFAULT_PAYLOAD_ON = "1"
const val PUSH_BUTTON_DEFAULT_PAYLOAD_OFF = "0"
// Cinema mode: the Home Assistant automation the panels use (changeable in Settings)
const val CINEMA_MODE_COMMAND_DEFAULT_TOPIC = "ha_automations/cinema_mode"
const val CINEMA_MODE_DEFAULT_PAYLOAD_ON = "1"
const val CINEMA_MODE_DEFAULT_PAYLOAD_OFF = "0"
const val PROXIMITY_SENSOR_DEFAULT_TOPIC = "pushbutton2"
const val PROXIMITY_SENSOR_DEFAULT_STATE = false
const val PROXIMITY_SENSOR_DEFAULT_PAYLOAD_ON = "1"
const val PROXIMITY_SENSOR_DEFAULT_PAYLOAD_OFF = "0"
// Empty: no status topic, the toggle then only knows what this panel sent.
const val PROXIMITY_SENSOR_DEFAULT_STATUS_TOPIC = ""
const val PROXIMITY_SENSOR_THRESHOLD = 500
const val PROXIMITY_SENSOR_MIN_DETECTION_DURATION = 1000L
const val DIMMER_LIGHT_DEFAULT_BRIGHTNESS = 100
const val DIMMER_LIGHT_STEP_PERCENT = 10
const val SHOW_ASR_RESULT_TIMEOUT = 1000L //2s
