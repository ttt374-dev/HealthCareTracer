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


data class BloodPressure(val systolic: Int?, val diastolic: Int?) {
    val upper: Int? get() = systolic
    val lower: Int? get() = diastolic
    //val isValid: Boolean get() = upper != null && lower != null
    fun toDisplayString(showUnit: Boolean = true) =
            bloodPressureFormatted(upper, lower, showUnit)
    fun htnCategory()= BloodPressureGuideline.getCategory(upper, lower)
//    fun toAnnotatedString(): AnnotatedString {
//        return bloodPressureFormatted(upper, lower)
//    }
}
fun bloodPressureFormatted(bpUpper: Int?, bpLower: Int?, showUnit: Boolean = true): AnnotatedString {
    return buildAnnotatedString {
        fun appendBp(value: Int?, isSbp: Boolean) {
            if (value != null) {
                pushStyle(SpanStyle(color = BloodPressureGuideline.getCategory(value, isSbp).color))
                append(value.toString())
                pop()
            } else {
                append("-")
            }
        }
        appendBp(bpUpper, true)
        append("/")
        appendBp(bpLower, false)

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

