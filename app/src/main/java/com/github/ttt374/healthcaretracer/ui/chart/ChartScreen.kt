package com.github.ttt374.healthcaretracer.ui.chart

import android.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.ttt374.healthcaretracer.data.DailyItem
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.TimeRangeDropdown
import java.time.Instant
import java.time.ZoneId

@Composable
fun ChartScreen(chartViewModel: ChartViewModel = hiltViewModel(), appNavigator: AppNavigator){
    val selectedRange by chartViewModel.selectedRange.collectAsState()
//    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.systemDefault())
//    val cutoffDate = Instant.now().minus(selectedRange.days, ChronoUnit.DAYS)

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Blood Pressure", "Pulse", "Body Weight")

    val bpUpperEntries by chartViewModel.bpUpperEntries.collectAsState()
    val bpLowerEntries by chartViewModel.bpLowerEntries.collectAsState()
    val pulseEntries by chartViewModel.pulseEntries.collectAsState()
    val bodyWeightEntries by chartViewModel.bodyWeightEntries.collectAsState()

    Scaffold(topBar = { CustomTopAppBar("Chart") },
        bottomBar = {
            CustomBottomAppBar(appNavigator)
        }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Box(modifier = Modifier.padding(16.dp)) {
                Column {
                    TimeRangeDropdown(selectedRange) { chartViewModel.setSelectedRange(it) }
//                    Text("from: " + dateFormatter.format(cutoffDate))
//                    Text("# of data: " + bpUpperEntries.size)
                }
            }
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            // 選択されたタブに応じて異なるグラフを表示
            when (selectedTabIndex) {
                0 -> BloodPressureChart(bpUpperEntries, bpLowerEntries)
                1 -> PulseChart(pulseEntries)
                2 -> BodyWeightChart(bodyWeightEntries)
            }
        }
    }
}
////////////
private fun LineChart.setupChart() {
    description.isEnabled = false
    xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return Instant.ofEpochMilli(value.toLong())
                    .atZone(ZoneId.systemDefault()).toLocalDate().toString()
            }
        }
    }
    axisLeft.apply {
        spaceTop = 40f
        spaceBottom = 40f
        //axisMinimum = 0f
    }
    axisRight.isEnabled = false
}

private fun LineDataSet.applyStyle(color: Int) = apply {
    this.color = color
    setCircleColor(color)
    valueTextColor = color
    lineWidth = 2f
    circleRadius = 4f
}

fun List<DailyItem>.toEntries(takeValue: (DailyItem) -> Double?): List<Entry> {
    return mapNotNull { dailyItem ->
        takeValue(dailyItem)?.toFloat()?.let { value ->
            Entry(dailyItem.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli().toFloat(), value)
        }
    }
}

@Composable
fun HealthChart(update: (LineChart) -> Unit){
    AndroidView(
        factory = { context -> LineChart(context).apply { setupChart() } },
        modifier = Modifier.fillMaxSize(),
        update = { chart -> update(chart) }
    )
}
@Composable
fun BloodPressureChart(bpUpperEntries: List<Entry>, bpLowerEntries: List<Entry>){
    HealthChart(){ chart ->
        val bpHighDataSet = LineDataSet(bpUpperEntries, "BP High").applyStyle(Color.BLUE)
        val bpLowDataSet = LineDataSet(bpLowerEntries, "BP Low").applyStyle(Color.GREEN)
        chart.data = LineData(bpHighDataSet, bpLowDataSet)
        chart.invalidate()
    }
}

@Composable
fun PulseChart(pulseEntries: List<Entry>){
    HealthChart(){ chart ->val pulseDataSet = LineDataSet(pulseEntries, "Pulse").applyStyle(Color.RED)
        chart.data = LineData(pulseDataSet)
        chart.invalidate()
    }
}
@Composable
fun BodyWeightChart(bodyWeightEntries: List<Entry>){
    HealthChart(){ chart ->
        val bodyWeightDataSet = LineDataSet(bodyWeightEntries, "Body Weight").applyStyle(Color.GREEN)
        chart.data = LineData(bodyWeightDataSet)
        chart.invalidate()
    }
}