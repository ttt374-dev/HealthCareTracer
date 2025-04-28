package com.github.ttt374.healthcaretracer.data.metric


//////////
data class StatValue(
    val avg: Double? = null,
    val max: Double? = null,
    val min: Double? = null,
    val count: Int = 0,
)
data class StatData (val metricDef: MetricDef, val all: StatValue = StatValue(), val byPeriod: Map<DayPeriod, StatValue> = emptyMap())

fun List<Double>.toStatValue() = StatValue(avg = averageOrNull(), max = maxOrNull(), min = minOrNull(), count = count())

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

