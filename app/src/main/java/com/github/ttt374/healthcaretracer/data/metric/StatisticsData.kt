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
            val upperStatValue = this.map { (it as MetricValue.BloodPressure).value.upper.toMetricValue() }.toStatValueFromMetric()
            val lowerStatValue = this.map { (it as MetricValue.BloodPressure).value.lower.toMetricValue() }.toStatValueFromMetric()


            StatValue(
                avg = ((upperStatValue.avg as? MetricValue.Int)?.value to (lowerStatValue.avg as? MetricValue.Int)?.value).toBloodPressure()?.toMetricValue(),
                max = ((upperStatValue.max as? MetricValue.Int)?.value to (lowerStatValue.max as? MetricValue.Int)?.value).toBloodPressure()?.toMetricValue(),
                min = ((upperStatValue.min as? MetricValue.Int)?.value to (lowerStatValue.min as? MetricValue.Int)?.value).toBloodPressure()?.toMetricValue(),
                count = upperStatValue.count
            )
        }
        null -> StatValue()
    }
}

//fun List<Double>.toStatValue() = StatValue(avg = averageOrNull(), max = maxOrNull(), min = minOrNull(), count = count())
//fun List<Double>.averageOrNull(): Double? = this.takeIf { it.isNotEmpty() }?.average()

fun Iterable<Double>.averageOrNull(): Double? = if (this.any()) this.average() else null
