package com.github.ttt374.healthcaretracer.ui.common

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

//@Composable
//fun TextFieldDialog(title: @Composable () -> Unit = {}, initialValue: String, onConfirm: (String) -> Unit, closeDialog: () -> Unit = {},
//                    keyboardOptions: KeyboardOptions = KeyboardOptions.Default){
//    var value by remember { mutableStateOf(initialValue) }
//
//    val focusRequester = remember { FocusRequester() }
//    LaunchedEffect(Unit){
//        focusRequester.requestFocus()
//    }
//    ConfirmDialog(title = title, text = { OutlinedTextField(value, { value = it}, keyboardOptions = keyboardOptions, modifier = Modifier.focusRequester(focusRequester)) },
//        onConfirm = { onConfirm(value)}, closeDialog = closeDialog)
//}
@Composable
fun TextFieldDialog(title: @Composable () -> Unit = {}, initialValue: String, onConfirm: (String) -> Unit, closeDialog: () -> Unit = {},
                    validate: (String) -> Boolean = { true },
                    keyboardOptions: KeyboardOptions = KeyboardOptions.Default){

    val inputText = @Composable { value: String, onValueChange: (String) -> Unit, modifier: Modifier ->
        OutlinedTextField(value, onValueChange, keyboardOptions = keyboardOptions, modifier = modifier)
    }
    InputDialog(initialValue, title = title, inputText = inputText, onConfirm = { onConfirm(it) }, closeDialog = closeDialog, validate = validate)

}
