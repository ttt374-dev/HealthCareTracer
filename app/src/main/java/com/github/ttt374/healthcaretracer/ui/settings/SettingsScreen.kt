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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.BuildConfig
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import com.github.ttt374.healthcaretracer.data.bloodpressure.toBloodPressure
import com.github.ttt374.healthcaretracer.data.item.Vitals
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
import java.time.DateTimeException
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class TargetVitalsType(val resId: Int, val selector: (Vitals) -> Any?, val validator: (String) -> Boolean, val keyboardType: KeyboardType) {
    BpUpper(R.string.targetBpUpper, { vitals: Vitals -> vitals.bp?.upper }, ConfigValidator::validatePositiveInt, KeyboardType.Decimal),
    BpLower(R.string.targetBpLower, { vitals: Vitals -> vitals.bp?.lower }, ConfigValidator::validatePositiveInt, KeyboardType.Decimal),
    BodyWeight(R.string.targetBodyWeight, { vitals: Vitals -> vitals.bodyWeight }, ConfigValidator::validatePositiveDouble, KeyboardType.Number),;

    fun updateTargetVitals(config: Config, input: String): Vitals{
        return when (this) {  // TODO:  refactor
            BpUpper -> config.targetVitals.copy(bp = (input.toIntOrNull() to config.targetVitals.bp?.lower).toBloodPressure())
            BpLower -> config.targetVitals.copy(bp = (config.targetVitals.bp?.upper to input.toIntOrNull()).toBloodPressure())
            BodyWeight -> config.targetVitals.copy(bodyWeight = input.toDoubleOrNull())
        }
    }
}

object ConfigValidator {
    fun validatePositiveInt(input: String): Boolean =
        input.toIntOrNull()?.let { it > 0 } == true

    fun validatePositiveDouble(input: String): Boolean =
        input.toDoubleOrNull()?.let { it > 0.0} == true

    fun validateZoneId(input: String): Boolean =
        input.isNotBlank() && try { ZoneId.of(input); true } catch (e: DateTimeException){ false}
}

/////////////////////
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel(), appNavigator: AppNavigator) {
    val config by viewModel.config.collectAsState()
    val timezoneList by viewModel.timezoneList.collectAsState()
    //val configValidator: ConfigValidator = ConfigValidator()

    ////////////////////////////////////////////////
    Scaffold(
        topBar = { CustomTopAppBar(stringResource(R.string.settings)) },
        bottomBar = { CustomBottomAppBar(appNavigator) }
    ) { innerPadding ->
        Column (Modifier.padding(innerPadding).padding(16.dp)){
            BloodPressureGuidelineSection(config, viewModel::saveConfig)
            TargetVitalsSection(config, viewModel::saveConfig)
            DayPeriodSection(config, viewModel::saveConfig)
            TimeZoneSection(config, viewModel::saveConfig, timezoneList)
        }
    }
}
@Composable
fun BloodPressureGuidelineSection(config: Config, onSaveConfig: (Config) -> Unit){
    val bpGuidelineState = rememberDialogState()
    if (bpGuidelineState.isOpen){
        HorizontalSelector(BloodPressureGuideline.entries.map { it.name }, config.bloodPressureGuideline.name,
            onOptionSelected = { selected ->
                val guideline = BloodPressureGuideline.entries.find { it.name == selected } ?: BloodPressureGuideline.Default
                onSaveConfig(config.copy(bloodPressureGuideline = guideline))
            } )
        BpGuidelineTable(config.bloodPressureGuideline, modifier=Modifier.padding(start = 4.dp))
    }
    SettingsRow(stringResource(R.string.htn_guideline)) {
        Row (Modifier.clickable { bpGuidelineState.toggle() }) {
            Text(config.bloodPressureGuideline.name)
            if (bpGuidelineState.isOpen) Icon(Icons.Filled.ExpandLess, "close") else
                Icon(Icons.Filled.ExpandMore, "expand")
        }
    }
}

