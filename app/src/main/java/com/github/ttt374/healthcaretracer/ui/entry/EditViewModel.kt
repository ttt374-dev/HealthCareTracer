package com.github.ttt374.healthcaretracer.ui.entry

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.ttt374.healthcaretracer.data.BloodPressure
import com.github.ttt374.healthcaretracer.data.Item
import com.github.ttt374.healthcaretracer.data.ItemRepository
import com.github.ttt374.healthcaretracer.data.MAX_BP
import com.github.ttt374.healthcaretracer.data.MAX_PULSE
import com.github.ttt374.healthcaretracer.data.MIN_BP
import com.github.ttt374.healthcaretracer.data.MIN_PULSE
import com.github.ttt374.healthcaretracer.usecase.ExportDataUseCase
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.TimeZone
import javax.inject.Inject
import kotlin.system.exitProcess

@HiltViewModel
class EditViewModel @Inject constructor (val exportDataUseCase: ExportDataUseCase, savedStateHandle: SavedStateHandle, private val itemRepository: ItemRepository): ViewModel() {
    private val itemId: Long? = savedStateHandle["itemId"] // TODO: error check
    private val dateString: String? = savedStateHandle["date"]
    private val date: LocalDate = dateString?.let { LocalDate.parse(it)} ?: LocalDate.now()

    private val _itemUiState = MutableStateFlow(ItemUiState()) // MutableStateFlow に変更
    val itemUiState: StateFlow<ItemUiState> get() = _itemUiState // StateFlow として公開
    private val _saveState = MutableStateFlow(false)
    val saveState: StateFlow<Boolean> get() = _saveState

    val locationList = itemRepository.getAllLocationsFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        if (itemId != null){
            viewModelScope.launch {
                itemRepository.getItemFlow(itemId)
                    .filterNotNull()
                    .map { it.toItemUiState().copy(id = itemId) }
                    .collect { _itemUiState.value = it } // `_itemUiState` を更新
            }
        } else {
            _itemUiState.value = ItemUiState(measuredAt = Instant.now().withDate(date))
        }
    }
    fun updateItemUiState(uiState: ItemUiState) {
        Log.d("itemuistate update", uiState.toString())
        _itemUiState.value = uiState
    }
    fun upsertItem(){
        viewModelScope.launch {
            itemRepository.upsertItem(itemUiState.value.toItem())
            exportDataUseCase("items-autosave.csv")
            setSuccessState(true)
        }

    }
    fun deleteItem(){
        viewModelScope.launch {
            itemRepository.deleteItem(itemUiState.value.toItem())
            setSuccessState(true)
        }
    }
    override fun onCleared() {
        super.onCleared()
        setSuccessState(false)
    }
    private fun setSuccessState(value: Boolean){
        //_itemUiState.value = itemUiState.value.copy(isSuccess = value)
        _saveState.value = value
    }
}
data class ItemUiState (
    val id: Long? = null,
    val bpUpper: String = "",
    val bpLower: String = "",
    val pulse: String = "",
    val bodyWeight: String = "",
    val location: String = "",
    val memo: String = "",
    val measuredAt: Instant = Instant.now(),

    //val isSuccess: Boolean = false,
){
    fun toItem() = Item(
        //id = (this.editMode as? EditMode.Edit)?.itemId ?: 0, // editModeがEditならidを更新、それ以外は0,
        id = id?: 0,
        bp = BloodPressure(bpUpper.toIntOrNull() ?: 0, bpLower.toIntOrNull() ?: 0),
        //bpHigh = bpHigh.toIntOrNull() ?: 0,
        //bpLow = bpLow.toIntOrNull() ?:0,
        pulse = pulse.toIntOrNull() ?: 0,
        bodyWeight = bodyWeight.toFloatOrNull() ?: 0F,
        memo = memo, location = location, measuredAt = measuredAt)
//    fun isBpUpperValid(): Boolean {
//        return (bpUpper.toIntOrNull() ?: 0) in MIN_BP..MAX_BP
//    }
//    fun isBpLowerValid(): Boolean {
//        return (bpLower.toIntOrNull() ?: 0) in MIN_BP..MAX_BP
//    }
//    fun isPulseValid(): Boolean {
//        return (pulse.toIntOrNull() ?: 0) in MIN_PULSE..MAX_PULSE
//    }
    fun isValid(): Boolean {
        val bpUpperInt = bpUpper.toIntOrNull() ?: 0
        val bpLowerInt = bpLower.toIntOrNull() ?: 0
        val pulseInt = pulse.toIntOrNull() ?: 0

        return bpUpperInt in MIN_BP..MAX_BP &&
                bpLowerInt in MIN_BP..MAX_BP &&
                pulseInt in MIN_PULSE..MAX_PULSE &&
                bpUpperInt > bpLowerInt
    }
}
fun Item.toItemUiState(): ItemUiState {
    return ItemUiState(  this.id,
        this.bp.upper.toString(), this.bp.lower.toString(), this.pulse.toString(),
        //if (this.bodyWeight == 0.0F) "" else this.bodyWeight.toString(),
        this.bodyWeight.takeIf { it != 0.0F }?.toString().orEmpty(),
        this.location, this.memo, this.measuredAt)
}

sealed class EditMode {
    data object Entry : EditMode()
    data object Edit: EditMode()
    //data class Edit(val itemId: Long) : EditMode()
}

fun <T : Comparable<T>> Pair<T, T>.contains(value: T): Boolean {
     val (min, max) = if (first <= second) this else second to first
    return value in min..max
}

fun Instant.withDate(newDate: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Instant {
    val currentDateTime = LocalDateTime.ofInstant(this, zone)
    val newDateTime = LocalDateTime.of(newDate, currentDateTime.toLocalTime())
    return newDateTime.atZone(zone).toInstant()
}
