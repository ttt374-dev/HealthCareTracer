package com.github.ttt374.healthcaretracer.data.datastore

import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = ConfigSerializer::class)
data class Config (
    val bloodPressureGuideline: BloodPressureGuideline = BloodPressureGuideline.WHO,
//    val morningRange: LocalTimeRange = LocalTimeRange(LocalTime.of(4, 0), LocalTime.of(11,59)),
//    val eveningRange: LocalTimeRange = LocalTimeRange(LocalTime.of(7, 0), LocalTime.of(2, 59)),

    val targetBpUpper: Int = 120,
    val targetBpLower: Int = 80,
//    val targetBodyWeight: Double = 60.0,
)

object ConfigSerializer : KSerializer<Config> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Config") {
        element<Int>("targetBpUpper")
        element<Int>("targetBpLower")
    }

    override fun serialize(encoder: Encoder, value: Config) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeIntElement(descriptor, 0, value.targetBpUpper)
        composite.encodeIntElement(descriptor, 1, value.targetBpLower)
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): Config {
        val dec = decoder.beginStructure(descriptor)
        var upper = 120
        var lower = 80

        loop@ while (true) {
            when (val index = dec.decodeElementIndex(descriptor)) {
                0 -> upper = dec.decodeIntElement(descriptor, 0)
                1 -> lower = dec.decodeIntElement(descriptor, 1)
                CompositeDecoder.DECODE_DONE -> break@loop
                else -> throw SerializationException("Unexpected index: $index")
            }
        }
        dec.endStructure(descriptor)
        return Config(targetBpUpper = upper, targetBpLower = lower)
    }
}