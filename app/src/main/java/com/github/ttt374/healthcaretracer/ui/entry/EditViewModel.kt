package com.github.ttt374.healthcaretracer.ui.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.Item
import com.github.ttt374.healthcaretracer.data.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor (savedStateHandle: SavedStateHandle, private val itemRepository: ItemRepository): ViewModel() {
    //private val itemId: Long = checkNotNull(savedStateHandle["itemId"])
    private val itemId: Long? = savedStateHandle["itemId"] // TODO: error check

    private var _itemUiState = MutableStateFlow(ItemUiState()) // MutableStateFlow に変更
    val itemUiState: StateFlow<ItemUiState> = _itemUiState // StateFlow として公開
    val locationList = itemRepository.getAllLocationsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        itemId?.let { id ->
            viewModelScope.launch {
                itemRepository.getItemFlow(id)
                    .filterNotNull()
                    .map { it.toItemUiState(editMode = EditMode.Edit(id)) }
                    .collect { _itemUiState.value = it } // `_itemUiState` を更新
            }
        }
    }
    fun updateItemUiState(uiState: ItemUiState) {
        _itemUiState.update { uiState }
    }
    fun upsertItem(){
        if (itemUiState.value.isValid){
            viewModelScope.launch {
                itemRepository.upsertItem(itemUiState.value.toItem())
                _itemUiState.update { itemUiState.value.copy(isSuccess = true) }
            }
        }
    }
//    override fun onCleared() {
//        super.onCleared()
//        _itemUiState.update { itemUiState.value.copy(isSuccess = false)  }
//    }
}
data class ItemUiState (
    val editMode: EditMode = EditMode.Entry,
    val rawInput: String = "",
    val bpHigh: String = "",
    val bpLow: String = "",
    val pulse: String = "",
    val location: String = "",
    val measuredAt: Instant = Instant.now(),

    val isSuccess: Boolean = false,
){
    val isValid: Boolean
        get(){

                val item = toItem()
                return item.bpHigh > item.bpLow && item.bpHigh > 50 && item.bpLow > 50 && item.pulse > 40


        }

    fun toItem(): Item {
        return when(this.editMode){
            is EditMode.Edit ->
                Item(id = this.editMode.itemId ,
                    bpHigh = bpHigh.toIntOrNull() ?: 0,
                    bpLow = bpLow.toIntOrNull() ?:0,
                    pulse = pulse.toIntOrNull() ?: 0,
                    location = location, measuredAt = measuredAt)
            else ->
                parseRawInput(rawInput)
        }
    }
    private fun parseRawInput(rawInput: String): Item {
        val values = rawInput.split(" ").mapNotNull { it.toIntOrNull() }
        val item = if (values.size == 3) {
            Item(
                bpHigh = values[0],
                bpLow = values[1],
                pulse = values[2],
            )
        } else Item()
        return item.copy(location = this.location, measuredAt = this.measuredAt)
    }
}
fun Item.toItemUiState(editMode: EditMode = EditMode.Entry): ItemUiState {
    return ItemUiState(editMode,  "", this.bpHigh.toString(), this.bpLow.toString(), this.pulse.toString(),
        this.location, this.measuredAt, false)
}

sealed class EditMode {
    data object Entry : EditMode()
    data class Edit(val itemId: Long) : EditMode()
}