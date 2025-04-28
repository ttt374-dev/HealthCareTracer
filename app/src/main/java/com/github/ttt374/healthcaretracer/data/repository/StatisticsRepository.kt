package com.github.ttt374.healthcaretracer.data.repository

import com.github.ttt374.healthcaretracer.data.metric.DayPeriod
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.metric.MetricDef
import com.github.ttt374.healthcaretracer.data.metric.StatValue
import com.github.ttt374.healthcaretracer.data.metric.toDayPeriod
import com.github.ttt374.healthcaretracer.data.metric.toStatValue
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsRepository @Inject constructor(private val itemRepository: ItemRepository, configRepository: ConfigRepository) {
    private val timeOfDayConfigFlow = configRepository.dataFlow.map { it.dayPeriodCOnfig }

    fun getStatValuesForCategory(category: MetricType, days: Long?): Flow<List<StatValue>> {
        val defs = category.defs
        return combine(
            defs.map { def -> getStatValueFlow(def, days) }
        ) { statValues ->
            statValues.toList()
        }
    }
    fun getDayPeriodStatValuesForCategory(category: MetricType, days: Long?): Flow<Map<DayPeriod, List<StatValue>>> {
        val defs = category.defs
        return combine(
                defs.map { def -> getDayPeriodStatValueFlow(def, days) }
            ) { statValueMaps ->
                // statValueMaps: List<Map<DayPeriod, StatValue>>
                DayPeriod.entries.associateWith { period ->
                    statValueMaps.mapNotNull { it[period] }
                }
            }
    }
    fun getStatValueFlow(metricDef: MetricDef, days: Long?): Flow<StatValue> {
        return itemRepository.getMeasuredValuesFlow(metricDef, days).map { items ->
            items.map { it.value }.toStatValue()
        }
    }
    fun getDayPeriodStatValueFlow(metricDef: MetricDef, days: Long?): Flow<Map<DayPeriod, StatValue>> {
        return timeOfDayConfigFlow.flatMapLatest { timeOfDayConfig ->
            itemRepository.getMeasuredValuesFlow(metricDef, days).map { list ->
                val grouped = list.groupBy { (measuredAt, _) ->
                    measuredAt.toDayPeriod(config = timeOfDayConfig)
                }
                DayPeriod.entries.associateWith { period ->
                    grouped[period].orEmpty().map { it.value }.toStatValue()
                }
            }
        }
    }
    fun getMeasuredValuesFlow(metricDef: MetricDef, days: Long?) = itemRepository.getMeasuredValuesFlow(metricDef, days) // delegate to metric repository

//    fun getMeasuredValuesFlow(metricDef: MetricDef, days: Long?): Flow<List<MeasuredValue>> {
//        return itemRepository.getRecentItemsFlow(days).mapNotNull { items -> items.toMeasuredValue(metricDef) }
//    }
}
