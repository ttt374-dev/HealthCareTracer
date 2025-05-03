package com.github.ttt374.healthcaretracer.ui.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.repository.ChartRepository
import com.github.ttt374.healthcaretracer.data.repository.ConfigRepository
import com.github.ttt374.healthcaretracer.data.repository.PreferencesRepository
import com.github.ttt374.healthcaretracer.data.repository.StatisticsRepository
import com.github.ttt374.healthcaretracer.di.modules.DefaultMetricCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch



@HiltViewModel
class MetricViewModel @javax.inject.Inject constructor(private val preferencesRepository: PreferencesRepository) : ViewModel() {
    val selectedMetricType: StateFlow<MetricType> = preferencesRepository.dataFlow.map { it.selectedMetricType }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MetricType.Default)
    val displayMode: StateFlow<DisplayMode> = preferencesRepository.dataFlow.map { it.displayMode }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DisplayMode.Default)

    fun setMetricType(metricType: MetricType) {
        viewModelScope.launch {
            preferencesRepository.updateData { it.copy(selectedMetricType = metricType) }
        }
        //_selectedMetricType.value = metricType
    }
    fun setDisplayMode(displayMode: DisplayMode) {
        viewModelScope.launch {
            preferencesRepository.updateData { it.copy(displayMode = displayMode) }
        }
    }
}