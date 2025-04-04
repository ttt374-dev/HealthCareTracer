package com.github.ttt374.healthcaretracer.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.datastore.Config
import com.github.ttt374.healthcaretracer.data.datastore.ConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SettingsViewModel @Inject constructor(private val configRepository: ConfigRepository) : ViewModel(){
    val config = configRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Config())
//    private val _settingsUiState = MutableStateFlow(SettingsUiState())
//    val settingsUiState: StateFlow<SettingsUiState> = _settingsUiState
//
//    init {
//        viewModelScope.launch {
//            configRepository.dataFlow.collect { config ->
//                _settingsUiState.update {
//                    config.toSettingsUiState()
//                }
//            }
//        }
//    }
//
//    //fun updateSetting(update: SettingsUiState.() -> SettingsUiState) {
//    fun updateSetting(uiState: SettingsUiState){
//        _settingsUiState.value = uiState
//            //_isModified.update { true }
//    }
//    fun savePreferences() {
//        viewModelScope.launch {
//            configRepository.updateData(_settingsUiState.value.toConfig())
//        }
//    }
    fun saveConfig(config: Config){
        viewModelScope.launch {
            configRepository.updateData(config)
        }
    }
}