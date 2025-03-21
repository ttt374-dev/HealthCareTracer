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
    //private val itemId: Long = checkNotNull(savedStateHandle["itemId"])
    private val itemId: Long? = savedStateHandle["itemId"]
    init {
        itemId?.let { id ->
            viewModelScope.launch {
                itemUiState = itemRepository.getItemFlow(id).filterNotNull().first().toItemUiState(editMode=EditMode.Edit)
            }
        }
    }
    fun updateItemUiState(uiState: ItemUiState) {
        itemUiState = uiState
    }
    fun upsertItem(){
        if (itemUiState.isValid){
            viewModelScope.launch {
                itemRepository.upsertItem(itemUiState.toItem())
                itemUiState = itemUiState.copy(isSuccess = true)
            }
        }
    }
}
data class ItemUiState (
    val editMode: EditMode = EditMode.Entry,
    val id: Long? = null,
    val rawInput: String = "",
    val bpHigh: String = "",
    val bpLow: String = "",
    val pulse: String = "",
    val measuredAt: Instant = Instant.now(),

    //val isEditing: Boolean = false,
    val isSuccess: Boolean = false,

){
    val isValid: Boolean
        get() = when(editMode){
            is EditMode.Edit ->
                bpHigh.isNotBlank() && bpLow.isNotBlank() && pulse.isNotBlank()
            else ->
                rawInput.split(" ").mapNotNull { it.toIntOrNull() }.size == 3
        }

    fun toItem(): Item {
        return when(this.editMode){
            is EditMode.Edit ->
                Item(id = id ?: 0, bpHigh = bpHigh.toInt(), bpLow = bpLow.toInt(), pulse = pulse.toInt(), measuredAt = measuredAt)
            else ->
                parseRawInput(rawInput)
        }
    }
    private fun parseRawInput(rawInput: String): Item {
        val values = rawInput.split(" ").mapNotNull { it.toIntOrNull() }
        return if (values.size == 3) {
            Item(
                bpHigh = values[0],
                bpLow = values[1],
                pulse = values[2],
                measuredAt = measuredAt
            )
        } else Item()
    }
}
fun Item.toItemUiState(editMode: EditMode = EditMode.Entry): ItemUiState {
    return ItemUiState(editMode, this.id, "", this.bpHigh.toString(), this.bpLow.toString(), this.pulse.toString(), this.measuredAt, false)
}
//data class EntryUiState (
//    val itemUiState: ItemUiState = ItemUiState(),
//
//    //val isValid: Boolean = false,
//    val isSuccess: Boolean = false
//)
sealed class EditMode {
    data object Entry : EditMode()
    data object Edit: EditMode()
    //data class Edit(val itemId: Long) : EditMode()
}