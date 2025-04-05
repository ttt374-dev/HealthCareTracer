package com.github.ttt374.healthcaretracer.ui.settings

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.BuildConfig
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.ConfirmDialog
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.rememberDialogState
import com.github.ttt374.healthcaretracer.ui.entry.EditMode

@Composable
fun TextFieldDialog(initialValue: String, onConfirm: (String) -> Unit, onCancel: () -> Unit = {}, closeDialog: () -> Unit = {},
                    keyboardOptions: KeyboardOptions = KeyboardOptions.Default){
    var text by remember { mutableStateOf(initialValue) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit){
        focusRequester.requestFocus()
    }
    ConfirmDialog({
        OutlinedTextField(text, { text = it}, keyboardOptions = keyboardOptions, modifier = Modifier.focusRequester(focusRequester))},
        onConfirm = { onConfirm(text)}, onCancel = onCancel, closeDialog = closeDialog)
}
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel(), appNavigator: AppNavigator) {
    val config by viewModel.config.collectAsState()

    // dialogs
    val bpGuidelineState = rememberDialogState()
    val targetBpUpperState = rememberDialogState()
    val numberKeyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
    val decimalKeyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)

    if (targetBpUpperState.isOpen){
        TextFieldDialog(
            config.targetBpUpper.toString(),
            onConfirm = {
                viewModel.saveConfig(config.copy(targetBpUpper = it.toInt()))
            },
            closeDialog = { targetBpUpperState.close() },
            keyboardOptions = numberKeyboardOptions,

        )
    }
    val targetBpLowerState = rememberDialogState()
    if (targetBpLowerState.isOpen){
        TextFieldDialog(config.targetBpLower.toString(), onConfirm = {
            viewModel.saveConfig(config.copy(targetBpLower = it.toInt()))
        },
            closeDialog = { targetBpLowerState.close() },
            keyboardOptions = numberKeyboardOptions)
    }
    val targetBodyWeightState = rememberDialogState()
    if (targetBodyWeightState.isOpen){
        TextFieldDialog(config.targetBodyWeight.toString(), onConfirm = {
            viewModel.saveConfig(config.copy(targetBodyWeight = it.toDouble()))
        },
            closeDialog = { targetBodyWeightState.close() },
            keyboardOptions = decimalKeyboardOptions)
    }

    ///
    Scaffold(
        topBar = { CustomTopAppBar("Settings") },
        bottomBar = { CustomBottomAppBar(appNavigator) }
    ) { innerPadding ->
        Column (
            Modifier.padding(innerPadding).padding(16.dp)){
            SettingsRow("HTN Guideline Type", onClick = { bpGuidelineState.open() }) {
                Text(config.bloodPressureGuideline.name)
                    BpGuidelineDropMenu(bpGuidelineState.isOpen, onSelected = { selected ->
                        Log.d("dropdown", selected)
                        val guideline = BloodPressureGuideline.bloodPressureGuidelines[selected] ?: BloodPressureGuideline.WHO
                        viewModel.saveConfig(config.copy(bloodPressureGuideline = guideline))
                        bpGuidelineState.close()
                    }, onDismissRequest = { bpGuidelineState.close() })
            }
            SettingsRow("Target Bp Upper", onClick = { targetBpUpperState.open()}){
                Text(config.targetBpUpper.toString())
                //Text(uiState.targetBpUpper)
            }
            SettingsRow("Target Bp Lower", onClick = { targetBpLowerState.open() }){
                Text(config.targetBpLower.toString())
                //Text(uiState.targetBpLower)
            }
            SettingsRow("Target Body Weight", onClick = { targetBodyWeightState.open() }){
                Text(config.targetBodyWeight.toString())
                //Text(uiState.targetBpLower)
            }
//            SettingsRow("Target Body Weight"){ Text("XX kg")}
//
//            SettingsRow("Morning Range"){ Text("am-am")}
//            SettingsRow("Evening Range"){ Text("pm-pm")}

            SettingsRow("Version") { Text(BuildConfig.VERSION_NAME) }
        }
    }
}
@Composable
fun SettingsRow(label: String, onClick: (() -> Unit)? = null, content: @Composable () -> Unit ){
    Row(modifier= Modifier
        .padding(4.dp)
        .clickable(onClick != null) { onClick?.invoke() }) {
        Text(label, Modifier.weight(1f))
        content()
     }
}
@Composable
fun BpGuidelineDropMenu(expanded: Boolean, onSelected: (String) -> Unit, onDismissRequest: () -> Unit ){
    val guidelineList = BloodPressureGuideline.bloodPressureGuidelines.keys
    DropdownMenu(expanded, onDismissRequest = onDismissRequest){
        guidelineList.forEach {
            DropdownMenuItem({Text(it)}, onClick = {
                onSelected(it)
            })
        }
    }
}
