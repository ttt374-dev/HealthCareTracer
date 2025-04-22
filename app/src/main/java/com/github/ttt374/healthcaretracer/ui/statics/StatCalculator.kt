package com.github.ttt374.healthcaretracer.ui.statics

import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.item.DailyItem
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.repository.LocalTimeRange
import com.github.ttt374.healthcaretracer.ui.common.TimeOfDay
import com.github.ttt374.healthcaretracer.ui.common.TimeOfDayConfig
import com.github.ttt374.healthcaretracer.ui.common.averageOrNull
import com.github.ttt374.healthcaretracer.ui.common.maxOrNull
import com.github.ttt374.healthcaretracer.ui.common.minOrNull
import com.github.ttt374.healthcaretracer.ui.common.toTimeOfDay
import java.time.ZoneId

//object StatCalculator {
//    fun calculateAll(timeOfDayConfig: TimeOfDayConfig, items: List<Item>): StatisticsData { ... }
//}

object StatCalculator { // (private val timeOfDayConfig: TimeOfDayConfig) {
    fun calculateAll(items: List<Item>, timeOfDayConfig: TimeOfDayConfig): StatisticsData {
        return StatisticsData(
            bloodPressure = calculateStat(items, timeOfDayConfig) { it.vitals.bp },
            pulse = calculateStat(items, timeOfDayConfig) { it.vitals.pulse },
            bodyWeight = calculateStat(items, timeOfDayConfig) { it.vitals.bodyWeight },
            bodyTemperature = calculateStat(items, timeOfDayConfig) { it.vitals.bodyTemperature },
            meGap = getMeStats(items, timeOfDayConfig),
            items = items
        )
    }
    private fun <T> calculateStat(items: List<Item>, timeOfDayConfig: TimeOfDayConfig, takeValue: (Item) -> T?): StatTimeOfDay<T> {
        val valuesWithTime = items.mapNotNull { item ->
            takeValue(item)?.let { item to it }
        }

        val grouped = valuesWithTime.groupBy { (item, _) ->
            item.measuredAt.toTimeOfDay(config = timeOfDayConfig)
        }

        return StatTimeOfDay(
            all = valuesWithTime.map { it.second }.toStatValue(),
            morning = grouped[TimeOfDay.Morning].orEmpty().map { it.second }.toStatValue(),
            afternoon = grouped[TimeOfDay.Afternoon].orEmpty().map { it.second }.toStatValue(),
            evening = grouped[TimeOfDay.Evening].orEmpty().map { it.second }.toStatValue()
        )
    }

    private fun getMeStats(items: List<Item>, timeOfDayConfig: TimeOfDayConfig): List<Double> {
        val zone = ZoneId.systemDefault()
        return items.groupBy { it.measuredAt.atZone(zone).toLocalDate() }
            .map { (date, dailyItems) ->
                DailyItem(date = date, items = dailyItems).meGap(
                    zone,
                    LocalTimeRange(timeOfDayConfig.morning, timeOfDayConfig.afternoon),
                    LocalTimeRange(timeOfDayConfig.evening, timeOfDayConfig.morning)
                )
            }.filterNotNull()
    }
}

data class StatTimeOfDay <T> (
    val all: StatValue<T> = StatValue(),
    val morning: StatValue<T> = StatValue(),
    val afternoon: StatValue<T> = StatValue(),
    val evening: StatValue<T> = StatValue(),
)
data class StatValue<T>(
    val avg: T? = null,
    val max: T? = null,
    val min: T? = null,
    val count: Int = 0,
)

data class StatisticsData(
    val bloodPressure: StatTimeOfDay<BloodPressure> = StatTimeOfDay(),
    val pulse: StatTimeOfDay<Double> = StatTimeOfDay(),
    val bodyWeight: StatTimeOfDay<Double> = StatTimeOfDay(),
    val bodyTemperature: StatTimeOfDay<Double> = StatTimeOfDay(),
    val meGap: List<Double> = emptyList(),
    val items: List<Item> = emptyList(),
)

fun <T> List<T>.toStatValue() =
    StatValue(avg = averageOrNull(), max = maxOrNull(), min = minOrNull(), count = count())