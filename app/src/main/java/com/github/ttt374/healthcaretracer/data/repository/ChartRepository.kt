package com.github.ttt374.healthcaretracer.data.repository

import com.github.mikephil.charting.data.Entry
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.item.DailyItem
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.item.Vitals
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
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

//fun DailyItem.toChartableItem() =
//    ChartableItem(bpUpper = vitals.bp?.upper?.toDouble(), bpLower = vitals.bp?.lower?.toDouble(), pulse = vitals.pulse, bodyTemperature = vitals.bodyTemperature, bodyWeight = vitals.bodyWeight)

//fun Item.toChartableItem() =
//    ChartableItem(bpUpper = vitals.bp?.upper?.toDouble(), bpLower = vitals.bp?.lower?.toDouble(), pulse = vitals.pulse?.toDouble(), bodyTemperature = vitals.bodyTemperature, bodyWeight = vitals.bodyWeight)

@OptIn(ExperimentalCoroutinesApi::class)
class ChartRepository @Inject constructor(val itemRepository: ItemRepository,
                                          private val configRepository: ConfigRepository,
){

//    private val dailyItemsFlow = preferencesRepository.dataFlow.map { it.timeRangeChart }.flatMapLatest { range ->
//        itemRepository.getRecentItemsFlow(range.days).map { items -> items.groupByDate()}
//    }
    private fun getItemsFlow(timeRange: TimeRange) =
        itemRepository.getRecentItemsFlow(timeRange.days)
    private fun getDailyItemsFlow(timeRange: TimeRange) =
        itemRepository.getRecentItemsFlow(timeRange.days).map { items -> items.groupByDate()}

    private fun getEntriesFlow(takeValue: (Item) -> Double?, timeRange: TimeRange) =
        getItemsFlow(timeRange).map { list -> list.toEntries(getTime = { it.measuredAt}, takeValue = { takeValue(it) } )}

//    private fun getEntriesFlow(takeValue: (DailyItem) -> Double?, timeRange: TimeRange) =
//        getDailyItemsFlow(timeRange).map { list -> list.toEntries({ it.date.atStartOfDay(zoneId).toInstant(zoneId) }, { takeValue(it) }) }

    private val targetValuesFlow = configRepository.dataFlow.map {
        it.toVitals()
    }
    private fun getChartSeriesFlow(seriesDef: SeriesDef, timeRange: TimeRange): Flow<ChartSeries> {
        return targetValuesFlow.flatMapLatest { targetValues ->
            getEntriesFlow(takeValue = { seriesDef.takeValue(it.vitals) }, timeRange).map {
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
//fun List<DailyItem>.toEntries(zoneId: ZoneId = ZoneId.systemDefault(), takeValue: (DailyItem) -> Double?): List<Entry> {
//    return mapNotNull { dailyItem ->
//        takeValue(dailyItem)?.toFloat()?.let { value ->
//            Entry(dailyItem.date.atStartOfDay(zoneId).toInstant().toEpochMilli().toFloat(), value)
//        }
//    }
//}
fun <T> List<T>.toEntries(zoneId: ZoneId = ZoneId.systemDefault(), getTime: (T) -> Instant, takeValue: (T) -> Double?): List<Entry> {
    return mapNotNull { item ->
        takeValue(item)?.toFloat()?.let { value ->
            Entry(getTime(item).toEpochMilli().toFloat(), value)
        }
    }
}
private fun Config.toVitals() = Vitals(
    bp = BloodPressure(targetBpUpper, targetBpLower),
    bodyWeight = targetBodyWeight
)


//fun List<Item>.toEntries(zoneId: ZoneId = ZoneId.systemDefault(), takeValue: (Item) -> Double?): List<Entry> {
//    return mapNotNull { item ->
//        takeValue(item)?.toFloat()?.let { value ->
//            Entry(item.measuredAt.toEpochMilli().toFloat(), value)
//        }
//    }
//}

inline fun <reified T> List<Flow<T>>.combineList(): Flow<List<T>> =
    combine(*toTypedArray()) { it.toList() }