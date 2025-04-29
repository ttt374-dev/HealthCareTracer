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
        //else -> StatValue()
    }
}

fun List<Double>.toStatValue() = StatValue(avg = averageOrNull(), max = maxOrNull(), min = minOrNull(), count = count())
fun List<Double>.averageOrNull(): Double? =
    this.takeIf { it.isNotEmpty() }?.average()


//fun MetricValue?.toAnnotatedString(format: String? = null, guideline: BloodPressureGuideline? = null, showUnit: Boolean = true): AnnotatedString {
//    return when (this){
//        is MetricNumber -> this.value.toAnnotatedString(format)
//        is MetricBloodPressure -> this.value.toAnnotatedString(guideline, showUnit)
//        else -> AnnotatedString("-")
//    }
//}
//fun List<MetricNumber>.toStateValue() = map { it.value }.toStatValue()
//fun List<MetricBloodPressure>.toStateValue() = StatValue() // TODO


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

