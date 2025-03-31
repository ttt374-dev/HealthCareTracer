package com.github.ttt374.healthcaretracer.ui.entry

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
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.data.MAX_BP
import com.github.ttt374.healthcaretracer.data.MAX_PULSE
import com.github.ttt374.healthcaretracer.data.MIN_BP
import com.github.ttt374.healthcaretracer.data.MIN_PULSE
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.ConfirmDialog
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.DateTimeDialog
import com.github.ttt374.healthcaretracer.ui.common.SelectableTextField
import com.github.ttt374.healthcaretracer.ui.common.rememberDialogState
import com.github.ttt374.healthcaretracer.ui.common.rememberItemDialogState
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
class FocusManager (private val focusRequesters: List<FocusRequester>, initialIndex: Int = 0) {
    private var currentFocusIndex: Int = initialIndex

    private fun shiftFocus(){
        if (currentFocusIndex < focusRequesters.size - 1) {
            currentFocusIndex++ // 次のフィールドに移動
        } else {
            currentFocusIndex = 0 // もし最後のフィールドなら最初に戻る
        }
        focusRequesters[currentFocusIndex].requestFocus() // 次のフィールドにフォーカスを移す
    }
    fun shiftFocusIf(condition: () -> Boolean){
        if (condition()) shiftFocus()
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
    val dateTimeDialogState = rememberDialogState(false)
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())

    if (dateTimeDialogState.isOpen)
        DateTimeDialog(itemUiState.measuredAt, dateTimeFormatter.zone,
                onConfirm = { updateItemUiState(itemUiState.copy(measuredAt = it)) },
                closeDialog = { dateTimeDialogState.close() })

    // focus requesters
    val bpUpperFocusRequester = remember { FocusRequester() }
    val bpLowerFocusRequester = remember { FocusRequester() }
    val pulseFocusRequester = remember { FocusRequester() }
    val focusManager = remember { FocusManager(listOf(bpUpperFocusRequester, bpLowerFocusRequester, pulseFocusRequester)) }

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
        val labelModifier = Modifier.weight(1f)
        InputFieldRow("Measured At"){
            OutlinedButton(onClick = { dateTimeDialogState.open() }){
                Text(dateTimeFormatter.format(itemUiState.measuredAt))
            }
        }
        InputFieldRow("Bp Upper"){
            TextField(itemUiState.bpUpper,
                onValueChange = {
                     updateItemUiState(itemUiState.copy(bpUpper = it))
                    focusManager.shiftFocusIf { (it.toIntOrNull() ?: 0) > MIN_BP }
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
                    focusManager.shiftFocusIf { (it.toIntOrNull() ?: 0) > MIN_BP }
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
                onValueChange = { updateItemUiState(itemUiState.copy(bodyWeight = it))},
                keyboardOptions = decimalKeyboardOptions,
                label = { Text("Body Weight (optional)")})
        }
        InputFieldRow("Location") {
            SelectableTextField(itemUiState.location, locationList,
                onValueChange = { updateItemUiState(itemUiState.copy(location = it)) },
            )
        }
        InputFieldRow("Memo") {
            TextField(itemUiState.memo, onValueChange = { updateItemUiState(itemUiState.copy(memo = it))},
                label = { Text("Memo (optional)")})
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
    Row {
        Text(label, modifier = Modifier.weight(1f))
        inputField()
    }
}
