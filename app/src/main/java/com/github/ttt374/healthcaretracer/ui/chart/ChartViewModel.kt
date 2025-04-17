package com.github.ttt374.healthcaretracer.ui.chart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.github.ttt374.healthcaretracer.data.ChartRepository
import com.github.ttt374.healthcaretracer.data.datastore.Config
import com.github.ttt374.healthcaretracer.data.datastore.ConfigRepository
import com.github.ttt374.healthcaretracer.data.datastore.PreferencesRepository
import com.github.ttt374.healthcaretracer.data.item.DailyItem
import com.github.ttt374.healthcaretracer.data.item.ItemRepository
import com.github.ttt374.healthcaretracer.ui.common.TimeRange
import com.github.ttt374.healthcaretracer.ui.home.groupByDate
import dagger.hilt.android.lifecycle.HiltViewModel
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
    //val selectedTabIndex: Int = 0,
)
@HiltViewModel
class ChartViewModel @Inject constructor(private val chartRepository: ChartRepository) : ViewModel() {
     val uiState: StateFlow<ChartUiState> = combine(
        chartRepository.actualEntriesFlow,
        chartRepository.targetEntriesFlow,
        chartRepository.timeRangeFlow,
    ) { actualEntries, targetEntries, timeRange ->
        ChartUiState(
            actualEntries = actualEntries,
            targetEntries = targetEntries,
            timeRange = timeRange,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChartUiState())

    fun updateTimeRange(range: TimeRange) {
        viewModelScope.launch {
            runCatching {
                chartRepository.updatePreferences {
                    it.copy(timeRangeChart = range)
                }
            }.onFailure {
                // エラーハンドリング: 例) ログ出力やUIへのエラーメッセージ表示
                Log.e("ChartViewModel", "Failed to update time range", it)
            }
        }
    }
//    fun updateSelectedTabIndex(index: Int) {
//        //selectedTabIndexFlow.value = index
//        viewModelScope.launch {
//            selectedTabIndexFlow.emit(index)
//        }
//
//    }
}
fun List<DailyItem>.toEntries(zoneId: ZoneId = ZoneId.systemDefault(), takeValue: (DailyItem) -> Double?): List<Entry> {
    return mapNotNull { dailyItem ->
        takeValue(dailyItem)?.toFloat()?.let { value ->
            Entry(dailyItem.date.atStartOfDay(zoneId).toInstant().toEpochMilli().toFloat(), value)
        }
    }
}
internal fun <T> List<T>.firstAndLast(): List<T> {
    return when (size) {
        0 -> emptyList()
        1 -> listOf(first())
        else -> listOf(first(), last())
    }
}