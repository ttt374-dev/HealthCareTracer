package com.github.ttt374.healthcaretracer.data.repository

import com.github.ttt374.healthcaretracer.data.item.DailyItem
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.metric.DayPeriod
import com.github.ttt374.healthcaretracer.data.metric.DayPeriodConfig
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.metric.MetricDef
import com.github.ttt374.healthcaretracer.data.metric.StatData
import com.github.ttt374.healthcaretracer.data.metric.StatValue
import com.github.ttt374.healthcaretracer.data.metric.toDayPeriod
import com.github.ttt374.healthcaretracer.data.metric.toStatValue
import com.github.ttt374.healthcaretracer.ui.home.toDailyItemList
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsRepository @Inject constructor(private val itemRepository: ItemRepository) {
    //private val dayPeriodConfigFlow = configRepository.dataFlow.map { it.dayPeriodConfig }

    fun getStatDataForMetricDef(def: MetricDef, days: Long? = null): Flow<StatData> {
        return getStatValueFlow(def, days).flatMapLatest { allStat ->
            getDayPeriodStatValueFlow(def, days).map { byPeriodStat ->
                StatData(metricDef = def, all = allStat, byPeriod = byPeriodStat)
            }
        }
    }
    fun getStatDataListForMetricType(type: MetricType, days: Long? = null): Flow<List<StatData>>{
        return combine( type.defs.map { getStatDataForMetricDef(it, days)}){ it.toList()}
    }
    fun getStatValueFlow(metricDef: MetricDef, days: Long?): Flow<StatValue> {
        return itemRepository.getMeasuredValuesFlow(metricDef, days).map { items ->
            items.map { it.value }.toStatValue()
        }
    }
    fun getDayPeriodStatValueFlow(metricDef: MetricDef, days: Long?, dayPeriodConfig: DayPeriodConfig = DayPeriodConfig()): Flow<Map<DayPeriod, StatValue>> {
        return itemRepository.getMeasuredValuesFlow(metricDef, days).map { list ->
                val grouped = list.groupBy { (measuredAt, _) ->
                    measuredAt.toDayPeriod(dayPeriodConfig)
                }
                DayPeriod.entries.associateWith { period ->
                    grouped[period].orEmpty().map { it.value }.toStatValue()
                }
            }

    }
    fun getMeGapStatValueFlow(days: Long? = null, dayPeriodConfig: DayPeriodConfig = DayPeriodConfig()): Flow<StatValue>  {
        return itemRepository.getRecentItemsFlow(days).map { list ->
                list.toDailyItemList().mapNotNull { it.meGap(dayPeriodConfig)}.toStatValue()
        }

    }
}
fun DailyItem.meGap(dayPeriodConfig: DayPeriodConfig, zoneId: ZoneId = ZoneId.systemDefault()): Double? {
    val morningAvg = items.filterDayPeriod(DayPeriod.Morning, dayPeriodConfig, zoneId).mapNotNull { it.vitals.bp?.upper }.takeIf { it.isNotEmpty() }?.average()
    val eveningAvg = items.filterDayPeriod(DayPeriod.Evening, dayPeriodConfig, zoneId).mapNotNull { it.vitals.bp?.upper }.takeIf { it.isNotEmpty() }?.average()

    return if (morningAvg != null && eveningAvg != null) morningAvg - eveningAvg else null

    //return morningAvg - eveningAvg
}

//fun List<Item>.calculateMeGap(timeOfDayConfig: DayPeriodConfig, zoneId: ZoneId) =
//    filterDayPeriod(DayPeriod.Morning, timeOfDayConfig, zoneId).bpUpperAverageOrNull()?.let { morning ->
//        filterDayPeriod(DayPeriod.Evening, timeOfDayConfig, zoneId).bpUpperAverageOrNull()?.let { evening ->
//            morning - evening
//        }
//    }
fun List<Item>.filterDayPeriod(dayPeriod: DayPeriod, timeOfDayConfig: DayPeriodConfig, zoneId: ZoneId) =
    filter { it.measuredAt.toDayPeriod(timeOfDayConfig, zoneId) == dayPeriod}
