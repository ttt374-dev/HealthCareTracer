package com.github.ttt374.healthcaretracer.data.repository

import com.github.ttt374.healthcaretracer.data.item.meGap
import com.github.ttt374.healthcaretracer.data.item.toDailyItemList
import com.github.ttt374.healthcaretracer.data.metric.DayPeriod
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.metric.MetricValue
import com.github.ttt374.healthcaretracer.data.metric.StatData
import com.github.ttt374.healthcaretracer.data.metric.StatValue
import com.github.ttt374.healthcaretracer.data.metric.toDayPeriod
import com.github.ttt374.healthcaretracer.data.metric.toMetricNumber
import com.github.ttt374.healthcaretracer.data.metric.toStatValue
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsRepository @Inject constructor(private val itemRepository: ItemRepository, configRepository: ConfigRepository) {
    private val dayPeriodConfigFlow = configRepository.dataFlow.map { it.dayPeriodConfig }

    fun getStatDataFlow(type: MetricType, days: Long? = null, zoneId: ZoneId): Flow<StatData<MetricValue>> {
        return getStatValueFlow(type, days).flatMapLatest { allStat ->
            getDayPeriodStatValueFlow(type, days, zoneId).map { byPeriodStat ->
                StatData(metricType = type, all = allStat, byPeriod = byPeriodStat)
            }
        }
    }
    private fun getStatValueFlow(metricType: MetricType, days: Long?): Flow<StatValue<MetricValue>> {
        return itemRepository.getMeasuredValuesFlow(metricType, days).map { items ->
            items.map { it.value }.toStatValue()
        }
    }
    private fun getDayPeriodStatValueFlow(metricType: MetricType, days: Long?, zoneId: ZoneId): Flow<Map<DayPeriod, StatValue<MetricValue>>> {
        return dayPeriodConfigFlow.flatMapLatest { dayPeriodConfig ->
            itemRepository.getMeasuredValuesFlow(metricType, days).map { list ->
                val grouped = list.groupBy { (measuredAt, _) ->
                    measuredAt.toDayPeriod(dayPeriodConfig, zoneId)
                }
                DayPeriod.entries.associateWith { period ->
                    grouped[period].orEmpty().map { it.value }.toStatValue()
                }
            }
        }
    }
    fun getMeGapStatValueFlow(days: Long? = null, zoneId: ZoneId): Flow<StatValue<MetricValue>>  {
        return  dayPeriodConfigFlow.flatMapLatest { dayPeriodConfig ->
            itemRepository.getRecentItemsFlow(days).map { list ->
                list.toDailyItemList(zoneId).mapNotNull { it.meGap(dayPeriodConfig, zoneId)?.toMetricNumber()}.toStatValue()
            }
        }
    }
}
