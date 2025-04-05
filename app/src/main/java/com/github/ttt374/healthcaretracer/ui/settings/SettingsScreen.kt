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
    val bpGuidelineState = rememberDialogState()
    val targetBpUpperState = rememberDialogState()
    val numberKeyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
    val decimalKeyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)

    TextFieldDialog(targetBpUpperState.isOpen,
        config.targetBpUpper.toString(),
        onConfirm = {
            viewModel.saveConfig(config.copy(targetBpUpper = it.toInt()))
        },
        closeDialog = { targetBpUpperState.close() },
        keyboardOptions = numberKeyboardOptions,
    )

    val targetBpLowerState = rememberDialogState()
    TextFieldDialog(targetBpLowerState.isOpen, config.targetBpLower.toString(), onConfirm = {
        viewModel.saveConfig(config.copy(targetBpLower = it.toInt()))
    },
        closeDialog = { targetBpLowerState.close() },
        keyboardOptions = numberKeyboardOptions)

    val targetBodyWeightState = rememberDialogState()
    TextFieldDialog(targetBodyWeightState.isOpen, config.targetBodyWeight.toString(), onConfirm = {
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
            SettingsRow("Morning Start", onClick = { morningRangeStartState.open()}){
                Text(config.morningRange.start.format(localTimeFormat))
            }
            SettingsRow("Morning End", onClick = { morningRangeEndState.open()}){
                Text(config.morningRange.endInclusive.format(localTimeFormat))
            }
            SettingsRow("Evening Start", onClick = { eveningRangeStartState.open()}){
                Text(config.eveningRange.start.format(localTimeFormat))
            }
            SettingsRow("Evening End", onClick = { eveningRangeEndState.open()}){
                Text(config.eveningRange.endInclusive.format(localTimeFormat))
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
    val guidelineList = BloodPressureGuideline.bloodPressureGuidelines.keys
    DropdownMenu(expanded, onDismissRequest = onDismissRequest){
        guidelineList.forEach {
            DropdownMenuItem({Text(it)}, onClick = {
                onSelected(it)
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
