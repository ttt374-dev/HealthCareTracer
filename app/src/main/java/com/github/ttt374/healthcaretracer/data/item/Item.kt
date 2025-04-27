package com.github.ttt374.healthcaretracer.data.item

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureConverter
import kotlinx.serialization.Serializable
import java.time.Instant

//const val MIN_PULSE = 30
//const val MAX_PULSE = 200
const val MIN_BP = 50
//const val MAX_BP = 260

@Serializable
data class Vitals(
    val bp: BloodPressure? = null,
    val pulse: Int? = null,
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
    val location: String = "",
    val memo: String = "",

    val measuredAt: Instant = Instant.now(),
)

