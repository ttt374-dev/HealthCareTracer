package com.github.ttt374.healthcaretracer.ui.entry

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.ItemRepository
import com.github.ttt374.healthcaretracer.usecase.ExportDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject


@HiltViewModel
class EntryViewModel @Inject constructor (savedStateHandle: SavedStateHandle, private val itemRepository: ItemRepository): ViewModel() {
    private val dateString: String? = savedStateHandle["date"]
    private val date: LocalDate = dateString?.let { LocalDate.parse(it)} ?: LocalDate.now()

    private val _itemUiState = MutableStateFlow(ItemUiState()) // MutableStateFlow に変更
    val itemUiState: StateFlow<ItemUiState> get() = _itemUiState // StateFlow として公開
//    private val _saveState = MutableStateFlow(false)
//    val saveState: StateFlow<Boolean> get() = _saveState

    //val locationList = itemRepository.getAllLocationsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        _itemUiState.value = ItemUiState(measuredAt = Instant.now().withDate(date))
    }
    fun updateItemUiState(uiState: ItemUiState) {
        _itemUiState.value = uiState
    }
//    fun upsertItem(){
//        viewModelScope.launch {
//            itemRepository.upsertItem(itemUiState.value.toItem())
//            exportDataUseCase("items-autosave.csv")
//            setSuccessState(true)
//        }
//
//    }
//    override fun onCleared() {
//        super.onCleared()
//        setSuccessState(false)
//    }
//    private fun setSuccessState(value: Boolean){
//        _saveState.value = value
//    }
}

fun Instant.withDate(newDate: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Instant {
    val currentDateTime = LocalDateTime.ofInstant(this, zone)
    val newDateTime = LocalDateTime.of(newDate, currentDateTime.toLocalTime())
    return newDateTime.atZone(zone).toInstant()
}
