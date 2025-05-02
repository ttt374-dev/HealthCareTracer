package com.github.ttt374.healthcaretracer.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.BuildConfig
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import com.github.ttt374.healthcaretracer.data.bloodpressure.toAnnotatedString
import com.github.ttt374.healthcaretracer.data.bloodpressure.toBloodPressure
import com.github.ttt374.healthcaretracer.data.item.MIN_BP
import com.github.ttt374.healthcaretracer.data.metric.DayPeriod
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.shared.toBodyWeightString
import com.github.ttt374.healthcaretracer.ui.common.ConfirmDialog
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.DialogState
import com.github.ttt374.healthcaretracer.ui.common.DialogStateImpl
import com.github.ttt374.healthcaretracer.ui.common.HorizontalSelector
import com.github.ttt374.healthcaretracer.ui.common.SelectableTextFieldDialog
import com.github.ttt374.healthcaretracer.ui.common.TextFieldDialog
import com.github.ttt374.healthcaretracer.ui.common.TimePickerDialog
import com.github.ttt374.healthcaretracer.ui.common.rememberDialogState
import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class TargetVitals(val resId: Int) { BloodPressure(R.string.targetBp), BodyWeight(R.string.targetBodyWeight) }

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel(), appNavigator: AppNavigator) {
    val config by viewModel.config.collectAsState()

    // dialogs
    val decimalKeyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal)
    val numberKeyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
    val bpGuidelineState = rememberDialogState()
    val validatePosInt = { value: String -> value.toIntOrNull()?.let { it > 0} ?: false}
    val validatePosDouble = { value: String -> value.toDoubleOrNull()?.let { it > 0.0 } ?: false}

    val targetBpUpperDialogState = rememberDialogState()
    if (targetBpUpperDialogState.isOpen){
        TextFieldDialog(title = { Text(stringResource(R.string.targetBpUpper))},
            initialValue = config.targetVitals.bp?.upper.toString(),
            onConfirm = {
                val vitals = config.targetVitals.copy(bp = (it.toIntOrNull() to config.targetVitals.bp?.lower).toBloodPressure())
                viewModel.saveConfig(config.copy(targetVitals = vitals))},
            validate = validatePosInt,
            keyboardOptions = numberKeyboardOptions,
            closeDialog = { targetBpUpperDialogState.close() }
        )
    }
    val targetBpLowerDialogState = rememberDialogState()
    if (targetBpLowerDialogState.isOpen){
        TextFieldDialog(title = { Text(stringResource(R.string.targetBpLower))},
            initialValue = config.targetVitals.bp?.lower.toString(),
            onConfirm = {
                val vitals = config.targetVitals.copy(bp = (it.toIntOrNull() to config.targetVitals.bp?.lower).toBloodPressure())
                viewModel.saveConfig(config.copy(targetVitals = vitals))},
            validate = validatePosInt,
            keyboardOptions = numberKeyboardOptions,
            closeDialog = { targetBpLowerDialogState.close() }
        )
    }
    val targetBodyWeightDialogState = rememberDialogState()
    if (targetBodyWeightDialogState.isOpen){
        TextFieldDialog(title = { Text(stringResource(R.string.targetBodyWeight))},
            initialValue = config.targetVitals.bodyWeight.toString(),
            onConfirm = {
                val vitals = config.targetVitals.copy(bodyWeight = it.toDoubleOrNull())
                viewModel.saveConfig(config.copy(targetVitals = vitals))},
            validate = validatePosDouble,
            keyboardOptions = decimalKeyboardOptions,
            closeDialog = { targetBodyWeightDialogState.close() }
        )
    }
