package com.github.ttt374.healthcaretracer.ui.statics


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.DailyItem
import com.github.ttt374.healthcaretracer.data.Item
import com.github.ttt374.healthcaretracer.data.ItemRepository
import com.github.ttt374.healthcaretracer.data.averageOrNull
import com.github.ttt374.healthcaretracer.data.isEvening
import com.github.ttt374.healthcaretracer.data.isMorning
import com.github.ttt374.healthcaretracer.ui.common.TimeRange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor (itemRepository: ItemRepository) : ViewModel(){
    private val _selectedRange = MutableStateFlow(TimeRange.ONE_WEEK)
    val selectedRange: StateFlow<TimeRange> = _selectedRange

    //val items = itemRepository.getAllItemsFlow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    //val filteredItems = itemRepository.getRecentItemsFlow(selectedRange.value.days.toInt()).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredItems = selectedRange.flatMapLatest { range ->
        itemRepository.getRecentItemsFlow(range.days)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

//
//
//    val filteredItems = combine(items, selectedRange) { items, range ->
//        val cutoffDate = Instant.now().minus(range.days, ChronoUnit.DAYS)
//        items.filter { it.measuredAt.isAfter(cutoffDate) }
//    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSelectedRange(range: TimeRange) {
        if (_selectedRange.value != range) {
            _selectedRange.value = range
        }
    }

    val statistics = filteredItems.map { items -> getStatisticData(items) }
        .stateIn(viewModelScope, SharingStarted.Lazily, StatisticsData())
    val statisticsMorning = filteredItems.map { items -> getStatisticData(items.filter { it.measuredAt.isMorning()}) }
        .stateIn(viewModelScope, SharingStarted.Lazily, StatisticsData())
    val statisticsEvening = filteredItems.map { items -> getStatisticData(items.filter { it.measuredAt.isEvening()}) }
        .stateIn(viewModelScope, SharingStarted.Lazily, StatisticsData())

    private fun getStatisticData(items: List<Item>): StatisticsData {
        val bpUpperList = items.mapNotNull { it.bpUpper?.toDouble() }
        val bpLowerList = items.mapNotNull { it.bpLower?.toDouble() }
        val pulseList = items.mapNotNull { it.pulse?.toDouble() }
        val bodyWeightList = items.mapNotNull { it.bodyWeight?.toDouble() }
        val meGapList = items.groupBy { it.measuredAt.atZone(ZoneId.systemDefault()).toLocalDate() }
            .map { (date, dailyItems) -> DailyItem(date=date, items=dailyItems).meGap() }.filterNotNull()


        return StatisticsData(
            bpUpper = StatValue(
                avg = bpUpperList.averageOrNull(),
                max = bpUpperList.maxOrNull(),
                min = bpUpperList.minOrNull()
            ),
            bpLower = StatValue(
                avg = bpLowerList.averageOrNull(),
                max = bpLowerList.maxOrNull(),
                min = bpLowerList.minOrNull()
            ),
            meGap = StatValue(
                avg = meGapList.averageOrNull(),
                max = meGapList.maxOrNull(),
                min = meGapList.minOrNull()
            ),
            pulse = StatValue(
                avg = pulseList.averageOrNull(),
                max = pulseList.maxOrNull(),
                min = pulseList.minOrNull()
            ),
            bodyWeight = StatValue(
                avg = bodyWeightList.averageOrNull(),
                max = bodyWeightList.maxOrNull(),
                min = bodyWeightList.minOrNull()
            ),
        )
    }
}

data class StatisticsData(
    val bpUpper: StatValue = StatValue(),
    val bpLower: StatValue = StatValue(),
    val pulse: StatValue = StatValue(),
    val bodyWeight: StatValue = StatValue(),
    val meGap: StatValue = StatValue(),
)
data class StatTimeDay (
    val all: StatValue,
    val morning: StatValue,
    val afternoon: StatValue,
    val evening: StatValue,
    val night: StatValue,

)
data class StatValue(
    val avg: Double? = null,
    val max: Double? = null,
    val min: Double? = null
)
