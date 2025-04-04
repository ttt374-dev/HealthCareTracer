package com.github.ttt374.healthcaretracer.ui.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.item.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditViewModel @Inject constructor (savedStateHandle: SavedStateHandle, private val itemRepository: ItemRepository): ViewModel() {
    private val itemId: Long = checkNotNull(savedStateHandle["itemId"]) // TODO: error check
     private val _itemUiState = MutableStateFlow(ItemUiState()) // MutableStateFlow に変更
    val itemUiState: StateFlow<ItemUiState> get() = _itemUiState // StateFlow として公開

    init {
        viewModelScope.launch {
            itemRepository.getItemFlow(itemId)
                .filterNotNull()
                .map { it.toItemUiState().copy(id = itemId) }
                .collect { _itemUiState.value = it } // `_itemUiState` を更新
        }
    }
    fun updateItemUiState(uiState: ItemUiState) {
        _itemUiState.value = uiState
    }
}
