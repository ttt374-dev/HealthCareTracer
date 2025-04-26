package com.github.ttt374.healthcaretracer.data.repository

import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.item.Vitals
import com.github.ttt374.healthcaretracer.data.metric.DayPeriod
import com.github.ttt374.healthcaretracer.data.metric.MeasuredValue
import com.github.ttt374.healthcaretracer.data.metric.MetricDef
import com.github.ttt374.healthcaretracer.data.metric.StatValue
import com.github.ttt374.healthcaretracer.data.metric.toDayPeriod
import com.github.ttt374.healthcaretracer.data.metric.toStatValue
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsRepository @Inject constructor(private val metricRepository: MetricRepository, val itemRepository: ItemRepository, configRepository: ConfigRepository) {
    private val timeOfDayConfigFlow = configRepository.dataFlow.map { it.timeOfDayConfig }

    fun getStatValueFlow(metricDef: MetricDef, days: Long?): Flow<StatValue> {
//        itemRepository.getRecentItemsFlow(days).map { items ->
//            items.mapNotNull { item -> metricDef.selector(item.vitals) }.toStatValue()
//        }
        return metricRepository.getMeasuredValuesFlow(metricDef, days).map { items ->
            items.map { it.value }.toStatValue()
        }
    }
    fun getDayPeriodStatValueFlow(metricDef: MetricDef, days: Long?): Flow<Map<DayPeriod, StatValue>> {
        return timeOfDayConfigFlow.flatMapLatest { timeOfDayConfig ->
            metricRepository.getMeasuredValuesFlow(metricDef, days).map { list ->
                val grouped = list.groupBy { (measuredAt, _) ->
                    measuredAt.toDayPeriod(config = timeOfDayConfig)
                }
                DayPeriod.entries.associateWith { period ->
                    grouped[period].orEmpty().map { it.value }.toStatValue()
                }
            }
        }
    }
    fun getMeasuredValuesFlow(metricDef: MetricDef, days: Long?) = metricRepository.getMeasuredValuesFlow(metricDef, days) // delegate to metric repository

//    fun getMeasuredValuesFlow(metricDef: MetricDef, days: Long?): Flow<List<MeasuredValue>> {
//        return itemRepository.getRecentItemsFlow(days).mapNotNull { items -> items.toMeasuredValue(metricDef) }
//    }
}
