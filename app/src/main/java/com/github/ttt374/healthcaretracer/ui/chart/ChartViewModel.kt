package com.github.ttt374.healthcaretracer.ui.chart

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.datastore.Config
import com.github.ttt374.healthcaretracer.data.datastore.ConfigRepository
import com.github.ttt374.healthcaretracer.data.item.ItemRepository
import com.github.ttt374.healthcaretracer.ui.common.TimeRange
import com.github.ttt374.healthcaretracer.ui.home.groupByDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChartViewModel @Inject constructor(itemRepository: ItemRepository, configRepository: ConfigRepository) : ViewModel() {
    private val _selectedRange = MutableStateFlow(TimeRange.ONE_WEEK)
    val selectedRange: StateFlow<TimeRange> = _selectedRange

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyItems = selectedRange.flatMapLatest { range ->
        itemRepository.getRecentItemsFlow(range.days).map { items -> items.groupByDate()}
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    private val configFlow = configRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Config())

    private val upperTarget = mutableIntStateOf(140)
    private val lowerTarget = mutableIntStateOf(90)
    init {
        viewModelScope.launch {
            configFlow.collect {
                upperTarget.intValue = it.targetBpUpper
                lowerTarget.intValue = it.targetBpLower
            }
        }
    }

//    val dailyItems = itemRepository.getRecentItemsFlow(selectedRange.value.days.toInt()).map { items ->items.groupByDate() }
//        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    //val dailyItems = itemRepository.getDailyItemsFlow()
    //    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

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
        _selectedRange.value = range
    }

    //val targetBpUpperEntries = dailyItems.map { it.toEntries { upperTarget.intValue.toDouble() }}
//    val upperTarget = selectedGuideline.normal.upperRange.last
//    val lowerTarget = selectedGuideline.normal.lowerRange.last
    val targetBodyWeight = 60.0
    val targetBpUpperEntries = dailyItems.map { it.toEntries { upperTarget.intValue.toDouble() }}
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val targetBpLowerEntries = dailyItems.map { it.toEntries { lowerTarget.intValue.toDouble() }}
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    val targetBodyWeightEntries = dailyItems.map { it.toEntries { targetBodyWeight }}
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
