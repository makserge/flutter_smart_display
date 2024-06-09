package com.smsoft.smartdisplay.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smsoft.smartdisplay.data.AlarmSoundToneType
import com.smsoft.smartdisplay.data.AlarmSoundType

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val time: Int,
    val days: Int,
    val isEnabled: Boolean,
    val radioPreset: Int,
    @ColumnInfo(name = "soundTone", defaultValue = "argon")
    val soundTone: String,
    @ColumnInfo(name = "soundType", defaultValue = "radio")
    val soundType: String
)

val emptyAlarm = Alarm(
    id = 0,
    time = 0,
    days = 62, //weekdays
    isEnabled = true,
    radioPreset = 0,
    soundTone = AlarmSoundToneType.getDefaultId(),
    soundType = AlarmSoundType.getDefaultId()
)