@Composable
fun rememberTargetVitalsDialogStates(): Map<TargetVitalsType, DialogState> {
    return remember { TargetVitalsType.entries.associateWith { DialogStateImpl() }}
}
@Composable
fun TargetVitalsSection(config: Config, onSaveConfig: (Config) -> Unit){
    val targetVitalsDialogState = rememberTargetVitalsDialogStates()
    TargetVitalsType.entries.forEach { targetVitalsType ->
        if (targetVitalsDialogState.getValue(targetVitalsType).isOpen) {
            TextFieldDialog(
                title = { Text(stringResource(targetVitalsType.resId)) },
                initialValue = targetVitalsType.selector(config.targetVitals)?.toString() ?: "",
                onConfirm = { input ->
                    val updatedVitals = targetVitalsType.updateTargetVitals(config, input)
//                    val updatedVitals = when (targetVital) {  // TODO:  refactor
//                        TargetVitalsType.BpUpper ->   config.targetVitals.copy(bp = (input.toIntOrNull() to config.targetVitals.bp?.lower).toBloodPressure())
//                        TargetVitalsType.BpLower -> config.targetVitals.copy(bp = (config.targetVitals.bp?.upper to input.toIntOrNull()).toBloodPressure())
//                        TargetVitalsType.BodyWeight -> config.targetVitals.copy(bodyWeight = input.toDoubleOrNull())
//                    }
                    onSaveConfig(config.copy(targetVitals = updatedVitals))
                },
                validate = targetVitalsType.validator,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = targetVitalsType.keyboardType),
                closeDialog = { targetVitalsDialogState[targetVitalsType]?.close() }
            )
        }
    }
    TargetVitalsType.entries.forEach { targetVital ->
        SettingsRow(stringResource(targetVital.resId)){
            Text(targetVital.selector(config.targetVitals)?.toString() ?: "",
                Modifier.clickable { targetVitalsDialogState[targetVital]?.open() })
        }
    }
}
@Composable
fun rememberDayPeriodDialogStates(): Map<DayPeriod, DialogState> {
    return remember {
        DayPeriod.entries.associateWith { DialogStateImpl() }
    }
}
@Composable
fun DayPeriodSection(config: Config, onSaveConfig: (Config) -> Unit){
    val dayPeriodDialogState = rememberDayPeriodDialogStates()
    DayPeriod.entries.forEach { dayPeriod ->
        if (dayPeriodDialogState.getValue(dayPeriod).isOpen){
            LocalTimeDialog(config.dayPeriodConfig[dayPeriod],
                onTimeSelected = {
                    val timeOfDayConfig = config.dayPeriodConfig.update(dayPeriod, it)
                    onSaveConfig(config.copy(dayPeriodConfig = timeOfDayConfig))
                },
                onDismiss = { dayPeriodDialogState[dayPeriod]?.close()})
        }
    }
    val localTimeFormat = DateTimeFormatter.ofPattern("h:mm a").withLocale(Locale.getDefault())  // .withZone(ZoneId.systemDefault())
    DayPeriod.entries.forEach { dayPeriod ->
        SettingsRow(stringResource(dayPeriod.resId)){
            Text(config.dayPeriodConfig[dayPeriod].format(localTimeFormat),
                //Text(dayPeriod.takeStartValue(config.timeOfDayConfig).format(localTimeFormat),
                modifier = Modifier.clickable { dayPeriodDialogState[dayPeriod]?.open() })
        }
    }
}
@Composable
fun TimeZoneSection(config: Config, onSaveConfig: (Config) -> Unit, timezoneList: List<String>){
    val zoneIdDialogState = rememberDialogState()
    if (zoneIdDialogState.isOpen){
        SelectableTextFieldDialog(title = { Text(stringResource(R.string.timeZone))}, config.zoneId.toString(), selectableList = timezoneList,
            onConfirm = {
                val zoneId = runCatching { ZoneId.of(it) }.getOrNull()
                if (zoneId != null) onSaveConfig(config.copy(zoneId = zoneId))
            },
            closeDialog = { zoneIdDialogState.close()},
            validate = ConfigValidator::validateZoneId
        )
        SettingsRow(stringResource(R.string.timeZone)){
            Text(config.zoneId.toString(), Modifier.clickable { zoneIdDialogState.open()})
        }
        SettingsRow("Version") { Text(BuildConfig.VERSION_NAME) }
    }
}
/////////////////////////////
@Composable
fun SettingsRow(label: String, onClick: (() -> Unit)? = null, content: @Composable () -> Unit ){
    Row(modifier= Modifier.padding(4.dp).clickable(onClick != null) { onClick?.invoke() }) {
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