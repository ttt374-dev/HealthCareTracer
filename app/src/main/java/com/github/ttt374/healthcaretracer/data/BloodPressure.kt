package com.github.ttt374.healthcaretracer.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.sp


data class BloodPressure(val systolic: Int = 0, val diastolic: Int = 0) {
    val upper: Int get() = systolic
    val lower: Int get() = diastolic
    fun toAnnotatedString(): AnnotatedString {
        return buildAnnotatedString {
            val sbpColor = BloodPressureCategory.getCategory(upper, true).color
            val dbpColor = BloodPressureCategory.getCategory(lower, false).color
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = sbpColor))
            append(upper.toString())
            pop()
            append("/")

            pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = dbpColor))
            append(lower.toString())
            pop()

            pushStyle(SpanStyle(fontSize = 8.sp, baselineShift = BaselineShift.Subscript))
            append(" mmHg")
            pop()
        }
    }
}

//val OrangeLight = Color(0xFFFFC107) // Light Orange (Amber 500)
//val OrangeDark = Color(0xFFF57C00)  // Dark Orange (Orange 700)

sealed class BloodPressureCategory(
    val name: String,
    val sbpRange: IntRange,
    val dbpRange: IntRange,
    val color: Color,
) {
    data object Normal : BloodPressureCategory("Normal", 90..119, 60..79, Color.Unspecified)
    data object Elevated : BloodPressureCategory("Elevated", 120..129, 60..79, Color.Unspecified)
    data object HypertensionStage1 : BloodPressureCategory("HTN Stage 1", 130..139, 80..89, Color(0xFFF57C00)) // dark orange
    data object HypertensionStage2 : BloodPressureCategory("HTN Stage 2", 140..179, 90..119, Color.Red)
    data object HypertensiveCrisis : BloodPressureCategory("HTN Crisis",180..Int.MAX_VALUE,120..Int.MAX_VALUE,Color.Magenta)

    companion object {
        fun getCategory(bp: BloodPressure): BloodPressureCategory {
            return values().find { bp.upper in it.sbpRange || bp.lower in it.dbpRange } ?: Normal
        }

        fun getCategory(value: Int, isSbp: Boolean): BloodPressureCategory {
            return values().find { if (isSbp) value in it.sbpRange else value in it.dbpRange } ?: Normal
        }

        private fun values() = listOf(Normal, Elevated, HypertensionStage1, HypertensionStage2, HypertensiveCrisis).reversed()
    }
}
