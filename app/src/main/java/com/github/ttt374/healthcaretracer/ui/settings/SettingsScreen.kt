package com.github.ttt374.healthcaretracer.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.BuildConfig
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import com.github.ttt374.healthcaretracer.data.metric.DayPeriod
import com.github.ttt374.healthcaretracer.data.repository.Config
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.DialogState
import com.github.ttt374.healthcaretracer.ui.common.DialogStateImpl
import com.github.ttt374.healthcaretracer.ui.common.HorizontalSelector
import com.github.ttt374.healthcaretracer.ui.common.SelectableTextFieldDialog
import com.github.ttt374.healthcaretracer.ui.common.TextFieldDialog
import com.github.ttt374.healthcaretracer.ui.common.TimePickerDialog
import com.github.ttt374.healthcaretracer.ui.common.rememberDialogState
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


/////////////////////
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel(), appNavigator: AppNavigator) {
    val config by viewModel.config.collectAsState()
    val timezoneList by viewModel.timezoneList.collectAsState()

    ////////////////////////////////////////////////
    Scaffold(
        topBar = { CustomTopAppBar(stringResource(R.string.settings)) },
        bottomBar = { CustomBottomAppBar(appNavigator) }
    ) { innerPadding ->
        Column (Modifier.padding(innerPadding).padding(16.dp)){
            BloodPressureGuidelineSection(config, { viewModel.updateBloodPressureGuidelineByName(it) })
            TargetVitalsSection(config, onUpdateTargetVital = { type, input ->
                viewModel.updateTargetVital(type, input)
            })
            DayPeriodSection(config, onUpdateDayPeriod = { dayPeriod, localTime ->
                viewModel.updateDayPeriod(dayPeriod, localTime)})
            TimeZoneSection(config, viewModel::updateTimeZone, timezoneList)
            SettingsRow("Version") { Text(BuildConfig.VERSION_NAME) }
        }
    }
}
@Composable
fun BloodPressureGuidelineSection(config: Config, onUpdateBloodPressureByName: (String) -> Unit){
    val bpGuidelineState = rememberDialogState()

    SettingsRow(stringResource(R.string.htn_guideline)) {
        Row (Modifier.clickable { bpGuidelineState.toggle() }) {
            Text(config.bloodPressureGuideline.name)
            if (bpGuidelineState.isOpen) Icon(Icons.Filled.ExpandLess, "close") else
                Icon(Icons.Filled.ExpandMore, "expand")
        }
    }
    if (bpGuidelineState.isOpen){
        HorizontalSelector(BloodPressureGuideline.entries.map { it.name }, config.bloodPressureGuideline.name,
            onOptionSelected = { selected ->
                onUpdateBloodPressureByName(selected)
                //onSaveConfig(config.updateBloodPressureGuidelineByName(selected))
            } )
        BpGuidelineTable(config.bloodPressureGuideline, modifier=Modifier.padding(start = 4.dp))
    }
}
@Composable
fun <T : TargetVitalsType> rememberDialogStates(entries: List<T>): Map<T, DialogState> {
    return remember { entries.associateWith { DialogStateImpl() } }
}
fun <T> Map<T, DialogState>.isOpen(type: T): Boolean = this[type]?.isOpen == true
fun <T> Map<T, DialogState>.close(type: T) = this[type]?.close()

@Composable
fun TargetVitalsSection(config: Config, onUpdateTargetVital: (TargetVitalsType, String) -> Unit){
    val targetVitalsDialogStates = rememberDialogStates(TargetVitalsType.entries)

    TargetVitalsType.entries.forEach { targetVitalsType ->
        if (targetVitalsDialogStates.isOpen(targetVitalsType)) {
            TextFieldDialog(
                title = { Text(stringResource(targetVitalsType.resId)) },
                initialValue = targetVitalsType.getStringValue(config.targetVitals),
                onConfirm = { input -> onUpdateTargetVital(targetVitalsType, input) },
                validate = { targetVitalsType.validate(it) },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = targetVitalsType.keyboardType),
                closeDialog = { targetVitalsDialogStates.close(targetVitalsType) }
            )
        }
    }
    TargetVitalsType.entries.forEach { targetVital ->
        SettingsRow(stringResource(targetVital.resId)){
            Text(targetVital.getStringValue(config.targetVitals),
                Modifier.clickable { targetVitalsDialogStates[targetVital]?.open() })
        }
    }
}

