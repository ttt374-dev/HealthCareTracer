package com.github.ttt374.healthcaretracer.data.repository

import com.github.mikephil.charting.data.Entry
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.metric.toEntries
import com.github.ttt374.healthcaretracer.data.metric.ChartData
import com.github.ttt374.healthcaretracer.data.metric.ChartSeries
import com.github.ttt374.healthcaretracer.data.metric.MeasuredValue
import com.github.ttt374.healthcaretracer.data.metric.MetricBloodPressure
import com.github.ttt374.healthcaretracer.data.metric.MetricNumber
import com.github.ttt374.healthcaretracer.data.metric.toMetricValue
import com.github.ttt374.healthcaretracer.ui.analysis.TimeRange
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChartRepository @Inject constructor(private val itemRepository: ItemRepository, configRepository: ConfigRepository){
    private val targetValuesFlow = configRepository.dataFlow.map { it.targetVitals }
//    private fun getChartSeriesFlow(metricType: MetricType, timeRange: TimeRange, targetValues: Vitals): Flow<ChartSeries> {
//        return itemRepository.getMeasuredValuesFlow(metricType, timeRange.days).map { list ->
//            val entries = list.toEntry()
//            //val mv  = metricType.selector(targetValues)
//
//            //val targetEntries = metricType.selector(targetValues)?.let { mv -> entries.toTargetEntries(, timeRange)}
//            ChartSeries(metricType.resId, entries, null)
//            //ChartSeries(metricType, entries, targetEntries)  // TODO: target entries
//        }
//    }
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getChartDataFlow(metricType: MetricType, timeRange: TimeRange): Flow<ChartData> {
        return targetValuesFlow.flatMapLatest { targetValues ->
            when (metricType){
                MetricType.BLOOD_PRESSURE -> {
                    itemRepository.getMeasuredValuesFlow(metricType, timeRange.days).map { list ->
                        val upperEntries = list.map { mv ->
                            MeasuredValue(mv.measuredAt, (mv.value as MetricBloodPressure).value.upper.toMetricValue())  // TODO cast check
                        }.toEntries()
                        val upperTargetEntries = targetValues.bp?.upper?.let { tv -> upperEntries.toTargetEntries(tv, timeRange) }
                        val lowerEntries = list.map { mv ->
                            MeasuredValue(mv.measuredAt, (mv.value as MetricBloodPressure).value.lower.toMetricValue())
                        }.toEntries()
                        val lowerTargetEntries = targetValues.bp?.lower?.let { tv -> lowerEntries.toTargetEntries(tv, timeRange) }
                        ChartData(metricType, listOf(
                            ChartSeries(R.string.bpUpper, upperEntries, upperTargetEntries),
                            ChartSeries(R.string.bpLower, lowerEntries, lowerTargetEntries)
                        ))
                    }
                }
                else -> {
                    itemRepository.getMeasuredValuesFlow(metricType, timeRange.days).map { list ->
                    //getChartSeriesFlow(metricType, timeRange, targetValues).map {
                        val actualEntries = list.toEntries()
                        val targetEntries = metricType.selector(targetValues)?.let { mv -> actualEntries.toTargetEntries((mv as MetricNumber).value, timeRange )}
                        ChartData(metricType, listOf(ChartSeries(metricType.resId, actualEntries, targetEntries)))
                    }
                }
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

inline fun <reified T> List<Flow<T>>.combineIntoList(): Flow<List<T>> =
    combine(*toTypedArray()) { it.toList() }