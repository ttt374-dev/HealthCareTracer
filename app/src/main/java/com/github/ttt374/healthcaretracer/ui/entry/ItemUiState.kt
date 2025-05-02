package com.github.ttt374.healthcaretracer.ui.entry

import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.item.MIN_BP
import com.github.ttt374.healthcaretracer.data.item.Vitals
import java.time.Instant

data class ItemUiState (
    val id: Long? = null,
    val bpUpper: String = "",
    val bpLower: String = "",
    val pulse: String = "",
    val bodyWeight: String = "",
    val bodyTemperature: String = "",
    val location: String = "",
    val memo: String = "",
    val measuredAt: Instant = Instant.now(),
){
    fun toItem() = Item(
        id = id?: 0,
        vitals = Vitals(
            bp = bpUpper.toIntOrNull()?.let { upper -> bpLower.toIntOrNull()?.let { lower -> BloodPressure(upper, lower)}},
            pulse = pulse.toIntOrNull(),
            bodyWeight = bodyWeight.toDoubleOrNull(),
            bodyTemperature = bodyTemperature.toDoubleOrNull()),
        memo = memo, location = location, measuredAt = measuredAt)

    val isValid: Boolean
        get() = bpUpper.toIntOrNull()?.let { upper ->
            bpLower.toIntOrNull()?.let { lower ->
                upper > lower && upper >= MIN_BP && lower >= MIN_BP
            }
        } ?: false
}
fun Item.toItemUiState(): ItemUiState {
    return ItemUiState(  this.id,
        this.vitals.bp?.upper.toStringOrEmpty(),
        this.vitals.bp?.lower.toStringOrEmpty(),
        this.vitals.pulse.toStringOrEmpty(),
        this.vitals.bodyWeight.toStringOrEmpty(),
        this.vitals.bodyTemperature.toStringOrEmpty(),
        this.location, this.memo, this.measuredAt)
}

fun Number?.toStringOrEmpty(): String = this?.toString() ?: ""