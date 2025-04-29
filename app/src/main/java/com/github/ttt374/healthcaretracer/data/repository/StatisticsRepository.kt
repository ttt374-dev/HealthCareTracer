package com.github.ttt374.healthcaretracer.data.repository

import com.github.ttt374.healthcaretracer.data.item.meGap
import com.github.ttt374.healthcaretracer.data.metric.DayPeriod
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.metric.StatData
import com.github.ttt374.healthcaretracer.data.metric.StatValue
import com.github.ttt374.healthcaretracer.data.metric.toDayPeriod
import com.github.ttt374.healthcaretracer.data.metric.toStatValue
import com.github.ttt374.healthcaretracer.ui.home.toDailyItemList
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsRepository @Inject constructor(private val itemRepository: ItemRepository, configRepository: ConfigRepository) {
    private val dayPeriodConfigFlow = configRepository.dataFlow.map { it.dayPeriodConfig }

//    fun getStatDataList(type: MetricType, days: Long? = null): Flow<List<StatData>>{
//        return getStatData(type, days)
//        //return combine( type.defs.map { getStatData(it, days)}){ it.toList()}
//    }
    fun getStatData(type: MetricType, days: Long? = null): Flow<StatData> {
        return getStatValueFlow(type, days).flatMapLatest { allStat ->
            getDayPeriodStatValueFlow(type, days).map { byPeriodStat ->
                StatData(metricType = type, all = allStat, byPeriod = byPeriodStat)
            }
        }
    }
    private fun getStatValueFlow(metricType: MetricType, days: Long?): Flow<StatValue> {
        return itemRepository.getMeasuredValuesFlow(metricType, days).map { items ->
            items.map { it.value }.toStatValue()
        }
    }
    private fun getDayPeriodStatValueFlow(metricType: MetricType, days: Long?): Flow<Map<DayPeriod, StatValue>> {
        return dayPeriodConfigFlow.flatMapLatest { dayPeriodConfig ->
            itemRepository.getMeasuredValuesFlow(metricType, days).map { list ->
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
