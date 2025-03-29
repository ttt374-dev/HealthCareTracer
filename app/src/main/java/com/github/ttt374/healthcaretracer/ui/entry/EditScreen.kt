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
fun EditScreen(editViewModel: EditViewModel = hiltViewModel(), appNavigator: AppNavigator) {
    val itemUiState by editViewModel.itemUiState.collectAsState()
    val locationList by editViewModel.locationList.collectAsState()

    LaunchedEffect(itemUiState.isSuccess) {
        if (itemUiState.isSuccess) {
            appNavigator.navigateBack()
        }
    }
    Scaffold(topBar = { CustomTopAppBar("Edit", navigateBack = appNavigator::navigateBack) }){ innerPadding ->
        Column (modifier = Modifier.padding(innerPadding)) {
            ItemEntryContent(editMode = EditMode.Edit,
                itemUiState = itemUiState,
                updateItemUiState = editViewModel::updateItemUiState,
                locationList = locationList,
                onPost = editViewModel::upsertItem,
                onDelete = editViewModel::deleteItem)
        }
    }
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
    //val itemUiState = entryUiState.itemUiState
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
    //val submitFocusRequester = remember { FocusRequester() }
    //val bodyWeightFocusRequester = remember { FocusRequester() }

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
    Column (modifier=modifier){
        Row {
            Text("Measured At", modifier = Modifier.weight(1f))
            OutlinedButton(onClick = { dateTimeDialogState.open() }){
                Text(dateTimeFormatter.format(itemUiState.measuredAt))
            }
        }
        Row {
            Text("High BP", modifier = Modifier.weight(1f))
            BloodPressureInputField(
                value = itemUiState.bpUpper,
                onValueChange = { newValue ->
                    updateItemUiState(itemUiState.copy(bpUpper = newValue))
                    if ((newValue.toIntOrNull() ?: 0) > MIN_BP){
                        bpLowerFocusRequester.requestFocus()
                    }
                },
                label = "BP High",
                focusRequester = bpUpperFocusRequester,
                //modifier = Modifier.weight(2f)
            )
        }
        Row {
            Text("High Low", modifier = Modifier.weight(1f))
            BloodPressureInputField(
                value = itemUiState.bpLower,
                onValueChange = { newValue ->
                    updateItemUiState(itemUiState.copy(bpLower = newValue))
                    if ((newValue.toIntOrNull() ?: 0) > MIN_BP){
                        pulseFocusRequester.requestFocus()
                    }
                },
                label = "BP Low",
                focusRequester = bpLowerFocusRequester,
                //modifier = Modifier.weight(2f)
            )
        }
        Row {
            Text("Pulse", modifier = Modifier.weight(1f))
            BloodPressureInputField(
                value = itemUiState.pulse,
                onValueChange = { newValue ->
                    updateItemUiState(itemUiState.copy(pulse = newValue))
//                    if (itemUiState.isPulseValid()){
//                    //if (isValidPulse(newValue.toIntOrNull() ?: 0 )){
//                        //bodyWeightFocusRequester.requestFocus()
//                    }
                },
                label = "Pulse",
                focusRequester = pulseFocusRequester,
            )
        }
        Row {
            Text("", modifier = Modifier.weight(1f))

            if (editMode is EditMode.Edit){
                Button(onClick = { deleteDialogState.open(itemUiState.toItem()) }){
                    Text("Delete")
                }
            }
            Button(enabled = itemUiState.isValid(), onClick = { onPost() }){
                Text("OK")
            }
        }
        Row {
            Text("Body Weight", modifier = Modifier.weight(1f))
            TextField(itemUiState.bodyWeight, onValueChange = { updateItemUiState(itemUiState.copy(bodyWeight = it))},
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                //modifier = Modifier.focusRequester(bodyWeightFocusRequester),
                label = { Text("Body Weight (optional)")})
        }

        Row {
            Text("Location", modifier = Modifier.weight(1f))
            //TextField(itemUiState.location, { updateItemUiState(itemUiState.copy(location = it)) }, modifier = Modifier.weight(2f))
            SelectableTextField(itemUiState.location, locationList,
                onValueChange = {
                    updateItemUiState(itemUiState.copy(location = it))
                },
            )
        }
        Row {
            Text("Memo", modifier = Modifier.weight(1f))
            TextField(itemUiState.memo, onValueChange = { updateItemUiState(itemUiState.copy(memo = it))},
                label = { Text("Memo (optional)")})
        }
    }
}

@Composable
fun BloodPressureInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
        modifier = modifier.focusRequester(focusRequester)
    )
}
//fun isValidBpValue(bpValue: Int): Boolean {
//    return bpValue in MIN_BP..MAX_BP
//}
//fun isValidPulse(pulse: Int): Boolean {
//    return pulse in MIN_PULSE..MAX_PULSE
//}

