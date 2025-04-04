package com.github.ttt374.healthcaretracer.data.bloodpressure

import androidx.compose.ui.graphics.Color
import com.github.ttt374.healthcaretracer.data.BloodPressureGuidelineSerializer
import com.github.ttt374.healthcaretracer.data.LocalTimeRangeSerializer
import kotlinx.serialization.Serializable

data class BloodPressureCategory(
    val name: String,
    val upperRange: IntRange,
    val lowerRange: IntRange,
    val color: Color
)

/** ガイドラインごとの血圧カテゴリリスト */
@Serializable(with = BloodPressureGuidelineSerializer::class)
sealed class BloodPressureGuideline(val name: String, // val categories: List<BloodPressureCategory>,
                                    val normal: BloodPressureCategory,
                                    val elevated: BloodPressureCategory,
                                    val htn1: BloodPressureCategory,
                                    val htn2: BloodPressureCategory,
                                    val htn3: BloodPressureCategory,
    ) {
    data object JSH : BloodPressureGuideline("JSH",
        BloodPressureCategory("Normal", 0..119, 0..79, Color.Unspecified),
        BloodPressureCategory("Elevated", 120..139, 80..89, Color.Unspecified),
        BloodPressureCategory("HTN Stage 1", 140..159, 90..99, Color.Red),
        BloodPressureCategory("HTN Stage 2", 160..179, 100..109, Color.Red),
        BloodPressureCategory("HTN Crisis", 180..Int.MAX_VALUE, 110..Int.MAX_VALUE, Color.Red)
    )
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
/** 現在のガイドラインを選択（デフォルトは AHA） */
var selectedGuideline: BloodPressureGuideline = BloodPressureGuideline.WHO


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