package com.github.ttt374.healthcaretracer.ui.analysis

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.metric.ChartData
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.metric.StatData
import com.github.ttt374.healthcaretracer.data.metric.StatValue
import com.github.ttt374.healthcaretracer.data.repository.ChartRepository
import com.github.ttt374.healthcaretracer.data.repository.Config
import com.github.ttt374.healthcaretracer.data.repository.ConfigRepository
import com.github.ttt374.healthcaretracer.data.repository.PreferencesRepository
import com.github.ttt374.healthcaretracer.data.repository.StatisticsRepository
import com.github.ttt374.healthcaretracer.di.modules.DefaultMetricCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject


enum class TimeRange(val days: Long?,  @StringRes val labelRes: Int) {
    ONE_WEEK(7, R.string.range__1week),
    ONE_MONTH(30, R.string.range__1month),
    SIX_MONTHS(180, R.string.range__6months),
    ONE_YEAR(365, R.string.range__1year),
    FULL(null, R.string.range__full_range);

    fun startDate(now: Instant = Instant.now()): Instant? {
        return days?.let { now.minus(it, ChronoUnit.DAYS) }
    }
    //    fun toDisplayString(fullStartDate: Instant, zone: ZoneId = ZoneId.systemDefault()): String {
//        val startDate = this.startDate() ?: fullStartDate
//        val endDate = Instant.now()
//        val dateFormat = DateTimeFormatter.ofPattern("yyyy-M-d").withZone(zone)
//        return "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
//    }
    companion object {
        val Default = ONE_MONTH
    }
}

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class AnalysisViewModel @Inject constructor(private val chartRepository: ChartRepository,
                                            private val statisticsRepository: StatisticsRepository,
                                            configRepository: ConfigRepository,
                                            private val preferencesRepository: PreferencesRepository,
                                            @DefaultMetricCategory defaultMetricType: MetricType) : ViewModel() {
    private val timeRangeFlow = preferencesRepository.dataFlow.map { it.timeRange }
    val timeRange: StateFlow<TimeRange> = timeRangeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimeRange.Default)
    private val _selectedMetricType: MutableStateFlow<MetricType> = MutableStateFlow(defaultMetricType)
    val selectedMetricType: StateFlow<MetricType> = _selectedMetricType.asStateFlow()


    val displayMode: StateFlow<DisplayMode> = preferencesRepository.dataFlow.map { it.displayMode }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DisplayMode.Default)
    //val displayMode: StateFlow<DisplayMode> = _displayMode.asStateFlow()
//    private val _displayMode: MutableStateFlow<DisplayMode> = MutableStateFlow(DisplayMode.CHART)
//    val displayMode: StateFlow<DisplayMode> = _displayMode.asStateFlow()

    val config: StateFlow<Config> = configRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Config())
    // ローディング状態を追加
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val chartData = timeRangeFlow.flatMapLatest { timeRange ->
        selectedMetricType.flatMapLatest { type ->
            _isLoading.value = true
            chartRepository.getChartDataFlow(type, timeRange).onEach {
                _isLoading.value = false
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChartData(defaultMetricType))

    val statData: StateFlow<StatData> = selectedMetricType.flatMapLatest{ metricType ->
        timeRangeFlow.flatMapLatest { range ->
            _isLoading.value = true
            statisticsRepository.getStatData(metricType, range.days).onEach {
                _isLoading.value = false
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatData(metricType = MetricType.HEART))  // TODO Default

    /// M-E gap
    val meGapStatValue: StateFlow<StatValue> = statisticsRepository.getMeGapStatValueFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatValue())

    /////////////////////
    fun setMetricType(metricType: MetricType) {
        _selectedMetricType.value = metricType
    }
    fun setDisplayMode(displayMode: DisplayMode) {
        //_displayMode.value = displayMode
        viewModelScope.launch {
            preferencesRepository.updateData { it.copy(displayMode = displayMode) }
        }
    }
    fun setSelectedRange(range: TimeRange) {
        viewModelScope.launch {
            preferencesRepository.updateData { it.copy(timeRange = range)}
            //timeRangeRepository.setSelectedRange(range)
        }
    }
}