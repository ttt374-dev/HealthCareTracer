package com.github.ttt374.healthcaretracer.ui.entry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.DateTimeDialog
import com.github.ttt374.healthcaretracer.ui.common.rememberDialogState
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun EditScreen(editViewModel: EditViewModel = hiltViewModel(), navigateBack: () -> Unit = {}) {
    val uiState = editViewModel.itemUiState
    var text by remember { mutableStateOf("") }
    val dateTimeDialogState = rememberDialogState(false)
    //var measuredAt by remember { mutableStateOf(Instant.now()) }
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navigateBack()
        }
    }

    Scaffold(topBar = { CustomTopAppBar("Edit", navigateBack = navigateBack) }){ innerPadding ->
        if (dateTimeDialogState.isOpen)
            DateTimeDialog(uiState.measuredAt, dateTimeFormatter.zone,
                onConfirm = { editViewModel.updateUiState(uiState.copy(measuredAt = it)) },
                closeDialog = { dateTimeDialogState.close() })
        Column (Modifier.padding(innerPadding)){
//            Row {
//                Text("Id; ${uiState.id}")
//                IconButton(onClick = { editViewModel.deleteItem()}){
//                    Icon(Icons.Filled.Delete, "delete")
//                }
//            }
            Row {
                Text("Measured At", modifier = Modifier.weight(1f))
                OutlinedButton(onClick = { dateTimeDialogState.open() }, modifier = Modifier.weight(2f)){
                    Text(dateTimeFormatter.format(uiState.measuredAt))
                }
            }
            Row {
                Text("High BP", modifier = Modifier.weight(1f))
                TextField(uiState.bpHigh, { editViewModel.updateUiState(uiState.copy(bpHigh = it))}, modifier = Modifier.weight(2f))
            }
            Row {
                Text("High Low", modifier = Modifier.weight(1f))
                TextField(uiState.bpLow, { text = it}, modifier = Modifier.weight(2f))
            }
            Row {
                Text("Pulse", modifier = Modifier.weight(1f))
                TextField(uiState.pulse, { text = it}, modifier = Modifier.weight(2f))
            }
            Row {
                Text("", modifier = Modifier.weight(1f))

                Button(enabled = uiState.isValid, onClick = {
                    editViewModel.updateItem()
                }, modifier = Modifier.weight(2f) ){
                    Text("OK")
                }
            }
        }
    }
}
