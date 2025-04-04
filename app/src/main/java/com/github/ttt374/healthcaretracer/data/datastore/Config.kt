package com.github.ttt374.healthcaretracer.data.datastore

import com.github.ttt374.healthcaretracer.data.ConfigSerializer
import com.github.ttt374.healthcaretracer.data.LocalTimeRangeSerializer
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import kotlinx.serialization.Serializable
import java.time.LocalTime

@Serializable(with = ConfigSerializer::class)
data class Config (
    val bloodPressureGuideline: BloodPressureGuideline = BloodPressureGuideline.WHO,
    val morningRange: LocalTimeRange = LocalTimeRange(LocalTime.of(4, 0), LocalTime.of(11,59)),
    val eveningRange: LocalTimeRange = LocalTimeRange(LocalTime.of(7, 0), LocalTime.of(2, 59)),

    val targetBpUpper: Int = 120,
    val targetBpLower: Int = 80,
    val targetBodyWeight: Double = 60.0,
)

@Serializable(with = LocalTimeRangeSerializer::class)
data class LocalTimeRange(
    val start: LocalTime,
    val endInclusive: LocalTime
) {
    operator fun contains(time: LocalTime): Boolean {
        return if (start <= endInclusive) {
            // 普通の範囲
            time in start..endInclusive
        } else {
            // 0時をまたぐ場合（例: 22:00〜02:00）
            time >= start || time <= endInclusive
        }
    }
    override fun toString(): String = "$start..$endInclusive"
}
