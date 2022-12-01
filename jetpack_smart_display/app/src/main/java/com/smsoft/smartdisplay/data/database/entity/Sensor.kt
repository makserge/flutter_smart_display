package com.smsoft.smartdisplay.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sensors")
data class Sensor(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val title: String,
    val titleIcon: String,
    val topic1: String,
    val topic1Unit: String,
    val topic1Icon: String,
    val topic2: String,
    val topic2Unit: String,
    val topic2Icon: String,
    val topic3: String,
    val topic3Unit: String,
    val topic3Icon: String,
    val topic4: String,
    val topic4Unit: String,
    val topic4Icon: String
)

val emptySensor = Sensor(
    id = 0,
    title = "",
    titleIcon = "",
    topic1 = "",
    topic1Unit = "",
    topic1Icon = "",
    topic2 = "",
    topic2Unit = "",
    topic2Icon = "",
    topic3 = "",
    topic3Unit = "",
    topic3Icon = "",
    topic4 = "",
    topic4Unit = "",
    topic4Icon = ""
)
