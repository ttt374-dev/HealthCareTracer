package com.github.ttt374.healthcaretracer.data.metric

import com.github.ttt374.healthcaretracer.shared.averageOrNull
import com.github.ttt374.healthcaretracer.shared.maxOrNull
import com.github.ttt374.healthcaretracer.shared.minOrNull

//////////
data class StatValue<T>(
    val avg: T? = null,
    val max: T? = null,
    val min: T? = null,
    val count: Int = 0,
)
fun <T> List<T>.toStatValue() =
    StatValue(avg = averageOrNull(), max = maxOrNull(), min = minOrNull(), count = count())
