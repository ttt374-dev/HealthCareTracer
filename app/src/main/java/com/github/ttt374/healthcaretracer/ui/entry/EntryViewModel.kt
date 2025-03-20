package com.github.ttt374.healthcaretracer.ui.entry

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.Item
import com.github.ttt374.healthcaretracer.data.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class EntryViewModel @Inject constructor (private val itemRepository: ItemRepository): ViewModel() {
    var entryUiState by mutableStateOf(EntryUiState())
        private set

//    private val _uiState = MutableStateFlow(EntryUiState())
//    val uiState = _uiState.asStateFlow()

//    fun addNewEntryByText(text: String, measuredAt: Instant) {
//        val item = parseHealthData(text)?.copy(measuredAt = measuredAt)
//        if (item == null){
//            Log.e("text parse error", "input error: $text")
//            _uiState.update { EntryUiState(isSuccess = false) }
//        } else {
//            viewModelScope.launch {
//                itemRepository.insertItem(item)
//                _uiState.update { EntryUiState(isSuccess = true)}
//            }
//        }
//    }

//    private fun parseHealthData(input: String): Item? {
//        val parts = input.trim().split("\\s+".toRegex()) // 空白で分割
//        if (parts.size != 3) return null // 要素数が 3 つでなければ無効
//
//        val (high, low, pulse) = parts.mapNotNull { it.toIntOrNull() }
//        if (parts.size != 3) return null // 変換できなかったら無効
//
//        // バリデーション (常識的な範囲を設定)
//        if (high !in 50..250 || low !in 30..150 || pulse !in 30..300) return null
//
//        return Item(bpHigh = high, bpLow = low, pulse = pulse)
//    }
}

//data class EntryUiState(
//    val inputText: String = "",
//    val errorMessage: String? = null,
//    val isSuccess: Boolean = false
//)