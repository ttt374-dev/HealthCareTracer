package com.github.ttt374.healthcaretracer.ui.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.metric.ChartData
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.repository.ChartRepository
import com.github.ttt374.healthcaretracer.di.modules.ChartTimeRange
import com.github.ttt374.healthcaretracer.di.modules.DefaultMetricCategory
import com.github.ttt374.healthcaretracer.data.repository.TimeRange
import com.github.ttt374.healthcaretracer.data.repository.TimeRangeRepository
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

//@HiltViewModel
//@OptIn(ExperimentalCoroutinesApi::class)
//class ChartViewModel @Inject constructor(private val chartRepository: ChartRepository,
//                                         @ChartTimeRange private val timeRangeRepository: TimeRangeRepository,
//                                         @DefaultMetricCategory defaultMetricType: MetricType
//) : ViewModel() {
//    val timeRangeFlow = timeRangeRepository.timeRangeFlow
//    val timeRange = timeRangeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimeRange.Default)
//        private val _selectedChartType: MutableStateFlow<MetricType> = MutableStateFlow(defaultMetricType)
//    val selectedChartType: StateFlow<MetricType> = _selectedChartType.asStateFlow()
//
//    val chartData = timeRangeFlow.flatMapLatest { timeRange ->
//        selectedChartType.flatMapLatest { type ->
//            chartRepository.getChartDataFlow(type, timeRange)
//        }
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChartData(defaultMetricType))
//
//    fun setChartType(chartType: MetricType) {
//        _selectedChartType.value = chartType
//    }
//    fun setSelectedRange(range: TimeRange) {
//        viewModelScope.launch {
//            timeRangeRepository.setSelectedRange(range)
//        }
//    }
//}
