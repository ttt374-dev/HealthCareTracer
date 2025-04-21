package com.github.ttt374.healthcaretracer.ui.chart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.repository.ChartRepository
import com.github.ttt374.healthcaretracer.data.repository.ConfigRepository
import com.github.ttt374.healthcaretracer.data.repository.toVitals
import com.github.ttt374.healthcaretracer.di.modules.ChartTimeRange
import com.github.ttt374.healthcaretracer.di.modules.StatisticsTimeRange
import com.github.ttt374.healthcaretracer.shared.TimeRange
import com.github.ttt374.healthcaretracer.shared.TimeRangeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ChartViewModel @Inject constructor(private val chartRepository: ChartRepository,
                                         private val configRepository: ConfigRepository,
                                         @ChartTimeRange private val timeRangeManager: TimeRangeManager) : ViewModel() {
    val timeRange = timeRangeManager.timeRange
    private val _selectedChartType: MutableStateFlow<ChartType> = MutableStateFlow(ChartType.Default)
    val selectedChartType: StateFlow<ChartType> = _selectedChartType.asStateFlow()

    private val targetValuesFlow = configRepository.dataFlow.map {
        it.toVitals()
    }
//    fun onChartTypeSelected(type: ChartType) {
//        _selectedChartType.value = type
//    }

    val chartData = combine(selectedChartType, timeRange, targetValuesFlow){ type, range, targetValues ->
        Triple(type, range, targetValues)}.flatMapLatest {(type, range, targetValues) ->
        chartRepository.getChartDataFlow(type, range, targetValues)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChartData())
        //.flatMapLatest { type, range -> chartRepository.getChartDataFlow(type, range) }


    fun onPageChanged(index: Int) {
        _selectedChartType.value = ChartType.entries[index]
    }

    fun setSelectedRange(range: TimeRange) {
        viewModelScope.launch {
            timeRangeManager.setSelectedRange(range)
        }
    }

}
