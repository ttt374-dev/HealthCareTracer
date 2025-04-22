package com.github.ttt374.healthcaretracer.shared

import com.github.ttt374.healthcaretracer.data.repository.LocalTimeSerializer
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId


enum class DayPeriod { Morning, Afternoon, Evening }

@Serializable
data class TimeOfDayConfig(
    @Serializable(with= LocalTimeSerializer::class)
    val morning: LocalTime = LocalTime.of(5, 0),
    @Serializable(with= LocalTimeSerializer::class)
    val afternoon: LocalTime = LocalTime.of(12, 0),
    @Serializable(with= LocalTimeSerializer::class)
    val evening: LocalTime = LocalTime.of(18, 0)
)
fun Instant.toDayPeriod(config: TimeOfDayConfig = TimeOfDayConfig(), zoneId: ZoneId = ZoneId.systemDefault()): DayPeriod {
    val time = this.atZone(zoneId).toLocalTime()
    return when {
        time >= config.morning && time < config.afternoon -> DayPeriod.Morning
        time >= config.afternoon && time < config.evening -> DayPeriod.Afternoon
        else -> DayPeriod.Evening
    }
}
