package com.github.ttt374.healthcaretracer.ui.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.ItemRepository
import com.github.ttt374.healthcaretracer.ui.home.groupByDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ChartViewModel @Inject constructor(itemRepository: ItemRepository) : ViewModel() {
    val dailyItems = itemRepository.getAllItemsFlow().map { items ->items.groupByDate() }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
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
}
