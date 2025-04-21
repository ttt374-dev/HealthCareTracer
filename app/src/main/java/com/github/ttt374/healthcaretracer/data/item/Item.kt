package com.github.ttt374.healthcaretracer.data.item

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureConverter
import com.github.ttt374.healthcaretracer.data.repository.LocalTimeRange
import com.github.ttt374.healthcaretracer.ui.common.averageOrNull
import com.github.ttt374.healthcaretracer.ui.entry.toLocalTime
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

//const val MIN_PULSE = 30
//const val MAX_PULSE = 200
const val MIN_BP = 50
//const val MAX_BP = 260

data class Vitals(
    val bp: BloodPressure? = null,
    val pulse: Double? = null,
    val bodyWeight: Double? = null,
    val bodyTemperature: Double? = null,
)

@Entity(tableName = "items" )
@TypeConverters(BloodPressureConverter::class)
data class Item (
    @PrimaryKey(autoGenerate=true)
    val id: Long = 0,
    @Embedded
    val vitals: Vitals = Vitals(),
//    val bp: BloodPressure? = null,
//    val pulse: Int? = null,
//    val bodyWeight: Double? = null,
//    val bodyTemperature: Double? = null,
    val location: String = "",
    val memo: String = "",

    val measuredAt: Instant = Instant.now(),
)

data class DailyItem (
    val date: LocalDate,
    val vitals: Vitals = Vitals(),
//    val bp: BloodPressure? = null,
//    val pulse: Double? = null,
//    val bodyWeight: Double? = null,
//    val bodyTemperature: Double? = null,
    val items: List<Item> = emptyList(),
){
    fun meGap(zoneId: ZoneId = ZoneId.systemDefault(), morningRange: LocalTimeRange, eveningRange: LocalTimeRange): Double? {
        val morningAvg = items.filter { morningRange.contains(it.measuredAt.toLocalTime(zoneId)) }.map { it.vitals.bp?.upper }.averageOrNull()
        val eveningAvg = items.filter { eveningRange.contains(it.measuredAt.toLocalTime(zoneId)) }.map { it.vitals.bp?.upper }.averageOrNull()

        return morningAvg?.let { m -> eveningAvg?.let { m - it }}
    }
}
