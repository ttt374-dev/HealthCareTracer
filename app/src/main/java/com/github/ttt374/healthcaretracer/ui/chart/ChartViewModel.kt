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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZoneId
import javax.inject.Inject

data class ChartEntries(
    val bpUpper: List<Entry> = emptyList(),
    val bpLower: List<Entry> = emptyList(),
    val pulse: List<Entry> = emptyList(),
    val bodyWeight: List<Entry> = emptyList(),
//    val targetBpUpper: List<Entry> = emptyList(),
//    val targetBpLower: List<Entry> = emptyList(),
//    val targetBodyWeight: List<Entry> = emptyList(),
)
data class ChartUiState (
    val actualEntries: ChartEntries = ChartEntries(),
    val targetEntries: ChartEntries = ChartEntries(),
    val timeRange: TimeRange = TimeRange.Default,
    val selectedTabIndex: Int = 0,
)
@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ChartViewModel @Inject constructor(val itemRepository: ItemRepository, configRepository: ConfigRepository, private val preferencesRepository: PreferencesRepository) : ViewModel() {
    private val config = configRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Config())
    //private val pref = preferencesRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Preferences())

    // TimeRange だけを切り出して StateFlow として公開
    private val timeRangeFlow = preferencesRepository.dataFlow.map { it.timeRangeChart }
//    val timeRange: StateFlow<TimeRange> = timeRangeFlow
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimeRange.Default) // デフォルト指定

     private val dailyItemsFlow = preferencesRepository.dataFlow.map { it.timeRangeChart }.flatMapLatest { range ->
        itemRepository.getRecentItemsFlow(range.days).map { items -> items.groupByDate()}
    }

    private fun getEntriesFlow(takeValue: (DailyItem) -> Double?): Flow<List<Entry>>{
        return dailyItemsFlow.map { list -> list.toEntries { takeValue(it) } }
    }
//    fun getTargetEntriesFlow(getValue: (Config) -> Number): Flow<List<Entry>> {
//        return combine(config, dailyItemsFlow) { config, daily ->
//            val y = getValue(config).toFloat()
//            List(daily.size) { index -> Entry(index.toFloat(), y) }
//        }
//    }


    private fun getTargetEntriesFlow(takeValue: (Config) -> Number): Flow<List<Entry>> {
        return config.map { takeValue(it) }.flatMapLatest { target ->
            getEntriesFlow { target.toDouble() }
        }
    }
    // 各グラフ用の Entry リスト
//    val bpUpperEntries = getEntriesFlow { it.avgBpUpper }.stateInListEntry(viewModelScope)
//    val bpLowerEntries = getEntriesFlow { it.avgBpLower }.stateInListEntry(viewModelScope)
//    val pulseEntries = getEntriesFlow { it.avgPulse }.stateInListEntry(viewModelScope)
//    val bodyWeightEntries = getEntriesFlow { it.avgBodyWeight }.stateInListEntry(viewModelScope)
//
//    val targetBpUpperEntries = getTargetEntriesFlow { it.targetBpUpper }.stateInListEntry(viewModelScope)
//    val targetBpLowerEntries = getTargetEntriesFlow { it.targetBpLower }.stateInListEntry(viewModelScope)
//    val targetBodyWeightEntries = getTargetEntriesFlow({ it.targetBodyWeight }).stateInListEntry(viewModelScope)

