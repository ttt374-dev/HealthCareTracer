package com.github.ttt374.healthcaretracer.data.item

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.ttt374.healthcaretracer.data.repository.LocalTimeRange
import com.github.ttt374.healthcaretracer.data.repository.averageOrNull
import com.github.ttt374.healthcaretracer.ui.entry.toLocalTime
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
    val bodyWeight: Double? = null,
    val bodyTemperature: Double? = null,
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
    val avgBodyTemperature: Double? = null,
    val items: List<Item> = emptyList(),

    ){
    fun meGap(zoneId: ZoneId = ZoneId.systemDefault(), morningRange: LocalTimeRange, eveningRange: LocalTimeRange): Double? {
        val morningAvg = items.filter { morningRange.contains(it.measuredAt.toLocalTime(zoneId))  }.map { it.bpUpper }.averageOrNull()
        val eveningAvg = items.filter { eveningRange.contains(it.measuredAt.toLocalTime(zoneId)) }.map { it.bpUpper }.averageOrNull()

//        val morningAvg = items.filter { it.measuredAt.isMorning(zoneId) }.map { it.bpUpper }.averageOrNull()
//        val eveningAvg = items.filter { it.measuredAt.isEvening(zoneId) }.map { it.bpUpper }.averageOrNull()

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
//
//fun Instant.isMorning(zoneId: ZoneId = ZoneId.systemDefault()): Boolean {
//    val start = LocalTime.of(4, 0)
//    val last = LocalTime.of(11, 59)
//    return this.atZone(zoneId).toLocalTime() in start..last
//}
//
//fun Instant.isEvening(zoneId: ZoneId = ZoneId.systemDefault()): Boolean {
//    val start = LocalTime.of(17, 0)
//    val last = LocalTime.of(3, 59)
//    val localTime = this.atZone(zoneId).toLocalTime()
//    return localTime in start..LocalTime.MAX || localTime in LocalTime.MIN..last
//}
