package com.github.ttt374.healthcaretracer.ui.entry

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.Item
import com.github.ttt374.healthcaretracer.data.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor (savedStateHandle: SavedStateHandle, private val itemRepository: ItemRepository): ViewModel() {
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    private val itemId: Long = checkNotNull(savedStateHandle["itemId"])
    init {
        viewModelScope.launch {
            itemUiState = itemRepository.getItemFlow(itemId)
                .filterNotNull()
                .first()
                .toItemUiState(true)
        }
    }
    fun updateUiState(uiState: ItemUiState) {
        itemUiState =
            ItemUiState(bpHigh = uiState.bpHigh, bpLow = uiState.bpLow, pulse = uiState.pulse,
                isValid = validateInput(uiState))
    }
    fun updateItem(){
        if (itemUiState.isValid){
            viewModelScope.launch {
                itemRepository.updateItem(itemUiState.toItem())
                itemUiState = itemUiState.copy(isSuccess = true)
            }
        }
    }
    private fun validateInput(uiState: ItemUiState): Boolean {
        return with(uiState) {
            true
            //name.isNotBlank() && price.isNotBlank() && quantity.isNotBlank()
        }
    }
}
data class ItemUiState (
    val id: Long? = null,
    val bpHigh: String = "",
    val bpLow: String = "",
    val pulse: String = "",
    val measuredAt: Instant = Instant.now(),

    val isValid: Boolean = false,
    val isSuccess: Boolean = false
){
    fun toItem(): Item{
        return Item(id = id ?: 0, bpHigh = bpHigh.toInt(), bpLow = bpLow.toInt(), pulse = pulse.toInt(), measuredAt = measuredAt)
    }
}
fun Item.toItemUiState(isValid: Boolean = false): ItemUiState {
    return ItemUiState(this.id, this.bpHigh.toString(), this.bpLow.toString(), this.pulse.toString(), this.measuredAt, isValid)
}