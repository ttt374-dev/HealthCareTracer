package com.github.ttt374.healthcaretracer.ui.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.github.ttt374.healthcaretracer.data.item.MIN_BP
import com.github.ttt374.healthcaretracer.ui.common.ConfirmDialog
import com.github.ttt374.healthcaretracer.ui.common.DatePickerDialog
import com.github.ttt374.healthcaretracer.ui.common.SelectableTextField
import com.github.ttt374.healthcaretracer.ui.common.TimePickerDialog
import com.github.ttt374.healthcaretracer.ui.common.rememberDialogState
import com.github.ttt374.healthcaretracer.ui.common.rememberItemDialogState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter


sealed class EditMode {
    data object Entry : EditMode()
    data object Edit: EditMode()
}

enum class FocusField { BpUpper, BpLower, Pulse, BodyTemperature, BodyWeight,  }
class FocusRequestMap(private val map: Map<FocusField, FocusRequester>) {

    fun requestFirst() {
        map[FocusField.entries.first()]?.requestFocus()
    }

    fun requestNext(current: FocusField) {
        val nextIndex = current.ordinal + 1
        if (nextIndex < FocusField.entries.size) {
            val next = FocusField.entries[nextIndex]
            map[next]?.requestFocus()
        }
    }
    operator fun get(field: FocusField): FocusRequester = map.getValue(field)
}
@Composable
fun rememberFocusRequestMap(): FocusRequestMap {
    val map = remember {
        FocusField.entries.associateWith { FocusRequester() }
    }
    return remember { FocusRequestMap(map) }
}


@Composable
fun ItemEntryContent(modifier: Modifier = Modifier,
                     editMode: EditMode = EditMode.Entry,
                     itemUiState: ItemUiState,
                     onDelete: () -> Unit = {},
                     onPost: () -> Unit = {},
                     updateItemUiState: (ItemUiState) -> Unit = {},
                     locationList: List<String> = emptyList(),
){
    val focusMap = rememberFocusRequestMap()

    // 画面を開いたときに bpHigh にフォーカスを移動（新規エントリ時のみ）
    LaunchedEffect(editMode) {
        if (editMode is EditMode.Entry) {
            focusMap.requestFirst()
        }
    }
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button (onClick = onDelete ){
                Text("Delete")
            }
            Button (onClick = onPost ){
                Text("OK")
            }
        }
        Row {
            LazyColumn (modifier=modifier){
                item {
                    InputFieldRow("Measured At"){
                        Row (horizontalArrangement = Arrangement.spacedBy(16.dp)){
                            DateAndTimePickers(itemUiState,
                                onDateSelected = { updateItemUiState(itemUiState.copy(measuredAt = it)) },
                                onTimeSelected = { updateItemUiState(itemUiState.copy(measuredAt = it)) }
                            )
                        }
                    }
                }
                item {
                    VitalInputFields(itemUiState, updateItemUiState, focusMap)

                    InputFieldRow("Location") {
                        SelectableTextField(itemUiState.location, locationList,
                            onValueChange = { updateItemUiState(itemUiState.copy(location = it)) },
                        )
                    }
                    InputFieldRow("Memo") {
                        TextField(itemUiState.memo, onValueChange = { updateItemUiState(itemUiState.copy(memo = it))})
                    }
    //                InputFieldRow("") {
    //                    if (editMode is EditMode.Edit){
    //                        Button(onClick = { deleteDialogState.open(itemUiState.toItem()) }){
    //                            Text("Delete")
    //                        }
    //                    }
    //                }
                    Box ( Modifier.padding(32.dp))  // 下の余白
                }
            }
        }
        // IMEのすぐ上に固定
        //BottomActionButton(itemUiState, editMode, onPost, onDelete)
