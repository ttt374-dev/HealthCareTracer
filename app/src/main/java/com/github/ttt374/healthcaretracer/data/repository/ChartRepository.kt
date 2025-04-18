package com.github.ttt374.healthcaretracer.data.repository

import com.github.mikephil.charting.data.Entry
import com.github.ttt374.healthcaretracer.data.item.DailyItem
import com.github.ttt374.healthcaretracer.ui.chart.ChartData
import com.github.ttt374.healthcaretracer.ui.chart.ChartSeries
import com.github.ttt374.healthcaretracer.ui.chart.ChartType
import com.github.ttt374.healthcaretracer.ui.chart.ChartableItem
import com.github.ttt374.healthcaretracer.ui.chart.SeriesDef
import com.github.ttt374.healthcaretracer.ui.common.TimeRange
import com.github.ttt374.healthcaretracer.ui.home.groupByDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.ZoneId
import javax.inject.Inject

fun DailyItem.toChartableItem() =
    ChartableItem(bpUpper = avgBpUpper, bpLower = avgBpLower, pulse = avgPulse, bodyTemperature = avgBodyTemperature, bodyWeight = avgBodyWeight)

@OptIn(ExperimentalCoroutinesApi::class)
class ChartRepository @Inject constructor(val itemRepository: ItemRepository,
                                          private val configRepository: ConfigRepository,
){

//    private val dailyItemsFlow = preferencesRepository.dataFlow.map { it.timeRangeChart }.flatMapLatest { range ->
//        itemRepository.getRecentItemsFlow(range.days).map { items -> items.groupByDate()}
//    }
    private fun getDailyItemsFlow(timeRange: TimeRange) =
        itemRepository.getRecentItemsFlow(timeRange.days).map { items -> items.groupByDate()}

    private fun getEntriesFlow(takeValue: (DailyItem) -> Double?, timeRange: TimeRange) =
        getDailyItemsFlow(timeRange).map { list -> list.toEntries { takeValue(it) } }

    private val targetValuesFlow = configRepository.dataFlow.map {
        it.toChartableItem()
    }
    private fun getChartSeriesFlow(seriesDef: SeriesDef, timeRange: TimeRange): Flow<ChartSeries> {
        return targetValuesFlow.flatMapLatest { targetValues ->
            getEntriesFlow(takeValue = { seriesDef.takeValue(it.toChartableItem()) }, timeRange).map {
            //getEntriesFlow(takeValue = { seriesDef.takeDailyValue(it) }, timeRange).map {
                ChartSeries(seriesDef, it, seriesDef.createTargetEntries(targetValues, it))
            }
        }
    }
    fun getChartDataFlow(chartType: ChartType, timeRange: TimeRange): Flow<ChartData> {
        return chartType.seriesDefList.map {getChartSeriesFlow(it, timeRange) }.combineList().map {
            ChartData(chartType, it)
        }
    }
    //suspend fun updatePreferences (transform: suspend (t: Preferences) -> Preferences) = preferencesRepository.updateData(transform)

}
fun List<DailyItem>.toEntries(zoneId: ZoneId = ZoneId.systemDefault(), takeValue: (DailyItem) -> Double?): List<Entry> {
    return mapNotNull { dailyItem ->
        takeValue(dailyItem)?.toFloat()?.let { value ->
            Entry(dailyItem.date.atStartOfDay(zoneId).toInstant().toEpochMilli().toFloat(), value)
        }
    }
}
private fun Config.toChartableItem(): ChartableItem = ChartableItem(
    bpUpper = targetBpUpper.toDouble(),
    bpLower = targetBpLower.toDouble(),
    bodyWeight = targetBodyWeight
)
inline fun <reified T> List<Flow<T>>.combineList(): Flow<List<T>> =
    combine(*toTypedArray()) { it.toList() }