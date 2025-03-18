package com.github.ttt374.healthcaretracer.ui.entry

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.DateTimeDialog
import com.github.ttt374.healthcaretracer.ui.common.rememberDialogState
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun EntryScreen(entryViewModel: EntryViewModel = hiltViewModel(),
                navigateBack: () -> Unit = {}
                ) {
    val uiState by entryViewModel.uiState.collectAsState()
    var text by remember { mutableStateOf("") }
    //val dateTimeDialogState = rememberDialogState(false)
    var measuredAt by remember { mutableStateOf(Instant.now())}
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navigateBack()
        }
    }
    Scaffold(topBar = { CustomTopAppBar("Entry", navigateBack = navigateBack) }){ innerPadding ->
//        if (dateTimeDialogState.isOpen)
//            DateTimeDialog(measuredAt, dateTimeFormatter.zone,
//                onConfirm = { measuredAt = it },
//                closeDialog = { dateTimeDialogState.close() })

        Column (Modifier.padding(innerPadding)){

//            Row {
//                Text("Measured At", modifier = Modifier.weight(1f))
//                Button(onClick = { dateTimeDialogState.open() }, modifier = Modifier.weight(2f)){
//                    Text(dateTimeFormatter.format(measuredAt))
//                }
//            }
            Row {
                Text("High BP / Low BP / Pulse", modifier = Modifier.weight(1f))
                TextField(text, { text = it}, modifier = Modifier.weight(2f))
            }
            Row {
                Text("", modifier = Modifier.weight(1f))

                Button(onClick = {
                    entryViewModel.addNewEntryByText(text, measuredAt)
                }, modifier = Modifier.weight(2f) ){
                    Text("Add")
                }


            }
        }
    }
}