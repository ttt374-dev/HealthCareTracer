package com.github.ttt374.healthcaretracer.ui.statics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.data.Item
import com.github.ttt374.healthcaretracer.data.averageOrNull
import com.github.ttt374.healthcaretracer.data.bloodPressureFormatted
import com.github.ttt374.healthcaretracer.data.gapME
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.home.ItemsViewModel
import com.github.ttt374.healthcaretracer.ui.home.toBodyWeightString
import com.github.ttt374.healthcaretracer.ui.home.toPulseString
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit


@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel(), appNavigator: AppNavigator) {
    val selectedRange by viewModel.selectedRange.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val filteredItems by viewModel.filteredItems.collectAsState()

    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
    val cutoffDate = Instant.now().minus(selectedRange.days, ChronoUnit.DAYS)

    Scaffold(
        topBar = { CustomTopAppBar("Statistics") },
        bottomBar = { CustomBottomAppBar(appNavigator) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)){
            Box(modifier = Modifier.padding(16.dp)) {
                Column {
                    TimeRangeDropdown(selectedRange) { viewModel.setSelectedRange(it) }
                    Text("from: " + dateFormatter.format(cutoffDate))
                    Text("# of data: " + filteredItems.size)
                }
            }
            HorizontalDivider(thickness = 1.5.dp, color = Color.LightGray)
            StatisticsHeadersRow()
            StatisticsItemRow("average",
                statistics.avgBpUpper,
                statistics.avgBpLower,
                statistics.avgMeGap,
                statistics.avgPulse,
                statistics.avgBodyWeight
            )
            StatisticsItemRow("max",
                statistics.maxBpUpper,
                statistics.maxBpLower,
                statistics.maxMeGap,
                statistics.maxPulse,
                statistics.maxBodyWeight
            )
            StatisticsItemRow("min",
                statistics.minBpUpper,
                statistics.minBpLower,
                statistics.minMeGap,
                statistics.minPulse,
                statistics.minBodyWeight
            )
        }
    }
}

@Composable
fun StatisticsHeadersRow(){
    Row {
        Text("", Modifier.weight(1f))
        Text("Blood Pressure (ME gap)", Modifier.weight(1f))
        Text("Pulse", Modifier.weight(1f))
        Text("Body Weight", Modifier.weight(1f))
    }
    HorizontalDivider(thickness = 1.5.dp, color = Color.LightGray)
}

@Composable
fun StatisticsItemRow(label: String, bpUpper: Double?, bpLower: Double?, meGap: Double?, pulse: Double?, bodyWeight: Double?){
    Row {
        Text(label, Modifier.weight(1f))
        Text(bloodPressureFormatted(bpUpper?.toInt(), bpLower?.toInt(), meGap?.toInt()), Modifier.weight(1f))
        Text(pulse?.toInt().toPulseString(), Modifier.weight(1f))
        Text(bodyWeight.toBodyWeightString(), Modifier.weight(1f))
    }
    HorizontalDivider(thickness = 0.75.dp, color = Color.LightGray)
}
// filter
@Composable
fun TimeRangeDropdown(selectedRange: TimeRange, onRangeSelected: (TimeRange) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val ranges = TimeRange.entries

    //Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
    Box (modifier=Modifier.padding(8.dp)){
        Button(onClick = { expanded = true }) {
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

enum class TimeRange(val days: Long, val label: String) {
    ONE_WEEK(7, "1 Week"),
    ONE_MONTH(30, "1 Month"),
    SIX_MONTHS(180, "6 Months")
}

fun List<Item>.filterByRange(range: TimeRange): List<Item> {
    val cutoffDate = Instant.now().minus(range.days, ChronoUnit.DAYS)
    return filter { it.measuredAt.isAfter(cutoffDate) }
}

