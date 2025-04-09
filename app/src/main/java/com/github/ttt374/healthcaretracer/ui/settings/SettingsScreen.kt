package com.github.ttt374.healthcaretracer.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.BuildConfig
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import com.github.ttt374.healthcaretracer.data.datastore.LocalTimeRange
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.ConfirmDialog
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

    val targetBpState = rememberDialogState()
    if (targetBpState.isOpen){
        TargetBpDialog(config.targetBpUpper, config.targetBpLower, onConfirm = { upper, lower ->
            viewModel.saveConfig(config.copy(targetBpUpper = upper, targetBpLower = lower))
        }, closeDialog = { targetBpState.close() })
    }
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
        Column (Modifier.padding(innerPadding).padding(16.dp)){
            SettingsRow("HTN Guideline Type") {
                Row (Modifier.clickable { bpGuidelineState.toggle() }) {
                    Text(config.bloodPressureGuideline.name)
                    if (bpGuidelineState.isOpen) Icon(Icons.Filled.ExpandLess, "close") else
                        Icon(Icons.Filled.ExpandMore, "expand")
                }
//                BpGuidelineDropMenu(bpGuidelineState.isOpen, onSelected = { selected ->
//                    val guideline = BloodPressureGuideline.bloodPressureGuidelines.find { it.name == selected } ?: BloodPressureGuideline.Default
//                    viewModel.saveConfig(config.copy(bloodPressureGuideline = guideline))
//                    bpGuidelineState.close()
//                }, onDismissRequest = { bpGuidelineState.close() })
            }
            if (bpGuidelineState.isOpen){
                HorizontalSelector(BloodPressureGuideline.bloodPressureGuidelines.map { it.name }, config.bloodPressureGuideline.name,
                    onOptionSelected = { selected ->
                        val guideline = BloodPressureGuideline.bloodPressureGuidelines.find { it.name == selected } ?: BloodPressureGuideline.Default
                        viewModel.saveConfig(config.copy(bloodPressureGuideline = guideline))
                    } )
                BpGuidelineTable(config.bloodPressureGuideline, modifier=Modifier.padding(start = 16.dp))
            }
            SettingsRow("Target Blood Pressure"){
                Text("${config.targetBpUpper} / ${config.targetBpLower}", Modifier.clickable { targetBpState.open() })

//                Text(config.targetBpUpper.toString(), Modifier.clickable { targetBpUpperState.open() })
//                Text(" / ")
//                Text(config.targetBpLower.toString(), Modifier.clickable { targetBpLowerState.open() })
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
fun TargetBpDialog (bpUpper: Int, bpLower: Int, onConfirm: (Int, Int) -> Unit, closeDialog: () -> Unit){
    var bpUpperString by remember { mutableStateOf(bpUpper.toString())}
    var bpLowerString by remember { mutableStateOf(bpLower.toString())}

    ConfirmDialog(text = {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Bp Upper", Modifier.weight(1f))
                OutlinedTextField(bpUpperString, { bpUpperString = it}, modifier=Modifier.weight(2f))
            }
            Row {
                Text("Bp Lower", Modifier.weight(1f))
                OutlinedTextField(bpLowerString, { bpLowerString = it}, Modifier.weight(2f))
            }
        }
    }, onConfirm = { onConfirm(bpUpperString.toIntOrNull() ?: 0, bpLowerString.toIntOrNull() ?: 0); closeDialog() },
        onCancel = { closeDialog() }
        )
}
@Composable
fun BpGuidelineTable (guideline: BloodPressureGuideline, modifier: Modifier = Modifier){
    Column (modifier=modifier.border(1.dp, Color.Black).padding(4.dp)) {
//        Row {
//            Text(guideline.name)
//        }
        listOf(guideline.normal, guideline.elevated, guideline.htn1, guideline.htn2, guideline.htn3).forEach { cat ->
            Row (Modifier.border(1.dp, Color.Gray.copy(alpha = 0.3f), shape = RectangleShape)){
                Text(cat.name, Modifier.weight(1f))
                Text(cat.upperRange.toDisplayString(), textAlign = TextAlign.Center, modifier=Modifier.weight(1f))
                Text(cat.lowerRange.toDisplayString(), textAlign = TextAlign.Center, modifier=Modifier.weight(1f))
            }
        }

    }
}
internal fun IntRange.toDisplayString(): String {
    return if (start == 0){
        "< $endInclusive"
    } else if (endInclusive == Int.MAX_VALUE){
        "$start <"
    } else {
        "$start - $endInclusive"
    }
}
@Composable
fun HorizontalSelector(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit
) {
    Row {
        options.forEach { option ->
            Text(
                text = "[$option]",
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { onOptionSelected(option) }
                    .background(
                        if (option == selectedOption) Color.LightGray else Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                color = if (option == selectedOption) Color.Black else Color.Gray
            )
        }
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