//    val entries = EntriesFlow(
//        bpUpper = getEntriesFlow { it.avgBpUpper }.stateInListEntry(viewModelScope),
//        bpLower = getEntriesFlow { it.avgBpLower }.stateInListEntry(viewModelScope),
//        pulse = getEntriesFlow { it.avgPulse }.stateInListEntry(viewModelScope),
//        bodyWeight = getEntriesFlow { it.avgBodyWeight }.stateInListEntry(viewModelScope),
//        targetBpUpper = getTargetEntriesFlow { it.targetBpUpper }.stateInListEntry(viewModelScope),
//        targetBpLower = getTargetEntriesFlow { it.targetBpLower }.stateInListEntry(viewModelScope),
//        targetBodyWeight = getTargetEntriesFlow { it.targetBodyWeight }.stateInListEntry(viewModelScope)
//    )
//    val chartEntries = combine(
//        getEntriesFlow { it.avgBpUpper },
//        getEntriesFlow { it.avgBpLower },
//        getEntriesFlow { it.avgPulse },
//        getEntriesFlow { it.avgBodyWeight },
//    ){ upper, lower, pulse, bodyWeight -> ChartEntries(upper, lower, pulse, bodyWeight)
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChartEntries())
//
//    val chartTargetEntries = combine(
//        getTargetEntriesFlow { it.targetBpUpper },
//        getTargetEntriesFlow { it.targetBpLower },
//        getTargetEntriesFlow { it.targetBodyWeight },
//    ){ upper, lower, bodyWeight -> ChartEntries(upper, lower, emptyList(), bodyWeight)
//    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChartEntries())


    private val actualEntriesFlow = combine(
        getEntriesFlow { it.avgBpUpper },
        getEntriesFlow { it.avgBpLower },
        getEntriesFlow { it.avgPulse },
        getEntriesFlow { it.avgBodyWeight },
    ){ upper, lower, pulse, bodyWeight ->
        ChartEntries(upper, lower, pulse, bodyWeight)
    }

    private val targetEntriesFlow = combine(
        getTargetEntriesFlow { it.targetBpUpper },
        getTargetEntriesFlow { it.targetBpLower },
        getTargetEntriesFlow { it.targetBodyWeight },
    ){ upper, lower, bodyWeight ->
        ChartEntries(upper, lower, emptyList(), bodyWeight)
    }
    private val selectedTabIndexFlow = MutableStateFlow(0)

    val uiState: StateFlow<ChartUiState> = combine(
        actualEntriesFlow,
        targetEntriesFlow,
        timeRangeFlow,
        selectedTabIndexFlow,
    ) { actualEntries, targetEntries, timeRange, selectedTabIndex ->
        ChartUiState(
            actualEntries = actualEntries,
            targetEntries = targetEntries,
            timeRange = timeRange,
            selectedTabIndex = selectedTabIndex
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChartUiState())
//    fun getChartEntriesFlow(type: ChartType): Flow<List<List<Entry>>> {
//        return when (type) {
//            ChartType.BloodPressure -> combine(bpUpperEntries, bpLowerEntries) { upper, lower ->
//                listOf(upper, lower)
//            }
//            ChartType.Pulse -> pulseEntries.map { listOf(it) }
//            ChartType.BodyWeight -> combine(bodyWeightEntries, targetBodyWeightEntries) { actual, target ->
//                listOf(actual, target)
//            }
//        }
//    }

//    fun updatePreferences(transform: suspend (pref: Preferences) -> Preferences){
//        viewModelScope.launch {
//            runCatching {
//                preferencesRepository.updateData { transform(it) }
//            }.onFailure {
//                Log.e("pref error", "Failed to update pref")
//            }
//        }
//    }

    fun updateTimeRange(range: TimeRange) {
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
    fun updateSelectedTabIndex(index: Int) {
        //selectedTabIndexFlow.value = index
        viewModelScope.launch {
            selectedTabIndexFlow.emit(index)
        }

    }

//    private fun Flow<List<Entry>>.stateInListEntry(
//        scope: CoroutineScope,
//        defaultValue: List<Entry> = emptyList(),
//        sharingStarted: SharingStarted = SharingStarted.WhileSubscribed(5000)
//    ): StateFlow<List<Entry>> {
//        return this.stateIn(scope, sharingStarted, defaultValue)
//    }
}
fun List<DailyItem>.toEntries(zoneId: ZoneId = ZoneId.systemDefault(), takeValue: (DailyItem) -> Double?): List<Entry> {
    return mapNotNull { dailyItem ->
        takeValue(dailyItem)?.toFloat()?.let { value ->
            Entry(dailyItem.date.atStartOfDay(zoneId).toInstant().toEpochMilli().toFloat(), value)
        }
    }
}