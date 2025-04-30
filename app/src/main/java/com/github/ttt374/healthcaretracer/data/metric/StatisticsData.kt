package com.github.ttt374.healthcaretracer.data.metric

import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.bloodpressure.toBloodPressure

data class StatValue<T>(
    val avg: T? = null,
    val max: T? = null,
    val min: T? = null,
    val count: Int = 0,
)
fun StatValue<MetricValue>.get(type: StatType): MetricValue? {
    return when (type) {
        StatType.Average -> avg
        StatType.Max -> max
        StatType.Min -> min
        StatType.Count -> count.toMetricValue()
    }
}


enum class StatType (val resId: Int){ Average(R.string.average ), Max(R.string.max), Min(R.string.min),
    Count(R.string.count);}
data class StatData<T> (val metricType: MetricType = MetricType.Default, val all: StatValue<T> = StatValue(), val byPeriod: Map<DayPeriod, StatValue<T>> = emptyMap())

fun List<MetricValue>.toStatValue(): StatValue<MetricValue> {
    return when (this.firstOrNull()){
        is MetricValue.Double -> this.doubleToStatValue()
        is MetricValue.Int -> {
            val list = this.map  { (it as MetricValue.Int).value }
            val listDouble = this.map { (it as MetricValue.Int).value.toDouble() }
            StatValue(listDouble.averageOrNull()?.toMetricValue(), list.maxOrNull()?.toMetricValue(), list.minOrNull()?.toMetricValue(), list.count())
        }
        is MetricValue.BloodPressure -> this.bloodPressureToStatValue()
        null -> StatValue()
    }
}
fun List<MetricValue>.doubleToStatValue(): StatValue<MetricValue>{
    val list = this.map  { (it as MetricValue.Double).value }
    return StatValue(list.averageOrNull()?.toMetricValue(), list.maxOrNull()?.toMetricValue(), list.minOrNull()?.toMetricValue(), list.count())
}
fun List<MetricValue>.bloodPressureToStatValue(): StatValue<MetricValue>{
    val upperStatValue = this.mapNotNull { it.toBpValue()?.upper?.toDouble()?.toMetricValue() }.doubleToStatValue()
    val lowerStatValue = this.mapNotNull { it.toBpValue()?.lower?.toDouble()?.toMetricValue() }.doubleToStatValue()

    return StatValue(
        avg = (upperStatValue.avg to lowerStatValue.avg).toBloodPressure()?.toMetricValue(),
        max = (upperStatValue.max to lowerStatValue.max).toBloodPressure()?.toMetricValue(),
        min = (upperStatValue.min to lowerStatValue.min).toBloodPressure()?.toMetricValue(),
        count = upperStatValue.count
    )
}
fun MetricValue.toBpValue() =
    (this as? MetricValue.BloodPressure)?.value
fun Pair<MetricValue?, MetricValue?>.toBloodPressure() =
    (first?.asDouble() to second?.asDouble()).toBloodPressure()
fun MetricValue.asDouble(): Double? =
    (this as? MetricValue.Double)?.value

//fun List<Double>.toStatValue() = StatValue(avg = averageOrNull(), max = maxOrNull(), min = minOrNull(), count = count())
//fun List<Double>.averageOrNull(): Double? = this.takeIf { it.isNotEmpty() }?.average()

fun Iterable<Double>.averageOrNull(): Double? = if (this.any()) this.average() else null
