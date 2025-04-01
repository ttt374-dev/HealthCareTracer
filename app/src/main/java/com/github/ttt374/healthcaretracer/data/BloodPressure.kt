package com.github.ttt374.healthcaretracer.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneId


//data class BloodPressure(val systolic: Int = 0, val diastolic: Int = 0) {
//    val upper: Int get() = systolic
//    val lower: Int get() = diastolic
//
////    fun toAnnotatedString(): AnnotatedString {
////        return bloodPressureFormatted(upper, lower)
////    }
//}
fun bloodPressureFormatted(bpUpper: Int?, bpLower: Int?, meGap: Int? = null): AnnotatedString {
    return buildAnnotatedString {
        fun appendBp(value: Int?, isSbp: Boolean) {
            if (value != null) {
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = BloodPressureCategory.getCategory(value, isSbp).color))
                append(value.toString())
                pop()
            } else {
                append("-")
            }
        }
        appendBp(bpUpper, true)
        append("/")
        appendBp(bpLower, false)

        meGap?.let {
            append(" (")
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = if (it > 20) Color.Red else Color.Unspecified))
            append(it.toString())
            pop()
            append(")")
        }

        pushStyle(SpanStyle(fontSize = 8.sp, baselineShift = BaselineShift.Subscript))
        append("mmHg")
        pop() // 明示的に `pop()` を追加
    }
}

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
        private val categories = listOf(Normal, Elevated, HypertensionStage1, HypertensionStage2, HypertensiveCrisis).reversed()

        fun getCategory(bpUpper: Int, bpLower: Int): BloodPressureCategory {
            return categories.firstOrNull { bpUpper in it.sbpRange && bpLower in it.dbpRange }
                ?: categories.firstOrNull { bpUpper in it.sbpRange || bpLower in it.dbpRange }
                ?: Normal
        }

        fun getCategory(value: Int, isSbp: Boolean): BloodPressureCategory {
            return categories.firstOrNull { if (isSbp) value in it.sbpRange else value in it.dbpRange } ?: Normal
        }
    }
}
// ME Gap
fun List<Item>.gapME(zoneId: ZoneId = ZoneId.systemDefault()): Double? {
    val (morning, evening) = this
        .mapNotNull { it.bpUpper?.let { bp -> it.measuredAt to bp } }
        .partition { (instant, _) -> instant.isMorning(zoneId) }

    val morningAvg = morning.map { it.second }.averageOrNull()
    val eveningAvg = evening.map { it.second }.averageOrNull()

    return morningAvg?.let { m -> eveningAvg?.let { m - it } }
}

fun Instant.isMorning(zoneId: ZoneId = ZoneId.systemDefault()): Boolean {
    val hour = this.atZone(zoneId).hour
    return hour in 4..11
}

fun Instant.isEvening(zoneId: ZoneId = ZoneId.systemDefault()): Boolean {
    val hour = this.atZone(zoneId).hour
    return hour in 17..23 || hour in 0..2
}
