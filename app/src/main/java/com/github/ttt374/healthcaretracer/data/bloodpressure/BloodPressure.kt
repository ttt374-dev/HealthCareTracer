package com.github.ttt374.healthcaretracer.data.bloodpressure

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.sp
import androidx.room.TypeConverter
import com.github.ttt374.healthcaretracer.ui.statics.toAnnotatedString

//sealed interface StatComputable
//
////data class BloodPressure(val upper: Double, val lower: Double) : StatComputable
//
//// Double にも対応させる
//@JvmInline
//value class StatDouble(val value: Double) : StatComputable

data class BloodPressure(val upper: Int, val lower: Int)
//{ //} : StatComputable {
//    val systolic = upper
//    val diastolic = lower
//}

fun BloodPressure?.toAnnotatedString(guideline: BloodPressureGuideline = BloodPressureGuideline.Default, showUnit: Boolean = true) : AnnotatedString {
    val bp = this
    return this?.let {
        buildAnnotatedString {
            fun appendBp(value: Int?, color: Color) {
                if (value != null) {
                    pushStyle(SpanStyle(color = color))
                    append(value.toString())
                    pop()
                } else {
                    append("-")
                }
            }
            appendBp(upper, guideline.getCategory(bp).color)
            append("/")
            appendBp(lower, guideline.getCategory(bp).color)

            if (showUnit){
                pushStyle(SpanStyle(fontSize = 8.sp, baselineShift = BaselineShift.Subscript))
                append("mmHg")
                pop() // 明示的に `pop()` を追加
            }
        }
    }?: "-/-".toAnnotatedString()
}
    //return bloodPressureFormatted(upper, lower, showUnit, guideline)

//fun Pair<Number, Number>.toBloodPressure(): BloodPressure = BloodPressure(first.toInt(), second.toInt())

fun Pair<Number?, Number?>.toBloodPressure(): BloodPressure? {
    val (upper, lower) = this
    return if (upper != null && lower != null) {
        BloodPressure(upper.toInt(), lower.toInt())
    } else {
        null
    }
}


class BloodPressureConverter {
    @TypeConverter
    fun fromBloodPressure(bp: BloodPressure): String {
        return "${bp.upper},${bp.lower}"
    }

    @TypeConverter
    fun toBloodPressure(data: String): BloodPressure {
        val parts = data.split(",")
        val upper = parts[0].toInt()
        val lower = parts[1].toInt()
        return BloodPressure(upper, lower)
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

