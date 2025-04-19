package com.github.ttt374.healthcaretracer.ui.statics


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.item.DailyItem
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.repository.Config
import com.github.ttt374.healthcaretracer.data.repository.ConfigRepository
import com.github.ttt374.healthcaretracer.data.repository.ItemRepository
import com.github.ttt374.healthcaretracer.data.repository.LocalTimeRange
import com.github.ttt374.healthcaretracer.data.repository.PreferencesRepository
import com.github.ttt374.healthcaretracer.ui.common.TimeOfDay
import com.github.ttt374.healthcaretracer.ui.common.TimeRange
import com.github.ttt374.healthcaretracer.ui.common.averageOrNull
import com.github.ttt374.healthcaretracer.ui.common.maxOrNull
import com.github.ttt374.healthcaretracer.ui.common.minOrNull
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
class StatisticsViewModel @Inject constructor (itemRepository: ItemRepository, configRepository: ConfigRepository,
                                               private val preferencesRepository: PreferencesRepository) : ViewModel() {
    val config = configRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Config())

    // TimeRange だけを切り出して StateFlow として公開
    val timeRange: StateFlow<TimeRange> = preferencesRepository.dataFlow.map { it.timeRangeStatistics }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimeRange.Default) // デフォルト指定

    private val recentItemsFlow = timeRange.flatMapLatest { range -> itemRepository.getRecentItemsFlow(range.days)}

    val statistics = recentItemsFlow.map { items ->
        val bp = getStatTimeOfDay(items) { BloodPressure(it.bpUpper, it.bpLower)}
        val bpUpper: StatTimeOfDay<Double> = getStatTimeOfDay(items) { it: Item -> it.bpUpper?.toDouble()  }
        val bpLower = getStatTimeOfDay(items) { it.bpLower?.toDouble()  }
        val pulse = getStatTimeOfDay(items) { it.pulse?.toDouble()  }
        val bodyWeight = getStatTimeOfDay(items) { it.bodyWeight  }
        val bodyTemperature = getStatTimeOfDay(items) { it.bodyTemperature }
        val zone = ZoneId.systemDefault()
        val timeOfDayConfig = config.value.timeOfDayConfig
        val meGap = items.groupBy { it.measuredAt.atZone(zone).toLocalDate() }
            .map { (date, dailyItems) -> DailyItem(date = date, items = dailyItems).meGap(ZoneId.systemDefault(),
                LocalTimeRange(timeOfDayConfig.morning, timeOfDayConfig.afternoon),
                LocalTimeRange(timeOfDayConfig.evening, timeOfDayConfig.morning)) }.filterNotNull()

        StatisticsData(
            bloodPressure = bp,
            bpUpper = bpUpper,
            bpLower = bpLower,
            pulse = pulse,
            bodyWeight = bodyWeight,
            bodyTemperature = bodyTemperature,
            meGap = meGap
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsData())

    private fun <T> getStatValue(list: List<T>): StatValue<T> {
        return StatValue(
            avg = list.averageOrNull(),
            max = list.maxOrNull(),
            min = list.minOrNull()
        )
    }
//    private fun getStatValue(list: List<Double?>): StatValue<Double> {
//        return StatValue(
//            avg = list.filterNotNull().averageOrNull(),
//            max = list.filterNotNull().maxOrNull(),
//            min = list.filterNotNull().minOrNull()
//        )
//    }
//    private fun getStatValue(list: List<BloodPressure?>): StatValue<BloodPressure> {
//        return StatValue(
//            avg = list.filterNotNull().averageOrNull(),
//            max = list.filterNotNull().maxOrNull(),
//            min = list.filterNotNull().minOrNull()
//        )
//    }
    private fun <T> getStatTimeOfDay(items: List<Item>, takeValue: (Item) -> T?): StatTimeOfDay<T> {
        val valuesWithTime = items.mapNotNull { item ->
            takeValue(item)?.let { value: T -> item to value }
        }

        val allList = valuesWithTime.map { it.second }
        val morningList = valuesWithTime.filter { it.first.measuredAt.toTimeOfDay(config=config.value.timeOfDayConfig) is TimeOfDay.Morning }.map { it.second }
        val afternoonList = valuesWithTime.filter { it.first.measuredAt.toTimeOfDay(config=config.value.timeOfDayConfig) is TimeOfDay.Afternoon}.map { it.second }
        val eveningList = valuesWithTime.filter { it.first.measuredAt.toTimeOfDay(config=config.value.timeOfDayConfig) is TimeOfDay.Evening}.map { it.second }

        return StatTimeOfDay<T>(
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

data class StatTimeOfDay <T> (
    val all: StatValue<T> = StatValue(),
    val morning: StatValue<T> = StatValue(),
    val afternoon: StatValue<T> = StatValue(),
    val evening: StatValue<T> = StatValue(),
    //val night: StatValue = StatValue(),

)
data class StatValue<T>(
    val avg: T? = null,
    val max: T? = null,
    val min: T? = null
)
//data class StatValue(
//    val avg: Double? = null,
//    val max: Double? = null,
//    val min: Double? = null
//)

data class StatisticsData(
    val bloodPressure: StatTimeOfDay<BloodPressure> = StatTimeOfDay(),
    val bpUpper: StatTimeOfDay<Double> = StatTimeOfDay(),
    val bpLower: StatTimeOfDay<Double> = StatTimeOfDay(),
    val pulse: StatTimeOfDay<Double> = StatTimeOfDay(),
    val bodyWeight: StatTimeOfDay<Double> = StatTimeOfDay(),
    val bodyTemperature: StatTimeOfDay<Double> = StatTimeOfDay(),
    val meGap: List<Double> = emptyList(),
)