package com.github.ttt374.healthcaretracer.ui.statics


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.repository.Config
import com.github.ttt374.healthcaretracer.data.repository.ConfigRepository
import com.github.ttt374.healthcaretracer.data.repository.ItemRepository
import com.github.ttt374.healthcaretracer.data.repository.PreferencesRepository
import com.github.ttt374.healthcaretracer.ui.common.TimeRange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModel @Inject constructor (itemRepository: ItemRepository, configRepository: ConfigRepository,
                                               private val preferencesRepository: PreferencesRepository) : ViewModel() {
    private val configFlow = configRepository.dataFlow
    val config = configFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Config())
    private val timeOfDayConfigFlow = configFlow.map { it.timeOfDayConfig }
    private val timeRangeFlow = preferencesRepository.dataFlow.map { it.timeRangeStatistics }
    val timeRange = timeRangeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimeRange.Default)
    private val recentItemsFlow = timeRangeFlow.flatMapLatest { range -> itemRepository.getRecentItemsFlow(range.days) }

    val statistics = combine(timeOfDayConfigFlow, recentItemsFlow){ timeOfDayConfig, items ->
        val calculator = StatCalculator(timeOfDayConfig)

        StatisticsData(
            bloodPressure = calculator.calculateStat(items, { BloodPressure(it.bpUpper, it.bpLower) }),
            pulse = calculator.calculateStat(items,  { it.pulse?.toDouble() }),
            bodyWeight = calculator.calculateStat(items,  { it.bodyWeight }),
            bodyTemperature = calculator.calculateStat(items,  { it.bodyTemperature }),
            meGap = calculator.getMeStats(items)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsData())

    fun setSelectedRange(range: TimeRange) {
        viewModelScope.launch {
            preferencesRepository.updateData {
                it.copy(timeRangeStatistics = range)
            }
        }
    }
}

