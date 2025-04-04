package com.github.ttt374.healthcaretracer.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.BuildConfig
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import com.github.ttt374.healthcaretracer.data.datastore.Config
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.ConfirmDialog
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.rememberDialogState
import com.github.ttt374.healthcaretracer.ui.common.rememberExpandState

@Composable
fun TextFieldDialog(initialValue: String, onConfirm: (String) -> Unit, onCancel: () -> Unit = {}, closeDialog: () -> Unit = {}){
    var text by remember { mutableStateOf(initialValue) }
    ConfirmDialog({ OutlinedTextField(text, { text = it})}, onConfirm = { onConfirm(text)}, onCancel = onCancel, closeDialog = closeDialog)
}
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel(), appNavigator: AppNavigator) {
    val uiState by viewModel.settingsUiState.collectAsState()
    // dialogs
    val targetBpUpperState = rememberDialogState()
    if (targetBpUpperState.isOpen){
        TextFieldDialog(uiState.targetBpUpper, onConfirm = { viewModel.updateSetting(uiState.copy(targetBpUpper = it )) },
            closeDialog = { targetBpUpperState.close() })
    }
    val targetBpLowerState = rememberDialogState()
    if (targetBpLowerState.isOpen){
        TextFieldDialog(uiState.targetBpLower, onConfirm = { viewModel.updateSetting(uiState.copy(targetBpLower = it )) },
            closeDialog = { targetBpLowerState.close() })
    }
    ///
    Scaffold(
        topBar = { CustomTopAppBar("Settings") },
        bottomBar = { CustomBottomAppBar(appNavigator) }
    ) { innerPadding ->
        Column (Modifier.padding(innerPadding).padding(16.dp)){
            SettingsRow("HTN Guideline Type") {
                val guidelineList = listOf("WHO", "JST")
                val expandState = rememberExpandState()
                Text(uiState.bloodPressureGuidelineName)
            }
            SettingsRow("Target Bp Upper", onClick = { targetBpUpperState.open()}){
                Text(uiState.targetBpUpper)
            }
            SettingsRow("Target Bp Lower", onClick = { targetBpLowerState.open() }){
                Text(uiState.targetBpLower)
            }
            SettingsRow("Target Body Weight"){ Text("XX kg")}

            SettingsRow("Morning Range"){ Text("am-am")}
            SettingsRow("Evening Range"){ Text("pm-pm")}

            SettingsRow("Version") { Text(BuildConfig.VERSION_NAME) }
        }
    }
}
@Composable
fun SettingsRow(label: String, onClick: (() -> Unit)? = null, content: @Composable () -> Unit ){
    Row(modifier=Modifier.padding(4.dp).clickable(onClick != null) { onClick?.invoke() }) {
        Text(label, Modifier.weight(1f))
        content()
     }
}
