package com.github.ttt374.healthcaretracer.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// filter
@Composable
fun TimeRangeDropdown(selectedRange: TimeRange, onRangeSelected: (TimeRange) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val ranges = TimeRange.entries

    //Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
    Box (modifier= Modifier.padding(8.dp)){
        OutlinedButton(onClick = { expanded = true }) {
            Text(selectedRange.label)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ranges.forEach { range ->
                DropdownMenuItem(text = { Text(range.label) }, onClick = {
                    onRangeSelected(range)
                    expanded = false
                })
            }
        }
    }

}

enum class TimeRange(val days: Long?, val label: String) {
    ONE_WEEK(7, "1 Week"),
    ONE_MONTH(30, "1 Month"),
    SIX_MONTHS(180, "6 Months"),
    ONE_YEAR(365, "1 Year"),
    FULL(null, "Full Range")
}
