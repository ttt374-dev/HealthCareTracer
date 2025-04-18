package com.github.ttt374.healthcaretracer.data

import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.github.ttt374.healthcaretracer.data.datastore.Config
import com.github.ttt374.healthcaretracer.data.datastore.ConfigRepository
import com.github.ttt374.healthcaretracer.data.datastore.Preferences
import com.github.ttt374.healthcaretracer.data.datastore.PreferencesRepository
import com.github.ttt374.healthcaretracer.data.item.DailyItem
import com.github.ttt374.healthcaretracer.data.item.ItemRepository
import com.github.ttt374.healthcaretracer.ui.chart.ChartData
import com.github.ttt374.healthcaretracer.ui.chart.ChartEntries
import com.github.ttt374.healthcaretracer.ui.chart.ChartType
import com.github.ttt374.healthcaretracer.ui.chart.ChartableItem
import com.github.ttt374.healthcaretracer.ui.home.groupByDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ChartRepository @Inject constructor(val itemRepository: ItemRepository,
                                          private val configRepository: ConfigRepository,
                                          private val preferencesRepository: PreferencesRepository) : ViewModel() {

    private val dailyItemsFlow = preferencesRepository.dataFlow.map { it.timeRangeChart }.flatMapLatest { range ->
        itemRepository.getRecentItemsFlow(range.days).map { items -> items.groupByDate()}
    }
    private fun getEntriesFlow(takeValue: (DailyItem) -> Double?): Flow<List<Entry>> {
        return dailyItemsFlow.map { list -> list.toEntries { takeValue(it) } }
    }

    private val entriesFlow = combine(
        getEntriesFlow { it.avgBpUpper },
        getEntriesFlow { it.avgBpLower },
        getEntriesFlow { it.avgPulse },
        getEntriesFlow { it.avgBodyWeight },
        getEntriesFlow { it.avgBodyTemperature },
    ){ upper, lower, pulse, bodyWeight, bodyTemperature ->
        ChartEntries(upper, lower, pulse, bodyWeight, bodyTemperature)
    }

    fun getChartDataFlow(chartType: ChartType): Flow<ChartData> {
        return combine(entriesFlow, configRepository.dataFlow) { entries, config ->
            val targetValue = config.toChartableItem()
            ChartData(chartType, chartType.toChartSeriesList(entries, targetValue))
        }
    }
//    fun chartDataFlow(chartType: ChartType): Flow<ChartData> {
//        return combine(){
//            ChartData(chartType, )
//        }
//    }
    suspend fun updatePreferences (transform: suspend (t: Preferences) -> Preferences) = preferencesRepository.updateData(transform)

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
