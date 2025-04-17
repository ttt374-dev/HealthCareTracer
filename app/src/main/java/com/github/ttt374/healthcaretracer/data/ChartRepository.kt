package com.github.ttt374.healthcaretracer.data

import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.data.Entry
import com.github.ttt374.healthcaretracer.data.datastore.Config
import com.github.ttt374.healthcaretracer.data.datastore.ConfigRepository
import com.github.ttt374.healthcaretracer.data.datastore.Preferences
import com.github.ttt374.healthcaretracer.data.datastore.PreferencesRepository
import com.github.ttt374.healthcaretracer.data.item.DailyItem
import com.github.ttt374.healthcaretracer.data.item.ItemRepository
import com.github.ttt374.healthcaretracer.ui.chart.ChartEntries
import com.github.ttt374.healthcaretracer.ui.chart.firstAndLast
import com.github.ttt374.healthcaretracer.ui.chart.toEntries
import com.github.ttt374.healthcaretracer.ui.home.groupByDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ChartRepository @Inject constructor(val itemRepository: ItemRepository, private val configRepository: ConfigRepository, private val preferencesRepository: PreferencesRepository) : ViewModel() {

    private val dailyItemsFlow = preferencesRepository.dataFlow.map { it.timeRangeChart }.flatMapLatest { range ->
        itemRepository.getRecentItemsFlow(range.days).map { items -> items.groupByDate()}
    }
    private fun getEntriesFlow(takeValue: (DailyItem) -> Double?): Flow<List<Entry>> {
        return dailyItemsFlow.map { list -> list.toEntries { takeValue(it) } }
    }
    private fun getTargetEntriesFlow(takeValue: (Config) -> Number): Flow<List<Entry>> {
        return configRepository.dataFlow.map { takeValue(it) }.flatMapLatest { target ->
            //getEntriesFlow { target.toDouble() }
            dailyItemsFlow.map { list -> list.firstAndLast().toEntries { target.toDouble() }}
        }
    }
//    fun getChartEntriesFlow(chartType: ChartType): Flow<Chart> {
//        val list = chartType.seriesDefs.map { def ->
//            ChartSeries(def, getEntriesFlow { def.takeValue(it) })
//        }
//        return Chart()
//    }
private val actualEntriesFlow = combine(
        getEntriesFlow { it.avgBpUpper },
        getEntriesFlow { it.avgBpLower },
        getEntriesFlow { it.avgPulse },
        getEntriesFlow { it.avgBodyWeight },
        getEntriesFlow { it.avgBodyTemperature },
    ){ upper, lower, pulse, bodyWeight, bodyTemperature ->
        ChartEntries(upper, lower, pulse, bodyWeight, bodyTemperature)
    }

    private val targetEntriesFlow = combine(
        getTargetEntriesFlow { it.targetBpUpper },
        getTargetEntriesFlow { it.targetBpLower },
        getTargetEntriesFlow { it.targetBodyWeight },
    ){ upper, lower, bodyWeight ->
        ChartEntries(upper, lower, emptyList(), bodyWeight)
    }
    data class SeriesEntries (val actual: ChartEntries, val target: ChartEntries)
    val seriesEntriesFlow = combine(
        actualEntriesFlow, targetEntriesFlow
    ){ actual, target -> SeriesEntries(actual, target)}

    //fun getChartEntriesFlow(chartType: ChartType):
    //override suspend fun updateData(transform: suspend (t: T) -> T): T = dataStore.updateData(transform)
    suspend fun updatePreferences (transform: suspend (t: Preferences) -> Preferences) = preferencesRepository.updateData(transform)

}