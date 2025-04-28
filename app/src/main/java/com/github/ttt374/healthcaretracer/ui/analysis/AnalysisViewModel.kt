package com.github.ttt374.healthcaretracer.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.metric.ChartData
import com.github.ttt374.healthcaretracer.data.metric.MeasuredValue
import com.github.ttt374.healthcaretracer.data.metric.MetricDef
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.metric.StatData
import com.github.ttt374.healthcaretracer.data.metric.StatValue
import com.github.ttt374.healthcaretracer.data.metric.toMeGapStatValue
import com.github.ttt374.healthcaretracer.data.repository.ChartRepository
import com.github.ttt374.healthcaretracer.data.repository.Config
import com.github.ttt374.healthcaretracer.data.repository.ConfigRepository
import com.github.ttt374.healthcaretracer.data.repository.StatisticsRepository
import com.github.ttt374.healthcaretracer.data.repository.TimeRange
import com.github.ttt374.healthcaretracer.data.repository.TimeRangeRepository
import com.github.ttt374.healthcaretracer.di.modules.ChartTimeRange
import com.github.ttt374.healthcaretracer.di.modules.DefaultMetricCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class AnalysisViewModel @Inject constructor(private val chartRepository: ChartRepository,
                                            private val statisticsRepository: StatisticsRepository,
                                            private val configRepository: ConfigRepository,
                                            @ChartTimeRange private val timeRangeRepository: TimeRangeRepository,
                                         @DefaultMetricCategory defaultMetricType: MetricType) : ViewModel() {
    private val timeRangeFlow = timeRangeRepository.timeRangeFlow
    val timeRange: StateFlow<TimeRange> = timeRangeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimeRange.Default)
    private val _selectedMetricType: MutableStateFlow<MetricType> = MutableStateFlow(defaultMetricType)
    val selectedMetricType: StateFlow<MetricType> = _selectedMetricType.asStateFlow()

    private val _displayMode: MutableStateFlow<DisplayMode> = MutableStateFlow(DisplayMode.CHART)
    val displayMode: StateFlow<DisplayMode> = _displayMode.asStateFlow()
    val config: StateFlow<Config> = configRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Config())

    val chartData = timeRangeFlow.flatMapLatest { timeRange ->
        selectedMetricType.flatMapLatest { type ->
            chartRepository.getChartDataFlow(type, timeRange)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChartData(defaultMetricType))

    val statDataList: StateFlow<List<StatData>> = selectedMetricType.flatMapLatest{ metricType ->
        timeRangeFlow.flatMapLatest { range ->
            statisticsRepository.getStatDataListForMetricType(metricType, range.days)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /// M-E gap
    val meGapStatValue: StateFlow<StatValue> =
        combine(configRepository.dataFlow, timeRangeFlow) { config, timeRange ->
            config to timeRange
        }.flatMapLatest { (config, timeRange) ->
            MetricType.BLOOD_PRESSURE.defs.firstOrNull()?.let { def ->
                statisticsRepository.getMeasuredValuesFlow(def, timeRange.days).map { measuredValues ->
                    measuredValues.toMeGapStatValue(config.dayPeriodConfig)
                }
            } ?: flowOf(StatValue())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatValue())

    /////////////////////
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