package com.github.ttt374.healthcaretracer.ui.entry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.DateTimeDialog
import com.github.ttt374.healthcaretracer.ui.common.SelectableTextField
import com.github.ttt374.healthcaretracer.ui.common.rememberDialogState
import kotlinx.coroutines.flow.filter
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun EditScreen(editViewModel: EditViewModel = hiltViewModel(), navigateBack: () -> Unit = {}) {
    val itemUiState by editViewModel.itemUiState.collectAsState()
    val locationList by editViewModel.locationList.collectAsState()

    LaunchedEffect(Unit) {
        snapshotFlow { itemUiState.isSuccess }
            .filter { it }
            .collect {
                navigateBack()
            }
    }

    Scaffold(topBar = { CustomTopAppBar("Edit", navigateBack = navigateBack) }){ innerPadding ->
        Column (modifier = Modifier.padding(innerPadding)) {
            ItemEntryContent(itemUiState = itemUiState,
                updateItemUiState = editViewModel::updateItemUiState,
                locationList = locationList,
                onPost = editViewModel::upsertItem)
        }
    }
}
@Composable
fun ItemEntryContent(itemUiState: ItemUiState,
                     modifier: Modifier = Modifier,
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
    val locationFocusRequester = remember { FocusRequester() }

    // 画面を開いたときに bpHigh にフォーカスを移動
    LaunchedEffect(Unit) {
        bpHighFocusRequester.requestFocus()
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
                    handleBpInput(newValue, { updateItemUiState(itemUiState.copy(bpHigh = it))}, bpLowFocusRequester)
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
                    handleBpInput(newValue, { updateItemUiState(itemUiState.copy(bpLow = it))}, pulseFocusRequester)
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
                    handleBpInput(newValue, { updateItemUiState(itemUiState.copy(pulse = it))}, locationFocusRequester)
                },
                label = "Pulse",
                focusRequester = pulseFocusRequester,
                //modifier = Modifier.weight(2f)
            )
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
            Text("", modifier = Modifier.weight(1f))
            Button(enabled = itemUiState.isValid, onClick = {
                onPost()
                //editViewModel.updateItem()
            }, modifier = Modifier.focusRequester(submitFocusRequester) ){
                Text("OK")
            }
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
        modifier = modifier
            .focusRequester(focusRequester)
//            .onFocusChanged {
//                if (it.isFocused) onValueChange(value)
//            }
    )
}

fun handleBpInput(
    newValue: String,
    onValueChange: (String) -> Unit,
    nextFocusRequester: FocusRequester
) {
    if (newValue.all { it.isDigit() } && newValue.length <= 3) {
        onValueChange(newValue)
        newValue.toIntOrNull()?.let { value ->
            when {
                newValue.length >= 2 && value >= 60 -> nextFocusRequester.requestFocus()
            }
        }
    }
}