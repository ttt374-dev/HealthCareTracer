package com.github.ttt374.healthcaretracer.ui.analysis

import androidx.annotation.StringRes
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.metric.ChartData
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.metric.MetricValue
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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

    private val chartCache = mutableStateMapOf<Pair<MetricType, TimeRange>, ChartData>()
    private val statCache = mutableStateMapOf<Pair<MetricType, TimeRange>, StatData<MetricValue>>()

    private val _chartData = MutableStateFlow(ChartData(defaultMetricType))
    val chartData: StateFlow<ChartData> = _chartData

    private val _statData = MutableStateFlow(StatData<MetricValue>(metricType = defaultMetricType))
    val statData: StateFlow<StatData<MetricValue>> = _statData.asStateFlow()

    private suspend fun loadChartData(type: MetricType, range: TimeRange) {
        val key = type to range
        if (key !in chartCache) {
            _isLoading.value = true
            val data = chartRepository.getChartDataFlow(type, range).first()
            chartCache[key] = data
            _isLoading.value = false
        }
        _chartData.value = chartCache[key]!!
    }

    private suspend fun loadStatData(type: MetricType, range: TimeRange) {
        val key = type to range
        if (key !in statCache) {
            _isLoading.value = true
            val data = statisticsRepository.getStatDataFlow(type, range.days).first()
            statCache[key] = data
            _isLoading.value = false
        }
        _statData.value = statCache[key]!!
    }
    init {
        viewModelScope.launch {
            combine(_selectedMetricType, timeRange) { type, range ->
                type to range
            }.collect { (type, range) ->
                loadChartData(type, range)
                loadStatData(type, range)
            }
        }
    }


//
//    private val _chartDataMap = mutableStateMapOf<Pair<MetricType, TimeRange>, ChartData>()
//    val chartData = combine(_selectedMetricType, timeRange) { type, range ->
//        _chartDataMap[type to range] ?: ChartData(type)
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChartData(defaultMetricType))
//
//    private val _statDataMap = mutableStateMapOf<Pair<MetricType, TimeRange>, StatData<MetricValue>>()
//    val statData = combine(_selectedMetricType, timeRange) { type, range ->
//        _statDataMap[type to range] ?: StatData(metricType = type)
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatData(metricType = defaultMetricType))
//
//
//    init {
//        viewModelScope.launch {
//            MetricType.entries.forEach { metricType ->
//                TimeRange.entries.forEach { range ->
//                    launch {
//                        _isLoading.value = true
//                        val chart = chartRepository.getChartDataFlow(metricType, range).first()
//                        val stat = statisticsRepository.getStatData(metricType, range.days).first()
//                        _chartDataMap[metricType to range] = chart
//                        _statDataMap[metricType to range] = stat
//                        _isLoading.value = false
//                    }
//                }
//            }
//        }
//    }

//    val chartData = timeRangeFlow.flatMapLatest { timeRange ->
//        selectedMetricType.flatMapLatest { type ->
//            _isLoading.value = true
//            chartRepository.getChartDataFlow(type, timeRange).onEach {
//                _isLoading.value = false
//            }
//        }
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChartData(defaultMetricType))
//
//    val statData: StateFlow<StatData<MetricValue>> = selectedMetricType.flatMapLatest{ metricType ->
//        timeRangeFlow.flatMapLatest { range ->
//            _isLoading.value = true
//            statisticsRepository.getStatData(metricType, range.days).onEach {
//                _isLoading.value = false
//            }
//        }
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatData(metricType = MetricType.HEART))

    /// M-E gap
    val meGapStatValue: StateFlow<StatValue<MetricValue>> = statisticsRepository.getMeGapStatValueFlow()
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