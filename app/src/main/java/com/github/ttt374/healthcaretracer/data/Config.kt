package com.github.ttt374.healthcaretracer.data

import java.time.LocalTime

data class Config (
    val bloodPressureGuideline: BloodPressureGuideline,
    val morningStart: LocalTime,
    val morningLast: LocalTime,
    val eveningStart: LocalTime,
    val eveningLast: LocalTime,

    val targetBpUpper: Int,
    val targetBpLower: Int,
    val targetBodyWeight: Double,
)