package com.github.ttt374.healthcaretracer.ui.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.data.item.MIN_BP
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.ConfirmDialog
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.DatePickerDialog
import com.github.ttt374.healthcaretracer.ui.common.SelectableTextField
import com.github.ttt374.healthcaretracer.ui.common.TimePickerDialog
import com.github.ttt374.healthcaretracer.ui.common.rememberDialogState
import com.github.ttt374.healthcaretracer.ui.common.rememberItemDialogState
import java.time.ZoneId
import java.time.format.DateTimeFormatter

const val MIN_PULSE = 40

@Composable
fun EditScreen(editViewModel: EditViewModel = hiltViewModel(), itemViewModel: ItemViewModel = hiltViewModel(), appNavigator: AppNavigator) {
    val itemUiState by editViewModel.itemUiState.collectAsState()
    val locationList by itemViewModel.locationList.collectAsState()
    val saveState by itemViewModel.saveState.collectAsState()

    LaunchedEffect(saveState) {
        if (saveState) {
            appNavigator.navigateBack()
        }
    }
    Scaffold(topBar = { CustomTopAppBar("Edit", navigateBack = appNavigator::navigateBack) }){ innerPadding ->
        Column (modifier = Modifier.padding(innerPadding)) {
            ItemEntryContent(editMode = EditMode.Edit,
                itemUiState = itemUiState,
                updateItemUiState = editViewModel::updateItemUiState,
                locationList = locationList,
                onPost = { itemViewModel.upsertItem(itemUiState.toItem())},
                onDelete = { itemViewModel.deleteItem(itemUiState.toItem())})
        }
    }
}

sealed class EditMode {
    data object Entry : EditMode()
    data object Edit: EditMode()
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
    val datePickerDialogState = rememberDialogState(false)
    val timePickerDialogState = rememberDialogState(false)
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

    if (datePickerDialogState.isOpen){
        DatePickerDialog(itemUiState.measuredAt,
                { updateItemUiState(itemUiState.copy(measuredAt = it)) },
                 { datePickerDialogState.close() })
    }
    if (timePickerDialogState.isOpen){
       TimePickerDialog(itemUiState.measuredAt,
           { updateItemUiState(itemUiState.copy(measuredAt = it))}, { timePickerDialogState.close()})
    }

    // focus requesters
    val bpUpperFocusRequester = remember { FocusRequester() }
    val bpLowerFocusRequester = remember { FocusRequester() }
    val pulseFocusRequester = remember { FocusRequester() }
    val bodyWeightFocusRequester = remember { FocusRequester() }
    val bodyTemperatureFocusRequester = remember { FocusRequester() }
    //val focusManager = remember { FocusManager(listOf(bpUpperFocusRequester, bpLowerFocusRequester, pulseFocusRequester)) }