//    @Composable
//    fun rememberTargetDialogStates(): Map<TargetVitals, DialogState> {
//        return remember { TargetVitals.entries.associateWith { DialogStateImpl() }}
//    }
//    val targetVitalsDialogState = rememberTargetDialogStates()
//
//    if (targetVitalsDialogState[TargetVitals.BloodPressure]?.isOpen == true){
//        TargetBpDialog(config.targetVitals.bp, onConfirm = { bp ->
//            val newVitals = config.targetVitals.copy(bp = bp)
//            viewModel.saveConfig(config.copy(targetVitals = newVitals))
//        }, closeDialog = { targetVitalsDialogState[TargetVitals.BloodPressure]?.close() })
//    }
//    if (targetVitalsDialogState[TargetVitals.BodyWeight]?.isOpen == true)
//        TextFieldDialog(title = { Text(stringResource(R.string.targetBodyWeight))}, config.targetVitals.bodyWeight.toString(), onConfirm = {
//            val newVitals = config.targetVitals.copy(bodyWeight = it.toDoubleOrNull())
//            viewModel.saveConfig(config.copy(targetVitals = newVitals))
//        },
//            closeDialog = { targetVitalsDialogState[TargetVitals.BodyWeight]?.close() },
//            validate = { it.toDoubleOrNull()?.let { it > 0} ?: false },
//            keyboardOptions = decimalKeyboardOptions)
//

    @Composable
    fun rememberDayPeriodDialogStates(): Map<DayPeriod, DialogState> {
        return remember {
            DayPeriod.entries.associateWith { DialogStateImpl() }
        }
    }
    val dayPeriodDialogState = rememberDayPeriodDialogStates()

    DayPeriod.entries.forEach { dayPeriod ->
        if (dayPeriodDialogState[dayPeriod]?.isOpen == true){
            LocalTimeDialog(config.dayPeriodConfig[dayPeriod],
                onTimeSelected = {
                    //val timeOfDayConfig = config.timeOfDayConfig.copy(morning = it)
                    val timeOfDayConfig = config.dayPeriodConfig.update(dayPeriod, it)
                    viewModel.saveConfig(config.copy(dayPeriodConfig = timeOfDayConfig))
                },
                onDismiss = { dayPeriodDialogState[dayPeriod]?.close()})
        }
    }
    val zoneIdDialogState = rememberDialogState()
    if (zoneIdDialogState.isOpen){
        val context = LocalContext.current
        val timeZoneStrList = remember { context.resources.getStringArray(R.array.timezone_list).toList() }
        SelectableTextFieldDialog(title = { Text(stringResource(R.string.timeZone))}, config.zoneId.toString(), selectableList = timeZoneStrList,
            onConfirm = { viewModel.saveConfig(config.copy(zoneId = ZoneId.of(it)))},
            closeDialog = { zoneIdDialogState.close()},
            validate = { it.isNotBlank() && try { ZoneId.of(it); true } catch (e: DateTimeException){ false} })

//        SelectableTextFieldDialog(title = { Text(stringResource(R.string.timeZone))}, config.zoneId.toString(), selectableList = timeZoneStrList, onConfirm = {
//            try {
//                val zoneId = ZoneId.of(it)
//                viewModel.saveConfig(config.copy(zoneId = zoneId))
//                zoneIdDialogState.close()
//            } catch (e: Exception){
//                Log.e("zoneId", e.message.toString())
//            }
//        },
//            closeDialog = { zoneIdDialogState.close()})
    }
    //val localeSelectorState = rememberDialogState()
    val localTimeFormat = DateTimeFormatter.ofPattern("h:mm a")  // .withZone(ZoneId.systemDefault())
    ////////////////////////////////////////////////
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
            }
            if (bpGuidelineState.isOpen){
                HorizontalSelector(BloodPressureGuideline.entries.map { it.name }, config.bloodPressureGuideline.name,
                    onOptionSelected = { selected ->
                        val guideline = BloodPressureGuideline.entries.find { it.name == selected } ?: BloodPressureGuideline.Default
                        viewModel.saveConfig(config.copy(bloodPressureGuideline = guideline))
                    } )
                BpGuidelineTable(config.bloodPressureGuideline, modifier=Modifier.padding(start = 4.dp))
            }
            SettingsRow(stringResource(R.string.targetBpUpper)){
                Text(config.targetVitals.bp?.upper.toString(),
                    Modifier.clickable { targetBpUpperDialogState.open() })
            }
            SettingsRow(stringResource(R.string.targetBpLower)){
                Text(config.targetVitals.bp?.lower.toString(),
                    Modifier.clickable { targetBpLowerDialogState.open() })
            }
            SettingsRow(stringResource(R.string.targetBodyWeight)){
                Text(config.targetVitals.bodyWeight.toString(),
                    Modifier.clickable { targetBodyWeightDialogState.open() })
            }

            DayPeriod.entries.forEach { dayPeriod ->
                SettingsRow(stringResource(dayPeriod.resId)){
                    Text(config.dayPeriodConfig[dayPeriod].format(localTimeFormat),
                    //Text(dayPeriod.takeStartValue(config.timeOfDayConfig).format(localTimeFormat),
                        modifier = Modifier.clickable { dayPeriodDialogState[dayPeriod]?.open() })
                }
            }
            SettingsRow(stringResource(R.string.timeZone)){
                Text(config.zoneId.toString(), Modifier.clickable { zoneIdDialogState.open()})
            }
            SettingsRow("Version") { Text(BuildConfig.VERSION_NAME) }
        }
    }
}
@Composable
fun SettingsRow(label: String, onClick: (() -> Unit)? = null, content: @Composable () -> Unit ){
    Row(modifier= Modifier.padding(4.dp).clickable(onClick != null) { onClick?.invoke() }) {
        Text(label, Modifier.weight(1f))
        content()
     }
}
@Composable
fun TargetBpDialog (bp: BloodPressure?, onConfirm: (BloodPressure) -> Unit, closeDialog: () -> Unit){
//fun TargetBpDialog (bpUpper: Int, bpLower: Int, onConfirm: (Int, Int) -> Unit, closeDialog: () -> Unit){
    var bpUpperString by remember { mutableStateOf(bp?.upper.toString())}
    var bpLowerString by remember { mutableStateOf(bp?.lower.toString())}
    val numberKeyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)

    // focus requesters
    val bpUpperFocusRequester = remember { FocusRequester() }
    val bpLowerFocusRequester = remember { FocusRequester() }
    val confirmButtonFocusRequester = remember { FocusRequester() }
    //val focusManager = FocusManager(listOf(bpUpperFocusRequester, bpLowerFocusRequester, confirmButtonFocusRequester))

    ConfirmDialog(title = @Composable { Text(stringResource(R.string.targetBp))},
        text = @Composable {
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
        },
        confirmButton = {
            OutlinedButton(onClick = {
                onConfirm(BloodPressure(bpUpperString.toIntOrNull() ?: 0,bpLowerString.toIntOrNull() ?: 0));
            }, modifier = Modifier.focusRequester(confirmButtonFocusRequester).focusTarget()
            ) {
                Text("OK")
            }
        },
        onConfirm = { onConfirm(BloodPressure(bpUpperString.toIntOrNull() ?: 0,bpLowerString.toIntOrNull() ?: 0)) },
        closeDialog = closeDialog
        )
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