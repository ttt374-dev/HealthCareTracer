package com.github.ttt374.healthcaretracer.data.datastore

import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import kotlinx.serialization.Serializable
import java.time.LocalTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import java.time.format.DateTimeFormatter

@Serializable  // (with = ConfigSerializer::class)
data class Config (
    val bloodPressureGuideline: BloodPressureGuideline = BloodPressureGuideline.WHO,
    val morningRange: LocalTimeRange = LocalTimeRange(LocalTime.of(4, 0), LocalTime.of(11,59)),
    val eveningRange: LocalTimeRange = LocalTimeRange(LocalTime.of(17, 0), LocalTime.of(2, 59)),

    val targetBpUpper: Int = 120,
    val targetBpLower: Int = 80,
    val targetBodyWeight: Double = 60.0,
)

@Serializable
data class LocalTimeRange(
    @Serializable(with=LocalTimeSerializer::class)
    val start: LocalTime = LocalTime.of(0, 0),
    @Serializable(with=LocalTimeSerializer::class)
    val endInclusive: LocalTime = LocalTime.of(23, 59)
) {
    operator fun contains(time: LocalTime): Boolean {
        return if (start <= endInclusive) {
            time in start..endInclusive  // 普通の範囲
        } else {
            time >= start || time <= endInclusive  // 0時をまたぐ場合（例: 22:00〜02:00）
        }
    }
    override fun toString(): String = "$start..$endInclusive"
}

@Serializable
object LocalTimeSerializer : KSerializer<LocalTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_TIME // 例: "08:30:00"

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalTime) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalTime {
        return LocalTime.parse(decoder.decodeString(), formatter)
    }
}
