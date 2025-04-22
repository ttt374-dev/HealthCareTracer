package com.github.ttt374.healthcaretracer.data.item

import com.github.ttt374.healthcaretracer.shared.DayPeriod
import com.github.ttt374.healthcaretracer.shared.TimeOfDayConfig
import com.github.ttt374.healthcaretracer.shared.averageOrNull
import com.github.ttt374.healthcaretracer.shared.toDayPeriod
import java.time.LocalDate
import java.time.ZoneId

data class DailyItem (
    val date: LocalDate,
    val vitals: Vitals = Vitals(),
    val items: List<Item> = emptyList(),
)
fun List<Item>.calculateMeGap(timeOfDayConfig: TimeOfDayConfig, zoneId: ZoneId) =
    filterDayPeriod(DayPeriod.Morning, timeOfDayConfig, zoneId).bpUpperAverageOrNull()?.let { morning ->
        filterDayPeriod(DayPeriod.Evening, timeOfDayConfig, zoneId).bpUpperAverageOrNull()?.let { evening ->
            morning - evening
        }
    }
fun List<Item>.filterDayPeriod(dayPeriod: DayPeriod, timeOfDayConfig: TimeOfDayConfig, zoneId: ZoneId) =
    filter { it.measuredAt.toDayPeriod(timeOfDayConfig, zoneId) == dayPeriod}

fun List<Item>.bpUpperAverageOrNull() =
    mapNotNull { it.vitals.bp?.upper?.toDouble() }.averageOrNull()