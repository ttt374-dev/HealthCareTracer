package com.github.ttt374.healthcaretracer.ui.common


import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    selectedInstant: Instant,
    onDateSelected: (Instant) -> Unit,
    onDismiss: () -> Unit,
    zoneId: ZoneId,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedInstant.toEpochMilli()
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val newLocalDate = Instant.ofEpochMilli(millis).atZone(zoneId).toLocalDate()
                    val updatedInstant = selectedInstant.withLocalDate(newLocalDate, zoneId)
                    onDateSelected(updatedInstant)
                }
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

fun Instant.withLocalDate(localDate: LocalDate, zoneId: ZoneId): Instant {
    val currentDateTime = this.atZone(zoneId).toLocalTime() // 既存の時刻を保持
    return localDate.atTime(currentDateTime).atZone(zoneId).toInstant()
}