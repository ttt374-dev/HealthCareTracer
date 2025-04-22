package com.github.ttt374.healthcaretracer.data.repository

import com.github.mikephil.charting.data.Entry
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.item.Vitals
import com.github.ttt374.healthcaretracer.shared.TimeRange
import com.github.ttt374.healthcaretracer.ui.chart.ChartData
import com.github.ttt374.healthcaretracer.ui.chart.ChartSeries
import com.github.ttt374.healthcaretracer.ui.chart.ChartType
import com.github.ttt374.healthcaretracer.ui.chart.SeriesDef
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

class ChartRepository @Inject constructor(val itemRepository: ItemRepository, configRepository: ConfigRepository){
    private val targetValuesFlow = configRepository.dataFlow.map {
        it.toVitals()
    }
    private fun getEntriesFlow(takeValue: (Vitals) -> Double?, timeRange: TimeRange) =
        itemRepository.getRecentItemsFlow(timeRange.days).map { list -> list.toEntries(takeValue)}

    private fun getChartSeriesFlow(seriesDef: SeriesDef, timeRange: TimeRange, targetValues: Vitals): Flow<ChartSeries> {
        return getEntriesFlow({ seriesDef.takeValue(it) }, timeRange)
            .map { entries -> seriesDef.createSeries(entries, targetValues, timeRange) }
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getChartDataFlow(chartType: ChartType, timeRange: TimeRange): Flow<ChartData> {
        return targetValuesFlow.flatMapLatest { targetValues ->
            chartType.seriesDefList.map { def -> getChartSeriesFlow(def, timeRange, targetValues) }.combineList().map {
                ChartData(chartType, it)
            }
        }
    }
}
fun List<Item>.toEntries(takeValue: (Vitals) -> Double?): List<Entry> {
    return mapNotNull { item ->
        takeValue(item.vitals)?.toFloat()?.let { value ->
            Entry(item.measuredAt.toEpochMilli().toFloat(), value)
        }
    }
}
internal fun List<Entry>.toTargetEntries(targetValue: Number, timeRange: TimeRange): List<Entry> {
    if (isEmpty()) return emptyList()
    val startX = timeRange.startDate()?.toEpochMilli()?.toFloat() ?: first().x

//    val startX = timeRange.startDate()?.toEpochMilli()?.toFloat() ?: first().x
    //val startX = entries.first().x
    val endX = last().x

    return listOf(
        Entry(startX, targetValue.toFloat()),
        Entry(endX, targetValue.toFloat())
    )
}
//fun List<Item>.firstDate(): Instant? {
//    return this.firstOrNull()?.measuredAt
//}
fun Config.toVitals() = Vitals(
    bp = BloodPressure(targetBpUpper, targetBpLower),
    bodyWeight = targetBodyWeight
)
inline fun <reified T> List<Flow<T>>.combineList(): Flow<List<T>> =
    combine(*toTypedArray()) { it.toList() }