package com.github.ttt374.healthcaretracer.ui.entry

import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.item.MIN_BP
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
        bpUpper = bpUpper.toIntOrNull(),
        bpLower = bpLower.toIntOrNull(),
        pulse = pulse.toIntOrNull(),
        bodyWeight = bodyWeight.toDoubleOrNull(),
        bodyTemperature = bodyTemperature.toDoubleOrNull(),
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
        this.bpUpper.toStringOrEmpty(),
        this.bpLower.toStringOrEmpty(),
        this.pulse.toStringOrEmpty(),
        this.bodyWeight.toStringOrEmpty(),
        this.bodyTemperature.toStringOrEmpty(),
        this.location, this.memo, this.measuredAt)
}

fun Number?.toStringOrEmpty(): String = this?.toString() ?: ""