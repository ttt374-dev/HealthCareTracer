package com.github.ttt374.healthcaretracer.data.bloodpressure

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.sp


data class BloodPressure(val systolic: Int?, val diastolic: Int?) {
    val upper: Int? get() = systolic
    val lower: Int? get() = diastolic
    //val isValid: Boolean get() = upper != null && lower != null
    fun toDisplayString(showUnit: Boolean = true, guideline: BloodPressureGuideline? = null) : AnnotatedString {
        return bloodPressureFormatted(upper, lower, showUnit, guideline)
    }
}
fun bloodPressureFormatted(bpUpper: Int?, bpLower: Int?,
                           showUnit: Boolean = true,
                           guideline: BloodPressureGuideline? = null): AnnotatedString {
    return buildAnnotatedString {
        fun appendBp(value: Int?, color: Color) {
            if (value != null) {
                pushStyle(SpanStyle(color = color))
                append(value.toString())
                pop()
            } else {
                append("-")
            }
        }

        appendBp(bpUpper, bpUpper?.let { guideline?.getCategory(bpUpper, true)?.color } ?: Color.Unspecified)
        append("/")
        appendBp(bpLower, bpLower?.let { guideline?.getCategory(bpLower, false)?.color } ?: Color.Unspecified)

//        meGap?.let {
//            append(" (")
//            pushStyle(SpanStyle(color = if (it > 20) Color.Red else Color.Unspecified))
//            append(it.toString())
//            pop()
//            append(")")
//        }
        if (showUnit){
            pushStyle(SpanStyle(fontSize = 8.sp, baselineShift = BaselineShift.Subscript))
            append("mmHg")
            pop() // 明示的に `pop()` を追加
        }
    }
}
//
//sealed class BloodPressureCategory(
//    val name: String,
//    val sbpRange: IntRange,
//    val dbpRange: IntRange,
//    val color: Color,
//) {
//    data object Invalid: BloodPressureCategory("Normal", 0..90, 0..39, Color.Gray)
//    data object Normal : BloodPressureCategory("Normal", 40..119, 40..79, Color.Unspecified)
//    data object Elevated : BloodPressureCategory("Elevated", 120..129, 60..79, Color.Unspecified)
//    data object HypertensionStage1 : BloodPressureCategory("HTN Stage 1", 130..139, 80..89, Color(0xFFF57C00)) // dark orange
//    data object HypertensionStage2 : BloodPressureCategory("HTN Stage 2", 140..179, 90..119, Color.Red)
//    data object HypertensiveCrisis : BloodPressureCategory("HTN Crisis",180..Int.MAX_VALUE,120..Int.MAX_VALUE,Color.Magenta)
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


// ME Gap
//fun List<Item>.gapME(zoneId: ZoneId = ZoneId.systemDefault()): Double? {
//    val (morning, evening) = this
//        .mapNotNull { it.bpUpper?.let { bp -> it.measuredAt to bp } }
//        .partition { (instant, _) -> instant.isMorning(zoneId) }
//
//    val morningAvg = morning.map { it.second }.averageOrNull()
//    val eveningAvg = evening.map { it.second }.averageOrNull()
//
//    return morningAvg?.let { m -> eveningAvg?.let { m - it } }
//}

//fun Instant.isMorning(zoneId: ZoneId = ZoneId.systemDefault()): Boolean {
//    val hour = this.atZone(zoneId).hour
//    return hour in 4..11
//}
//
//fun Instant.isEvening(zoneId: ZoneId = ZoneId.systemDefault()): Boolean {
//    val hour = this.atZone(zoneId).hour
//    return hour in 17..23 || hour in 0..2
//}
