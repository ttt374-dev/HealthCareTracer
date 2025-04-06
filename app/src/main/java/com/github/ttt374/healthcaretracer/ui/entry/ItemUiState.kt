package com.github.ttt374.healthcaretracer.ui.entry

import com.github.ttt374.healthcaretracer.data.item.Item
import java.time.Instant

data class ItemUiState (
    val id: Long? = null,
    val bpUpper: String = "",
    val bpLower: String = "",
    val pulse: String = "",
    val bodyWeight: String = "",
    val location: String = "",
    val memo: String = "",
    val measuredAt: Instant = Instant.now(),
){
    fun toItem() = Item(
        id = id?: 0,
        bpUpper = bpUpper.toIntOrNull(),
        bpLower = bpLower.toIntOrNull(),
        pulse = pulse.toIntOrNull(),
        bodyWeight = bodyWeight.toFloatOrNull(),
        memo = memo, location = location, measuredAt = measuredAt)

    fun isValid(): Boolean {
        if (bpUpper.isEmpty() && bpLower.isEmpty()) return true
        if (bpUpper.isEmpty() || bpLower.isEmpty()) return false

        return bpUpper.toIntOrNull()?.let { upper ->
            bpLower.toIntOrNull()?.let { lower -> upper > lower }
        } ?: false
    }
}
fun Item.toItemUiState(): ItemUiState {
    return ItemUiState(  this.id,
        this.bpUpper.toStringOrEmpty(),
        this.bpLower.toStringOrEmpty(),
        this.pulse.toStringOrEmpty(),
        this.bodyWeight.toStringOrEmpty(),
        this.location, this.memo, this.measuredAt)
}

fun Number?.toStringOrEmpty(): String = this?.toString() ?: ""