package com.github.ttt374.healthcaretracer.ui.statics


import androidx.compose.ui.text.font.FontWeight
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
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

//    val bpUpperStatistics = recentItemsFlow.map { items -> getStatTimeOfDay(items, { it.bpUpper?.toDouble() ?: 0.0 }) }
//            .stateInStat(viewModelScope)
//    val bpLowerStatistics = recentItemsFlow.map { items -> getStatTimeOfDay(items, { it.bpLower?.toDouble() ?: 0.0 }) }
//            .stateInStat(viewModelScope)
//    val pulseStatistics = recentItemsFlow.map { items -> getStatTimeOfDay(items, { it.pulse?.toDouble() ?: 0.0 }) }
//            .stateInStat(viewModelScope)
//    val bodyWeightStatistics = recentItemsFlow.map { items -> getStatTimeOfDay(items, { it.bodyWeight?.toDouble() }) }
//            .stateInStat(viewModelScope)
//    val meGapList: StateFlow<List<Double>> = recentItemsFlow.map { items ->
//        items.groupBy { it.measuredAt.atZone(ZoneId.systemDefault()).toLocalDate() }
//            .map { (date, dailyItems) -> DailyItem(date = date, items = dailyItems).meGap(ZoneId.systemDefault(), config.value.morningRange, config.value.eveningRange) ?: 0.0 }
//    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

//
//    private val statisticsFlow = combine(
//        recentItemsFlow.map { items -> getStatTimeOfDay(items, { it.bpUpper?.toDouble() ?: 0.0 }) },
//        recentItemsFlow.map { items -> getStatTimeOfDay(items, { it.bpLower?.toDouble() ?: 0.0 }) },
//        recentItemsFlow.map { items -> getStatTimeOfDay(items, { it.pulse?.toDouble() ?: 0.0 }) },
//        recentItemsFlow.map { items -> getStatTimeOfDay(items, { it.bodyWeight?.toDouble() ?: 0.0 }) },
//        recentItemsFlow.map { items ->
//            items.groupBy { it.measuredAt.atZone(ZoneId.systemDefault()).toLocalDate() }
//                .map { (date, dailyItems) -> DailyItem(date = date, items = dailyItems).meGap(ZoneId.systemDefault(), config.value.morningRange, config.value.eveningRange) ?: 0.0 }
//        },
//
//    ){ bpUpper, bpLower, pulse, bodyWeight, meGap ->
//        StatisticsData(bpUpper, bpLower, pulse, bodyWeight, meGap)
//    }
private val statisticsDataFlow: StateFlow<StatisticsData> = recentItemsFlow.map { items ->
        val bpUpper = getStatTimeOfDay(items) { it.bpUpper?.toDouble() ?: 0.0 }
        val bpLower = getStatTimeOfDay(items) { it.bpLower?.toDouble() ?: 0.0 }
        val pulse = getStatTimeOfDay(items) { it.pulse?.toDouble() ?: 0.0 }
        val bodyWeight = getStatTimeOfDay(items) { it.bodyWeight?.toDouble() ?: 0.0 }
        val meGap = items.groupBy { it.measuredAt.atZone(ZoneId.systemDefault()).toLocalDate() }
            .map { (date, dailyItems) -> DailyItem(date = date, items = dailyItems).meGap(ZoneId.systemDefault(), config.value.morningRange, config.value.eveningRange) ?: 0.0 }

        StatisticsData(
            bpUpper = bpUpper,
            bpLower = bpLower,
            pulse = pulse,
            bodyWeight = bodyWeight,
            meGap = meGap
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsData())

    val statistics = statisticsDataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsData())

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

    fun setSelectedRange(range: TimeRange) {
        viewModelScope.launch {
            //preferencesRepository.updateData(pref.value.copy(timeRangeStatistics = range))
            preferencesRepository.updateData {
                it.copy(timeRangeStatistics = range)
            }
        }
    }
    // `morning` と `evening` の差を計算する関数
    fun calculateMeGap(
        items: List<Item>,
        morningRange: ClosedRange<LocalTime>,
        eveningRange: ClosedRange<LocalTime>,
        takeValue: (Item) -> Double?
    ): Double {
        // morning と evening のアイテムをフィルタリング
        val morningList = items.filter {
            it.measuredAt.toLocalTime().let { time -> morningRange.contains(time) }
        }.map { takeValue(it) }

        val eveningList = items.filter {
            it.measuredAt.toLocalTime().let { time -> eveningRange.contains(time) }
        }.map { takeValue(it) }

        // morning と evening の平均値を計算
        val morningValue = morningList.averageOrNull() ?: 0.0
        val eveningValue = eveningList.averageOrNull() ?: 0.0

        // 差（meGap）を計算
        return morningValue - eveningValue
    }

// 使用例
//
//    val meGap = items.groupBy { it.measuredAt.atZone(ZoneId.systemDefault()).toLocalDate() }
//        .map { (date, dailyItems) ->
//            // `calculateMeGap` 関数を使って差を計算
//            calculateMeGap(dailyItems, config.value.morningRange, config.value.eveningRange)
//        }
//
//    // `takeValue` はアイテムから必要な値を取り出すヘルパーメソッド
//    fun takeValue(item: Item): Double? {
//        return item.bpUpper?.toDouble() // 例えば `bpUpper` を取り出す
//    }

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

data class StatisticsData(
    val bpUpper: StatTimeOfDay = StatTimeOfDay(),
    val bpLower: StatTimeOfDay = StatTimeOfDay(),
    val pulse: StatTimeOfDay = StatTimeOfDay(),
    val bodyWeight: StatTimeOfDay = StatTimeOfDay(),
    val meGap: List<Double> = emptyList(),
)