package com.github.ttt374.healthcaretracer.ui.chart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.github.ttt374.healthcaretracer.data.datastore.Config
import com.github.ttt374.healthcaretracer.data.datastore.ConfigRepository
import com.github.ttt374.healthcaretracer.data.datastore.Preferences
import com.github.ttt374.healthcaretracer.data.datastore.PreferencesRepository
import com.github.ttt374.healthcaretracer.data.item.DailyItem
import com.github.ttt374.healthcaretracer.data.item.ItemRepository
import com.github.ttt374.healthcaretracer.ui.common.TimeRange
import com.github.ttt374.healthcaretracer.ui.home.groupByDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZoneId
import javax.inject.Inject

data class ChartEntries(
    val bpUpper: List<Entry> = emptyList(),
    val bpLower: List<Entry> = emptyList(),
    val pulse: List<Entry> = emptyList(),
    val bodyWeight: List<Entry> = emptyList(),
    val bodyTemperature: List<Entry> = emptyList(),
)
data class ChartUiState (
    val actualEntries: ChartEntries = ChartEntries(),
    val targetEntries: ChartEntries = ChartEntries(),
    val timeRange: TimeRange = TimeRange.Default,
    val selectedTabIndex: Int = 0,
)
@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ChartViewModel @Inject constructor(val itemRepository: ItemRepository, configRepository: ConfigRepository, private val preferencesRepository: PreferencesRepository) : ViewModel() {
     private val config = configRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Config())
     private val timeRangeFlow = preferencesRepository.dataFlow.map { it.timeRangeChart }
     private val dailyItemsFlow = preferencesRepository.dataFlow.map { it.timeRangeChart }.flatMapLatest { range ->
        itemRepository.getRecentItemsFlow(range.days).map { items -> items.groupByDate()}
    }

    private fun getEntriesFlow(takeValue: (DailyItem) -> Double?): Flow<List<Entry>>{
        return dailyItemsFlow.map { list -> list.toEntries { takeValue(it) } }
    }
    private fun getTargetEntriesFlow(takeValue: (Config) -> Number): Flow<List<Entry>> {
        return config.map { takeValue(it) }.flatMapLatest { target ->
            //getEntriesFlow { target.toDouble() }
            dailyItemsFlow.map { list -> list.firstAndLast().toEntries { target.toDouble() }}
        }
    }

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
    private val selectedTabIndexFlow = MutableStateFlow(0)

    val uiState: StateFlow<ChartUiState> = combine(
        actualEntriesFlow,
        targetEntriesFlow,
        timeRangeFlow,
        selectedTabIndexFlow,
    ) { actualEntries, targetEntries, timeRange, selectedTabIndex ->
        ChartUiState(
            actualEntries = actualEntries,
            targetEntries = targetEntries,
            timeRange = timeRange,
            selectedTabIndex = selectedTabIndex
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChartUiState())

    fun updateTimeRange(range: TimeRange) {
        viewModelScope.launch {
            runCatching {
                preferencesRepository.updateData {
                    it.copy(timeRangeChart = range)
                }
            }.onFailure {
                // エラーハンドリング: 例) ログ出力やUIへのエラーメッセージ表示
                Log.e("ChartViewModel", "Failed to update time range", it)
            }
        }
    }
    fun updateSelectedTabIndex(index: Int) {
        //selectedTabIndexFlow.value = index
        viewModelScope.launch {
            selectedTabIndexFlow.emit(index)
        }

    }
}
fun List<DailyItem>.toEntries(zoneId: ZoneId = ZoneId.systemDefault(), takeValue: (DailyItem) -> Double?): List<Entry> {
    return mapNotNull { dailyItem ->
        takeValue(dailyItem)?.toFloat()?.let { value ->
            Entry(dailyItem.date.atStartOfDay(zoneId).toInstant().toEpochMilli().toFloat(), value)
        }
    }
}
fun <T> List<T>.firstAndLast(): List<T> {
    return when (size) {
        0 -> emptyList()
        1 -> listOf(first())
        else -> listOf(first(), last())
    }
}