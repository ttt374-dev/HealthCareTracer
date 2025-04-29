package com.github.ttt374.healthcaretracer.data.metric

import android.util.Log
import androidx.compose.ui.text.AnnotatedString
import com.github.mikephil.charting.data.Entry
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.bloodpressure.toAnnotatedString
import com.github.ttt374.healthcaretracer.data.item.Vitals
import com.github.ttt374.healthcaretracer.shared.toAnnotatedString
import java.time.Instant

data class MeasuredValue<T>(
    val measuredAt: Instant,
    val value: T,
)
fun <T>MeasuredValue<T>.toEntries(): Entry {
    val xValue = measuredAt.toEpochMilli().toFloat()
    return when (value){
        is MetricValue.Double -> Entry(xValue, value.value.toFloat())
        is MetricValue.Int -> Entry(xValue, value.value.toFloat())
        is Number -> Entry(xValue, value.toFloat())
        else -> { Log.d("toEntry", "only works for MetricDouble: ${this::class.java}"); Entry() }
    }
}
fun Double.toMetricNumber() = MetricValue.Double(value = this)

fun <T>List<MeasuredValue<T>>.toEntries(): List<Entry> {
    return map { it.toEntries() }
}

sealed class MetricValue(val format: () -> AnnotatedString) {
    data class Double(val value: kotlin.Double) : MetricValue({
        value.toAnnotatedString("%.1f")
    })
    data class Int(val value: kotlin.Int) : MetricValue({
        value.toAnnotatedString("%d")
    })
    data class BloodPressure(
        val value: com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure) : MetricValue({
        value.toAnnotatedString()
    })
}

internal fun Int.toMetricValue() = MetricValue.Int(this)
internal fun Double.toMetricValue() = MetricValue.Double(this)
fun BloodPressure.toMetricValue() = MetricValue.BloodPressure(this)
//fun MetricValue?.toAnnotatedString(): AnnotatedString {
//    return when (this){
//        null -> { AnnotatedString("-")}
//        else -> { format() }
//    }
//}

///////////////////////////////////////////
enum class MetricType(
    val resId: Int,
    val selector: (Vitals) -> MetricValue?,
) {
    BLOOD_PRESSURE(
        resId = R.string.blood_pressure,
        selector = { it.bp?.toMetricValue() },
    ),
    PULSE(
        resId = R.string.pulse,
        selector = { it.pulse?.toMetricValue()},
    ),
    BODY_TEMPERATURE(
        resId = R.string.bodyTemperature,
        selector = { it.bodyTemperature?.toMetricValue() },
    ),
    WEIGHT(
        resId = R.string.bodyWeight,
        selector = { it.bodyWeight?.toMetricValue() },
    )
}
