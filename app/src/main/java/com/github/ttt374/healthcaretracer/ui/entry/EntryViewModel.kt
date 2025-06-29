package com.github.ttt374.healthcaretracer.ui.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.repository.Config
import com.github.ttt374.healthcaretracer.data.repository.ConfigRepository
import com.github.ttt374.healthcaretracer.data.repository.ItemRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject


@HiltViewModel
class EntryViewModel @Inject constructor (savedStateHandle: SavedStateHandle, configRepository: ConfigRepository): ViewModel() {
    private val dateString: String? = savedStateHandle["date"]
    private val date: LocalDate = dateString?.let { LocalDate.parse(it)} ?: LocalDate.now()
    val config = configRepository.dataFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Config())

    private val _itemUiState = MutableStateFlow(ItemUiState()) // MutableStateFlow に変更
    val itemUiState: StateFlow<ItemUiState> get() = _itemUiState // StateFlow として公開

    init {
        viewModelScope.launch {
            config.first().let { conf ->
                _itemUiState.value = ItemUiState(measuredAt = Instant.now().withDate(date, conf.zoneId))
            }
        }
    }
    fun updateItemUiState(uiState: ItemUiState) {
        _itemUiState.value = uiState
    }
}

fun Instant.withDate(newDate: LocalDate, zone: ZoneId): Instant {
    val currentDateTime = LocalDateTime.ofInstant(this, zone)
    val newDateTime = LocalDateTime.of(newDate, currentDateTime.toLocalTime())
    return newDateTime.atZone(zone).toInstant()
}
//fun Instant.toLocalTime(zoneId: ZoneId = ZoneId.systemDefault()): LocalTime {
//    return Instant.parse(this.toString())
//        .atZone(zoneId)
//        .toLocalTime()
//}