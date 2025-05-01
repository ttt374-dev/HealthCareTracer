package com.github.ttt374.healthcaretracer.ui.home

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.repository.ConfigRepository
import com.github.ttt374.healthcaretracer.usecase.ExportDataUseCase
import com.github.ttt374.healthcaretracer.usecase.ImportDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.github.ttt374.healthcaretracer.data.repository.Config

@HiltViewModel
class HomeViewModel @Inject constructor(
    configRepository: ConfigRepository,
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase,): ViewModel()
{
    val config = configRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Config())

    fun exportData(uri: Uri){
        viewModelScope.launch {
            exportDataUseCase(uri)
        }
    }
    fun importData(uri: Uri){
        viewModelScope.launch {
            importDataUseCase(uri)
        }
    }
}