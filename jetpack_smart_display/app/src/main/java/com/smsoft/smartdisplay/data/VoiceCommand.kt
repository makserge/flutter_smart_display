package com.smsoft.smartdisplay.data

data class VoiceCommand(
    val type: VoiceCommandType,
    val payload: String? = null,
    val timeStamp: Long = System.currentTimeMillis()
)
