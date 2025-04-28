package com.github.ttt374.healthcaretracer.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.metric.ChartData
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.repository.ChartRepository
import com.github.ttt374.healthcaretracer.data.repository.ItemRepository
import com.github.ttt374.healthcaretracer.data.repository.TimeRange
import com.github.ttt374.healthcaretracer.data.repository.TimeRangeRepository
import com.github.ttt374.healthcaretracer.di.modules.ChartTimeRange
import com.github.ttt374.healthcaretracer.di.modules.DefaultMetricCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(private val itemRepository: ItemRepository,
                                            private val chartRepository: ChartRepository,
                                            @ChartTimeRange private val timeRangeRepository: TimeRangeRepository,
                                         @DefaultMetricCategory defaultMetricType: MetricType) : ViewModel() {
    private val timeRangeFlow = timeRangeRepository.timeRangeFlow
    val timeRange = timeRangeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimeRange.Default)
    private val _selectedMetricType: MutableStateFlow<MetricType> = MutableStateFlow(defaultMetricType)
    val selectedMetricType: StateFlow<MetricType> = _selectedMetricType.asStateFlow()

    private val _displayMode: MutableStateFlow<DisplayMode> = MutableStateFlow(DisplayMode.CHART)
    val displayMode: StateFlow<DisplayMode> = _displayMode.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val chartData = timeRangeFlow.flatMapLatest { timeRange ->
        selectedMetricType.flatMapLatest { type ->
            chartRepository.getChartDataFlow(type, timeRange)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChartData(defaultMetricType))

    fun setMetricType(metricType: MetricType) {
        _selectedMetricType.value = metricType
    }
    fun setDisplayMode(displayMode: DisplayMode) {
        _displayMode.value = displayMode
    }
    fun setSelectedRange(range: TimeRange) {
        viewModelScope.launch {
            timeRangeRepository.setSelectedRange(range)
        }
    }



}