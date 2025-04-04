package com.github.ttt374.healthcaretracer.data.datastore

import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import kotlinx.serialization.Serializable

@Serializable  // (with = ConfigSerializer::class)
data class Config (
    val bloodPressureGuideline: BloodPressureGuideline = BloodPressureGuideline.WHO,
//    val morningRange: LocalTimeRange = LocalTimeRange(LocalTime.of(4, 0), LocalTime.of(11,59)),
//    val eveningRange: LocalTimeRange = LocalTimeRange(LocalTime.of(7, 0), LocalTime.of(2, 59)),

    val targetBpUpper: Int = 120,
    val targetBpLower: Int = 80,
//    val targetBodyWeight: Double = 60.0,
)
