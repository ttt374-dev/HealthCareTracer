package com.github.ttt374.healthcaretracer.data.metric

import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.bloodpressure.toBloodPressure
import com.github.ttt374.healthcaretracer.ui.analysis.StatValue

enum class StatType (val resId: Int){ Average(R.string.average ), Max(R.string.max), Min(R.string.min),
    Count(R.string.count);}
data class StatData<T> (val metricType: MetricType = MetricType.Default, val all: StatValue<T> = StatValue(), val byPeriod: Map<DayPeriod, StatValue<T>> = emptyMap())

fun List<MetricValue>.toStatValueFromMetric(): StatValue<MetricValue> {
    return when (this.firstOrNull()){
        is MetricValue.Double -> {
            val list = this.map { (it as MetricValue.Double).value }
            StatValue(list.averageOrNull()?.toMetricValue(), list.maxOrNull()?.toMetricValue(), list.minOrNull()?.toMetricValue(), list.count())
        }
        is MetricValue.Int -> {
            val list = this.map  { (it as MetricValue.Int).value }
            val listDouble = this.map { (it as MetricValue.Int).value.toDouble() }
            StatValue(listDouble.averageOrNull()?.toMetricValue(), list.maxOrNull()?.toMetricValue(), list.minOrNull()?.toMetricValue(), list.count())
        }
        is MetricValue.BloodPressure -> {
            val upperStatValue = this.map { (it as MetricValue.BloodPressure).value.upper.toDouble() }.toStatValue()
            val lowerStatValue = this.map { (it as MetricValue.BloodPressure).value.lower.toDouble() }.toStatValue()

            StatValue(
                avg = (upperStatValue.avg to lowerStatValue.avg).toBloodPressure()?.toMetricValue(),
                max = (upperStatValue.max to lowerStatValue.max).toBloodPressure()?.toMetricValue(),
                min = (upperStatValue.max to lowerStatValue.min).toBloodPressure()?.toMetricValue(),
                count = upperStatValue.count
            )
        }
        null -> StatValue()
    }
}

fun List<Double>.toStatValue() = StatValue(avg = averageOrNull(), max = maxOrNull(), min = minOrNull(), count = count())
fun List<Double>.averageOrNull(): Double? = this.takeIf { it.isNotEmpty() }?.average()
