package com.github.ttt374.healthcaretracer.shared

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

//enum class DayPeriod(val resId: Int, val takeStartValue: (TimeOfDayConfig) -> LocalTime) {
//    Morning(R.string.morning, { it.morning }),
//    Afternoon(R.string.afternoon, { it.afternoon} ),
//    Evening(R.string.evening, { it.evening });
//}

//@Serializable
//data class TimeOfDayConfig(
//    @Serializable(with= LocalTimeSerializer::class)
//    val morning: LocalTime = LocalTime.of(5, 0),
//    @Serializable(with= LocalTimeSerializer::class)
//    val afternoon: LocalTime = LocalTime.of(12, 0),
//    @Serializable(with= LocalTimeSerializer::class)
//    val evening: LocalTime = LocalTime.of(18, 0)
//){
//    operator fun get(period: DayPeriod): LocalTime = period.takeStartValue(this)
//}
@Serializable
data class TimeOfDayConfig(
    val timeMap: Map<DayPeriod, @Serializable(with = LocalTimeSerializer::class) LocalTime> =
        mapOf(
            DayPeriod.Morning to LocalTime.of(5, 0),
            DayPeriod.Afternoon to LocalTime.of(12, 0),
            DayPeriod.Evening to LocalTime.of(18, 0),
        )  // TODO: default
) {
//    init {  // TODO: initial check
//        val missing = DayPeriod.entries - timeMap.keys
//        require(missing.isEmpty()) {
//            "TimeOfDayConfig is missing values for: ${missing.joinToString()}"
//        }
//    }
    operator fun get(period: DayPeriod): LocalTime =
        timeMap[period] ?: error("Missing config for $period")

    fun update(period: DayPeriod, newTime: LocalTime): TimeOfDayConfig =
        copy(timeMap = timeMap + (period to newTime))
}


fun Instant.toDayPeriod(config: TimeOfDayConfig, zoneId: ZoneId = ZoneId.systemDefault()): DayPeriod {
    val time = this.atZone(zoneId).toLocalTime()
    return when {
        time >= config[DayPeriod.Morning] && time < config[DayPeriod.Afternoon] -> DayPeriod.Morning
        time >= config[DayPeriod.Afternoon] && time < config[DayPeriod.Evening] -> DayPeriod.Afternoon
        else -> DayPeriod.Evening
    }
}