    // dialog
    val deleteDialogState = rememberItemDialogState()
    if (deleteDialogState.isOpen){
        ConfirmDialog(title = { Text("Are you sure to delete ?") },
            text = { Text("") },
            onConfirm = onDelete,
            closeDialog = { deleteDialogState.close()})
    }
    // 画面を開いたときに bpHigh にフォーカスを移動（新規エントリ時のみ）
    LaunchedEffect(editMode) {
        if (editMode is EditMode.Entry) {
            bpUpperFocusRequester.requestFocus()
        }
    }
    val numberKeyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
    val decimalKeyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)

    Column (modifier=modifier){
        InputFieldRow("Measured At"){
            Row (horizontalArrangement = Arrangement.spacedBy(16.dp)){
                OutlinedButton(onClick = { datePickerDialogState.open() }){
                    Text(dateFormatter.format(itemUiState.measuredAt))
                }
                OutlinedButton(onClick = { timePickerDialogState.open() }){
                    Text(timeFormatter.format(itemUiState.measuredAt))
                }
            }
        }
        InputFieldRow("Bp Upper"){
            TextField(itemUiState.bpUpper,
                onValueChange = {
                    updateItemUiState(itemUiState.copy(bpUpper = it))
                    //focusManager.shiftFocusIf { (it.toIntOrNull() ?: 0) > MIN_BP }
                    if ((it.toIntOrNull() ?: 0) > MIN_BP) bpLowerFocusRequester.requestFocus()
                },
                label = { Text("Bp Upper")},
                keyboardOptions = numberKeyboardOptions,
                modifier = modifier.focusRequester(bpUpperFocusRequester)
            )
        }
        InputFieldRow("Bp Lower") {
            TextField(itemUiState.bpLower,
                onValueChange = {
                    updateItemUiState(itemUiState.copy(bpLower = it))
                    if ((it.toIntOrNull() ?: 0) > MIN_BP) pulseFocusRequester.requestFocus()
                    //focusManager.shiftFocusIf { (it.toIntOrNull() ?: 0) > MIN_BP }
                },
                label = { Text("Bp Lower")},
                keyboardOptions = numberKeyboardOptions,
                modifier = modifier.focusRequester(bpLowerFocusRequester)
            )
        }
        InputFieldRow("Pulse") {
            TextField(itemUiState.pulse,
                onValueChange = {
                    updateItemUiState(itemUiState.copy(pulse = it))
                    if ((it.toIntOrNull() ?: 0) > MIN_PULSE) bodyWeightFocusRequester.requestFocus()
                },
                label = { Text("Pulse")},
                keyboardOptions = numberKeyboardOptions,
                modifier = modifier.focusRequester(pulseFocusRequester)
            )
        }
        InputFieldRow("") {
            Button(enabled = itemUiState.isValid(), onClick = { onPost() }){
                Text("OK")
            }
        }
        InputFieldRow("Body Weight") {
            TextField(itemUiState.bodyWeight,
                onValueChange = {
                    updateItemUiState(itemUiState.copy(bodyWeight = it))
                    if ((it.toIntOrNull() ?: 0) > MIN_PULSE) bodyTemperatureFocusRequester.requestFocus()
                },
                keyboardOptions = decimalKeyboardOptions,
                modifier = modifier.focusRequester(bodyWeightFocusRequester),
                label = { Text("Body Weight")})
        }
        InputFieldRow("Body Temperature") {
            TextField(itemUiState.bodyTemperature,
                onValueChange = { updateItemUiState(itemUiState.copy(bodyTemperature = it))},
                keyboardOptions = decimalKeyboardOptions,
                modifier = modifier.focusRequester(bodyTemperatureFocusRequester),
                label = { Text("Body Temperature")})
        }
        InputFieldRow("Location") {
            SelectableTextField(itemUiState.location, locationList,
                onValueChange = { updateItemUiState(itemUiState.copy(location = it)) },
            )
        }
        InputFieldRow("Memo") {
            TextField(itemUiState.memo, onValueChange = { updateItemUiState(itemUiState.copy(memo = it))},
                label = { Text("Memo")})
        }
        InputFieldRow("") {
            if (editMode is EditMode.Edit){
                Button(onClick = { deleteDialogState.open(itemUiState.toItem()) }){
                    Text("Delete")
                }
            }
        }
    }
}
@Composable
fun InputFieldRow(label: String, inputField: @Composable () -> Unit){
    Row (horizontalArrangement = Arrangement.spacedBy(16.dp),) {
        Text(label, modifier = Modifier.weight(1f))
        inputField()
    }
}
//class FocusManager (private val focusRequesters: List<FocusRequester>, initialIndex: Int = 0) {
//    private var currentFocusIndex: Int = initialIndex
//
//    private fun shiftFocus(){
//        if (currentFocusIndex < focusRequesters.size - 1) {
//            currentFocusIndex++ // 次のフィールドに移動
//        } else {
//            currentFocusIndex = 0 // もし最後のフィールドなら最初に戻る
//        }
//        focusRequesters[currentFocusIndex].requestFocus() // 次のフィールドにフォーカスを移す
//    }
//    fun shiftFocusIf(condition: () -> Boolean){
//        if (condition()) shiftFocus()
//    }
//}