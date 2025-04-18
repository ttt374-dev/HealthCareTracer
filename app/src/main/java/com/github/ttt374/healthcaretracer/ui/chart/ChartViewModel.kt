package com.github.ttt374.healthcaretracer.ui.chart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.repository.ChartRepository
import com.github.ttt374.healthcaretracer.data.repository.Preferences
import com.github.ttt374.healthcaretracer.data.repository.PreferencesRepository
import com.github.ttt374.healthcaretracer.ui.common.TimeRange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ChartViewModel @Inject constructor(private val chartRepository: ChartRepository,
                                         private val preferencesRepository: PreferencesRepository,

) : ViewModel() {
    val timeRange: StateFlow<TimeRange> = preferencesRepository.dataFlow.map { it.timeRangeChart }
        .stateIn(viewModelScope,  SharingStarted.WhileSubscribed(5000), TimeRange.Default)

//    val selectedChartType: StateFlow<ChartType> = preferencesRepository.dataFlow.map { it.chartType }
//        .stateIn(viewModelScope,  SharingStarted.WhileSubscribed(5000), ChartType.Default)
    private val _selectedChartType: MutableStateFlow<ChartType> = MutableStateFlow(ChartType.Default)
    val selectedChartType: StateFlow<ChartType> = _selectedChartType.asStateFlow()

//    fun onChartTypeSelected(type: ChartType) {
//        _selectedChartType.value = type
//    }

    val chartData: StateFlow<ChartData> = combine(selectedChartType, timeRange){ type, range -> type to range  }
        .flatMapLatest { (type, range) -> chartRepository.getChartDataFlow(type, range) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChartData())


    fun onPageChanged(index: Int) {
        //updateChartType(ChartType.entries[index])
        _selectedChartType.value = ChartType.entries[index]
    }
//    fun updateChartType(chartType: ChartType){
//        updatePreferences { it.copy(chartType = chartType) }
//    }

    fun updateTimeRange(range: TimeRange) {
        updatePreferences { it.copy(timeRangeChart = range) }
    }
    private fun updatePreferences (transform: suspend (t: Preferences) -> Preferences){
        viewModelScope.launch {
            runCatching {
                preferencesRepository.updateData(transform)
            }.onFailure {
                // エラーハンドリング: 例) ログ出力やUIへのエラーメッセージ表示
                Log.e("ChartViewModel", "Failed to update time range", it)
            }
        }
    }

}
