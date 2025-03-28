package com.github.ttt374.healthcaretracer.ui.entry

import android.util.Log
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.ui.common.ConfirmDialog
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.DateTimeDialog
import com.github.ttt374.healthcaretracer.ui.common.SelectableTextField
import com.github.ttt374.healthcaretracer.ui.common.rememberDialogState
import com.github.ttt374.healthcaretracer.ui.common.rememberItemDialogState
import kotlinx.coroutines.flow.filter
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun EditScreen(editViewModel: EditViewModel = hiltViewModel(), navigateBack: () -> Unit = {}) {
    val itemUiState by editViewModel.itemUiState.collectAsState()
    val locationList by editViewModel.locationList.collectAsState()

    LaunchedEffect(itemUiState.isSuccess) {
        if (itemUiState.isSuccess) {
            navigateBack()
        }
    }

    Scaffold(topBar = { CustomTopAppBar("Edit", navigateBack = navigateBack) }){ innerPadding ->
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
    val bpHighFocusRequester = remember { FocusRequester() }
    val bpLowFocusRequester = remember { FocusRequester() }
    val pulseFocusRequester = remember { FocusRequester() }
    val submitFocusRequester = remember { FocusRequester() }
    val bodyWeightFocusRequester = remember { FocusRequester() }
    val locationFocusRequester = remember { FocusRequester() }

    // dialog
    val deleteDialogState = rememberItemDialogState()
    if (deleteDialogState.isOpen){
        ConfirmDialog(title = { Text("Are you sure to delete ?") },
            text = { Text("") },
            onConfirm = onDelete,
            closeDialog = { deleteDialogState.close()})
    }
    // 画面を開いたときに bpHigh にフォーカスを移動（新規エントリ時のみ）
    LaunchedEffect(Unit) {
        if (editMode is EditMode.Entry) {
            bpHighFocusRequester.requestFocus()
        }
    }
    Column (modifier=modifier){
        Row {
            Text("Measured At", modifier = Modifier.weight(1f))
            OutlinedButton(onClick = { dateTimeDialogState.open() }, modifier = Modifier.weight(2f)){
                Text(dateTimeFormatter.format(itemUiState.measuredAt))
            }
        }

        Row {
            Text("High BP", modifier = Modifier.weight(1f))
            BloodPressureInputField(
                value = itemUiState.bpHigh,
                onValueChange = { newValue ->
                    handleBpInput(newValue, { updateItemUiState(itemUiState.copy(bpHigh = it))}, { it.isValidBp }, bpLowFocusRequester)
                },
                label = "BP High",
                focusRequester = bpHighFocusRequester,
                //modifier = Modifier.weight(2f)
            )
        }
        Row {
            Text("High Low", modifier = Modifier.weight(1f))
            BloodPressureInputField(
                value = itemUiState.bpLow,
                onValueChange = { newValue ->
                    handleBpInput(newValue, { updateItemUiState(itemUiState.copy(bpLow = it))}, { it.isValidBp }, pulseFocusRequester)
                },
                label = "BP Low",
                focusRequester = bpLowFocusRequester,
                //modifier = Modifier.weight(2f)
            )
        }
        Row {
            Text("Pulse", modifier = Modifier.weight(1f))
            BloodPressureInputField(
                value = itemUiState.pulse,
                onValueChange = { newValue ->
                    handleBpInput(newValue, { updateItemUiState(itemUiState.copy(pulse = it))}, { it.isValidPulse }, bodyWeightFocusRequester)
                },
                label = "Pulse",
                focusRequester = pulseFocusRequester,
                //modifier = Modifier.weight(2f)
            )
        }
        Row {
            Text("", modifier = Modifier.weight(1f))

            if (editMode is EditMode.Edit){
                Button(enabled = itemUiState.isValid, onClick = {
                    deleteDialogState.open(itemUiState.toItem())
                }){
                    Text("Delete")
                }
            }
            Button(enabled = itemUiState.isValid, onClick = {
                onPost()
                //editViewModel.updateItem()
            }, modifier = Modifier.focusRequester(submitFocusRequester) ){
                Text("OK")
            }
        }
        Row {
            Text("Body Weight", modifier = Modifier.weight(1f))
            TextField(itemUiState.bodyWeight, onValueChange = { updateItemUiState(itemUiState.copy(bodyWeight = it))},
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.focusRequester(bodyWeightFocusRequester),
                label = { Text("Body Weight (optional)")})
        }

        Row {
            Text("Location", modifier = Modifier.weight(1f))
            //TextField(itemUiState.location, { updateItemUiState(itemUiState.copy(location = it)) }, modifier = Modifier.weight(2f))
            SelectableTextField(itemUiState.location, locationList,
                onValueChange = {
                    updateItemUiState(itemUiState.copy(location = it))
                },
                modifier = Modifier.focusRequester(locationFocusRequester)
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

fun String.isDigit(length: Int = 3) =
    this.all { it.isDigit() } && this.length <= length

val Int.isValidBp: Boolean
    get() = this in 60..250

val Int.isValidPulse: Boolean
    get() = this in 30..200

//fun Int.isValidBp() =
//    this in 60..250

//fun Int.isValidPulse() =
//    this in 30..200

fun handleBpInput(
    newValue: String,
    onValueChange: (String) -> Unit,
    validate: (Int) -> Boolean,
    nextFocusRequester: FocusRequester
) {
    if (newValue.isDigit(3)) {
        onValueChange(newValue)
        if (validate(newValue.toIntOrNull() ?: 0)) nextFocusRequester.requestFocus()
        //if (newValue.length >= 2 && (newValue.toIntOrNull() ?: 0) >= 60){
        //    nextFocusRequester.requestFocus()
        //}
//        newValue.toIntOrNull()?.let { value ->
//            when {
//                newValue.length >= 2 && value >= 60 -> nextFocusRequester.requestFocus()
//            }
//        }
    }
}