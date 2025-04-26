package com.github.ttt374.healthcaretracer.data.repository

import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.item.Vitals
import com.github.ttt374.healthcaretracer.data.metric.MeasuredValue
import com.github.ttt374.healthcaretracer.data.metric.MetricDef
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MetricRepository @Inject constructor(private val itemRepository: ItemRepository) {
    fun getMeasuredValuesFlow(metric: MetricDef, days: Long? = null): Flow<List<MeasuredValue>> {
        return itemRepository.getRecentItemsFlow(days)
            .map { items ->
                items.toMeasuredValue(metric.selector)
//                items.mapNotNull { item ->
//                    metric.selector(item.vitals)?.let { value -> MeasuredValue(item.measuredAt, value) }
//                }
            }
    }
//    fun getMetricsByCategory(category: MetricCategory): List<MetricDef> =
//        MetricDefRegistry.defs.filter { it.category == category }
}
fun List<Item>.toMeasuredValue(selector: (Vitals) -> Double?) = this.mapNotNull { item ->
    selector(item.vitals)?.let { MeasuredValue(item.measuredAt, it) }
}