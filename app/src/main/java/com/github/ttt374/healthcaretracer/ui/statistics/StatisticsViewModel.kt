package com.github.ttt374.healthcaretracer.ui.statistics


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.metric.MetricDef
import com.github.ttt374.healthcaretracer.data.metric.MetricDefRegistry
import com.github.ttt374.healthcaretracer.data.metric.StatValue
import com.github.ttt374.healthcaretracer.data.repository.Config
import com.github.ttt374.healthcaretracer.data.repository.ConfigRepository
import com.github.ttt374.healthcaretracer.data.repository.ItemRepository
import com.github.ttt374.healthcaretracer.data.repository.StatisticsRepository
import com.github.ttt374.healthcaretracer.di.modules.StatisticsTimeRange
import com.github.ttt374.healthcaretracer.data.metric.DayPeriod
import com.github.ttt374.healthcaretracer.data.metric.MeasuredValue
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.metric.StatData
import com.github.ttt374.healthcaretracer.data.metric.toMeGapStatValue
import com.github.ttt374.healthcaretracer.data.repository.TimeRange
import com.github.ttt374.healthcaretracer.data.repository.TimeRangeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject
//
//@HiltViewModel
//@OptIn(ExperimentalCoroutinesApi::class)
//class StatisticsViewModel @Inject constructor (private val statisticsRepository: StatisticsRepository, val configRepository: ConfigRepository,
//                                               itemRepository: ItemRepository,
//                                               @StatisticsTimeRange private val timeRangeRepository: TimeRangeRepository
//) : ViewModel() {
//    val config = configRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Config()) // for config.guideline
//    val timeRange = timeRangeRepository.timeRangeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TimeRange.Default)
//
//    fun getStatDataListForMetricType(metricType: MetricType): StateFlow<List<StatData>>{
//        return statisticsRepository.getStatDataListForMetricType(metricType)
//            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
//    }
////    val statValueMap: Map<MetricDef, StateFlow<StatValue>> =
////        MetricDefRegistry.allDefs.associateWith { def ->
////            timeRangeRepository.timeRangeFlow.flatMapLatest { range ->
////                statisticsRepository.getStatValueFlow(def, range.days)
////            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatValue())
////        }
////
////    val dayPeriodStatMap: Map<MetricDef, StateFlow<Map<DayPeriod, StatValue>>> =
////        MetricDefRegistry.allDefs.associateWith { def ->
////            timeRangeRepository.timeRangeFlow.flatMapLatest { range ->
////                statisticsRepository.getDayPeriodStatValueFlow(def, range.days)
////            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
////        }
//
//    val firstDate: StateFlow<Instant> = itemRepository.getAllItemsFlow().map { it.firstOrNull()?.measuredAt ?: Instant.now() }
//        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Instant.now())
//
//    val meGapStatValue: StateFlow<StatValue> =
//        configRepository.dataFlow.flatMapLatest { config ->
//            //val bpUpperDef = MetricDefRegistry.getById("bp_upper") ?: return@flatMapLatest flowOf(StatValue())
//            getMeasuredValuesFlow(MetricType.BLOOD_PRESSURE.defs.first()).map { measuredValues ->  // TODO: first() check
//                measuredValues.toMeGapStatValue(config.dayPeriodConfig)
//            }
//        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatValue())
////    val meGapStatValue: StateFlow<StatValue> =
////        configRepository.dataFlow.flatMapLatest { config ->
////            val bpUpperDef = MetricDefRegistry.getById("bp_upper") ?: return@flatMapLatest flowOf(StatValue())
////            getMeasuredValuesFlow(bpUpperDef).map { measuredValues ->
////                measuredValues.toMeGapStatValue(config.dayPeriodConfig)
////        }
////    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatValue())
//
//    private fun getMeasuredValuesFlow(metricDef: MetricDef): Flow<List<MeasuredValue>> =
//        timeRangeRepository.timeRangeFlow.flatMapLatest { range ->
//            statisticsRepository.getMeasuredValuesFlow(metricDef, range.days)
//        }
//
//    fun setSelectedRange(range: TimeRange) {
//        viewModelScope.launch {
//            timeRangeRepository.setSelectedRange(range)
//        }
//    }
//}
//
