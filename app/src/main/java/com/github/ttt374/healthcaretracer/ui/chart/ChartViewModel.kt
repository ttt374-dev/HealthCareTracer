package com.github.ttt374.healthcaretracer.ui.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.datastore.Config
import com.github.ttt374.healthcaretracer.data.datastore.ConfigRepository
import com.github.ttt374.healthcaretracer.data.datastore.Preferences
import com.github.ttt374.healthcaretracer.data.datastore.PreferencesRepository
import com.github.ttt374.healthcaretracer.data.item.ItemRepository
import com.github.ttt374.healthcaretracer.ui.common.TimeRange
import com.github.ttt374.healthcaretracer.ui.home.groupByDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChartViewModel @Inject constructor(itemRepository: ItemRepository, configRepository: ConfigRepository, private val preferencesRepository: PreferencesRepository) : ViewModel() {
    private val config = configRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Config())
    private val pref = preferencesRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Preferences())

    // TimeRange だけを切り出して StateFlow として公開
    val timeRange: StateFlow<TimeRange> = preferencesRepository.dataFlow.map { it.timeRangeChart }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimeRange.Default) // デフォルト指定

//    private val _selectedRange = MutableStateFlow(pref.value.timeRange)
//    val selectedRange: StateFlow<TimeRange> = _selectedRange

    @OptIn(ExperimentalCoroutinesApi::class)
     val dailyItems = timeRange.flatMapLatest { range ->
        itemRepository.getRecentItemsFlow(range.days).map { items -> items.groupByDate()}
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // 各グラフ用の Entry リスト
    val bpUpperEntries = dailyItems.map { it.toEntries { it.avgBpUpper } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val bpLowerEntries = dailyItems.map { it.toEntries { it.avgBpLower } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val pulseEntries = dailyItems.map { it.toEntries { it.avgPulse } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val bodyWeightEntries = dailyItems.map { it.toEntries { it.avgBodyWeight } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSelectedRange(range: TimeRange) {
        viewModelScope.launch {
            //preferencesRepository.updateData(pref.value.copy(timeRange = range))
            preferencesRepository.updateTimeRangeChart(range)
        }
    }
    val targetBpUpperEntries = dailyItems.map { it.toEntries { config.value.targetBpUpper.toDouble() }}
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val targetBpLowerEntries = dailyItems.map { it.toEntries { config.value.targetBpLower.toDouble() }}
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val targetBodyWeightEntries = dailyItems.map { it.toEntries { config.value.targetBodyWeight }}
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
