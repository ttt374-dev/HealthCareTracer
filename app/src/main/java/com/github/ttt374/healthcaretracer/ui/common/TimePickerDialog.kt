package com.github.ttt374.healthcaretracer.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    selectedInstant: Instant,
    onTimeSelected: (Instant) -> Unit,
    onDismiss: () -> Unit,
    zoneId: ZoneId = ZoneId.systemDefault(),
    is24Hour: Boolean = false,
) {
    val localTime = remember(selectedInstant) { selectedInstant.atZone(zoneId).toLocalTime() }
    val timePickerState = rememberTimePickerState(
        initialHour = localTime.hour,
        initialMinute = localTime.minute,
        is24Hour = is24Hour,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val newLocalTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                val updatedInstant = selectedInstant.withLocalTime(newLocalTime, zoneId)
                onTimeSelected(updatedInstant)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}

fun Instant.withLocalTime(localTime: LocalTime, zoneId: ZoneId = ZoneId.systemDefault()): Instant {
    val currentDate = this.atZone(zoneId).toLocalDate() // 既存の日付を保持
    return localTime.atDate(currentDate).atZone(zoneId).toInstant()
}
