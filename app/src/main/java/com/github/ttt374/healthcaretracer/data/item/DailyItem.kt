package com.github.ttt374.healthcaretracer.data.item

import com.github.ttt374.healthcaretracer.data.metric.DayPeriod
import com.github.ttt374.healthcaretracer.data.metric.DayPeriodConfig
import com.github.ttt374.healthcaretracer.data.metric.toDayPeriod
import java.time.LocalDate
import java.time.ZoneId

data class DailyItem (
    val date: LocalDate,
    val vitals: Vitals = Vitals(),
    val items: List<Item> = emptyList(),
)
fun DailyItem.meGap(dayPeriodConfig: DayPeriodConfig, zoneId: ZoneId): Double? {
    val morningAvg = items.filterDayPeriod(DayPeriod.Morning, dayPeriodConfig, zoneId).mapNotNull { it.vitals.bp?.upper }.takeIf { it.isNotEmpty() }?.average()
    val eveningAvg = items.filterDayPeriod(DayPeriod.Evening, dayPeriodConfig, zoneId).mapNotNull { it.vitals.bp?.upper }.takeIf { it.isNotEmpty() }?.average()

    return if (morningAvg != null && eveningAvg != null) morningAvg - eveningAvg else null
}

internal fun List<Item>.filterDayPeriod(dayPeriod: DayPeriod, timeOfDayConfig: DayPeriodConfig, zoneId: ZoneId) =
    filter { it.measuredAt.toDayPeriod(timeOfDayConfig, zoneId) == dayPeriod}


fun List<Item>.toDailyItemList(zoneId: ZoneId): List<DailyItem> {
    return groupBy { it.measuredAt.atZone(zoneId).toLocalDate() }
        .map { (date, dailyItems) ->
            DailyItem(
                date = date,
                vitals = dailyItems.toAveragedVitals(),
                items = dailyItems
            )
        }.sortedBy { it.date }
}
