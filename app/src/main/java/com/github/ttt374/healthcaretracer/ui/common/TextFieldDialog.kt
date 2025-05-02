package com.github.ttt374.healthcaretracer.ui.common

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester

@Composable
fun TextFieldDialog(title: @Composable () -> Unit = {}, initialValue: String, onConfirm: (String) -> Unit, closeDialog: () -> Unit = {},
                    keyboardOptions: KeyboardOptions = KeyboardOptions.Default){
    var text by remember { mutableStateOf(initialValue) }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit){
        focusRequester.requestFocus()
    }
    ConfirmDialog(title = title, text = { OutlinedTextField(text, { text = it}, keyboardOptions = keyboardOptions, modifier = Modifier.focusRequester(focusRequester)) },
        onConfirm = { onConfirm(text)}, closeDialog = closeDialog)
}