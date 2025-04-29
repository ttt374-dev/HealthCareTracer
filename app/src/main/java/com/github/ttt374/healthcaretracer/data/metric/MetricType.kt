package com.github.ttt374.healthcaretracer.data.metric

import androidx.compose.ui.text.AnnotatedString
import com.github.mikephil.charting.data.Entry
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.bloodpressure.toAnnotatedString
import com.github.ttt374.healthcaretracer.data.item.Vitals
import com.github.ttt374.healthcaretracer.shared.toAnnotatedString
import java.time.Instant

data class MeasuredValue(
    val measuredAt: Instant,
    val value: MetricValue,
)
fun MeasuredValue.toEntries(): Entry {
    return when (value){
        is MetricNumber -> Entry(measuredAt.toEpochMilli().toFloat(), value.value.toFloat())
        is MetricBloodPressure -> Entry() // TODO: measuredAt.toEpochMilli().toFloat(), value.toFloat())
        //is Number -> Entry(measuredAt.toEpochMilli().toFloat(), value.toFloat())
        //else -> Entry() // TODOx
    }
}
fun Double.toMetricNumber() = MetricNumber(value = this)

fun List<MeasuredValue>.toEntries(): List<Entry> {
    return map { it.toEntries() }
}
//////////////////////////////
sealed interface MetricValue

data class MetricNumber(val value: Double) : MetricValue
data class MetricBloodPressure(val value: BloodPressure) : MetricValue

internal fun Int.toMetricValue() = MetricNumber(this.toDouble())
internal fun Double.toMetricValue() = MetricNumber(this)
internal fun BloodPressure.toMetricValue() = MetricBloodPressure(this)

///////////////////////////////////////////
enum class MetricType(
    val resId: Int,
    val selector: (Vitals) -> MetricValue?,
    val format: (MetricValue?) -> AnnotatedString = {
        when (it){
            is MetricNumber -> it.value.toAnnotatedString("%.1f")
            is MetricBloodPressure -> it.value.toAnnotatedString()
            null -> AnnotatedString("-")
        }
    },
) {
    BLOOD_PRESSURE(
        resId = R.string.blood_pressure,
        selector = { it.bp?.toMetricValue() },
        //format = { (it as MetricBloodPressure).value.toAnnotatedString(showUnit = false)}
    ),
    HEART(
        resId = R.string.pulse,
        selector = { it.pulse?.toMetricValue()},
    ),
    TEMPERATURE(
        resId = R.string.bodyTemperature,
        selector = { it.bodyTemperature?.toMetricValue() },
    ),
    WEIGHT(
        resId = R.string.bodyWeight,
        selector = { it.bodyWeight?.toMetricValue() },
    )
}
//// def
//data class MetricDef(
//    val id: String,
//    val resId: Int,
//    val targetResId: Int?,
//    val selector: (Vitals) -> Double?,
//    val format: (Number?) -> AnnotatedString = { it.toAnnotatedString() }
//)
//object MetricDefs {
//    val BP_UPPER = MetricDef(
//        id = "bp_upper",
//        resId = R.string.bpUpper,
//        targetResId = R.string.targetBpUpper,
//        selector = { it.bp?.upper?.toDouble() },
//        format = { it.toAnnotatedString("%.0f") }
//    )
//    val BP_LOWER = MetricDef(
//        id = "bp_lower",
//        resId = R.string.bpLower,
//        targetResId = R.string.targetBpLower,
//        selector = { it.bp?.lower?.toDouble() },
//        format = { it.toAnnotatedString("%.0f") }
//    )
//    val PULSE = MetricDef(
//        id = "pulse",
//        resId = R.string.pulse,
//        targetResId = null,
//        selector = { it.pulse?.toDouble() },
//        format = { it.toAnnotatedString("%.0f") }
//    )
//    val BODY_TEMP = MetricDef(
//        id = "body_temp",
//        resId = R.string.bodyTemperature,
//        targetResId = null,
//        selector = { it.bodyTemperature },
//        format = { it.toAnnotatedString("%.1f") }
//    )
//    val BODY_WEIGHT = MetricDef(
//        id = "body_weight",
//        resId = R.string.bodyWeight,
//        targetResId = R.string.targetBodyWeight,
//        selector = { it.bodyWeight },
//        format = { it.toAnnotatedString("%.1f") }
//    )
//}
/////////////
// category


//object MetricDefRegistry {
//    val allDefs: List<MetricDef> = MetricType.entries.flatMap { it.defs }
//
//    fun getByCategory(category: MetricType): List<MetricDef> =
//        category.defs
//
//    fun getById(id: String): MetricDef? =
//        allDefs.find { it.id == id }
//}


//data class MetricDef(
//    val id: String,
//    val resId: Int,
//    val targetResId: Int?,
//    val category: MetricCategory,
//    val selector: (Vitals) -> Double?,
//    val format: (Number?) -> AnnotatedString = { it.toAnnotatedString() }
//)
//enum class MetricCategory(val resId: Int) {
//    BLOOD_PRESSURE(R.string.blood_pressure),
//    HEART(R.string.pulse),
//    TEMPERATURE(R.string.bodyTemperature),
//    WEIGHT(R.string.bodyWeight);
//}
//
//object MetricDefRegistry {
//    val defs: List<MetricDef> = listOf(
//        MetricDef(
//            id = "bp_upper",
//            resId = R.string.bpUpper,
//            targetResId = R.string.targetBpUpper,
//            category = MetricCategory.BLOOD_PRESSURE,
//            selector = { it.bp?.upper?.toDouble() },
//            format = { it.toAnnotatedString("%.0f")}
//        ),
//        MetricDef(
//            id = "bp_lower",
//            resId = R.string.bpLower,
//            targetResId = R.string.targetBpLower,
//            category = MetricCategory.BLOOD_PRESSURE,
//            selector = { it.bp?.lower?.toDouble() },
//            format = { it.toAnnotatedString("%.0f")}
//        ),
//        MetricDef(
//            id = "pulse",
//            resId = R.string.pulse,
//            targetResId = null,
//            category = MetricCategory.HEART,
//            selector = { it.pulse?.toDouble() },
//            format = { it.toAnnotatedString("%.0f")}
//        ),
//        MetricDef(
//            id = "body_temp",
//            resId = R.string.bodyTemperature,
//            targetResId = null,
//            category = MetricCategory.TEMPERATURE,
//            selector = { it.bodyTemperature },
//            format = { it.toAnnotatedString("%.1f")}
//        ),
//        MetricDef(
//            id = "body_weight",
//            resId = R.string.bodyWeight,
//            targetResId = R.string.targetBodyWeight,
//            category = MetricCategory.WEIGHT,
//            selector = { it.bodyWeight },
//            format = { it.toAnnotatedString("%.1f")}
//        )
//    )
//
//    fun getByCategory(category: MetricCategory): List<MetricDef> =
//        defs.filter { it.category == category }
//
//    fun getById(id: String): MetricDef? =
//        defs.find { it.id == id }
//}
//
