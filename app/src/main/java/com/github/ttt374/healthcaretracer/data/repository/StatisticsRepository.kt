package com.github.ttt374.healthcaretracer.data.repository

import com.github.ttt374.healthcaretracer.data.metric.MetricDef
import com.github.ttt374.healthcaretracer.shared.DayPeriod
import com.github.ttt374.healthcaretracer.shared.TimeRange
import com.github.ttt374.healthcaretracer.shared.toDayPeriod
import com.github.ttt374.healthcaretracer.ui.statics.StatCalculator
import com.github.ttt374.healthcaretracer.ui.statics.StatValue
import com.github.ttt374.healthcaretracer.ui.statics.StatisticsData
import com.github.ttt374.healthcaretracer.ui.statics.toStatValue
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class StatisticsRepository @Inject constructor(private val metricRepository: MetricRepository, val itemRepository: ItemRepository, configRepository: ConfigRepository) {
    private val timeOfDayConfigFlow = configRepository.dataFlow.map { it.timeOfDayConfig }

    fun getStatValueFlow(metricDef: MetricDef, days: Long?): Flow<StatValue<Double>> {
        return metricRepository.getMetricFlow(metricDef, days).map { list ->
            list.map { it.value }.toStatValue()
        }
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getDayPeriodStatValueFlow(metricDef: MetricDef, days: Long?): Flow<Map<DayPeriod, StatValue<Double>>> {
        return timeOfDayConfigFlow.flatMapLatest { timeOfDayConfig ->
            metricRepository.getMetricFlow(metricDef, days).map {
                val grouped = it.groupBy { (measuredAt, _) ->
                    measuredAt.toDayPeriod(config = timeOfDayConfig)
                }
                DayPeriod.entries.associateWith { period ->
                    grouped[period].orEmpty().map { it.value }.toStatValue()
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getStatisticsFlow(timeRange: TimeRange): Flow<StatisticsData> =
        timeOfDayConfigFlow.flatMapLatest { timeOfDayConfig ->
            itemRepository.getRecentItemsFlow(timeRange.days).map { items ->
                StatCalculator.calculateAll(items, timeOfDayConfig)
            }
        }.flowOn(Dispatchers.Default)
}
