package com.github.ttt374.healthcaretracer.data.metric


import java.time.ZoneId

//////////
data class StatValue(
    val avg: Double? = null,
    val max: Double? = null,
    val min: Double? = null,
    val count: Int = 0,
)

fun List<Double>.toStatValue() = StatValue(avg = averageOrNull(), max = maxOrNull(), min = minOrNull(), count = count())

fun List<Double>.averageOrNull(): Double? =
    this.takeIf { it.isNotEmpty() }?.average()

fun List<MeasuredValue>.toMeGapStatValue(dayPeriodCOnfig: DayPeriodConfig, zoneId: ZoneId = ZoneId.systemDefault()): StatValue {
    return groupBy { it.measuredAt.atZone(zoneId).toLocalDate() }
        .mapNotNull { (_, measuredValues) ->
            val periodMap = measuredValues.groupBy { it.measuredAt.toDayPeriod(dayPeriodCOnfig, zoneId) }
            periodMap[DayPeriod.Morning]?.map { it.value }?.averageOrNull()?.let { morningAvg ->
                periodMap[DayPeriod.Evening]?.map { it.value }?.averageOrNull()?.let { eveningAvg ->
                    morningAvg - eveningAvg
                }
            }
//            if (morningAvg != null && eveningAvg != null) morningAvg - eveningAvg else null
//            val morningAvg = periodMap[DayPeriod.Morning]?.map { it.value }?.averageOrNull()
//            val eveningAvg = periodMap[DayPeriod.Evening]?.map { it.value }?.averageOrNull()
//            if (morningAvg != null && eveningAvg != null) morningAvg - eveningAvg else null
//            measuredValues.filter { it.measuredAt.toDayPeriod(timeOfDayConfig, zoneId) == DayPeriod.Morning}.map { it.value }.averageOrNull()?.let { morningAvg ->
//                measuredValues.filter { it.measuredAt.toDayPeriod(timeOfDayConfig, zoneId) == DayPeriod.Evening}.map { it.value }.averageOrNull()?.let { eveningAvg ->
//                    morningAvg - eveningAvg
//                }
//            }
        }.toStatValue()
}

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

