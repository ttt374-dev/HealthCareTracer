package com.github.ttt374.healthcaretracer.ui.chart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.github.ttt374.healthcaretracer.data.ChartRepository
import com.github.ttt374.healthcaretracer.data.datastore.ConfigRepository
import com.github.ttt374.healthcaretracer.data.datastore.Preferences
import com.github.ttt374.healthcaretracer.data.datastore.PreferencesRepository
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


@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ChartViewModel @Inject constructor(private val chartRepository: ChartRepository,
                                         preferencesRepository: PreferencesRepository) : ViewModel() {
    private val timeRangeFlow = preferencesRepository.dataFlow.map { it.timeRangeChart }
    val timeRange: StateFlow<TimeRange> = timeRangeFlow.stateIn(viewModelScope,  SharingStarted.WhileSubscribed(5000), TimeRange.Default)

    private val _selectedChartType: MutableStateFlow<ChartType> = MutableStateFlow(ChartType.Default)
    private val selectedChartType: StateFlow<ChartType> = _selectedChartType.asStateFlow()

    fun onChartTypeSelected(type: ChartType) {
        _selectedChartType.value = type
    }

    fun onPageChanged(index: Int) {
        _selectedChartType.value = ChartType.entries[index]
    }
    val chartData: StateFlow<ChartData> = selectedChartType
        .flatMapLatest { chartRepository.getChartDataFlow(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChartData())

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
}
