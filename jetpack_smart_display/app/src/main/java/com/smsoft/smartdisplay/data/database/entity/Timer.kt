package com.smsoft.smartdisplay.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.smsoft.smartdisplay.data.AlarmSoundToneType
import com.smsoft.smartdisplay.ui.screen.timers.DEFAULT_TIMER_TITLE

@Entity(tableName = "timers")
data class Timer(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val title: String,
    val duration: Long,
    @ColumnInfo(name = "state", defaultValue = "idle")
    val soundTone: String
)

val emptyTimer = Timer(
    id = 0,
    title = DEFAULT_TIMER_TITLE,
    duration = 900, //15 minutes
    soundTone = AlarmSoundToneType.CYAN_ALARM.id
)