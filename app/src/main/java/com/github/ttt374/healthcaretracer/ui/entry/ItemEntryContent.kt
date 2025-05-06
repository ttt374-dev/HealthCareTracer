package com.github.ttt374.healthcaretracer.ui.entry

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.item.Item
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
    data class Edit(val item: Item): EditMode()
}


@Composable
fun ItemEntryContent(//modifier: Modifier = Modifier,
                     editMode: EditMode = EditMode.Entry,
                     itemUiState: ItemUiState = ItemUiState(),
                     onPost: () -> Unit = {},
                     onCancel: () -> Unit = {},
                     onDelete: () -> Unit = {},
                     updateItemUiState: (ItemUiState) -> Unit = {},
                     locationList: List<String> = emptyList(),
                     zoneId: ZoneId,
){
    val focusMap = rememberFocusRequestMap()

    // 画面を開いたときに bpHigh にフォーカスを移動（新規エントリ時のみ）
    LaunchedEffect(editMode) {
        if (editMode is EditMode.Entry) {
            focusMap.requestFirst()
        }
    }
//    val bringIntoViewRequester = remember { BringIntoViewRequester() }
//    LaunchedEffect(Unit) {
//        snapshotFlow { focusState.isFocused }
//            .filter { it }
//            .collect {
//                bringIntoViewRequester.bringIntoView()
//            }
//    }

    Column(Modifier.fillMaxSize().imePadding()) {
        LazyColumn (modifier=Modifier.weight(1f)){
            item {
                InputFieldRow(stringResource(R.string.measuredAt)){
                    Row (horizontalArrangement = Arrangement.spacedBy(16.dp)){
                        DateAndTimePickers(itemUiState,
                            onDateSelected = { updateItemUiState(itemUiState.copy(measuredAt = it)) },
                            onTimeSelected = { updateItemUiState(itemUiState.copy(measuredAt = it)) },
                            zoneId
                        )
                    }
                }
            }
            item {
                VitalInputFields(itemUiState, updateItemUiState, focusMap)

//                InputFieldRow(stringResource(R.string)) {
//                    SelectableTextField(itemUiState.location, locationList,
//                        onValueChange = { updateItemUiState(itemUiState.copy(location = it)) },
//                    )
//                }
                InputFieldRow(stringResource(R.string.memo)) {
                    TextField(itemUiState.memo, onValueChange = { updateItemUiState(itemUiState.copy(memo = it))})
                }
//                InputFieldRow("") {
//                    if (editMode is EditMode.Edit){
//                        Button(onClick = { deleteDialogState.open(itemUiState.toItem()) }){
//                            Text("Delete")
//                        }
//                    }
//                }
                //Box ( Modifier.padding(32.dp))  // 下の余白
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp).navigationBarsPadding(), horizontalArrangement = Arrangement.End) {
            ActionButtons(editMode, onPost, onCancel, onDelete, itemUiState.isValid)
        }
    }
}
@Composable
fun VitalInputFields(itemUiState: ItemUiState, updateItemUiState: (ItemUiState) -> Unit, focusMap: FocusRequestMap){
    val numberKeyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
    val decimalKeyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)

    InputFieldRow(stringResource(R.string.bpUpper)){
        TextField(itemUiState.bpUpper,
            onValueChange = {
                updateItemUiState(itemUiState.copy(bpUpper = it))
                focusMap.requestNextIf(FocusField.BpUpper){ it.isValidBp() }
                //if ((it.toIntOrNull() ?: 0) > MIN_BP) focusMap.requestNext(FocusField.BpUpper)
            },
            suffix = { Text("mmHg") },
            keyboardOptions = numberKeyboardOptions,
            modifier = Modifier.focusRequester(focusMap[FocusField.BpUpper])
        )
    }
    InputFieldRow(stringResource(R.string.bpLower)) {
        TextField(itemUiState.bpLower,
            onValueChange = {
                updateItemUiState(itemUiState.copy(bpLower = it))
                focusMap.requestNextIf(FocusField.BpLower){ it.isValidBp() }
                //if ((it.toIntOrNull() ?: 0) > MIN_BP) focusMap.requestNext(FocusField.BpLower)
                //focusManager.shiftFocusIf { (it.toIntOrNull() ?: 0) > MIN_BP }
            },
            suffix = { Text("mmHg") },
            keyboardOptions = numberKeyboardOptions,
            modifier = Modifier.focusRequester(focusMap[FocusField.BpLower])
        )
    }
    InputFieldRow(stringResource(R.string.pulse)) {
        TextField(itemUiState.pulse,
            onValueChange = {
                updateItemUiState(itemUiState.copy(pulse = it))
                focusMap.requestNextIf(FocusField.Pulse) { it.isValidPulse()}
            },
            suffix = { Text("bpm") },
            keyboardOptions = numberKeyboardOptions,
            modifier = Modifier.focusRequester(focusMap[FocusField.Pulse])
        )
    }
    InputFieldRow(stringResource(R.string.bodyTemperature)) {
        TextField(itemUiState.bodyTemperature,
            onValueChange = {
                updateItemUiState(itemUiState.copy(bodyTemperature = it))
                focusMap.requestNextIf(FocusField.BodyTemperature) { it.length >= 4 && it.toDoubleOrNull() != null}
                //focusMap.requestNextIf(FocusField.BodyTemperature) { it.isTwoDigits()}
            },
            keyboardOptions = decimalKeyboardOptions,
            modifier = Modifier.focusRequester(focusMap[FocusField.BodyTemperature]),
            suffix = { Text("℃") }
        )
    }
    InputFieldRow(stringResource(R.string.bodyWeight)) {
        TextField(itemUiState.bodyWeight,
            onValueChange = {
                updateItemUiState(itemUiState.copy(bodyWeight = it))

            },
            keyboardOptions = decimalKeyboardOptions,
            modifier = Modifier.focusRequester(focusMap[FocusField.BodyWeight]),
            suffix = { Text("Kg") })
    }
}
@Composable
private fun ActionButtons(editMode: EditMode, onPost: () -> Unit, onCancel: () -> Unit, onDelete: () -> Unit, enablePost: Boolean = true) {
    val deleteDialogState = rememberItemDialogState()
    if (deleteDialogState.isOpen){
        ConfirmDialog(title = { Text(stringResource(R.string.msgConfirmToDelete)) },
            text = { Text("") },
            onConfirm = onDelete,
            closeDialog = { deleteDialogState.close()})
    }

    Row (modifier = Modifier.fillMaxWidth(),
        //horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically){
        if (editMode is EditMode.Edit){
            Button(onClick = { deleteDialogState.open(editMode.item) }){
                Text("Delete")
            }

        }
        Spacer(modifier = Modifier.weight(1f)) // ← 間にスペース
        Row (horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onCancel) {
                Text("Cancel")
            }
            Button(enabled = enablePost, onClick = onPost) {
                Text("OK")
            }

        }

    }
}

