package com.github.ttt374.healthcaretracer.ui.common

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.res.stringResource
import com.github.ttt374.healthcaretracer.ui.analysis.TimeRange

// filter
@Composable
fun TimeRangeDropdown(selectedRange: TimeRange, onRangeSelected: (TimeRange) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    val ranges = TimeRange.entries

    //Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
    Box (modifier= modifier){
        OutlinedButton(onClick = { expanded = true }) {
            Text(stringResource(selectedRange.labelRes))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            ranges.forEach { range ->
                DropdownMenuItem(text = { Text(stringResource(range.labelRes)) }, onClick = {
                    onRangeSelected(range)
                    expanded = false
                })
            }
        }
    }

}

