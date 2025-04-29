package com.github.ttt374.healthcaretracer.data.metric

import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.repository.LocalTimeSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

enum class DayPeriod(val resId: Int) {
    Morning(R.string.morning),
    Afternoon(R.string.afternoon),
    Evening(R.string.evening);
}

@Serializable
data class DayPeriodConfig(
    val timeMap: Map<DayPeriod, @Serializable(with = LocalTimeSerializer::class) LocalTime> =
        mapOf(
            DayPeriod.Morning to LocalTime.of(5, 0),
            DayPeriod.Afternoon to LocalTime.of(12, 0),
            DayPeriod.Evening to LocalTime.of(18, 0),
        )
) {
    init {  // TODO: initial check
        val missing = DayPeriod.entries - timeMap.keys
        require(missing.isEmpty()) {
            "TimeOfDayConfig is missing values for: ${missing.joinToString()}"
        }
    }
    operator fun get(period: DayPeriod): LocalTime =
        timeMap[period] ?: error("Missing config for $period")

    fun update(period: DayPeriod, newTime: LocalTime): DayPeriodConfig =
        copy(timeMap = timeMap + (period to newTime))

//    companion object {
//        fun from(map: Map<DayPeriod, LocalTime>): DayPeriodConfig {
//            require(map.size == DayPeriod.entries.size && DayPeriod.entries.all { it in map }) {
//                "TimeOfDayConfig must contain values for all DayPeriods."
//            }
//            return DayPeriodConfig(map)
//        }
//    }
}

fun Instant.toDayPeriod(dayPeriodConfig: DayPeriodConfig, zoneId: ZoneId = ZoneId.systemDefault()): DayPeriod {
    val time = this.atZone(zoneId).toLocalTime()
    return when {
        time >= dayPeriodConfig[DayPeriod.Morning] && time < dayPeriodConfig[DayPeriod.Afternoon] -> DayPeriod.Morning
        time >= dayPeriodConfig[DayPeriod.Afternoon] && time < dayPeriodConfig[DayPeriod.Evening] -> DayPeriod.Afternoon
        else -> DayPeriod.Evening
    }
}
