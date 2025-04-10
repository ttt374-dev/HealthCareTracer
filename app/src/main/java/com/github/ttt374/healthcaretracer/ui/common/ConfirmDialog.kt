package com.github.ttt374.healthcaretracer.ui.common


import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

/**
 *  [ConfirmDialog]
 */
@Composable
fun ConfirmDialog(
    title: @Composable () ->  Unit = {},
    text: @Composable () -> Unit = {},
    onConfirm: () -> Unit,
    onCancel: () -> Unit = { },
    confirmButtonLabel: String = "OK",
    dismissButtonLabel: String = "Cancel",
    closeDialog: () -> Unit = {},
    confirmButton: @Composable () -> Unit = {
        OutlinedButton(onClick = { onConfirm(); closeDialog() }) {
            Text(confirmButtonLabel)
        }
    }
){

    AlertDialog(onDismissRequest = onCancel,
        confirmButton =  confirmButton,
        dismissButton = {
            OutlinedButton( onClick = { onCancel(); closeDialog() }) {
                Text(dismissButtonLabel)
            }
        },
        //title = { Text(titleString) },
        //text = { Text(text,style= TextStyle(fontSize = 16.sp, lineHeight = 20.sp)) } ,
        title = title,
        text = text,
        properties = DialogProperties(dismissOnClickOutside = true) // 画面外タップでキャンセル
    )

}
