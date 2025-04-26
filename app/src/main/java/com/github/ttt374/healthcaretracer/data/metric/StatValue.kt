package com.github.ttt374.healthcaretracer.data.metric

import com.github.ttt374.healthcaretracer.shared.averageOrNull
import com.github.ttt374.healthcaretracer.shared.maxOrNull
import com.github.ttt374.healthcaretracer.shared.minOrNull

//////////
data class StatValue(
    val avg: Double? = null,
    val max: Double? = null,
    val min: Double? = null,
    val count: Int = 0,
)
fun List<Double>.toStatValue() =
    StatValue(avg = averageOrNull(), max = maxOrNull(), min = minOrNull(), count = count())
