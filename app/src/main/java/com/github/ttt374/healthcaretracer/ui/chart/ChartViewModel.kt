package com.github.ttt374.healthcaretracer.ui.chart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.github.ttt374.healthcaretracer.data.ChartRepository
import com.github.ttt374.healthcaretracer.data.item.DailyItem
import com.github.ttt374.healthcaretracer.ui.common.TimeRange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZoneId
import javax.inject.Inject

data class ChartEntries(
    val bpUpper: List<Entry> =  emptyList(),
    val bpLower: List<Entry> = emptyList(),
    val pulse: List<Entry> = emptyList(),
    val bodyWeight: List<Entry> = emptyList(),
    val bodyTemperature: List<Entry> = emptyList(),
)
//data class ChartUiState (
//    val actualEntries: ChartEntries = ChartEntries(),
//    val targetEntries: ChartEntries = ChartEntries(),
//    val timeRange: TimeRange = TimeRange.Default,
//    //val selectedTabIndex: Int = 0,
//)
//data class Chart(val type: ChartType = ChartType.BloodPressure, val series: List<ChartSeries> = emptyList())
data class ChartSeries(val seriesDef: SeriesDef, val entries: List<Entry>, val targetEntries: List<Entry>)

@HiltViewModel
class ChartViewModel @Inject constructor(private val chartRepository: ChartRepository) : ViewModel() {
    private val _selectedChartType: MutableStateFlow<ChartType> = MutableStateFlow(ChartType.BloodPressure)
    val selectedChartType: StateFlow<ChartType> = _selectedChartType.asStateFlow()

    fun onChartTypeSelected(type: ChartType) {
        _selectedChartType.value = type
    }

    fun onPageChanged(index: Int) {
        _selectedChartType.value = ChartType.entries[index]
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    val chartSeries: StateFlow<List<ChartSeries>> = selectedChartType.flatMapLatest { chartType ->
        chartRepository.seriesEntriesFlow.map { seriesEntries ->
            chartType.seriesDefList.map { def ->
                ChartSeries(def, def.takeValue(seriesEntries.actual), def.takeValue(seriesEntries.target))
            }
        }
//        chartRepository.actualEntriesFlow.map { chartEntries ->
//            //chartRepository.targetEntriesFlow.map { targetChartEntries ->
//                chartType.seriesDefList.map { def ->
//                    ChartSeries(def, def.takeValue(chartEntries))
//                    //LineDataSet(def.takeValue(chartEntries), "")
//                    //def.createLineDataSet(chartEntries)
//                    //LineDataSet(chartEntries.pulse, "asdf")
//                }
//            //}
//        }
        //listOf(LineDataSet(chartEntries.pulse, "stringResource(R.string.pulse)"))
    }.stateIn(viewModelScope,  SharingStarted.WhileSubscribed(5000), emptyList())


    @OptIn(ExperimentalCoroutinesApi::class)

//     val uiState: StateFlow<ChartUiState> = combine(
//        chartRepository.actualEntriesFlow,
//        chartRepository.targetEntriesFlow,
//        chartRepository.timeRangeFlow,
//    ) { actualEntries, targetEntries, timeRange ->
//        ChartUiState(
//            actualEntries = actualEntries,
//            targetEntries = targetEntries,
//            timeRange = timeRange,
//        )
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChartUiState())

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