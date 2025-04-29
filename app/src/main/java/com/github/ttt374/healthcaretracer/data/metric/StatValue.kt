package com.github.ttt374.healthcaretracer.data.metric

import com.github.ttt374.healthcaretracer.data.bloodpressure.toBloodPressure


//////////

data class StatValue<T>(
    val avg: T? = null,
    val max: T? = null,
    val min: T? = null,
    val count: Int = 0,
)
data class StatData<T> (val metricType: MetricType = MetricType.BLOOD_PRESSURE, val all: StatValue<T> = StatValue(), val byPeriod: Map<DayPeriod, StatValue<T>> = emptyMap())

fun List<Double>.toStatValue() = StatValue(avg = averageOrNull(), max = maxOrNull(), min = minOrNull(), count = count())
fun List<MetricValue>.toStatValueFromMetric(): StatValue<MetricValue> {
    return when (this.firstOrNull()){
        is MetricDouble -> {
            val list = this.map { (it as MetricDouble).value }
            StatValue(list.averageOrNull()?.toMetricValue(), list.maxOrNull()?.toMetricValue(), list.minOrNull()?.toMetricValue(), list.count())
        }
        is MetricBloodPressure -> {
            val upperStatValue = this.map { (it as MetricBloodPressure).value.upper.toDouble() }.toStatValue()
            val lowerStatValue = this.map { (it as MetricBloodPressure).value.lower.toDouble() }.toStatValue()

            StatValue(
                avg = (upperStatValue.avg to lowerStatValue.avg).toBloodPressure()?.toMetricValue(),
                max = (upperStatValue.max to lowerStatValue.max).toBloodPressure()?.toMetricValue(),
                min = (upperStatValue.max to lowerStatValue.min).toBloodPressure()?.toMetricValue()
            )
        }
        else -> StatValue()
    }
}
//fun MetricValue?.toAnnotatedString(format: String? = null, guideline: BloodPressureGuideline? = null, showUnit: Boolean = true): AnnotatedString {
//    return when (this){
//        is MetricNumber -> this.value.toAnnotatedString(format)
//        is MetricBloodPressure -> this.value.toAnnotatedString(guideline, showUnit)
//        else -> AnnotatedString("-")
//    }
//}
//fun List<MetricNumber>.toStateValue() = map { it.value }.toStatValue()
//fun List<MetricBloodPressure>.toStateValue() = StatValue() // TODO

fun List<Double>.averageOrNull(): Double? =
    this.takeIf { it.isNotEmpty() }?.average()

//fun <T> List<T>.averageOrNull(): Double? {
//    if (isEmpty()) return null
//
//    if (firstOrNull() !is Number) return null
//
//    var sum = 0.0
//    var count = 0
//
//    for (element in this) {
//        if (element is Number) {
//            sum += element.toDouble()
//            count++
//        }
//    }
//    return if (count > 0) sum / count else null
//}
//fun List<MeasuredValue>.toMeGapStatValue(dayPeriodConfig: DayPeriodConfig, zoneId: ZoneId = ZoneId.systemDefault()): StatValue {
//    return groupBy { it.measuredAt.atZone(zoneId).toLocalDate() }
//        .mapNotNull { (_, measuredValues) ->
//            val periodMap = measuredValues.groupBy { it.measuredAt.toDayPeriod(dayPeriodConfig, zoneId) }
//            periodMap[DayPeriod.Morning]?.map { it.value }?.averageOrNull()?.let { morningAvg ->
//                periodMap[DayPeriod.Evening]?.map { it.value }?.averageOrNull()?.let { eveningAvg ->
//                    morningAvg - eveningAvg
//                }
//            }
//        }.toStatValue()
//}

//fun List<Item>.calculateMeGap(timeOfDayConfig: TimeOfDayConfig, zoneId: ZoneId) =
//    filterDayPeriod(DayPeriod.Morning, timeOfDayConfig, zoneId).bpUpperAverageOrNull()?.let { morning ->
//        filterDayPeriod(DayPeriod.Evening, timeOfDayConfig, zoneId).bpUpperAverageOrNull()?.let { evening ->
//            morning - evening
//        }
//    }
//fun List<Item>.filterDayPeriod(dayPeriod: DayPeriod, timeOfDayConfig: TimeOfDayConfig, zoneId: ZoneId) =
//    filter { it.measuredAt.toDayPeriod(timeOfDayConfig, zoneId) == dayPeriod}
//
//fun List<Item>.bpUpperAverageOrNull() =
//    mapNotNull { it.vitals.bp?.upper?.toDouble() }.averageOrNull()

