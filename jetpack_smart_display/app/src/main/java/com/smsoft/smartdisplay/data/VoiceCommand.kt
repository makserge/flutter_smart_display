package com.smsoft.smartdisplay.data

data class VoiceCommand(val type: VoiceCommandType, val timeStamp: Long = System.currentTimeMillis())
