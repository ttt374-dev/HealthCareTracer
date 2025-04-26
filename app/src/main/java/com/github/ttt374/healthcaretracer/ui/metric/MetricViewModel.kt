package com.github.ttt374.healthcaretracer.ui.metric

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.metric.MeasuredValue
import com.github.ttt374.healthcaretracer.data.metric.MetricDef
import com.github.ttt374.healthcaretracer.data.metric.MetricDefRegistry
import com.github.ttt374.healthcaretracer.data.repository.MetricRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MetricViewModel  @Inject constructor(private val metricRepository: MetricRepository): ViewModel() {

    val pulseDef = MetricDefRegistry.getById("pulse")!!
    val pulse = metricRepository.getMetricFlow(pulseDef, null).stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val metrics: Map<MetricDef, StateFlow<List<MeasuredValue>>> = MetricDefRegistry.defs.associateWith { def ->
        metricRepository.getMetricFlow(def, null).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList() )
    }
}