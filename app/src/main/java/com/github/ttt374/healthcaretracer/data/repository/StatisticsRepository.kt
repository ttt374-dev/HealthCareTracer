package com.github.ttt374.healthcaretracer.data.repository

import com.github.ttt374.healthcaretracer.data.metric.MetricDef
import com.github.ttt374.healthcaretracer.data.metric.StatValue
import com.github.ttt374.healthcaretracer.data.metric.toStatValue
import com.github.ttt374.healthcaretracer.shared.DayPeriod
import com.github.ttt374.healthcaretracer.shared.toDayPeriod
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class StatisticsRepository @Inject constructor(private val metricRepository: MetricRepository, val itemRepository: ItemRepository, configRepository: ConfigRepository) {
    private val timeOfDayConfigFlow = configRepository.dataFlow.map { it.timeOfDayConfig }

    fun getStatValueFlow(metricDef: MetricDef, days: Long?): Flow<StatValue> {
        return metricRepository.getMetricFlow(metricDef, days).map { list ->
            list.map { it.value }.toStatValue()
        }
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getDayPeriodStatValueFlow(metricDef: MetricDef, days: Long?): Flow<Map<DayPeriod, StatValue>> {
        return timeOfDayConfigFlow.flatMapLatest { timeOfDayConfig ->
            metricRepository.getMetricFlow(metricDef, days).map { list ->
                val grouped = list.groupBy { (measuredAt, _) ->
                    measuredAt.toDayPeriod(config = timeOfDayConfig)
                }
                DayPeriod.entries.associateWith { period ->
                    grouped[period].orEmpty().map { it.value }.toStatValue()
                }
            }
        }
    }
}
