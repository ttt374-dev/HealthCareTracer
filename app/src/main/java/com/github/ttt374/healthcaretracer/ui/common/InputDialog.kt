package com.github.ttt374.healthcaretracer.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.window.DialogProperties

@Composable
fun InputDialog(
    initialValue: String,
    title: @Composable () ->  Unit = {},
    inputText: @Composable (value: String, onValueChange: (String) -> Unit, modifier: Modifier) -> Unit,
    onConfirm: (value: String) -> Unit = {},
    confirmButtonLabel: String = "OK",
    dismissButtonLabel: String = "Cancel",
    closeDialog: () -> Unit = {},
    validate: (String) -> Boolean = { true },
    closeAfterConfirm: Boolean = true,
    dialogProperties: DialogProperties = DialogProperties(dismissOnClickOutside = true)
){
    var value by remember { mutableStateOf(initialValue) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit){
        focusRequester.requestFocus()
    }
    val confirmButton = @Composable {
        TextButton(enabled = validate(value),
            onClick = {
                onConfirm(value);
                if (closeAfterConfirm) closeDialog()
            }) {
            Text(confirmButtonLabel)
        }
    }
    AlertDialog(onDismissRequest = { closeDialog()},
        confirmButton =  confirmButton,
        dismissButton = {
            TextButton( onClick = { closeDialog()}) {
                Text(dismissButtonLabel)
            }
        },
        title = title,
        text = { inputText(value, { value = it }, Modifier.focusRequester(focusRequester)) },
        properties = dialogProperties,
    )
}
