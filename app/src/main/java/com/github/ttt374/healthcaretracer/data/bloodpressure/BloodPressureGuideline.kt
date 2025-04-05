package com.github.ttt374.healthcaretracer.data.bloodpressure

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
data class BloodPressureCategory(
    val name: String,
    @Serializable(with = IntRangeSerializer::class)
    val upperRange: IntRange,
    @Serializable(with = IntRangeSerializer::class)
    val lowerRange: IntRange,
    @Serializable(with = ColorSerializer::class)
    val color: Color
)

/** ガイドラインごとの血圧カテゴリリスト */
@Serializable
sealed class BloodPressureGuideline(val name: String, // val categories: List<BloodPressureCategory>,
                                    val normal: BloodPressureCategory,
                                    val elevated: BloodPressureCategory,
                                    val htn1: BloodPressureCategory,
                                    val htn2: BloodPressureCategory,
                                    val htn3: BloodPressureCategory,
    ) {
    @Serializable
    @SerialName("JSH")
    data object JSH : BloodPressureGuideline("JSH",
        BloodPressureCategory("Normal", 0..119, 0..79, Color.Unspecified),
        BloodPressureCategory("Elevated", 120..139, 80..89, Color.Unspecified),
        BloodPressureCategory("HTN Stage 1", 140..159, 90..99, Color.Red),
        BloodPressureCategory("HTN Stage 2", 160..179, 100..109, Color.Red),
        BloodPressureCategory("HTN Crisis", 180..Int.MAX_VALUE, 110..Int.MAX_VALUE, Color.Red)
    )
    @Serializable
    @SerialName("WHO")
    data object WHO : BloodPressureGuideline("WHO",
        BloodPressureCategory("Normal", 0..129, 0..84, Color.Unspecified),
        BloodPressureCategory("Elevated", 130..139, 85..89, Color.Unspecified),
        BloodPressureCategory("HTN Stage 1", 140..159, 90..99, Color.Red),
        BloodPressureCategory("HTN Stage 2", 160..179, 100..109, Color.Red),
        BloodPressureCategory("HTN Crisis", 180..Int.MAX_VALUE, 110..Int.MAX_VALUE, Color.Red)
    )

    val categories: List<BloodPressureCategory> get() = listOf(normal, elevated, htn1, htn2, htn3)
    val invalidCategory = BloodPressureCategory("Invalid", 0..0, 0..0, Color.Black)

    fun getCategory(bpUpper: Int?, bpLower: Int?): BloodPressureCategory {
        return categories.reversed().firstOrNull { bpUpper in it.upperRange && bpLower in it.lowerRange } ?:
                categories.reversed().firstOrNull { bpUpper in it.upperRange || bpLower in it.lowerRange } ?: invalidCategory
    }
    fun getCategory(value: Int, isUpper: Boolean): BloodPressureCategory {
        return categories.firstOrNull { if (isUpper) value in it.upperRange else value in it.lowerRange } ?: invalidCategory
    }
    companion object {
        val Default = WHO
        val bloodPressureGuidelines = mapOf("WHO" to BloodPressureGuideline.WHO, "JST" to BloodPressureGuideline.JSH)
    }
//    companion object {
//        private val invalidCategory = BloodPressureCategory("Invalid", 0..0, 0..0, Color.Black)
//
//        fun getCategory(bpUpper: Int?, bpLower: Int?, guideline: BloodPressureGuideline = selectedGuideline): BloodPressureCategory {
//            return guideline.categories.firstOrNull { bpUpper in it.upperRange && bpLower in it.lowerRange } ?:
//                    guideline.categories.firstOrNull { bpUpper in it.upperRange || bpLower in it.lowerRange } ?: invalidCategory
//        }
//        fun getCategory(value: Int, isUpper: Boolean, guideline: BloodPressureGuideline = selectedGuideline): BloodPressureCategory {
//            return guideline.categories.firstOrNull { if (isUpper) value in it.upperRange else value in it.lowerRange } ?: invalidCategory
//        }
}


object IntRangeSerializer : KSerializer<IntRange> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("IntRange") {
        element<Int>("start")
        element<Int>("endInclusive")
    }

    override fun serialize(encoder: Encoder, value: IntRange) {
        val composite = encoder.beginStructure(descriptor)
        composite.encodeIntElement(descriptor, 0, value.first)
        composite.encodeIntElement(descriptor, 1, value.last)
        composite.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): IntRange {
        val dec = decoder.beginStructure(descriptor)
        var start = 0
        var endInclusive = 0

        loop@ while (true) {
            when (val index = dec.decodeElementIndex(descriptor)) {
                0 -> start = dec.decodeIntElement(descriptor, 0)
                1 -> endInclusive = dec.decodeIntElement(descriptor, 1)
                CompositeDecoder.DECODE_DONE -> break@loop
                else -> throw SerializationException("Unexpected index: $index")
            }
        }

        dec.endStructure(descriptor)
        return start..endInclusive
    }
}

object ColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Color) {
        // ARGB を hex 表現 "#AARRGGBB" で保存
        val argb = value.value.toULong().toString(16).padStart(8, '0')
        encoder.encodeString("#${argb.uppercase()}")
    }

    override fun deserialize(decoder: Decoder): Color {
        val hex = decoder.decodeString().removePrefix("#")
        val value = hex.toULong(16)
        return Color(value)
    }
}


/** 現在のガイドラインを選択（デフォルトは WHO） */
//var selectedGuideline: BloodPressureGuideline = BloodPressureGuideline.WHO


//
//sealed class BloodPressureCategory(
//    val name: String,
//    val sbpRange: IntRange,
//    val dbpRange: IntRange,
//    val color: Color,
//) {
//    data object Normal : BloodPressureCategory("Normal", 40..119, 40..79, Color.Unspecified)
//    data object Elevated : BloodPressureCategory("Elevated", 120..129, 60..79, Color.Unspecified)
//    data object HypertensionStage1 : BloodPressureCategory("HTN Stage 1", 130..139, 80..89, Color(0xFFF57C00)) // dark orange
//    data object HypertensionStage2 : BloodPressureCategory("HTN Stage 2", 140..179, 90..119, Color.Red)
//    data object HypertensiveCrisis : BloodPressureCategory("HTN Crisis",180..Int.MAX_VALUE,120..Int.MAX_VALUE,
//        Color.Magenta)
//
//    companion object {
//        private val categories = listOf(Invalid, Normal, Elevated, HypertensionStage1, HypertensionStage2, HypertensiveCrisis).reversed()
//
//        fun getCategory(bpUpper: Int?, bpLower: Int?): BloodPressureCategory {
//            return categories.firstOrNull { bpUpper in it.sbpRange && bpLower in it.dbpRange }
//                ?: categories.firstOrNull { bpUpper in it.sbpRange || bpLower in it.dbpRange }
//                ?: Normal
//        }
//
//        fun getCategory(value: Int, isSbp: Boolean): BloodPressureCategory {
//            return categories.firstOrNull { if (isSbp) value in it.sbpRange else value in it.dbpRange } ?: Normal
//        }
//    }
//}