package com.github.ttt374.healthcaretracer.data.repository

import com.github.ttt374.healthcaretracer.data.item.MeasuredValue
import com.github.ttt374.healthcaretracer.data.item.MetricCategory
import com.github.ttt374.healthcaretracer.data.item.MetricDef
import com.github.ttt374.healthcaretracer.data.item.MetricDefRegistry
import com.github.ttt374.healthcaretracer.shared.TimeRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MetricRepository @Inject constructor(
    private val itemRepository: ItemRepository
) {
    fun getMetricFlow(metric: MetricDef, days: Long? = null): Flow<List<MeasuredValue>> {
        return itemRepository.getRecentItemsFlow(days)
            .map { items ->
                items.mapNotNull { item ->
                    metric.selector(item.vitals)?.let { value -> MeasuredValue(item.measuredAt, value) }
                }
            }
    }
//    fun getMetricsByCategory(category: MetricCategory): List<MetricDef> =
//        MetricDefRegistry.defs.filter { it.category == category }
}
