package com.github.ttt374.healthcaretracer.data.repository

import com.github.ttt374.healthcaretracer.data.item.meGap
import com.github.ttt374.healthcaretracer.data.metric.DayPeriod
import com.github.ttt374.healthcaretracer.data.metric.DayPeriodConfig
import com.github.ttt374.healthcaretracer.data.metric.MetricDef
import com.github.ttt374.healthcaretracer.data.metric.MetricType
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

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsRepository @Inject constructor(private val itemRepository: ItemRepository, configRepository: ConfigRepository) {
    private val dayPeriodConfigFlow = configRepository.dataFlow.map { it.dayPeriodConfig }

    fun getStatDataList(type: MetricType, days: Long? = null): Flow<List<StatData>>{
        return combine( type.defs.map { getStatData(it, days)}){ it.toList()}
    }
    private fun getStatData(def: MetricDef, days: Long? = null): Flow<StatData> {
        return getStatValueFlow(def, days).flatMapLatest { allStat ->
            getDayPeriodStatValueFlow(def, days).map { byPeriodStat ->
                StatData(metricDef = def, all = allStat, byPeriod = byPeriodStat)
            }
        }
    }
    private fun getStatValueFlow(metricDef: MetricDef, days: Long?): Flow<StatValue> {
        return itemRepository.getMeasuredValuesFlow(metricDef, days).map { items ->
            items.map { it.value }.toStatValue()
        }
    }
    private fun getDayPeriodStatValueFlow(metricDef: MetricDef, days: Long?): Flow<Map<DayPeriod, StatValue>> {
        return dayPeriodConfigFlow.flatMapLatest { dayPeriodConfig ->
            itemRepository.getMeasuredValuesFlow(metricDef, days).map { list ->
                val grouped = list.groupBy { (measuredAt, _) ->
                    measuredAt.toDayPeriod(dayPeriodConfig)
                }
                DayPeriod.entries.associateWith { period ->
                    grouped[period].orEmpty().map { it.value }.toStatValue()
                }
            }
        }
    }
    fun getMeGapStatValueFlow(days: Long? = null): Flow<StatValue>  {
        return  dayPeriodConfigFlow.flatMapLatest { dayPeriodConfig ->
            itemRepository.getRecentItemsFlow(days).map { list ->
                list.toDailyItemList().mapNotNull { it.meGap(dayPeriodConfig)}.toStatValue()
            }
        }
    }
}
