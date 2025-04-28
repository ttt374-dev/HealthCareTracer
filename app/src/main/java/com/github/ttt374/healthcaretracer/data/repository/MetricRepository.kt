package com.github.ttt374.healthcaretracer.data.repository

import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.item.Vitals
import com.github.ttt374.healthcaretracer.data.metric.MeasuredValue
import com.github.ttt374.healthcaretracer.data.metric.MetricDef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
//
//class MetricRepository @Inject constructor(private val itemRepository: ItemRepository) {
//    fun getMeasuredValuesFlow(metric: MetricDef, days: Long? = null) = itemRepository.getMeasuredValuesFlow(metric, days)
//}
