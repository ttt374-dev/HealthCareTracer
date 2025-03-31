package com.github.ttt374.healthcaretracer.ui.entry

import com.github.ttt374.healthcaretracer.data.BloodPressure
import com.github.ttt374.healthcaretracer.data.Item
import com.github.ttt374.healthcaretracer.data.MAX_BP
import com.github.ttt374.healthcaretracer.data.MAX_PULSE
import com.github.ttt374.healthcaretracer.data.MIN_BP
import com.github.ttt374.healthcaretracer.data.MIN_PULSE
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

    //val isSuccess: Boolean = false,
){
    fun toItem() = Item(
        //id = (this.editMode as? EditMode.Edit)?.itemId ?: 0, // editModeがEditならidを更新、それ以外は0,
        id = id?: 0,
        bp = BloodPressure(bpUpper.toIntOrNull() ?: 0, bpLower.toIntOrNull() ?: 0),
        //bpHigh = bpHigh.toIntOrNull() ?: 0,
        //bpLow = bpLow.toIntOrNull() ?:0,
        pulse = pulse.toIntOrNull() ?: 0,
        bodyWeight = if (bodyWeight == "") bodyWeight.toFloatOrNull() else null,
        memo = memo, location = location, measuredAt = measuredAt)
//    fun isBpUpperValid(): Boolean {
//        return (bpUpper.toIntOrNull() ?: 0) in MIN_BP..MAX_BP
//    }
//    fun isBpLowerValid(): Boolean {
//        return (bpLower.toIntOrNull() ?: 0) in MIN_BP..MAX_BP
//    }
//    fun isPulseValid(): Boolean {
//        return (pulse.toIntOrNull() ?: 0) in MIN_PULSE..MAX_PULSE
//    }
    fun isValid(): Boolean {
        val bpUpperInt = bpUpper.toIntOrNull() ?: 0
        val bpLowerInt = bpLower.toIntOrNull() ?: 0
        val pulseInt = pulse.toIntOrNull() ?: 0

        return bpUpperInt in MIN_BP..MAX_BP &&
                bpLowerInt in MIN_BP..MAX_BP &&
                pulseInt in MIN_PULSE..MAX_PULSE &&
                bpUpperInt > bpLowerInt
    }
}
fun Item.toItemUiState(): ItemUiState {
    return ItemUiState(  this.id,
        this.bp.upper.toString(), this.bp.lower.toString(), this.pulse.toString(),
        //if (this.bodyWeight == 0.0F) "" else this.bodyWeight.toString(),
        this.bodyWeight.takeIf { it != 0.0F }?.toString().orEmpty(),
        this.location, this.memo, this.measuredAt)
}