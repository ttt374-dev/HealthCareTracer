package com.github.ttt374.healthcaretracer.data.metric

import androidx.compose.ui.text.AnnotatedString
import com.github.mikephil.charting.data.Entry
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.item.Vitals
import com.github.ttt374.healthcaretracer.shared.toAnnotatedString
import java.time.Instant

data class MeasuredValue(
    val measuredAt: Instant,
    val value: Double,
)
fun MeasuredValue.toEntry() = Entry(measuredAt.toEpochMilli().toFloat(), value.toFloat())

fun List<MeasuredValue>.toEntry(): List<Entry> {
    return map { it.toEntry() }
}


///////////////////////////////////////////
enum class MetricType(
    val resId: Int,
    val selector: (Vitals) -> Double?,
    val format: (Number?) -> AnnotatedString = { it.toAnnotatedString() },
    //val defs: List<MetricDef>
) {
    BLOOD_PRESSURE(
        resId = R.string.blood_pressure,
        selector = { it.bp?.upper?.toDouble() },
    ),
    HEART(
        resId = R.string.pulse,
        selector = { it.pulse?.toDouble()},
    ),
    TEMPERATURE(
        resId = R.string.bodyTemperature,
        selector = { it.bodyTemperature },
    ),
    WEIGHT(
        resId = R.string.bodyWeight,
        selector = { it.bodyWeight },
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
