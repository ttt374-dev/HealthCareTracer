package com.github.ttt374.healthcaretracer.ui.common


import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CalendarLocale
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Composable
fun rememberDateTimePickerState(date: Instant, zoneId: ZoneId, is24Hour: Boolean = true, locale: CalendarLocale = Locale.getDefault()): DateTimePickerState =
    remember { DateTimePickerState(date, zoneId, is24Hour, locale) }
/////////////////////////////
class DateTimePickerState (date: Instant, private val zoneId: ZoneId = ZoneId.systemDefault(),
                           is24Hour: Boolean = true, locale: CalendarLocale = Locale.getDefault()){
    private val initialZonedDateTime: ZonedDateTime = ZonedDateTime.ofInstant(date, zoneId)
    @OptIn(ExperimentalMaterial3Api::class)
    val datePickerState = DatePickerState(
        locale = locale,
        initialSelectedDateMillis = initialZonedDateTime.toInstant().toEpochMilli(),
        initialDisplayedMonthMillis = initialZonedDateTime.toInstant().toEpochMilli(),
    )
    @OptIn(ExperimentalMaterial3Api::class)
    val timePickerState = TimePickerState(
        initialZonedDateTime.hour, initialZonedDateTime.minute, is24Hour,
    )
    @OptIn(ExperimentalMaterial3Api::class)
    fun selectedDateTime(): Instant {
        val instant =Instant.ofEpochMilli(datePickerState.selectedDateMillis ?: 0)
        val zonedDateTime =  ZonedDateTime.ofInstant(instant, zoneId)
        return LocalDateTime.of(
            zonedDateTime.year, zonedDateTime.month, zonedDateTime.dayOfMonth,
            timePickerState.hour, timePickerState.minute, zonedDateTime.second,
        ).atZone(zoneId).toInstant()
    }
    @OptIn(ExperimentalMaterial3Api::class)
    fun updateNow(){
        val nowZonedDateTime = getCurrentZonedDateTime(zoneId)
        datePickerState.selectedDateMillis = nowZonedDateTime.toInstant().toEpochMilli()
        timePickerState.hour = nowZonedDateTime.hour
        timePickerState.minute = nowZonedDateTime.minute
    }
    @OptIn(ExperimentalMaterial3Api::class)
    fun updateTime(time: LocalTime){
        timePickerState.hour = time.hour
        timePickerState.minute = time.minute
    }
    private fun getCurrentZonedDateTime(zoneId: ZoneId): ZonedDateTime {
        val now = nowMillis()
        return ZonedDateTime.ofInstant(now, zoneId)
    }
}

////////////////////////////////////////////////////
@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeDialog(date: Instant, zoneId: ZoneId = ZoneId.systemDefault(), is24Hour: Boolean = true,
                   selectNowButton: @Composable (DateTimePickerState) -> Unit = {state ->
                       DateTimePickerSelectNowButton(state)
                   },
                   //openTimePickerButton: @Composable () -> Unit,
                   onConfirm: (Instant) -> Unit,
                   closeDialog: () -> Unit = {}){
    val timePickerVisibleState = rememberDialogState(false)
    val dateTimePickerState = rememberDateTimePickerState(date, zoneId, is24Hour)

    DatePickerDialog(
        onDismissRequest = closeDialog,
        confirmButton = { TextButton(onClick={
            onConfirm(dateTimePickerState.selectedDateTime())
            closeDialog()
        }){ Text("OK")} },
        dismissButton = {
            TextButton(onClick=closeDialog){Text("Cancel")}
        }
    ){
        DatePicker(dateTimePickerState.datePickerState,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, modifier= Modifier.padding(4.dp)) {
                    Text("Select Date/Time")
                    //DateTimePickerSelectNow(dateTimePickerState)
                    selectNowButton(dateTimePickerState)
                    TextButton(onClick = { timePickerVisibleState.open()}){
                        val time = dateTimePickerState.selectedDateTime().atZone(zoneId).toLocalTime() //       .toLocalDateTime(zoneId.toKotlinTimeZone()).time //   .toJavaLocalDateTime().toLocalTime()
                        Text(time.format(getTimeFormatter(is24Hour)))
                    }
                }
            }
        )
    }
    if (timePickerVisibleState.isOpen){
//        TimePickerDialog(LocalTime.of(dateTimePickerState.timePickerState.hour, dateTimePickerState.timePickerState.minute),
//            is24Hour, onConfirm = { time ->
//                dateTimePickerState.updateTime(LocalTime.of(time.hour, time.minute))
//            },
//            closeDialog = { timePickerVisibleState.close() }
//        )
    }
}
fun getTimeFormatter(is24Hour: Boolean): DateTimeFormatter {
    return DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "h:mm a")
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun TimePickerDialog(time: LocalTime, is24Hour: Boolean = false, onConfirm: (LocalTime) -> Unit, closeDialog: () -> Unit){
//    val state = remember { TimePickerState(time.hour, time.minute, is24Hour)}
//
//    ConfirmDialog(
//        title = { Text("select time")},
//        text = { TimePicker(state = state) },
//        onConfirm = { onConfirm(LocalTime.of(state.hour, state.minute))},
//        closeDialog = closeDialog,
//    )
//}
@Composable
fun DateTimePickerSelectNowButton(dateTimePickerState: DateTimePickerState, label: String = "Now"){
    TextButton(onClick = { dateTimePickerState.updateNow() }){
        Text(label)
    }
}

///////////
internal fun nowMillis() : Instant {
    return Instant.now().truncatedTo(ChronoUnit.MILLIS)
}
