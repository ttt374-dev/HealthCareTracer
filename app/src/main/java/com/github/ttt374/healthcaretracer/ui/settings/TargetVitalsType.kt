package com.github.ttt374.healthcaretracer.ui.settings

import androidx.compose.ui.text.input.KeyboardType
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.item.TargetVitals
import java.time.DateTimeException
import java.time.ZoneId

sealed class TargetVitalsType {
    abstract val resId: Int
    abstract fun selector(vitals: TargetVitals): Any?
    abstract fun validate(input: String): Boolean
    abstract fun update(vitals: TargetVitals, input: String): TargetVitals
    abstract val keyboardType: KeyboardType
    fun getStringValue(targetVitals: TargetVitals): String {
        return selector(targetVitals)?.toString() ?: ""
    }

    data object BpUpper : TargetVitalsType() {
        override val resId = R.string.targetBpUpper
        override fun selector(vitals: TargetVitals) = vitals.bp.upper
        override fun validate(input: String) = ConfigValidator.validatePositiveInt(input)
        override fun update(vitals: TargetVitals, input: String) =
            vitals.copy(bp = BloodPressure(input.toIntOrNull() ?: 0, vitals.bp.lower))
        override val keyboardType = KeyboardType.Decimal
    }

    data object BpLower : TargetVitalsType() {
        override val resId = R.string.targetBpLower
        override fun selector(vitals: TargetVitals) = vitals.bp.lower
        override fun validate(input: String) = ConfigValidator.validatePositiveInt(input)
        override fun update(vitals: TargetVitals, input: String) =
            vitals.copy(bp = BloodPressure(vitals.bp.upper, input.toIntOrNull() ?: 0))
        override val keyboardType = KeyboardType.Decimal
    }

    data object BodyWeight : TargetVitalsType() {
        override val resId = R.string.targetBodyWeight
        override fun selector(vitals: TargetVitals) = vitals.bodyWeight
        override fun validate(input: String) = ConfigValidator.validatePositiveDouble(input)
        override fun update(vitals: TargetVitals, input: String) =
            vitals.copy(bodyWeight = input.toDoubleOrNull() ?: 0.0)
        override val keyboardType = KeyboardType.Number
    }
    companion object {
        val entries = listOf(BpUpper, BpLower, BodyWeight)
    }
}

object ConfigValidator {
    fun validatePositiveInt(input: String): Boolean =
        input.toIntOrNull()?.let { it > 0 } == true

    fun validatePositiveDouble(input: String): Boolean =
        input.toDoubleOrNull()?.let { it > 0.0} == true

    fun validateZoneId(input: String): Boolean =
        input.isNotBlank() && try { ZoneId.of(input); true } catch (e: DateTimeException){ false}
}


//
//enum class TargetVitalsType(val resId: Int, val selector: (TargetVitals) -> Any?, val validator: (String) -> Boolean,
//                            val updateTargetVitals: (TargetVitals, String) -> TargetVitals, val keyboardType: KeyboardType) {
//    BpUpper(R.string.targetBpUpper, { vitals -> vitals.bp.upper }, ConfigValidator::validatePositiveInt,
//        { targetVitals, input ->  targetVitals.copy(bp = BloodPressure(input.toIntOrNull() ?: 0, targetVitals.bp.lower))},
//        KeyboardType.Number),
//    BpLower(R.string.targetBpLower, { vitals -> vitals.bp.lower }, ConfigValidator::validatePositiveInt,
//        { targetVitals, input -> targetVitals.copy(bp = BloodPressure(targetVitals.bp.upper, input.toIntOrNull() ?: 0))},
//        KeyboardType.Number),
//    BodyWeight(R.string.targetBodyWeight, { vitals -> vitals.bodyWeight }, ConfigValidator::validatePositiveDouble,
//        { targetVitals, input -> targetVitals.copy(bodyWeight = input.toDoubleOrNull() ?: 0.0)},
//        KeyboardType.Decimal);
//    fun getStringValue(targetVitals: TargetVitals): String {
//        return selector(targetVitals)?.toString() ?: ""
//    }
//}