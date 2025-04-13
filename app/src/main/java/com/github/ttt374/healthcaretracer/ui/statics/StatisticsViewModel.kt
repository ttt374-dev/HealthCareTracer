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
import com.github.ttt374.healthcaretracer.ui.common.TimeOfDay
import com.github.ttt374.healthcaretracer.ui.common.TimeRange
import com.github.ttt374.healthcaretracer.ui.common.toTimeOfDay
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
@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModel @Inject constructor (itemRepository: ItemRepository, configRepository: ConfigRepository, val preferencesRepository: PreferencesRepository) : ViewModel() {
    val config = configRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Config())

    // TimeRange だけを切り出して StateFlow として公開
    val timeRange: StateFlow<TimeRange> = preferencesRepository.dataFlow.map { it.timeRangeStatistics }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimeRange.Default) // デフォルト指定

    private val recentItemsFlow = timeRange.flatMapLatest { range -> itemRepository.getRecentItemsFlow(range.days)}

    val statistics = recentItemsFlow.map { items ->
        val bpUpper = getStatTimeOfDay(items) { it.bpUpper?.toDouble()  }
        val bpLower = getStatTimeOfDay(items) { it.bpLower?.toDouble()  }
        val pulse = getStatTimeOfDay(items) { it.pulse?.toDouble()  }
        val bodyWeight = getStatTimeOfDay(items) { it.bodyWeight  }
        val zone = ZoneId.systemDefault()
        val meGap = items.groupBy { it.measuredAt.atZone(zone).toLocalDate() }
            .map { (date, dailyItems) -> DailyItem(date = date, items = dailyItems).meGap(ZoneId.systemDefault(), config.value.morningRange, config.value.eveningRange)}.filterNotNull()

        StatisticsData(
            bpUpper = bpUpper,
            bpLower = bpLower,
            pulse = pulse,
            bodyWeight = bodyWeight,
            meGap = meGap
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsData())

    private fun getStatValue(list: List<Double?>): StatValue {
        return StatValue(
            avg = list.filterNotNull().averageOrNull(),
            max = list.filterNotNull().maxOrNull(),
            min = list.filterNotNull().minOrNull()
        )
    }

    private fun getStatTimeOfDay(items: List<Item>, takeValue: (Item) -> Double?): StatTimeOfDay {
        val valuesWithTime = items.map { it to takeValue(it) }

        val allList = valuesWithTime.map { it.second }
        val morningList = valuesWithTime.filter { it.first.measuredAt.toTimeOfDay(config=config.value.timeOfDayConfig) is TimeOfDay.Morning }.map { it.second }
        val afternoonList = valuesWithTime.filter { it.first.measuredAt.toTimeOfDay(config=config.value.timeOfDayConfig) is TimeOfDay.Afternoon}.map { it.second }
        val eveningList = valuesWithTime.filter { it.first.measuredAt.toTimeOfDay(config=config.value.timeOfDayConfig) is TimeOfDay.Evening}.map { it.second }

//        val allList = items.map { takeValue(it) }
//        val morningList = items.filter { it.measuredAt.toTimeOfDay(config = config.value.timeOfDayConfig) is TimeOfDay.Morning }.map { takeValue(it) }
//        val afternoonList = items.filter { it.measuredAt.toTimeOfDay(config = config.value.timeOfDayConfig) is TimeOfDay.Afternoon }.map { takeValue(it) }
//        val eveningList = items.filter { it.measuredAt.toTimeOfDay(config = config.value.timeOfDayConfig) is TimeOfDay.Evening }.map { takeValue(it) }

        return StatTimeOfDay(
            all = getStatValue(allList),
            morning = getStatValue(morningList),
            afternoon = getStatValue(afternoonList),
            evening = getStatValue(eveningList)
        )
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
    //val night: StatValue = StatValue(),

)
data class StatValue(
    val avg: Double? = null,
    val max: Double? = null,
    val min: Double? = null
)

data class StatisticsData(
    val bpUpper: StatTimeOfDay = StatTimeOfDay(),
    val bpLower: StatTimeOfDay = StatTimeOfDay(),
    val pulse: StatTimeOfDay = StatTimeOfDay(),
    val bodyWeight: StatTimeOfDay = StatTimeOfDay(),
    val meGap: List<Double> = emptyList(),
)