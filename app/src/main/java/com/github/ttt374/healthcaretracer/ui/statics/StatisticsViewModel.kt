package com.github.ttt374.healthcaretracer.ui.statics


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.metric.MeasuredValue
import com.github.ttt374.healthcaretracer.data.metric.MetricDef
import com.github.ttt374.healthcaretracer.data.metric.MetricDefRegistry
import com.github.ttt374.healthcaretracer.data.repository.Config
import com.github.ttt374.healthcaretracer.data.repository.ConfigRepository
import com.github.ttt374.healthcaretracer.data.repository.StatisticsRepository
import com.github.ttt374.healthcaretracer.di.modules.StatisticsTimeRange
import com.github.ttt374.healthcaretracer.shared.DayPeriod
import com.github.ttt374.healthcaretracer.shared.TimeRange
import com.github.ttt374.healthcaretracer.shared.TimeRangeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModel @Inject constructor (val statisticsRepository: StatisticsRepository, configRepository: ConfigRepository,
                                               @StatisticsTimeRange private val timeRangeManager: TimeRangeManager) : ViewModel() {
    val config = configRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Config()) // for config.guideline
    val timeRange = timeRangeManager.timeRange
    val statisticsData = timeRangeManager.timeRangeFlow.flatMapLatest { timeRange ->
        statisticsRepository.getStatisticsFlow(timeRange)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsData())

//    fun statValue(metricDef: MetricDef): StateFlow<StatValue<Double>>{
//        return timeRangeManager.timeRangeFlow.flatMapLatest { range ->
//            statisticsRepository.getStatValueFlow(metricDef, range.days)
//        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatValue())
//    }
//    fun dayPeriodStatValue(metricDef: MetricDef): StateFlow<Map<DayPeriod, StatValue<Double>>> =
//        timeRange.flatMapLatest { range ->
//            statisticsRepository.getDayPeriodStatValueFlow(metricDef, range.days)
//        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val statValueMap: Map<MetricDef, StateFlow<StatValue<Double>>> =
        MetricDefRegistry.defs.associateWith { def ->
            timeRangeManager.timeRangeFlow.flatMapLatest { range ->
                statisticsRepository.getStatValueFlow(def, range.days)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatValue())
        }

    val dayPeriodStatMap: Map<MetricDef, StateFlow<Map<DayPeriod, StatValue<Double>>>> =
        MetricDefRegistry.defs.associateWith { def ->
            timeRangeManager.timeRangeFlow.flatMapLatest { range ->
                statisticsRepository.getDayPeriodStatValueFlow(def, range.days)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
        }

    fun setSelectedRange(range: TimeRange) {
        viewModelScope.launch {
            timeRangeManager.setSelectedRange(range)
        }
    }
}

