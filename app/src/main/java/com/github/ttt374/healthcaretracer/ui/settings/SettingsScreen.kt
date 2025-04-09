package com.github.ttt374.healthcaretracer.ui.settings

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.BuildConfig
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import com.github.ttt374.healthcaretracer.data.datastore.LocalTimeRange
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.TextFieldDialog
import com.github.ttt374.healthcaretracer.ui.common.TimePickerDialog
import com.github.ttt374.healthcaretracer.ui.common.rememberDialogState
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel(), appNavigator: AppNavigator) {
    val config by viewModel.config.collectAsState()

    // dialogs
    val numberKeyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
    val decimalKeyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)

    val bpGuidelineState = rememberDialogState()

    val targetBpUpperState = rememberDialogState()
    if (targetBpUpperState.isOpen)
        TextFieldDialog(config.targetBpUpper.toString(),
            onConfirm = {
                viewModel.saveConfig(config.copy(targetBpUpper = it.toInt()))
            },
            closeDialog = { targetBpUpperState.close() },
            keyboardOptions = numberKeyboardOptions,
        )

    val targetBpLowerState = rememberDialogState()
    if (targetBpLowerState.isOpen)
        TextFieldDialog(config.targetBpLower.toString(), onConfirm = {
            viewModel.saveConfig(config.copy(targetBpLower = it.toInt()))
        },
            closeDialog = { targetBpLowerState.close() },
            keyboardOptions = numberKeyboardOptions)

    val targetBodyWeightState = rememberDialogState()
    if (targetBodyWeightState.isOpen)
        TextFieldDialog(config.targetBodyWeight.toString(), onConfirm = {
            viewModel.saveConfig(config.copy(targetBodyWeight = it.toDouble()))
        },
            closeDialog = { targetBodyWeightState.close() },
            keyboardOptions = decimalKeyboardOptions)

    val morningRangeStartState = rememberDialogState()
    if (morningRangeStartState.isOpen){
        LocalTimeRangeDialog(config.morningRange, true,
            onTimeSelected = { viewModel.saveConfig(config.copy(morningRange = it))},
            onDismiss = { morningRangeStartState.close()})
    }
    val morningRangeEndState = rememberDialogState()
    if (morningRangeEndState.isOpen){
        LocalTimeRangeDialog(config.morningRange, false,
            onTimeSelected = { viewModel.saveConfig(config.copy(morningRange = it))},
            onDismiss = { morningRangeEndState.close()})
    }
    val eveningRangeStartState = rememberDialogState()
    if (eveningRangeStartState.isOpen){
        LocalTimeRangeDialog(config.eveningRange, true,
            onTimeSelected = { viewModel.saveConfig(config.copy(eveningRange = it))},
            onDismiss = { eveningRangeStartState.close()})
    }
    val eveningRangeEndState = rememberDialogState()
    if (eveningRangeEndState.isOpen){
        LocalTimeRangeDialog(config.eveningRange, false,
            onTimeSelected = { viewModel.saveConfig(config.copy(eveningRange = it))},
            onDismiss = { eveningRangeEndState.close()})
    }

    val localTimeFormat = DateTimeFormatter.ofPattern("h:mm a")  // .withZone(ZoneId.systemDefault())

    ///
    Scaffold(
        topBar = { CustomTopAppBar("Settings") },
        bottomBar = { CustomBottomAppBar(appNavigator) }
    ) { innerPadding ->
        Column (
            Modifier.padding(innerPadding).padding(16.dp)){
            SettingsRow("HTN Guideline Type") {
                Text(config.bloodPressureGuideline.name, Modifier.clickable { bpGuidelineState.open() })
                BpGuidelineDropMenu(bpGuidelineState.isOpen, onSelected = { selected ->
                    val guideline = BloodPressureGuideline.bloodPressureGuidelines.find { it.name == selected } ?: BloodPressureGuideline.Default
                    viewModel.saveConfig(config.copy(bloodPressureGuideline = guideline))
                    bpGuidelineState.close()
                }, onDismissRequest = { bpGuidelineState.close() })
            }
            SettingsRow("Target Blood Pressure"){
                Text(config.targetBpUpper.toString(), Modifier.clickable { targetBpUpperState.open() })
                Text(" / ")
                Text(config.targetBpLower.toString(), Modifier.clickable { targetBpLowerState.open() })
            }
            SettingsRow("Target Body Weight (Kg)"){
                Text(config.targetBodyWeight.toString(), Modifier.clickable {  targetBodyWeightState.open() })
            }
            SettingsRow("Morning Range"){
                Text(config.morningRange.start.format(localTimeFormat), Modifier.clickable { morningRangeStartState.open()})
                Text(" - ")
                Text(config.morningRange.endInclusive.format(localTimeFormat), Modifier.clickable { morningRangeEndState.open() })
            }

            SettingsRow("Evening Range"){
                Text(config.eveningRange.start.format(localTimeFormat), Modifier.clickable { eveningRangeStartState.open() })
                Text(" - ")
                Text(config.eveningRange.endInclusive.format(localTimeFormat), Modifier.clickable { eveningRangeEndState.open() })
            }
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
    val guidelineList = BloodPressureGuideline.bloodPressureGuidelines
    DropdownMenu(expanded, onDismissRequest = onDismissRequest){
        guidelineList.forEach {
            DropdownMenuItem({Text(it.name)}, onClick = {
                onSelected(it.name)
            })
        }
    }
}
@Composable
fun LocalTimeRangeDialog(range: LocalTimeRange, isStart: Boolean, onTimeSelected: (LocalTimeRange) -> Unit, onDismiss: () -> Unit){
    val zone = ZoneId.systemDefault()
    if (isStart){
        TimePickerDialog(range.start.atDate(LocalDate.now()).atZone(zone).toInstant(),
            onTimeSelected = { onTimeSelected(range.copy(start = it.atZone(zone).toLocalTime()))},
            onDismiss = onDismiss)
    } else {
        TimePickerDialog(range.endInclusive.atDate(LocalDate.now()).atZone(zone).toInstant(),
            onTimeSelected = { onTimeSelected(range.copy(endInclusive = it.atZone(zone).toLocalTime()))},
            onDismiss = onDismiss)
    }
}
