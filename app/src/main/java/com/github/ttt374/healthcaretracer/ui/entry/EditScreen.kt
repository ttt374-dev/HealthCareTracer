package com.github.ttt374.healthcaretracer.ui.entry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.DateTimeDialog
import com.github.ttt374.healthcaretracer.ui.common.rememberDialogState
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun EditScreen(editViewModel: EditViewModel = hiltViewModel(), navigateBack: () -> Unit = {}) {
    val itemUiState by editViewModel.itemUiState.collectAsState()

    LaunchedEffect(itemUiState.isSuccess) {
        if (itemUiState.isSuccess) {
            navigateBack()
        }
    }
    Scaffold(topBar = { CustomTopAppBar("Edit", navigateBack = navigateBack) }){ innerPadding ->
        Column (modifier = Modifier.padding(innerPadding)) {
            ItemEntryContent(itemUiState = itemUiState,
                updateItemUiState = editViewModel::updateItemUiState,
                onPost = editViewModel::upsertItem)
        }
    }
}
@Composable
fun ItemEntryContent(itemUiState: ItemUiState,
                     modifier: Modifier = Modifier,
                     onPost: () -> Unit = {},
                     updateItemUiState: (ItemUiState) -> Unit = {}){
    //val itemUiState = entryUiState.itemUiState
    val dateTimeDialogState = rememberDialogState(false)
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())

    if (dateTimeDialogState.isOpen)
        DateTimeDialog(itemUiState.measuredAt, dateTimeFormatter.zone,
                onConfirm = { updateItemUiState(itemUiState.copy(measuredAt = it)) },
                closeDialog = { dateTimeDialogState.close() })
    Column (modifier=modifier){
        Row {
            Text("Measured At", modifier = Modifier.weight(1f))
            OutlinedButton(onClick = { dateTimeDialogState.open() }, modifier = Modifier.weight(2f)){
                Text(dateTimeFormatter.format(itemUiState.measuredAt))
            }
        }
        if (itemUiState.editMode is EditMode.Edit){
            Row {
                Text("High BP", modifier = Modifier.weight(1f))
                TextField(itemUiState.bpHigh, { updateItemUiState(itemUiState.copy(bpHigh = it))}, modifier = Modifier.weight(2f))
            }
            Row {
                Text("Low BP", modifier = Modifier.weight(1f))
                TextField(itemUiState.bpLow, { updateItemUiState(itemUiState.copy(bpLow = it))}, modifier = Modifier.weight(2f))
            }
            Row {
                Text("Pulse", modifier = Modifier.weight(1f))
                TextField(itemUiState.pulse, { updateItemUiState(itemUiState.copy(pulse = it)) }, modifier = Modifier.weight(2f))
            }
        } else {
            Row {
                Text("h/l/p", modifier = Modifier.weight(1f))
                TextField(itemUiState.rawInput, { updateItemUiState(itemUiState.copy(rawInput = it))}, modifier = Modifier.weight(2f))
            }
        }
        Row {
            Text("", modifier = Modifier.weight(1f))

            Button(enabled = itemUiState.isValid, onClick = {
                onPost()
                //editViewModel.updateItem()
            }, modifier = Modifier.weight(2f) ){
                Text("OK")
            }
        }
    }
}