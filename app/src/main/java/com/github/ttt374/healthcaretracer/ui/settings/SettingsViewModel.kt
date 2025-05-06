package com.github.ttt374.healthcaretracer.ui.settings

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import com.github.ttt374.healthcaretracer.data.metric.DayPeriod
import com.github.ttt374.healthcaretracer.data.repository.Config
import com.github.ttt374.healthcaretracer.data.repository.ConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val configRepository: ConfigRepository, @ApplicationContext val context: Context) : ViewModel(){
    val config = configRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Config())
    val timezoneList: StateFlow<List<String>> = flow {
        val timezones = context.resources.getStringArray(R.array.timezone_list).toList()
        emit(timezones)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun updateBloodPressureGuidelineByName(name: String) {
        val bloodPressureGuideline = BloodPressureGuideline.entries.find { it.name == name } ?: BloodPressureGuideline.Default
        saveConfig { it.copy(bloodPressureGuideline=bloodPressureGuideline) }
    }
    fun updateTargetVital(type: TargetVitalsType, input: String){
        val updatedVitals = type.update(config.value.targetVitals, input)
        saveConfig { it.copy(targetVitals = updatedVitals) }
    }
    fun updateDayPeriod(dayPeriod: DayPeriod, time: LocalTime){
        saveConfig { it.copy(dayPeriodConfig = config.value.dayPeriodConfig.update(dayPeriod, time))}
    }
    fun updateTimeZone(input: String){
        val zoneId = runCatching { ZoneId.of(input) }.getOrNull()
        if (zoneId != null) {
            saveConfig{ it.copy(zoneId = zoneId)}
        } else {
            Log.e("zoneId", "illegal zoneId: $input")
        }
    }

    private fun saveConfig(update: (Config) -> Config) {
        viewModelScope.launch {
            configRepository.updateData { currentConfig ->
                update(currentConfig)
            }
        }
    }
//    private fun saveConfig(config: Config){
//        viewModelScope.launch {
//            configRepository.updateData{ config }
//        }
//    }
}

