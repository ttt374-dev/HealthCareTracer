package com.github.ttt374.healthcaretracer.data.bloodpressure

import androidx.compose.ui.graphics.Color
import com.github.ttt374.healthcaretracer.R
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*



@Serializable
sealed class BloodPressureCategory(
    val name: String,
    val nameLabel: Int,
    @Serializable(with = IntRangeSerializer::class)
    val upperRange: IntRange,
    @Serializable(with = IntRangeSerializer::class)
    val lowerRange: IntRange,
    @Serializable(with = ColorSerializer::class)
    val color: Color
){
    @Serializable
    data class Normal(
        @Serializable(with = IntRangeSerializer::class) val bpUpperRange: IntRange,
        @Serializable(with = IntRangeSerializer::class) val bpLowerRange: IntRange) :
            BloodPressureCategory("Normal", R.string.bpcategory__normal, bpUpperRange, bpLowerRange, Color.Unspecified)
    @Serializable
    data class Elevated(
        @Serializable(with = IntRangeSerializer::class) val bpUpperRange: IntRange,
        @Serializable(with = IntRangeSerializer::class) val bpLowerRange: IntRange) :
            BloodPressureCategory("Elevated", R.string.bpcategory__elevated, bpUpperRange, bpLowerRange, Color.Unspecified)
    @Serializable
    data class Htn1(
        @Serializable(with = IntRangeSerializer::class) val bpUpperRange: IntRange,
        @Serializable(with = IntRangeSerializer::class) val bpLowerRange: IntRange) :
            BloodPressureCategory("Htn1", R.string.bpcategory__htn1, bpUpperRange, bpLowerRange, Color(0xFFFFA200))
    @Serializable
    data class Htn2(
        @Serializable(with = IntRangeSerializer::class) val bpUpperRange: IntRange,
        @Serializable(with = IntRangeSerializer::class) val bpLowerRange: IntRange) :
            BloodPressureCategory("Htn2", R.string.bpcategory__htn2, bpUpperRange, bpLowerRange, Color(0xFFFFA500))
    @Serializable
    data class Htn3(
        @Serializable(with = IntRangeSerializer::class) val bpUpperRange: IntRange,
        @Serializable(with = IntRangeSerializer::class) val bpLowerRange: IntRange) :
            BloodPressureCategory("Htn3", R.string.bpcategory__htn3, bpUpperRange, bpLowerRange, Color.Red)
    @Serializable
    data object Invalid : BloodPressureCategory("Normal", R.string.bpcategory__invalid, 0..0, 0..0, Color.Unspecified)
}

/** ガイドラインごとの血圧カテゴリリスト */
@Serializable
sealed class BloodPressureGuideline(val name: String, // val categories: List<BloodPressureCategory>,
                                    val normal: BloodPressureCategory.Normal,
                                    val elevated: BloodPressureCategory.Elevated,
                                    val htn1: BloodPressureCategory.Htn1,
                                    val htn2: BloodPressureCategory.Htn2,
                                    val htn3: BloodPressureCategory.Htn3,
    ) {
    @Serializable
    @SerialName("AHA/ACC")
    data object AHA_ACC : BloodPressureGuideline("AHA/ACC",
        BloodPressureCategory.Normal( 0..119, 0..79),
        BloodPressureCategory.Elevated(120..129, 0..79),
        BloodPressureCategory.Htn1(130..139, 80..89),
        BloodPressureCategory.Htn2(140..179, 90..119),
        BloodPressureCategory.Htn3( 180..Int.MAX_VALUE, 120..Int.MAX_VALUE)
    )
    @Serializable
    @SerialName("JSH2019")
    data object JSH2019 : BloodPressureGuideline("JSH2019",
        BloodPressureCategory.Normal( 0..119, 0..79),
        BloodPressureCategory.Elevated( 120..139, 80..89),
        BloodPressureCategory.Htn1( 140..159, 90..99),
        BloodPressureCategory.Htn2( 160..179, 100..109),
        BloodPressureCategory.Htn3( 180..Int.MAX_VALUE, 110..Int.MAX_VALUE)
    )
    @Serializable
    @SerialName("ESC/ESH")
    data object ESC_ESH : BloodPressureGuideline("ESC/ESH",
        BloodPressureCategory.Normal(0..129, 0..84),
        BloodPressureCategory.Elevated( 130..139, 85..89),
        BloodPressureCategory.Htn1( 140..159, 90..99),
        BloodPressureCategory.Htn2( 160..179, 100..109),
        BloodPressureCategory.Htn3( 180..Int.MAX_VALUE, 110..Int.MAX_VALUE)
    )

    val categories: List<BloodPressureCategory> get() = listOf(normal, elevated, htn1, htn2, htn3)
    //val invalidCategory = BloodPressureCategory("Invalid", 0..0, 0..0, Color.Black)
    private val invalidCategory = BloodPressureCategory.Invalid

    fun getCategory(bpUpper: Int?, bpLower: Int?): BloodPressureCategory {
        return categories.reversed().firstOrNull { bpUpper in it.upperRange && bpLower in it.lowerRange } ?:
                categories.reversed().firstOrNull { bpUpper in it.upperRange || bpLower in it.lowerRange } ?: invalidCategory
    }
    fun getCategory(value: Int, isUpper: Boolean): BloodPressureCategory {
        return categories.firstOrNull { if (isUpper) value in it.upperRange else value in it.lowerRange } ?: invalidCategory
    }
    companion object {
        val Default = AHA_ACC
        val entries = listOf(AHA_ACC, JSH2019, ESC_ESH)
        val bloodPressureGuidelines = entries

//        val bloodPressureGuidelines = mapOf(
//            "AHA/ACC" to BloodPressureGuideline.AHA_ACC,
//
//            "JST" to BloodPressureGuideline.JSH)
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
        val argb = value.value.toString(16).padStart(8, '0')
        encoder.encodeString("#${argb.uppercase()}")
    }

    override fun deserialize(decoder: Decoder): Color {
        val hex = decoder.decodeString().removePrefix("#")
        val value = hex.toULong(16)
        return Color(value)
    }
}


