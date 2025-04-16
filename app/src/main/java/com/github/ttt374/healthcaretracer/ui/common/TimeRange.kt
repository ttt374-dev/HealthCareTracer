package com.github.ttt374.healthcaretracer.ui.common

import androidx.annotation.StringRes
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.ttt374.healthcaretracer.R

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

enum class TimeRange(val days: Long?,  @StringRes val labelRes: Int) {
    ONE_WEEK(7, R.string.range__1week),
    ONE_MONTH(30, R.string.range__1month),
    SIX_MONTHS(180, R.string.range__6months),
    ONE_YEAR(365, R.string.range__1year),
    FULL(null, R.string.range__full_range);

    companion object {
        val Default = ONE_MONTH
    }
}
