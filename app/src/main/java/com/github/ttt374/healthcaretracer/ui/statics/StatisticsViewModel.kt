package com.github.ttt374.healthcaretracer.ui.statics


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.metric.MeasuredValue
import com.github.ttt374.healthcaretracer.data.metric.MetricDef
import com.github.ttt374.healthcaretracer.data.metric.MetricDefRegistry
import com.github.ttt374.healthcaretracer.data.metric.StatValue
import com.github.ttt374.healthcaretracer.data.repository.Config
import com.github.ttt374.healthcaretracer.data.repository.ConfigRepository
import com.github.ttt374.healthcaretracer.data.repository.ItemRepository
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModel @Inject constructor (private val statisticsRepository: StatisticsRepository, configRepository: ConfigRepository,
                                               private val itemRepository: ItemRepository,
                                               @StatisticsTimeRange private val timeRangeManager: TimeRangeManager) : ViewModel() {
    val config = configRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Config()) // for config.guideline
    val timeRange = timeRangeManager.timeRange

    val statValueMap: Map<MetricDef, StateFlow<StatValue>> =
        MetricDefRegistry.defs.associateWith { def ->
            timeRangeManager.timeRangeFlow.flatMapLatest { range ->
                statisticsRepository.getStatValueFlow(def, range.days)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatValue())
        }

    val dayPeriodStatMap: Map<MetricDef, StateFlow<Map<DayPeriod, StatValue>>> =
        MetricDefRegistry.defs.associateWith { def ->
            timeRangeManager.timeRangeFlow.flatMapLatest { range ->
                statisticsRepository.getDayPeriodStatValueFlow(def, range.days)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
        }

    val firstDateFlow: StateFlow<Instant> = itemRepository.getAllItemsFlow().map { it.firstOrNull()?.measuredAt ?: Instant.now() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Instant.now())

    fun setSelectedRange(range: TimeRange) {
        viewModelScope.launch {
            timeRangeManager.setSelectedRange(range)
        }
    }
}

