package com.github.ttt374.healthcaretracer.data.datastore

import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import com.github.ttt374.healthcaretracer.ui.common.TimeOfDayConfig
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Serializable  // (with = ConfigSerializer::class)
data class Config (
    val bloodPressureGuideline: BloodPressureGuideline = BloodPressureGuideline.Default,
    //val morningRange: LocalTimeRange = LocalTimeRange(LocalTime.of(4, 0), LocalTime.of(11,59)),
    //val eveningRange: LocalTimeRange = LocalTimeRange(LocalTime.of(17, 0), LocalTime.of(2, 59)),
    val timeOfDayConfig: TimeOfDayConfig = TimeOfDayConfig(),

    val targetBpUpper: Int = 120,
    val targetBpLower: Int = 80,
    val targetBodyWeight: Double = 60.0,

    //val localeTag: String = "en_US"
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
    fun toClosedRange(): ClosedRange<LocalTime> = object : ClosedRange<LocalTime> {
        override val start = this@LocalTimeRange.start
        override val endInclusive = this@LocalTimeRange.endInclusive
    }
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
