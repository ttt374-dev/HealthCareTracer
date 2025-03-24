package com.github.ttt374.healthcaretracer.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "items" )
data class Item (
    @PrimaryKey(autoGenerate=true)
    val id: Long = 0,
    val measuredAt: Instant = Instant.now(),
    val bpHigh: Int = 0,
    val bpLow: Int = 0,
    val pulse: Int = 0,
    val location: String = "",
)