internal fun String.isValidBp() =
    (this.toIntOrNull() ?: 0) > MIN_BP
internal fun String.isValidPulse() =
    (this.toIntOrNull() ?: 0) > MIN_PULSE

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
    onTimeSelected: (Instant) -> Unit,
    zoneId: ZoneId = ZoneId.systemDefault()
) {
    val datePickerDialogState = rememberDialogState(false)
    val timePickerDialogState = rememberDialogState(false)

    if (datePickerDialogState.isOpen) {
        DatePickerDialog(itemUiState.measuredAt, onDateSelected, datePickerDialogState::close, zoneId)
    }
    if (timePickerDialogState.isOpen) {
        TimePickerDialog(itemUiState.measuredAt, onTimeSelected, timePickerDialogState::close, zoneId)
    }

    Row( horizontalArrangement = Arrangement.spacedBy(16.dp), modifier=Modifier.fillMaxWidth().padding(8.dp)) {
        val dateFormatter = remember(zoneId) { DateTimeFormatter.ofPattern("yyyy-M-d").withZone(zoneId) }
        val timeFormatter = remember(zoneId) { DateTimeFormatter.ofPattern("HH:mm a").withZone(zoneId) }

        Text(dateFormatter.format(itemUiState.measuredAt), modifier=Modifier.clickable { datePickerDialogState.open()})
        Text(timeFormatter.format(itemUiState.measuredAt), modifier=Modifier.clickable { timePickerDialogState.open()})
//        OutlinedButton(onClick = { datePickerDialogState.open() }) {
//            Text(dateFormatter.format(itemUiState.measuredAt))
//        }
//        OutlinedButton(onClick = { timePickerDialogState.open() }) {
//            Text(timeFormatter.format(itemUiState.measuredAt))
//        }
    }
}
