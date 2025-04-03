package com.github.ttt374.healthcaretracer.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

//const val MIN_PULSE = 30
//const val MAX_PULSE = 200
const val MIN_BP = 50
//const val MAX_BP = 260

@Entity(tableName = "items" )
data class Item (
    @PrimaryKey(autoGenerate=true)
    val id: Long = 0,
    val bpUpper: Int? = null,
    val bpLower: Int? = null,
    val pulse: Int? = 0,
    val bodyWeight: Float? = null,
    val location: String = "",
    val memo: String = "",

    val measuredAt: Instant = Instant.now(),
)

data class DailyItem (
    val date: LocalDate,
    val avgBpUpper: Double? = null,
    val avgBpLower: Double? = null,
    val avgPulse: Double? = null,
    val avgBodyWeight: Double? = null,
    val items: List<Item> = emptyList(),

){
    fun meGap(zoneId: ZoneId = ZoneId.systemDefault()): Double? {
        val morningAvg = items.filter { it.measuredAt.isMorning(zoneId) }.map { it.bpUpper }.averageOrNull()
        val eveningAvg = items.filter { it.measuredAt.isEvening(zoneId) }.map { it.bpUpper }.averageOrNull()

        return morningAvg?.let { m -> eveningAvg?.let { m - it }}

//        with(items){
//            val (morning, evening) = this
//                .mapNotNull { it.bpUpper?.let { bp -> it.measuredAt to bp } }
//                .partition { (instant, _) -> instant.isMorning(zoneId) }
//
//            val morningAvg = morning.map { it.second }.averageOrNull()
//            val eveningAvg = evening.map { it.second }.averageOrNull()
//
//            return morningAvg?.let { m -> eveningAvg?.let { m - it } }
//        }
    }
}

fun Instant.isMorning(zoneId: ZoneId = ZoneId.systemDefault()): Boolean {
    val hour = this.atZone(zoneId).hour
    return hour in 4..11
}

fun Instant.isEvening(zoneId: ZoneId = ZoneId.systemDefault()): Boolean {
    val hour = this.atZone(zoneId).hour
    return hour in 17..23 || hour in 0..2
}
