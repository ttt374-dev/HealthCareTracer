package com.github.ttt374.healthcaretracer.data.metric

import com.github.mikephil.charting.data.Entry
import com.github.ttt374.healthcaretracer.ui.analysis.toInstant
import java.time.Instant

data class ChartData(val metricType: MetricType, val chartSeriesList: List<ChartSeries> = emptyList())
data class ChartSeries(val metricDef: MetricDef, val actualEntries: List<Entry> = emptyList(), val targetEntries: List<Entry>? = null)

fun List<ChartSeries>.firstDate(): Instant? {
    return this.flatMap { it.actualEntries }.minByOrNull { it.x }?.x?.toLong()?.toInstant()
}
