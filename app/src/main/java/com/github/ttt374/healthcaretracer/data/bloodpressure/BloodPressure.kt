package com.github.ttt374.healthcaretracer.data.bloodpressure

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.sp
import androidx.room.TypeConverter
import kotlinx.serialization.Serializable

@Serializable
data class BloodPressure(val upper: Int, val lower: Int)

fun BloodPressure?.toAnnotatedString(guideline: BloodPressureGuideline = BloodPressureGuideline.Default, showUnit: Boolean = true) : AnnotatedString {
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
            appendBp(upper, guideline.getCategoryUpper(upper).color)
            append("/")
            appendBp(lower, guideline.getCategoryLower(lower).color)

            if (showUnit){
                pushStyle(SpanStyle(fontSize = 8.sp, baselineShift = BaselineShift.Subscript))
                append("mmHg")
                pop() // 明示的に `pop()` を追加
            }
        }
    }?: toAnnotatedString()
}

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
