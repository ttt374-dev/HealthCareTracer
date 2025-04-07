package com.github.ttt374.healthcaretracer.ui.statics


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.datastore.Config
import com.github.ttt374.healthcaretracer.data.datastore.ConfigRepository
import com.github.ttt374.healthcaretracer.data.datastore.PreferencesRepository
import com.github.ttt374.healthcaretracer.data.item.DailyItem
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.item.ItemRepository
import com.github.ttt374.healthcaretracer.data.item.averageOrNull
import com.github.ttt374.healthcaretracer.ui.common.TimeRange
import com.github.ttt374.healthcaretracer.ui.entry.toLocalTime
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
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor (itemRepository: ItemRepository, configRepository: ConfigRepository, val preferencesRepository: PreferencesRepository) : ViewModel() {
    val config = configRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Config())

    // TimeRange だけを切り出して StateFlow として公開
    val timeRange: StateFlow<TimeRange> = preferencesRepository.dataFlow.map { it.timeRangeStatistics }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimeRange.Default) // デフォルト指定

    @OptIn(ExperimentalCoroutinesApi::class)
    val recentItemsFlow = timeRange.flatMapLatest { range -> itemRepository.getRecentItemsFlow(range.days)}

    val bpUpperStatistics = recentItemsFlow.map { items -> getStatTimeOfDay(items, { it.bpUpper?.toDouble() ?: 0.0 }) }
            .stateInStat(viewModelScope)
    val bpLowerStatistics = recentItemsFlow.map { items -> getStatTimeOfDay(items, { it.bpLower?.toDouble() ?: 0.0 }) }
            .stateInStat(viewModelScope)
    val pulseStatistics = recentItemsFlow.map { items -> getStatTimeOfDay(items, { it.pulse?.toDouble() ?: 0.0 }) }
            .stateInStat(viewModelScope)
    val bodyWeightStatistics = recentItemsFlow.map { items -> getStatTimeOfDay(items, { it.bodyWeight?.toDouble() }) }
            .stateInStat(viewModelScope)
    val meGapList: StateFlow<List<Double>> = recentItemsFlow.map { items ->
        items.groupBy { it.measuredAt.atZone(ZoneId.systemDefault()).toLocalDate() }
            .map { (date, dailyItems) -> DailyItem(date = date, items = dailyItems).meGap(ZoneId.systemDefault(), config.value.morningRange, config.value.eveningRange) ?: 0.0 }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private fun getStatValue(list: List<Double?>): StatValue {
        return StatValue(
            avg = list.averageOrNull(),
            max = list.filterNotNull().maxOrNull(),
            min = list.filterNotNull().minOrNull()
        )
    }

    private fun getStatTimeOfDay(items: List<Item>, takeValue: (Item) -> Double?): StatTimeOfDay {
        val allList = items.map { takeValue(it) }
        val morningList = items.filter { config.value.morningRange.contains(it.measuredAt.toLocalTime()) }.map { takeValue(it) }
        val eveningList = items.filter { config.value.eveningRange.contains(it.measuredAt.toLocalTime()) }.map { takeValue(it) }

        return StatTimeOfDay(
            all = getStatValue(allList),
            morning = getStatValue(morningList),
            evening = getStatValue(eveningList)
        )
    }
    private fun Flow<StatTimeOfDay>.stateInStat(
        scope: CoroutineScope,
        defaultValue: StatTimeOfDay = StatTimeOfDay(),
        sharingStarted: SharingStarted = SharingStarted.WhileSubscribed(5000)
    ): StateFlow<StatTimeOfDay> {
        return this.stateIn(scope, sharingStarted, defaultValue)
    }

    fun setSelectedRange(range: TimeRange) {
        viewModelScope.launch {
            //preferencesRepository.updateData(pref.value.copy(timeRangeStatistics = range))
            preferencesRepository.updateData {
                it.copy(timeRangeStatistics = range)
            }
        }
    }
}

data class StatTimeOfDay (
    val all: StatValue = StatValue(),
    val morning: StatValue = StatValue(),
    val afternoon: StatValue = StatValue(),
    val evening: StatValue = StatValue(),
    val night: StatValue = StatValue(),

)
data class StatValue(
    val avg: Double? = null,
    val max: Double? = null,
    val min: Double? = null

)
