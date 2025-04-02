package com.github.ttt374.healthcaretracer.ui.statics


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.Item
import com.github.ttt374.healthcaretracer.data.ItemRepository
import com.github.ttt374.healthcaretracer.data.averageOrNull
import com.github.ttt374.healthcaretracer.data.gapME
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor (itemRepository: ItemRepository) : ViewModel(){
    private val _selectedRange = MutableStateFlow(TimeRange.ONE_WEEK)
    val selectedRange: StateFlow<TimeRange> = _selectedRange

    //val items = itemRepository.getAllItemsFlow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    //val filteredItems = itemRepository.getRecentItemsFlow(selectedRange.value.days.toInt()).stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredItems = selectedRange.flatMapLatest { range ->
        itemRepository.getRecentItemsFlow(range.days.toInt())
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

//
//
//    val filteredItems = combine(items, selectedRange) { items, range ->
//        val cutoffDate = Instant.now().minus(range.days, ChronoUnit.DAYS)
//        items.filter { it.measuredAt.isAfter(cutoffDate) }
//    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSelectedRange(range: TimeRange) {
        _selectedRange.value = range
    }

    val statistics = filteredItems.map { items ->
        val bpUpperList = items.mapNotNull { it.bpUpper?.toDouble() }
        val bpLowerList = items.mapNotNull { it.bpLower?.toDouble() }
        val pulseList = items.mapNotNull { it.pulse?.toDouble() }
        val bodyWeightList = items.mapNotNull { it.bodyWeight?.toDouble() }
        val meGapList = items.groupBy { it.measuredAt.atZone(ZoneId.systemDefault()).toLocalDate() }
            .map { (_, dailyItems) -> dailyItems.gapME() }.filterNotNull()

        StatisticsData(
            avgBpUpper = bpUpperList.averageOrNull(),
            avgBpLower = bpLowerList.averageOrNull(),
            avgPulse = pulseList.averageOrNull(),
            avgBodyWeight = bodyWeightList.averageOrNull(),
            avgMeGap = meGapList.averageOrNull(),
            maxBpUpper = bpUpperList.maxOrNull(),
            maxBpLower = bpLowerList.maxOrNull(),
            maxPulse = pulseList.maxOrNull(),
            maxBodyWeight = bodyWeightList.maxOrNull(),
            maxMeGap = meGapList.maxOrNull(),
            minBpUpper = bpUpperList.minOrNull(),
            minBpLower = bpLowerList.minOrNull(),
            minPulse = pulseList.minOrNull(),
            minBodyWeight = bodyWeightList.minOrNull(),
            minMeGap = meGapList.minOrNull()
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, StatisticsData())

}

data class StatisticsData(
    val avgBpUpper: Double? = null,
    val avgBpLower: Double? = null,
    val avgPulse: Double? = null,
    val avgBodyWeight: Double? = null,
    val avgMeGap: Double? = null,
    val maxBpUpper: Double? = null,
    val maxBpLower: Double? = null,
    val maxPulse: Double? = null,
    val maxBodyWeight: Double? = null,
    val maxMeGap: Double? = null,
    val minBpUpper: Double? = null,
    val minBpLower: Double? = null,
    val minPulse: Double? = null,
    val minBodyWeight: Double? = null,
    val minMeGap: Double? = null
)
