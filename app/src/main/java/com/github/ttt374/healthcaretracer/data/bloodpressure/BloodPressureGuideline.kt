package com.github.ttt374.healthcaretracer.data.bloodpressure

import androidx.compose.ui.graphics.Color
import com.github.ttt374.healthcaretracer.R
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

sealed class BloodPressureCategory(val name: String, val nameLabel: Int,
    val upperRange: IntRange, val lowerRange: IntRange, val color: Color){
    data class Normal(val bpUpperRange: IntRange, val bpLowerRange: IntRange) :
            BloodPressureCategory("Normal", R.string.bpcategory__normal, bpUpperRange, bpLowerRange, Color.Unspecified)
    data class Elevated(val bpUpperRange: IntRange, val bpLowerRange: IntRange) :
            BloodPressureCategory("Elevated", R.string.bpcategory__elevated, bpUpperRange, bpLowerRange, Color.Unspecified)
    data class Htn1(val bpUpperRange: IntRange, val bpLowerRange: IntRange) :
            BloodPressureCategory("Htn1", R.string.bpcategory__htn1, bpUpperRange, bpLowerRange, Color(0xFFFFA200))
    data class Htn2(val bpUpperRange: IntRange, val bpLowerRange: IntRange) :
            BloodPressureCategory("Htn2", R.string.bpcategory__htn2, bpUpperRange, bpLowerRange, Color(0xFFFFA500))
    data class Htn3(val bpUpperRange: IntRange, val bpLowerRange: IntRange) :
            BloodPressureCategory("Htn3", R.string.bpcategory__htn3, bpUpperRange, bpLowerRange, Color.Red)
    data object Invalid : BloodPressureCategory("Normal", R.string.bpcategory__invalid, 0..0, 0..0, Color.Unspecified)
}

/** ガイドラインごとの血圧カテゴリリスト */
@Serializable(with = BloodPressureGuidelineSerializer::class)
sealed class BloodPressureGuideline(val name: String, // val categories: List<BloodPressureCategory>,
                                    val normal: BloodPressureCategory.Normal,
                                    val elevated: BloodPressureCategory.Elevated,
                                    val htn1: BloodPressureCategory.Htn1,
                                    val htn2: BloodPressureCategory.Htn2,
                                    val htn3: BloodPressureCategory.Htn3,
    ) {
    data object AHAACC : BloodPressureGuideline("AHA/ACC",
        BloodPressureCategory.Normal( 0..119, 0..79),
        BloodPressureCategory.Elevated(120..129, 0..79),
        BloodPressureCategory.Htn1(130..139, 80..89),
        BloodPressureCategory.Htn2(140..179, 90..119),
        BloodPressureCategory.Htn3( 180..Int.MAX_VALUE, 120..Int.MAX_VALUE)
    )
    data object JSH2019 : BloodPressureGuideline("JSH2019",
        BloodPressureCategory.Normal( 0..119, 0..79),
        BloodPressureCategory.Elevated( 120..139, 80..89),
        BloodPressureCategory.Htn1( 140..159, 90..99),
        BloodPressureCategory.Htn2( 160..179, 100..109),
        BloodPressureCategory.Htn3( 180..Int.MAX_VALUE, 110..Int.MAX_VALUE)
    )
    data object ESCESH : BloodPressureGuideline("ESC/ESH",
        BloodPressureCategory.Normal(0..129, 0..84),
        BloodPressureCategory.Elevated( 130..139, 85..89),
        BloodPressureCategory.Htn1( 140..159, 90..99),
        BloodPressureCategory.Htn2( 160..179, 100..109),
        BloodPressureCategory.Htn3( 180..Int.MAX_VALUE, 110..Int.MAX_VALUE)
    )

    val categories: List<BloodPressureCategory> get() = listOf(normal, elevated, htn1, htn2, htn3)

    //fun getCategory(bpUpper: Int?, bpLower: Int?): BloodPressureCategory {
    fun getCategory(bp: BloodPressure?): BloodPressureCategory {
        return categories.reversed().firstOrNull { bp?.upper in it.upperRange && bp?.lower in it.lowerRange } ?:
                categories.reversed().firstOrNull { bp?.upper in it.upperRange || bp?.lower in it.lowerRange } ?: BloodPressureCategory.Invalid
    }
    fun getCategoryUpper(value: Int): BloodPressureCategory {
        return categories.firstOrNull { value in it.upperRange } ?: BloodPressureCategory.Invalid
    }
    fun getCategoryLower(value: Int): BloodPressureCategory {
        return categories.firstOrNull { value in it.lowerRange } ?: BloodPressureCategory.Invalid
    }
//    fun getCategory(value: Int, isUpper: Boolean): BloodPressureCategory {
//        return categories.firstOrNull { if (isUpper) value in it.upperRange else value in it.lowerRange } ?: BloodPressureCategory.Invalid
//    }
    companion object {
        val Default = AHAACC
        val entries = listOf(AHAACC, JSH2019, ESCESH)
        //val bloodPressureGuidelines = entries

    }
}

object BloodPressureGuidelineSerializer : KSerializer<BloodPressureGuideline> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BloodPressureGuideline", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: BloodPressureGuideline) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): BloodPressureGuideline {
        val name = decoder.decodeString()
        return BloodPressureGuideline.entries.find { it.name == name }
            ?: throw SerializationException("Unknown guideline name: $name")
    }
}

