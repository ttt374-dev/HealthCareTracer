package com.github.ttt374.healthcaretracer.data

import androidx.compose.ui.graphics.Color
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import com.github.ttt374.healthcaretracer.data.datastore.LocalTimeRange
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import java.time.LocalTime
import java.time.format.DateTimeFormatter


object ColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Color", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Color) {
        encoder.encodeInt(value.value.toInt()) // RGBA Int に変換して保存
    }

    override fun deserialize(decoder: Decoder): Color {
        val intValue = decoder.decodeInt()
        return Color(intValue)
    }
}

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
///////////////

object LocalTimeRangeSerializer : KSerializer<LocalTimeRange> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("LocalTimeRange") {
        element<String>("start")
        element<String>("endInclusive")
    }

    override fun serialize(encoder: Encoder, value: LocalTimeRange) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeStringElement(descriptor, 0, value.start.toString()) // ISO 8601 string
        composite.encodeStringElement(descriptor, 1, value.endInclusive.toString())
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): LocalTimeRange {
        val dec = decoder.beginStructure(descriptor)
        var start: LocalTime? = null
        var endInclusive: LocalTime? = null

        loop@ while (true) {
            when (val index = dec.decodeElementIndex(descriptor)) {
                0 -> start = LocalTime.parse(dec.decodeStringElement(descriptor, 0))
                1 -> endInclusive = LocalTime.parse(dec.decodeStringElement(descriptor, 1))
                CompositeDecoder.DECODE_DONE -> break@loop
                else -> error("Unexpected index: $index")
            }
        }
        dec.endStructure(descriptor)

        return LocalTimeRange(
            start = start ?: error("Missing start time"),
            endInclusive = endInclusive ?: error("Missing endInclusive time")
        )
    }
}


object BloodPressureGuidelineSerializer : KSerializer<BloodPressureGuideline> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("BloodPressureGuideline", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BloodPressureGuideline) {
        // シリアライズ時にnameを保存
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): BloodPressureGuideline {
        // デコード時にnameを取得し、それを元に対応するBloodPressureGuidelineを返す
        val name = decoder.decodeString()
        return BloodPressureGuideline.bloodPressureGuidelines[name]
            ?: throw IllegalArgumentException("Unknown BloodPressureGuideline name: $name")
    }
}