//        Button(onClick = onPost,
//            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter)) {
//            Text("OK")
//        }
    }
}
@Composable
fun VitalInputFields(itemUiState: ItemUiState, updateItemUiState: (ItemUiState) -> Unit, focusMap: FocusRequestMap){
    val numberKeyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
    val decimalKeyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)

    InputFieldRow("Bp Upper"){
        TextField(itemUiState.bpUpper,
            onValueChange = {
                updateItemUiState(itemUiState.copy(bpUpper = it))
                if ((it.toIntOrNull() ?: 0) > MIN_BP) focusMap.requestNext(FocusField.BpUpper)
            },
            suffix = { Text("mmHg") },
            keyboardOptions = numberKeyboardOptions,
            modifier = Modifier.focusRequester(focusMap[FocusField.BpUpper])
        )
    }
    InputFieldRow("Bp Lower") {
        TextField(itemUiState.bpLower,
            onValueChange = {
                updateItemUiState(itemUiState.copy(bpLower = it))
                if ((it.toIntOrNull() ?: 0) > MIN_BP) focusMap.requestNext(FocusField.BpLower)
                //focusManager.shiftFocusIf { (it.toIntOrNull() ?: 0) > MIN_BP }
            },
            suffix = { Text("mmHg") },
            keyboardOptions = numberKeyboardOptions,
            //modifier = modifier.focusRequester(bpLowerFocusRequester)
            modifier = Modifier.focusRequester(focusMap[FocusField.BpLower])
        )
    }
    InputFieldRow("Pulse") {
        TextField(itemUiState.pulse,
            onValueChange = {
                updateItemUiState(itemUiState.copy(pulse = it))
                if ((it.toIntOrNull() ?: 0) > MIN_PULSE) focusMap.requestNext(FocusField.Pulse)
            },
            suffix = { Text("bpm") },
            keyboardOptions = numberKeyboardOptions,
            modifier = Modifier.focusRequester(focusMap[FocusField.Pulse])
        )
    }
    InputFieldRow("Body Temperature") {
        TextField(itemUiState.bodyTemperature,
            onValueChange = {
                updateItemUiState(itemUiState.copy(bodyTemperature = it))
                if ((it.toIntOrNull() ?: 0) > 20) focusMap.requestNext(FocusField.BodyTemperature)
            },
            keyboardOptions = decimalKeyboardOptions,
            modifier = Modifier.focusRequester(focusMap[FocusField.BodyTemperature]),
            suffix = { Text("℃") }
        )
    }
    InputFieldRow("Body Weight") {
        TextField(itemUiState.bodyWeight,
            onValueChange = {
                updateItemUiState(itemUiState.copy(bodyWeight = it))
                if ((it.toIntOrNull() ?: 0) > MIN_PULSE) focusMap.requestNext(FocusField.BodyWeight)
            },
            keyboardOptions = decimalKeyboardOptions,
            modifier = Modifier.focusRequester(focusMap[FocusField.BodyWeight]),
            suffix = { Text("Kg") })
    }
}
@Composable
private fun BottomActionButton(itemUiState: ItemUiState, editMode: EditMode = EditMode.Entry, onPost: () -> Unit, onDelete: () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
//        if (editMode is EditMode.Edit){
//            // dialog
//            val deleteDialogState = rememberItemDialogState()
//            if (deleteDialogState.isOpen){
//                ConfirmDialog(title = { Text("Are you sure to delete ?") },
//                    text = { Text("") },
//                    onConfirm = onDelete,
//                    closeDialog = { deleteDialogState.close()})
//            }
//            Button(onClick = { deleteDialogState.open(itemUiState.toItem()) }){
//                    Text("Delete")
//                }
//            }
//        }
        Button(
            onClick = onPost,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text("OK")
        }
    }
}

@Composable
fun InputFieldRow(label: String, inputField: @Composable () -> Unit){
    Row (horizontalArrangement = Arrangement.spacedBy(16.dp),) {
        Text(label, modifier = Modifier.weight(1f))
        Box(Modifier.weight(2f)){
            inputField()
        }
    }
}
@Composable
private fun DateAndTimePickers(
    itemUiState: ItemUiState,
    onDateSelected: (Instant) -> Unit,
    onTimeSelected: (Instant) -> Unit
) {
    val datePickerDialogState = rememberDialogState(false)
    val timePickerDialogState = rememberDialogState(false)

    if (datePickerDialogState.isOpen) {
        DatePickerDialog(itemUiState.measuredAt, onDateSelected, datePickerDialogState::close)
    }
    if (timePickerDialogState.isOpen) {
        TimePickerDialog(itemUiState.measuredAt, onTimeSelected, timePickerDialogState::close)
    }

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault()) }
        val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm a").withZone(ZoneId.systemDefault()) }

        OutlinedButton(onClick = { datePickerDialogState.open() }) {
            Text(dateFormatter.format(itemUiState.measuredAt))
        }
        OutlinedButton(onClick = { timePickerDialogState.open() }) {
            Text(timeFormatter.format(itemUiState.measuredAt))
        }
    }
}
