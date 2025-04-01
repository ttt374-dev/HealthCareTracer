package com.github.ttt374.healthcaretracer.data

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
fun bloodPressureFormatted(bpUpper: Int?, bpLower: Int?, meGap: Int?=null): AnnotatedString {
    return buildAnnotatedString {
        if (bpUpper != null){
            val sbpColor = BloodPressureCategory.getCategory(bpUpper, true).color
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = sbpColor))
            append(bpUpper.toString())
            pop()
        } else {
            append("-")
        }
        append("/")

        if (bpLower != null){
            val dbpColor = BloodPressureCategory.getCategory(bpLower, false).color
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = dbpColor))
            append(bpLower.toString())
            pop()
        } else {
            append("-")
        }
        if (meGap != null){
            append(" (")
            val gapColor = if (meGap > 20) Color.Red else Color.Unspecified
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = gapColor))
            append(meGap.toString())
            pop()
            append(")")
        }
        pushStyle(SpanStyle(fontSize = 8.sp, baselineShift = BaselineShift.Subscript))
        append("mmHg")
        pop()
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
        //fun getCategory(bp: BloodPressure): BloodPressureCategory {
        fun getCategory(bpUpper: Int, bpLower: Int): BloodPressureCategory {
            return values().find { bpUpper in it.sbpRange && bpLower in it.dbpRange }
                ?: values().find { bpUpper in it.sbpRange || bpLower in it.dbpRange }
                ?: Normal
        }

        fun getCategory(value: Int, isSbp: Boolean): BloodPressureCategory {
            return values().find { if (isSbp) value in it.sbpRange else value in it.dbpRange } ?: Normal
        }

        private fun values() = listOf(Normal, Elevated, HypertensionStage1, HypertensionStage2, HypertensiveCrisis).reversed()
    }
}
// ME Gap
fun List<Item>.gapME(zoneId: ZoneId = ZoneId.systemDefault()): Double? {
    val morningAvg = this.filter { it.measuredAt.isMorning(zoneId) }.map { it.bpUpper }.averageOrNull()
    val eveningAvg = this.filter { it.measuredAt.isEvening(zoneId) }.map { it.bpUpper }.averageOrNull()

    return if (morningAvg != null && eveningAvg != null) {
        morningAvg - eveningAvg // ME差の計算
    } else {
        null
    }
}
fun Instant.isMorning(zoneId: ZoneId = ZoneId.systemDefault()): Boolean {
    val localTime = this.atZone(zoneId).toLocalTime()
    return localTime.hour in 4..10 || (localTime.hour == 11 && localTime.minute == 0)
}

fun Instant.isEvening(zoneId: ZoneId = ZoneId.systemDefault()): Boolean {
    val localTime = this.atZone(zoneId).toLocalTime()
    return localTime.hour in 17..23 || localTime.hour in 0..2
}