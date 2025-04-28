package com.github.ttt374.healthcaretracer.data.repository

import com.github.mikephil.charting.data.Entry
import com.github.ttt374.healthcaretracer.data.metric.MetricCategory
import com.github.ttt374.healthcaretracer.data.metric.MetricDef
import com.github.ttt374.healthcaretracer.data.metric.MetricDefRegistry
import com.github.ttt374.healthcaretracer.data.item.Vitals
import com.github.ttt374.healthcaretracer.data.metric.toEntry
import com.github.ttt374.healthcaretracer.data.metric.ChartData
import com.github.ttt374.healthcaretracer.data.metric.ChartSeries
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChartRepository @Inject constructor(private val itemRepository: ItemRepository, configRepository: ConfigRepository){
    private val targetValuesFlow = configRepository.dataFlow.map { it.targetVitals }
    private fun getChartSeriesFlow(metricDef: MetricDef, timeRange: TimeRange, targetValues: Vitals): Flow<ChartSeries> {
        return itemRepository.getMeasuredValuesFlow(metricDef, timeRange.days).map { list ->
            val entries = list.toEntry()
            val targetEntries = metricDef.selector(targetValues)?.let { entries.toTargetEntries(it, timeRange)}
            ChartSeries(metricDef, entries, targetEntries)
        }
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getChartDataFlow(metricCategory: MetricCategory, timeRange: TimeRange): Flow<ChartData> {
        return targetValuesFlow.flatMapLatest { targetValues ->
            MetricDefRegistry.getByCategory(metricCategory).map { def ->
                getChartSeriesFlow(def, timeRange, targetValues)
            }.combineList().map {
                ChartData(metricCategory, it)
            }
        }
    }
}

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