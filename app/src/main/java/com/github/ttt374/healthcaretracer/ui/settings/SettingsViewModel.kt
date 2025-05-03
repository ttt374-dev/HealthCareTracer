package com.github.ttt374.healthcaretracer.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.bloodpressure.toBloodPressure
import com.github.ttt374.healthcaretracer.data. repository.Config
import com.github.ttt374.healthcaretracer.data.repository.ConfigRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val configRepository: ConfigRepository, @ApplicationContext val context: Context) : ViewModel(){
    val config = configRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Config())
    val timezoneList: StateFlow<List<String>> = flow {
        val timezones = context.resources.getStringArray(R.array.timezone_list).toList()
        emit(timezones)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun saveConfig(config: Config){
        viewModelScope.launch {
            configRepository.updateData{ config }
        }
    }
}

