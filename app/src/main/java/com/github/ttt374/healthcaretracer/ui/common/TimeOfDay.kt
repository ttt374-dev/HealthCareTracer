package com.github.ttt374.healthcaretracer.ui.common

import com.github.ttt374.healthcaretracer.data.repository.LocalTimeSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId


@Serializable
sealed class TimeOfDay {
    object Morning : TimeOfDay()
    object Afternoon : TimeOfDay()
    object Evening : TimeOfDay()

    companion object {
        fun from(localTime: LocalTime, config: TimeOfDayConfig = TimeOfDayConfig()): TimeOfDay {
            return when {
                localTime >= config.morning && localTime < config.afternoon -> Morning
                localTime >= config.afternoon && localTime < config.evening -> Afternoon
                else -> Evening
            }
        }
    }
}

@Serializable
data class TimeOfDayConfig(
    @Serializable(with= LocalTimeSerializer::class)
    val morning: LocalTime = LocalTime.of(5, 0),
    @Serializable(with= LocalTimeSerializer::class)
    val afternoon: LocalTime = LocalTime.of(12, 0),
    @Serializable(with= LocalTimeSerializer::class)
    val evening: LocalTime = LocalTime.of(18, 0)
)

fun Instant.toTimeOfDay(
    zoneId: ZoneId = ZoneId.systemDefault(),
    config: TimeOfDayConfig = TimeOfDayConfig()
): TimeOfDay {
    val localTime = this.atZone(zoneId).toLocalTime()
    return TimeOfDay.from(localTime, config)
}
