package com.github.ttt374.healthcaretracer.data.item

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureConverter
import com.github.ttt374.healthcaretracer.data.bloodpressure.toBloodPressure
import com.github.ttt374.healthcaretracer.data.metric.averageOrNull
import kotlinx.serialization.Serializable
import java.time.Instant

//const val MIN_PULSE = 30
//const val MAX_PULSE = 200
const val MIN_BP = 50
//const val MAX_BP = 260

@Entity(tableName = "items" )
@TypeConverters(BloodPressureConverter::class)
data class Item (
    @PrimaryKey(autoGenerate=true)
    val id: Long = 0,
    @Embedded
    val vitals: Vitals = Vitals(),
    val location: String = "",
    val memo: String = "",

    val measuredAt: Instant = Instant.now(),
)

fun List<Item>.toAveragedVitals(): Vitals {
    return Vitals(
        bp = (mapNotNull { it.vitals.bp?.upper?.toDouble() }.averageOrNull() to mapNotNull { it.vitals.bp?.lower?.toDouble() }.averageOrNull())
            .toBloodPressure(),
        pulse = mapNotNull { it.vitals.pulse?.toDouble() }.averageOrNull()?.toInt(),
        bodyWeight = mapNotNull { it.vitals.bodyWeight }.averageOrNull(),
        bodyTemperature = mapNotNull { it.vitals.bodyTemperature }.averageOrNull(),
    )
}
@Serializable
data class Vitals(
    val bp: BloodPressure? = null,
    val pulse: Int? = null,
    val bodyWeight: Double? = null,
    val bodyTemperature: Double? = null,
)
@Serializable
data class TargetVitals(
    val bp: BloodPressure = BloodPressure(120, 80),
    val bodyWeight: Double = 60.0
)