//@Composable
//fun rememberDayPeriodDialogStates(entries: List<DayPeriod> ): Map<DayPeriod, DialogState> {
//    return remember { entries.associateWith { DialogStateImpl() } }
//}
@Composable
fun <T : Enum<T>> rememberDayPeriodDialogStates(entries: Array<T>): Map<T, DialogState> {
    return remember { entries.associateWith { DialogStateImpl() } }
}
@Composable
fun DayPeriodSection(config: Config, onUpdateDayPeriod: (DayPeriod, LocalTime) -> Unit){
    val dayPeriodDialogStates = rememberDayPeriodDialogStates(DayPeriod.entries.toTypedArray())
    DayPeriod.entries.forEach { dayPeriod ->

        if (dayPeriodDialogStates.isOpen(dayPeriod)){
            LocalTimeDialog(config.dayPeriodConfig[dayPeriod],
                onTimeSelected = { onUpdateDayPeriod(dayPeriod, it) },
                onDismiss = { dayPeriodDialogStates.close(dayPeriod)})
        }
    }
    val localTimeFormat = remember { DateTimeFormatter.ofPattern("h:mm a").withLocale(Locale.getDefault()).withZone(config.zoneId) }
    DayPeriod.entries.forEach { dayPeriod ->
        SettingsRow(stringResource(dayPeriod.resId)){
            Text(config.dayPeriodConfig[dayPeriod].format(localTimeFormat),
                modifier = Modifier.clickable { dayPeriodDialogStates[dayPeriod]?.open() })
        }
    }
}
@Composable
fun TimeZoneSection(config: Config, onUpdateTimeZone: (String) -> Unit, timezoneList: List<String>){
    val zoneIdDialogState = rememberDialogState()
    if (zoneIdDialogState.isOpen){
        SelectableTextFieldDialog(title = { Text(stringResource(R.string.timeZone))}, config.zoneId.toString(), selectableList = timezoneList,
            onConfirm = { onUpdateTimeZone(it) },
            closeDialog = { zoneIdDialogState.close()},
            validate = ConfigValidator::validateZoneId
        )
    }
    SettingsRow(stringResource(R.string.timeZone)){
        Text(config.zoneId.toString(), Modifier.clickable { zoneIdDialogState.open()})
    }
}
/////////////////////////////
@Composable
fun SettingsRow(label: String, onClick: (() -> Unit)? = null, content: @Composable () -> Unit ){
    Row(modifier= Modifier.clickable(onClick != null) { onClick?.invoke() }.padding(4.dp)) {
        Text(label, Modifier.weight(1f))
        content()
     }
}

@Composable
fun BpGuidelineTable (guideline: BloodPressureGuideline, modifier: Modifier = Modifier){
    Column (modifier=modifier.border(1.dp, Color.Black).padding(4.dp)) {
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
fun LocalTimeDialog(localTime: LocalTime, onTimeSelected: (LocalTime) -> Unit, onDismiss: () -> Unit){
    val zoneId = ZoneId.systemDefault()

    TimePickerDialog(localTime.atDate(LocalDate.now()).atZone(zoneId).toInstant(),
        onTimeSelected = { onTimeSelected(it.atZone(zoneId).toLocalTime())},
        onDismiss = onDismiss, zoneId = zoneId)
}
//@Composable
//fun LocalTimeRangeDialog(range: LocalTimeRange, isStart: Boolean, onTimeSelected: (LocalTimeRange) -> Unit, onDismiss: () -> Unit){
//    val zone = ZoneId.systemDefault()
//    if (isStart){
//        TimePickerDialog(range.start.atDate(LocalDate.now()).atZone(zone).toInstant(),
//            onTimeSelected = { onTimeSelected(range.copy(start = it.atZone(zone).toLocalTime()))},
//            onDismiss = onDismiss)
//    } else {
//        TimePickerDialog(range.endInclusive.atDate(LocalDate.now()).atZone(zone).toInstant(),
//            onTimeSelected = { onTimeSelected(range.copy(endInclusive = it.atZone(zone).toLocalTime()))},
//            onDismiss = onDismiss)
//    }
//}
//

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

//@Composable
//fun TargetBpDialog (bp: BloodPressure?, onConfirm: (BloodPressure) -> Unit, closeDialog: () -> Unit){
////fun TargetBpDialog (bpUpper: Int, bpLower: Int, onConfirm: (Int, Int) -> Unit, closeDialog: () -> Unit){
//    var bpUpperString by remember { mutableStateOf(bp?.upper.toString())}
//    var bpLowerString by remember { mutableStateOf(bp?.lower.toString())}
//    val numberKeyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
//
//    // focus requesters
//    val bpUpperFocusRequester = remember { FocusRequester() }
//    val bpLowerFocusRequester = remember { FocusRequester() }
//    val confirmButtonFocusRequester = remember { FocusRequester() }
//    //val focusManager = FocusManager(listOf(bpUpperFocusRequester, bpLowerFocusRequester, confirmButtonFocusRequester))
//
//    ConfirmDialog(title = @Composable { Text(stringResource(R.string.targetBp))},
//        text = @Composable {
//            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
//                OutlinedTextField(bpUpperString, {
//                    bpUpperString = it
//                    if ((it.toIntOrNull() ?: 0) > MIN_BP) bpLowerFocusRequester.requestFocus()},
//                        //focusManager.shiftFocusIf { (it.toIntOrNull() ?: 0) > MIN_BP }},
//                    Modifier.weight(1f).focusRequester(bpUpperFocusRequester), keyboardOptions = numberKeyboardOptions)
//                Text(" / ")
//                OutlinedTextField(bpLowerString, { bpLowerString = it; if ((it.toIntOrNull() ?: 0) > MIN_BP) confirmButtonFocusRequester.requestFocus()},
//                    Modifier.weight(1f).focusRequester(bpLowerFocusRequester), keyboardOptions = numberKeyboardOptions)
//            }
//        },
//        confirmButton = {
//            OutlinedButton(onClick = {
//                onConfirm(BloodPressure(bpUpperString.toIntOrNull() ?: 0,bpLowerString.toIntOrNull() ?: 0));
//            }, modifier = Modifier.focusRequester(confirmButtonFocusRequester).focusTarget()
//            ) {
//                Text("OK")
//            }
//        },
//        onConfirm = { onConfirm(BloodPressure(bpUpperString.toIntOrNull() ?: 0,bpLowerString.toIntOrNull() ?: 0)) },
//        closeDialog = closeDialog
//        )
//}