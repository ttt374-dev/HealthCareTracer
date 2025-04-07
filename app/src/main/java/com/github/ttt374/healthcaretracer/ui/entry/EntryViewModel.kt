package com.github.ttt374.healthcaretracer.ui.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.github.ttt374.healthcaretracer.data.item.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject


@HiltViewModel
class EntryViewModel @Inject constructor (savedStateHandle: SavedStateHandle, private val itemRepository: ItemRepository): ViewModel() {
    private val dateString: String? = savedStateHandle["date"]
    private val date: LocalDate = dateString?.let { LocalDate.parse(it)} ?: LocalDate.now()

    private val _itemUiState = MutableStateFlow(ItemUiState()) // MutableStateFlow に変更
    val itemUiState: StateFlow<ItemUiState> get() = _itemUiState // StateFlow として公開

    init {
        _itemUiState.value = ItemUiState(measuredAt = Instant.now().withDate(date))
    }
    fun updateItemUiState(uiState: ItemUiState) {
        _itemUiState.value = uiState
    }
}

fun Instant.withDate(newDate: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Instant {
    val currentDateTime = LocalDateTime.ofInstant(this, zone)
    val newDateTime = LocalDateTime.of(newDate, currentDateTime.toLocalTime())
    return newDateTime.atZone(zone).toInstant()
}
fun Instant.toLocalTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalTime {
    return Instant.parse(this.toString())
        .atZone(zoneId)
        .toLocalTime()
}