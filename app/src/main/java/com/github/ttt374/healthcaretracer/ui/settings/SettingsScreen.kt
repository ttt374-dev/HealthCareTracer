package com.github.ttt374.healthcaretracer.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.BuildConfig
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import com.github.ttt374.healthcaretracer.data.item.MIN_BP
import com.github.ttt374.healthcaretracer.data.repository.LocalTimeRange
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.ConfirmDialog
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.TextFieldDialog
import com.github.ttt374.healthcaretracer.ui.common.TimePickerDialog
import com.github.ttt374.healthcaretracer.ui.common.rememberDialogState
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel(), appNavigator: AppNavigator) {
    val config by viewModel.config.collectAsState()

    // dialogs
    val decimalKeyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)

    val bpGuidelineState = rememberDialogState()

    val targetBpState = rememberDialogState()
    if (targetBpState.isOpen){
        TargetBpDialog(config.targetBpUpper, config.targetBpLower, onConfirm = { upper, lower ->
            viewModel.saveConfig(config.copy(targetBpUpper = upper, targetBpLower = lower))
        }, closeDialog = { targetBpState.close() })
    }
//    val targetBpUpperState = rememberDialogState()
//    if (targetBpUpperState.isOpen)
//        TextFieldDialog(config.targetBpUpper.toString(),
//            onConfirm = {
//                viewModel.saveConfig(config.copy(targetBpUpper = it.toInt()))
//            },
//            closeDialog = { targetBpUpperState.close() },
//            keyboardOptions = numberKeyboardOptions,
//        )
//
//    val targetBpLowerState = rememberDialogState()
//    if (targetBpLowerState.isOpen)
//        TextFieldDialog(config.targetBpLower.toString(), onConfirm = {
//            viewModel.saveConfig(config.copy(targetBpLower = it.toInt()))
//        },
//            closeDialog = { targetBpLowerState.close() },
//            keyboardOptions = numberKeyboardOptions)

    val targetBodyWeightState = rememberDialogState()
    if (targetBodyWeightState.isOpen)
        TextFieldDialog(config.targetBodyWeight.toString(), onConfirm = {
            viewModel.saveConfig(config.copy(targetBodyWeight = it.toDouble()))
        },
            closeDialog = { targetBodyWeightState.close() },
            keyboardOptions = decimalKeyboardOptions)

    val morningStartState = rememberDialogState()
    if (morningStartState.isOpen){
        LocalTimeDialog(config.timeOfDayConfig.morning,
            onTimeSelected = {
                val timeOfDayConfig = config.timeOfDayConfig.copy(morning = it)
                viewModel.saveConfig(config.copy(timeOfDayConfig = timeOfDayConfig))
            },
            onDismiss = { morningStartState.close()})
    }
    val afternoonStartState = rememberDialogState()
    if (afternoonStartState.isOpen){
        LocalTimeDialog(config.timeOfDayConfig.afternoon,
            onTimeSelected = {
                val timeOfDayConfig = config.timeOfDayConfig.copy(afternoon = it)
                viewModel.saveConfig(config.copy(timeOfDayConfig = timeOfDayConfig))
            },
            onDismiss = { afternoonStartState.close()})
    }
    val eveningStartState = rememberDialogState()
    if (eveningStartState.isOpen){
        LocalTimeDialog(config.timeOfDayConfig.evening,
            onTimeSelected = {
                val timeOfDayConfig = config.timeOfDayConfig.copy(evening = it)
                viewModel.saveConfig(config.copy(timeOfDayConfig = timeOfDayConfig))
            },
            onDismiss = { eveningStartState.close()})
    }

    //val localeSelectorState = rememberDialogState()

    val localTimeFormat = DateTimeFormatter.ofPattern("h:mm a")  // .withZone(ZoneId.systemDefault())
    ///
    Scaffold(
        topBar = { CustomTopAppBar(stringResource(R.string.settings)) },
        bottomBar = { CustomBottomAppBar(appNavigator) }
    ) { innerPadding ->
        Column (Modifier.padding(innerPadding).padding(16.dp)){
            SettingsRow(stringResource(R.string.htn_guideline)) {
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
                HorizontalSelector(BloodPressureGuideline.entries.map { it.name }, config.bloodPressureGuideline.name,
                    onOptionSelected = { selected ->
                        val guideline = BloodPressureGuideline.entries.find { it.name == selected } ?: BloodPressureGuideline.Default
                        viewModel.saveConfig(config.copy(bloodPressureGuideline = guideline))
                    } )
                BpGuidelineTable(config.bloodPressureGuideline, modifier=Modifier.padding(start = 4.dp))
            }
            SettingsRow(stringResource(R.string.targetBp)){
                Text("${config.targetBpUpper} / ${config.targetBpLower}", Modifier.clickable { targetBpState.open() })

//                Text(config.targetBpUpper.toString(), Modifier.clickable { targetBpUpperState.open() })
//                Text(" / ")
//                Text(config.targetBpLower.toString(), Modifier.clickable { targetBpLowerState.open() })
            }
            SettingsRow("${stringResource(R.string.targetBodyWeight)} (Kg)"){
                Text(config.targetBodyWeight.toString(), Modifier.clickable {  targetBodyWeightState.open() })
            }
            SettingsRow(stringResource(R.string.morning)){
                Text(config.timeOfDayConfig.morning.format(localTimeFormat), Modifier.clickable { morningStartState.open() })
            }
            SettingsRow(stringResource(R.string.afternoon)){
                Text(config.timeOfDayConfig.afternoon.format(localTimeFormat), Modifier.clickable { afternoonStartState.open() })
            }
            SettingsRow(stringResource(R.string.evening)){
                Text(config.timeOfDayConfig.evening.format(localTimeFormat), Modifier.clickable { eveningStartState.open() })
            }
//            SettingsRow(stringResource(R.string.morning_range)){
//                Text(config.morningRange.start.format(localTimeFormat), Modifier.clickable { morningRangeStartState.open()})
//                Text(" - ")
//                Text(config.morningRange.endInclusive.format(localTimeFormat), Modifier.clickable { morningRangeEndState.open() })
//            }
//
//            SettingsRow(stringResource(R.string.evening_range)){
//                Text(config.eveningRange.start.format(localTimeFormat), Modifier.clickable { eveningRangeStartState.open() })
//                Text(" - ")
//                Text(config.eveningRange.endInclusive.format(localTimeFormat), Modifier.clickable { eveningRangeEndState.open() })
//            }
//            SettingsRow(stringResource(R.string.language)) {
//                val locale = Locale.forLanguageTag(config.localeTag)
//
//                Text(locale.getDisplayLanguage(locale), Modifier.clickable { localeSelectorState.open() })
//                if (localeSelectorState.isOpen){
//                    LanguageDropMenu(localeSelectorState.isOpen, config.localeTag,
//                        { newLocaleTag ->
//                            val newLocale = Locale.forLanguageTag(newLocaleTag)
//                            val localesChangedToDefault = AppCompatDelegate.getApplicationLocales()
//                            val createdLocale = LocaleListCompat.create(locale)
//                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(newLocale))
//                            val listcompat = LocaleListCompat.forLanguageTags("en")
//                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
//                            val localesChangedToDefaultChanged = AppCompatDelegate.getApplicationLocales()
//                            viewModel.saveConfig(config.copy(localeTag = newLocaleTag )); localeSelectorState.close() },
//                        onDismissRequest = { localeSelectorState.close()})
//                }
//            }
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
    val numberKeyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)

    // focus requesters
    val bpUpperFocusRequester = remember { FocusRequester() }
    val bpLowerFocusRequester = remember { FocusRequester() }
    val confirmButtonFocusRequester = remember { FocusRequester() }
    //val focusManager = FocusManager(listOf(bpUpperFocusRequester, bpLowerFocusRequester, confirmButtonFocusRequester))


    ConfirmDialog(title = { Text(stringResource(R.string.targetBp))},
        text = {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(bpUpperString, {
                    bpUpperString = it
                    if ((it.toIntOrNull() ?: 0) > MIN_BP) bpLowerFocusRequester.requestFocus()},
                        //focusManager.shiftFocusIf { (it.toIntOrNull() ?: 0) > MIN_BP }},
                    Modifier.weight(1f).focusRequester(bpUpperFocusRequester), keyboardOptions = numberKeyboardOptions)
                Text(" / ")
                OutlinedTextField(bpLowerString, { bpLowerString = it; if ((it.toIntOrNull() ?: 0) > MIN_BP) confirmButtonFocusRequester.requestFocus()},
                    Modifier.weight(1f).focusRequester(bpLowerFocusRequester), keyboardOptions = numberKeyboardOptions)
            }
    }, confirmButton = {
            OutlinedButton(onClick = {
                onConfirm(bpUpperString.toIntOrNull() ?: 0, bpLowerString.toIntOrNull() ?: 0); closeDialog()
                closeDialog() }, modifier = Modifier.focusRequester(confirmButtonFocusRequester).focusTarget()
            ) {
                Text("OK")
            }
        },
        onConfirm = { onConfirm(bpUpperString.toIntOrNull() ?: 0, bpLowerString.toIntOrNull() ?: 0); closeDialog() },
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
                Text(stringResource(cat.nameLabel), Modifier.weight(1f))
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
fun LocalTimeDialog(localTime: LocalTime, onTimeSelected: (LocalTime) -> Unit, onDismiss: () -> Unit){
    val zone = ZoneId.systemDefault()

    TimePickerDialog(localTime.atDate(LocalDate.now()).atZone(zone).toInstant(),
        onTimeSelected = { onTimeSelected(it.atZone(zone).toLocalTime())},
        onDismiss = onDismiss)
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


//@Composable
//fun LanguageDropMenu(expanded: Boolean, currentLanguageTag: String, onSelected: (String) -> Unit, onDismissRequest: () -> Unit ){
//    val languages = listOf("en", "ja", "fr")
//    val currentLocale = Locale.forLanguageTag(currentLanguageTag)
//    DropdownMenu(expanded, onDismissRequest = onDismissRequest){
//        languages.forEach { tag ->
//            val locale = Locale.forLanguageTag(tag)
//            val localeName = locale.getDisplayLanguage(currentLocale)
//            DropdownMenuItem({Text(localeName)}, onClick = {
//                onSelected(tag)
//            })
//        }
//    }
//}