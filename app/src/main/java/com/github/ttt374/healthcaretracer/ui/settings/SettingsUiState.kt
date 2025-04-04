package com.github.ttt374.healthcaretracer.ui.settings

import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import com.github.ttt374.healthcaretracer.data.datastore.Config

data class SettingsUiState(
    val bloodPressureGuidelineName: String = "",
    val targetBpUpper: String = "",
    val targetBpLower: String = "",
){
    fun toConfig() = Config(
        bloodPressureGuideline = BloodPressureGuideline.bloodPressureGuidelines[bloodPressureGuidelineName] ?: BloodPressureGuideline.WHO,
        targetBpUpper = targetBpUpper.toInt(),
        targetBpLower = targetBpLower.toInt(),
    )
}
fun Config.toSettingsUiState() =
    SettingsUiState(this.bloodPressureGuideline.name, this.targetBpUpper.toString(), this.targetBpLower.toString())