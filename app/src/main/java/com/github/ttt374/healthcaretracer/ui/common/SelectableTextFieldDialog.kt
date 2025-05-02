package com.github.ttt374.healthcaretracer.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

//@Composable
//fun SelectableTextFieldDialog(title: @Composable () -> Unit = {}, initialValue: String, selectableList: List<String>, onConfirm: (String) -> Unit, closeDialog: () -> Unit = {},
//                    ){  // keyboardOptions: KeyboardOptions = KeyboardOptions.Default
//    var text by remember { mutableStateOf(initialValue) }
//
//    val focusRequester = remember { FocusRequester() }
//    LaunchedEffect(Unit){
//        focusRequester.requestFocus()
//    }
//    ConfirmDialog(title = title,
//        text = {  // keyboardOptions = keyboardOptions,
//        SelectableTextField(text, selectableList, onValueChange = { text = it},  modifier = Modifier.focusRequester(focusRequester))
//
//    },
//        onConfirm = { onConfirm(text)}, closeDialog = closeDialog)
//}
@Composable
fun SelectableTextFieldDialog(title: @Composable () -> Unit = {}, initialValue: String,  selectableList: List<String>, onConfirm: (String) -> Unit, closeDialog: () -> Unit = {},
                    validate: (String) -> Boolean = { true },){

    val inputText = @Composable { value: String, onValueChange: (String) -> Unit, modifier: Modifier ->
        SelectableTextField(value, selectableList, onValueChange = onValueChange, modifier = modifier)
    }
    InputDialog(initialValue, title = title, inputText = inputText, onConfirm = { onConfirm(it) }, closeDialog = closeDialog, validate = validate)

}