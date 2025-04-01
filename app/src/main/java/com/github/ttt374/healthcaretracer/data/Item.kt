package com.github.ttt374.healthcaretracer.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.ZoneId

const val MIN_PULSE = 30
const val MAX_PULSE = 200
const val MIN_BP = 50
const val MAX_BP = 260

@Entity(tableName = "items" )
data class Item (
    @PrimaryKey(autoGenerate=true)
    val id: Long = 0,
    val bpUpper: Int? = null,
    val bpLower: Int? = null,
    //val bp: BloodPressure = BloodPressure(),
    val pulse: Int? = 0,
    val bodyWeight: Float? = null,
    val location: String = "",
    val memo: String = "",

    val measuredAt: Instant = Instant.now(),
)

