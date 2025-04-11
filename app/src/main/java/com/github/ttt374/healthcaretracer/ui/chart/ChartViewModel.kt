package com.github.ttt374.healthcaretracer.ui.chart

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.github.ttt374.healthcaretracer.data.datastore.Config
import com.github.ttt374.healthcaretracer.data.datastore.ConfigRepository
import com.github.ttt374.healthcaretracer.data.datastore.Preferences
import com.github.ttt374.healthcaretracer.data.datastore.PreferencesRepository
import com.github.ttt374.healthcaretracer.data.item.DailyItem
import com.github.ttt374.healthcaretracer.data.item.ItemRepository
import com.github.ttt374.healthcaretracer.ui.common.TimeRange
import com.github.ttt374.healthcaretracer.ui.home.groupByDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChartViewModel @Inject constructor(val itemRepository: ItemRepository, configRepository: ConfigRepository, private val preferencesRepository: PreferencesRepository) : ViewModel() {
    private val config = configRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Config())
    private val pref = preferencesRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Preferences())

    // TimeRange だけを切り出して StateFlow として公開
    val timeRange: StateFlow<TimeRange> = preferencesRepository.dataFlow.map { it.timeRangeChart }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimeRange.Default) // デフォルト指定

    @OptIn(ExperimentalCoroutinesApi::class)
     val dailyItemsFlow = preferencesRepository.dataFlow.map { it.timeRangeChart }.flatMapLatest { range ->
        itemRepository.getRecentItemsFlow(range.days).map { items -> items.groupByDate()}
    }

    private fun getEntriesFlow(takeValue: (DailyItem) -> Double?): Flow<List<Entry>>{
        return dailyItemsFlow.map { list -> list.toEntries { takeValue(it) } }
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTargetEntriesFlow(takeValue: (Config) -> Number): Flow<List<Entry>> {
        return config.map { takeValue(it) }.flatMapLatest { target ->
            getEntriesFlow( { target.toDouble()})
        }
    }
    // 各グラフ用の Entry リスト
    val bpUpperEntries = getEntriesFlow { it.avgBpUpper }.stateInListEntry(viewModelScope)
    val bpLowerEntries = getEntriesFlow { it.avgBpLower }.stateInListEntry(viewModelScope)
    val pulseEntries = getEntriesFlow { it.avgPulse }.stateInListEntry(viewModelScope)
    val bodyWeightEntries = getEntriesFlow { it.avgBodyWeight }.stateInListEntry(viewModelScope)

    val targetBpUpperEntries = getTargetEntriesFlow { it.targetBpUpper }.stateInListEntry(viewModelScope)
    val targetBpLowerEntries = getTargetEntriesFlow { it.targetBpLower }.stateInListEntry(viewModelScope)
    val targetBodyWeightEntries = getTargetEntriesFlow({ it.targetBodyWeight }).stateInListEntry(viewModelScope)


    fun setSelectedRange(range: TimeRange) {
        viewModelScope.launch {
            runCatching {
                preferencesRepository.updateData {
                    it.copy(timeRangeChart = range)
                }
            }.onFailure {
                // エラーハンドリング: 例) ログ出力やUIへのエラーメッセージ表示
                Log.e("ChartViewModel", "Failed to update time range", it)
            }
        }
    }

    private fun Flow<List<Entry>>.stateInListEntry(
        scope: CoroutineScope,
        defaultValue: List<Entry> = emptyList(),
        sharingStarted: SharingStarted = SharingStarted.WhileSubscribed(5000)
    ): StateFlow<List<Entry>> {
        return this.stateIn(scope, sharingStarted, defaultValue)
    }
}
