package com.github.ttt374.healthcaretracer.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

@Entity(tableName = "items" )
data class Item (
    @PrimaryKey(autoGenerate=true)
    val id: Long = 0,
    val bpHigh: Int = 0,
    val bpLow: Int = 0,
    val pulse: Int = 0,
    val measuredAt: Instant = Instant.now(),

){
    fun date(): LocalDate {
        return measuredAt.toLocalDate()
    }
}

fun Instant.toLocalDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate {
    return this.atZone(zoneId).toLocalDate()
}
