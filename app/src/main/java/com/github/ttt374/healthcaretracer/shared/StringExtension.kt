package com.github.ttt374.healthcaretracer.shared

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

fun String.withSubscript(subscript: String, textFontSize: TextUnit = 16.sp, subscriptFontSize: TextUnit = 8.sp): AnnotatedString {
    return AnnotatedString.Builder().apply {
        pushStyle(SpanStyle(fontSize = textFontSize)) // 大きめのフォントサイズ
        append(this@withSubscript)

        // 小さな単位部分
        pop()
        pushStyle(SpanStyle(fontSize = subscriptFontSize, baselineShift = BaselineShift.Subscript))
        append(subscript)
    }.toAnnotatedString()
}

fun Number?.toDisplayString(format: String? = null): String {
    return this?.let  {
        if (format != null)
            String.format(format, this)
        else
            this.toString()
    }   ?: "-"
}

fun Number?.toPulseString(): AnnotatedString = toDisplayString().withSubscript("bpm")
fun Number?.toBodyWeightString(): AnnotatedString = toDisplayString("%.1f").withSubscript("kg")
fun Number?.toBodyTemperatureString(): AnnotatedString = toDisplayString("%.1f").withSubscript("℃")
fun Number?.toAnnotatedString(format: String? = null) = toDisplayString(format).toAnnotatedString()

fun String.toAnnotatedString() = AnnotatedString(this)
