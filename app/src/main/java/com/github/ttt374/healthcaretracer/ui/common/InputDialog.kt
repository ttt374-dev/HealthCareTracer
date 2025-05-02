package com.github.ttt374.healthcaretracer.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

@Composable
fun InputDialog(
    title: @Composable () ->  Unit = {},
    text: @Composable () -> Unit = {},
    onConfirm: () -> Unit = {},
    confirmButtonLabel: String = "OK",
    dismissButtonLabel: String = "Cancel",
    closeDialog: () -> Unit = {},
    confirmButton: @Composable () -> Unit = {
        OutlinedButton(onClick = { onConfirm(); closeDialog() }) {
            Text(confirmButtonLabel)
        }
    }
){
    AlertDialog(onDismissRequest = { closeDialog()},
        confirmButton =  confirmButton,
        dismissButton = {
            OutlinedButton( onClick = { closeDialog()}) {
                Text(dismissButtonLabel)
            }
        },
        title = title,
        text = text,
        properties = DialogProperties(dismissOnClickOutside = true) // 画面外タップでキャンセル
    )
}
