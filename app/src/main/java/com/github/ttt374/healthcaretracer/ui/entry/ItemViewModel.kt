package com.github.ttt374.healthcaretracer.ui.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.data.item.ItemRepository
import com.github.ttt374.healthcaretracer.usecase.ExportDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItemViewModel @Inject constructor (val exportDataUseCase: ExportDataUseCase, private val itemRepository: ItemRepository): ViewModel() {
    val locationList = itemRepository.getAllLocationsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _saveState = MutableStateFlow(false)
    val saveState: StateFlow<Boolean> get() = _saveState

    fun upsertItem(item: Item){
        viewModelScope.launch {
            itemRepository.upsertItem(item)
            exportDataUseCase("items-autosave.csv")
            setSuccessState(true)
        }
    }
    fun deleteItem(item: Item){
        viewModelScope.launch {
            itemRepository.deleteItem(item)
            setSuccessState(true)
        }
    }
    override fun onCleared() {
        super.onCleared()
        setSuccessState(false)
    }
    private fun setSuccessState(value: Boolean){
        _saveState.value = value
    }
}