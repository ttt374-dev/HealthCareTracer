package com.github.ttt374.healthcaretracer.data

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.sp
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.github.ttt374.healthcaretracer.ui.home.BloodPressureCategory
import java.time.Instant

const val MIN_PULSE = 30
const val MAX_PULSE = 200
const val MIN_BP = 50
const val MAX_BP = 260

@Entity(tableName = "items" )
data class Item (
    @PrimaryKey(autoGenerate=true)
    val id: Long = 0,
    val measuredAt: Instant = Instant.now(),
//    val bpHigh: Int = 0,
//    val bpLow: Int = 0,
    val bp: BloodPressure = BloodPressure(),
    val pulse: Int = 0,
    val bodyWeight: Float = 0F,
    val location: String = "",

    val memo: String = "",
){
//    val isValid: Boolean
//        get() = bp.isValid && pulse in MIN_PULSE..MAX_PULSE
}

data class BloodPressure (
    val systolic: Int = 0,
    val diastolic: Int = 0,
) {
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
//    val isValid: Boolean
//        get() = systolic in MIN_BP..MAX_BP && diastolic in MIN_BP..MAX_BP &&
//                systolic > diastolic
}
