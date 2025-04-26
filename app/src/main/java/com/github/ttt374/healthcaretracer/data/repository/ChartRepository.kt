package com.github.ttt374.healthcaretracer.data.repository

import com.github.mikephil.charting.data.Entry
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.metric.MetricCategory
import com.github.ttt374.healthcaretracer.data.metric.MetricDef
import com.github.ttt374.healthcaretracer.data.metric.MetricDefRegistry
import com.github.ttt374.healthcaretracer.data.item.Vitals
import com.github.ttt374.healthcaretracer.data.metric.toEntry
import com.github.ttt374.healthcaretracer.shared.TimeRange
import com.github.ttt374.healthcaretracer.data.metric.ChartData
import com.github.ttt374.healthcaretracer.data.metric.ChartSeries
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChartRepository @Inject constructor(val metricRepository: MetricRepository, val itemRepository: ItemRepository, configRepository: ConfigRepository){
    private val targetValuesFlow = configRepository.dataFlow.map {
        it.targetVitals
    }
//    private fun getEntriesFlow(takeValue: (Vitals) -> Double?, timeRange: TimeRange) =
//        itemRepository.getRecentItemsFlow(timeRange.days).map { list -> list.toEntries(takeValue)}

//    private fun getMeasuredValuesFlow(selector: (Vitals) -> Double?, days: Long? = null) =
//        itemRepository.getRecentItemsFlow(days).map { list -> list.mapNotNull { item -> selector(item.vitals)?.let { MeasuredValue(item.measuredAt, it) }}}
//
    private fun getChartSeriesFlow(metricDef: MetricDef, timeRange: TimeRange, targetValues: Vitals): Flow<ChartSeries> {
        return metricRepository.getMetricFlow(metricDef, timeRange.days).map { list ->
            val entries = list.toEntry()
            ChartSeries(metricDef, list.toEntry(), entries.toTargetEntries(metricDef.selector(targetValues) ?: 0, timeRange)) // TODO nonnull ???
        }
//
//        return getEntriesFlow({ metricDef.selector(it) }, timeRange)
//            .map { entries -> metricDef.createSeries(entries, targetValues, timeRange) }
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getChartDataFlow(metricCategory: MetricCategory, timeRange: TimeRange): Flow<ChartData> {
        return targetValuesFlow.flatMapLatest { targetValues ->
            MetricDefRegistry.getByCategory(metricCategory).map { def ->
                getChartSeriesFlow(def, timeRange, targetValues) }.combineList().map {
                ChartData(metricCategory, it)
            }
//            metricCategory.seriesDefList.map { def -> getChartSeriesFlow(def, timeRange, targetValues) }.combineList().map {
//                ChartData(metricCategory, it)
//            }
        }
    }
}
//fun List<Item>.toEntries(takeValue: (Vitals) -> Double?): List<Entry> {
//    return mapNotNull { item ->
//        takeValue(item.vitals)?.toFloat()?.let { value ->
//            Entry(item.measuredAt.toEpochMilli().toFloat(), value)
//        }
//    }
//}
internal fun List<Entry>.toTargetEntries(targetValue: Number, timeRange: TimeRange): List<Entry> {
    if (isEmpty()) return emptyList()
    val startX = timeRange.startDate()?.toEpochMilli()?.toFloat() ?: first().x
    val endX = last().x

    return listOf(
        Entry(startX, targetValue.toFloat()),
        Entry(endX, targetValue.toFloat())
    )
}

inline fun <reified T> List<Flow<T>>.combineList(): Flow<List<T>> =
    combine(*toTypedArray()) { it.toList() }