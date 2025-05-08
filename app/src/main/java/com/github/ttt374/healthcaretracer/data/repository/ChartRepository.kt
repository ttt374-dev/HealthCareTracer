package com.github.ttt374.healthcaretracer.data.repository

import com.github.mikephil.charting.data.Entry
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.item.TargetVitals
import com.github.ttt374.healthcaretracer.data.item.Vitals
import com.github.ttt374.healthcaretracer.data.metric.ChartData
import com.github.ttt374.healthcaretracer.data.metric.ChartSeries
import com.github.ttt374.healthcaretracer.data.metric.MeasuredValue
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.metric.MetricValue
import com.github.ttt374.healthcaretracer.data.metric.toEntries
import com.github.ttt374.healthcaretracer.ui.analysis.TimeRange
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class ChartRepository @Inject constructor(private val itemRepository: ItemRepository, configRepository: ConfigRepository ){
    private val targetValuesFlow = configRepository.dataFlow.map { it.targetVitals }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getChartDataFlow(metricType: MetricType, timeRange: TimeRange): Flow<ChartData> {
        return targetValuesFlow.flatMapLatest { targetValues ->
            when (metricType) {
                MetricType.BLOOD_PRESSURE -> getBloodPressureChartDataFlow(timeRange, targetValues)
                else -> getStandardChartDataFlow(metricType, timeRange, targetValues)
            }
        }
    }
    private fun createBpEntries(measuredValues: List<MeasuredValue<BloodPressure>>, selector: (BloodPressure) -> Int?): List<Entry> {
        return measuredValues.map {
            MeasuredValue(it.measuredAt, selector(it.value)).toEntries()
        }
    }
    private data class BpEntries (val upper: List<Entry>, val lower: List<Entry>)

    private fun getBloodPressureChartDataFlow(timeRange: TimeRange, targetValues: TargetVitals): Flow<ChartData> {
        val metricType = MetricType.BLOOD_PRESSURE
        return itemRepository.getMeasuredValuesFlow(metricType, timeRange.days).map { list ->
            val bpList = list.mapNotNull {
                (it.value as? MetricValue.BloodPressure)?.let { bp -> MeasuredValue(it.measuredAt, bp.value) }
            }
            val actualEntries = BpEntries(
                createBpEntries(bpList) { it.upper },
                createBpEntries(bpList) { it.lower }
            )
            val startDate = timeRange.startDate()
            val targetEntries = BpEntries(
                actualEntries.upper.toTargetEntriesOrEmpty(targetValues.bp.upper, startDate),
                actualEntries.lower.toTargetEntriesOrEmpty(targetValues.bp.lower, startDate),
            )

            ChartData(metricType,
                listOf(
                    ChartSeries(R.string.bpUpper, actualEntries.upper, targetEntries.upper),
                    ChartSeries(R.string.bpLower, actualEntries.lower, targetEntries.lower),
                )
            )
        }
    }
    private fun getStandardChartDataFlow(metricType: MetricType, timeRange: TimeRange, targetValues: TargetVitals): Flow<ChartData> {
        return itemRepository.getMeasuredValuesFlow(metricType, timeRange.days).map { list ->
            val actualEntries = list.toEntries()
            val targetValue = metricType.targetSelector(targetValues) as? MetricValue.Double
            val targetEntries = targetValue?.let { actualEntries.toTargetEntries(it.value, timeRange.startDate()) }

            ChartData(
                metricType,
                listOf(ChartSeries(metricType.resId, actualEntries, targetEntries))
            )
        }
    }
}
internal fun List<Entry>.toTargetEntriesOrEmpty(targetValue: Number?, startDate: Instant? = null): List<Entry>{
    return targetValue?.let { toTargetEntries(it, startDate)}.orEmpty()
}
internal fun List<Entry>.toTargetEntries(targetValue: Number, startDate: Instant? = null): List<Entry> {
    if (isEmpty()) return emptyList()
    val startX = startDate?.toEpochMilli()?.toFloat() ?: first().x
    //val endX = last().x
    val endX = Instant.now().toEpochMilli().toFloat()

    return listOf(
        Entry(startX, targetValue.toFloat()),
        Entry(endX, targetValue.toFloat())
    )
}

//inline fun <reified T> List<Flow<T>>.combineIntoList(): Flow<List<T>> =
//    combine(*toTypedArray()) { it.toList() }