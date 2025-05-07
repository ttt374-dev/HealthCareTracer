package com.github.ttt374.healthcaretracer.ui.home

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.repository.ConfigRepository
import com.github.ttt374.csv_backup_lib.ExportDataUseCase
import com.github.ttt374.csv_backup_lib.ImportDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.github.ttt374.healthcaretracer.data.repository.Config
import com.github.ttt374.healthcaretracer.data.repository.ItemRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest

@HiltViewModel
class HomeViewModel @Inject constructor(itemRepository: ItemRepository, configRepository: ConfigRepository,
    private val exportDataUseCase: ExportDataUseCase<Item>,
    private val importDataUseCase: ImportDataUseCase<Item>,): ViewModel()
{
    val config = configRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Config())
    @OptIn(ExperimentalCoroutinesApi::class)
    val dailyItems = config.flatMapLatest { config ->
        itemRepository.getAllDailyItemsFlow(config.zoneId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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