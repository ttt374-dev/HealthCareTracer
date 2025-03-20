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
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.DateTimeDialog
import com.github.ttt374.healthcaretracer.ui.common.rememberDialogState
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun EditScreen(editViewModel: EditViewModel = hiltViewModel(), navigateBack: () -> Unit = {}) {
    val uiState = editViewModel.uiState

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navigateBack()
        }
    }

    Scaffold(topBar = { CustomTopAppBar("Edit", navigateBack = navigateBack) }){ innerPadding ->
        ItemEntryContent(entryUiState = uiState,
            updateItemUiState = { itemUiState -> editViewModel.updateItemUiState(itemUiState)},
            onPost = editViewModel::upsertItem,
            modifier=Modifier.padding(innerPadding))

    }
}
@Composable
fun ItemEntryContent(entryUiState: EntryUiState,
                     onPost: () -> Unit = {},
                     updateItemUiState: (ItemUiState) -> Unit = {},
                     modifier: Modifier = Modifier){
    val itemUiState = entryUiState.itemUiState
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
                    Text(dateTimeFormatter.format(entryUiState.itemUiState.measuredAt))
                }
            }
            Row {
                Text("High BP", modifier = Modifier.weight(1f))
                TextField(itemUiState.bpHigh, { updateItemUiState(itemUiState.copy(bpHigh = it))}, modifier = Modifier.weight(2f))
            }
            Row {
                Text("High Low", modifier = Modifier.weight(1f))
                TextField(itemUiState.bpLow, { updateItemUiState(itemUiState.copy(bpLow = it))}, modifier = Modifier.weight(2f))
            }
            Row {
                Text("Pulse", modifier = Modifier.weight(1f))
                TextField(itemUiState.pulse, { updateItemUiState(itemUiState.copy(pulse = it)) }, modifier = Modifier.weight(2f))
            }
            Row {
                Text("", modifier = Modifier.weight(1f))

                Button(enabled = entryUiState.isValid, onClick = {
                    onPost()
                    //editViewModel.updateItem()
                }, modifier = Modifier.weight(2f) ){
                    Text("OK")
                }
            }
        }
}