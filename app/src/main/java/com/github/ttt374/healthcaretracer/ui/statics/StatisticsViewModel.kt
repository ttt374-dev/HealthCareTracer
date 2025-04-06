package com.github.ttt374.healthcaretracer.ui.statics


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.datastore.Config
import com.github.ttt374.healthcaretracer.data.datastore.ConfigRepository
import com.github.ttt374.healthcaretracer.data.datastore.Preferences
import com.github.ttt374.healthcaretracer.data.datastore.PreferencesRepository
import com.github.ttt374.healthcaretracer.data.item.DailyItem
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.item.ItemRepository
import com.github.ttt374.healthcaretracer.data.item.averageOrNull
import com.github.ttt374.healthcaretracer.data.item.isEvening
import com.github.ttt374.healthcaretracer.data.item.isMorning
import com.github.ttt374.healthcaretracer.ui.common.TimeRange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    val config = configRepository.dataFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        Config()
    )
    private val pref = preferencesRepository.dataFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        Preferences()
    )
    init {
        viewModelScope.launch {
            preferencesRepository.dataFlow.collect {
                Log.d("pref dataflow", it.toString())
            }
        }
    }

    // TimeRange だけを切り出して StateFlow として公開
    val timeRange: StateFlow<TimeRange> = preferencesRepository.dataFlow.map { it.timeRangeStatistics }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimeRange.Default) // デフォルト指定

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredItems = timeRange.flatMapLatest { range ->
        itemRepository.getRecentItemsFlow(range.days)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedRange(range: TimeRange) {
        viewModelScope.launch {
            //preferencesRepository.updateData(pref.value.copy(timeRangeStatistics = range))
            preferencesRepository.updateTimeRangeStatistics(range)
        }
    }

    val bpUpperStatistics =
        filteredItems.map { items -> getStatTimeOfDay(items, { it.bpUpper?.toDouble() ?: 0.0 }) }
            .stateIn(viewModelScope, SharingStarted.Lazily, StatTimeOfDay())
    val bpLowerStatistics =
        filteredItems.map { items -> getStatTimeOfDay(items, { it.bpLower?.toDouble() ?: 0.0 }) }
            .stateIn(viewModelScope, SharingStarted.Lazily, StatTimeOfDay())
    val pulseStatistics =
        filteredItems.map { items -> getStatTimeOfDay(items, { it.pulse?.toDouble() ?: 0.0 }) }
            .stateIn(viewModelScope, SharingStarted.Lazily, StatTimeOfDay())
    val bodyWeightStatistics =
        filteredItems.map { items -> getStatTimeOfDay(items, { it.bodyWeight?.toDouble() }) }
            .stateIn(viewModelScope, SharingStarted.Lazily, StatTimeOfDay())
    val meGapList: StateFlow<List<Double>> = filteredItems.map { items ->
        items.groupBy { it.measuredAt.atZone(ZoneId.systemDefault()).toLocalDate() }
            .map { (date, dailyItems) -> DailyItem(date = date, items = dailyItems).meGap() ?: 0.0 }
    }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private fun getStatValue(list: List<Double?>): StatValue {
        return StatValue(
            avg = list.averageOrNull(),
            max = list.filterNotNull().maxOrNull(),
            min = list.filterNotNull().minOrNull()
        )
    }

    private fun getStatTimeOfDay(items: List<Item>, takeValue: (Item) -> Double?): StatTimeOfDay {
        val allList = items.map { takeValue(it) }
        val morningList = items.filter { it.measuredAt.isMorning() }.map { takeValue(it) }
        val eveningList = items.filter { it.measuredAt.isEvening() }.map { takeValue(it) }

        return StatTimeOfDay(
            all = getStatValue(allList),
            morning = getStatValue(morningList),
            evening = getStatValue(eveningList)
        )
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
