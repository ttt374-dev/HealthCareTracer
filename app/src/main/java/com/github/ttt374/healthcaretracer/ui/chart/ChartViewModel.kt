package com.github.ttt374.healthcaretracer.ui.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.item.MetricCategory
import com.github.ttt374.healthcaretracer.data.repository.ChartRepository
import com.github.ttt374.healthcaretracer.di.modules.ChartTimeRange
import com.github.ttt374.healthcaretracer.di.modules.DefaultMetricCategory
import com.github.ttt374.healthcaretracer.shared.TimeRange
import com.github.ttt374.healthcaretracer.shared.TimeRangeManager
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
@OptIn(ExperimentalCoroutinesApi::class)
class ChartViewModel @Inject constructor(private val chartRepository: ChartRepository,
                                         @ChartTimeRange private val timeRangeManager: TimeRangeManager,
                                         @DefaultMetricCategory defaultMetricCategory: MetricCategory
) : ViewModel() {
    val timeRange = timeRangeManager.timeRange
    private val _selectedChartType: MutableStateFlow<MetricCategory> = MutableStateFlow(defaultMetricCategory)
    val selectedChartType: StateFlow<MetricCategory> = _selectedChartType.asStateFlow()

    val chartData = timeRange.flatMapLatest { timeRange ->
        selectedChartType.flatMapLatest { type ->
            chartRepository.getChartDataFlow(type, timeRange)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChartData(defaultMetricCategory))

    fun setChartType(chartType: MetricCategory) {
        _selectedChartType.value = chartType
    }
    fun setSelectedRange(range: TimeRange) {
        viewModelScope.launch {
            timeRangeManager.setSelectedRange(range)
        }
    }
}
