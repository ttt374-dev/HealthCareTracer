package com.github.ttt374.healthcaretracer.ui.chart

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.item.DailyItem
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.TimeRangeDropdown
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class ChartType(val labelResId: Int) {
    BloodPressure(R.string.blood_pressure),
    Pulse(R.string.pulse),
    BodyWeight(R.string.body_weight)
}


@Composable
fun ChartScreen(chartViewModel: ChartViewModel = hiltViewModel(), // configViewModel: ConfigViewModel = hiltViewModel(),
                appNavigator: AppNavigator){
    val timeRange by chartViewModel.timeRange.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    //val tabs = listOf(stringResource(R.string.blood_pressure), stringResource(R.string.pulse), stringResource(R.string.body_weight))
    val chartTypes = ChartType.entries.toTypedArray()
    val selectedChartType = chartTypes[selectedTabIndex]

    val bpUpperEntries by chartViewModel.bpUpperEntries.collectAsState()
    val bpLowerEntries by chartViewModel.bpLowerEntries.collectAsState()
    val pulseEntries by chartViewModel.pulseEntries.collectAsState()
    val bodyWeightEntries by chartViewModel.bodyWeightEntries.collectAsState()
    val targetBpUpperEntries by chartViewModel.targetBpUpperEntries.collectAsState()
    val targetBpLowerEntries by chartViewModel.targetBpLowerEntries.collectAsState()
    val targetBodyWeightEntries by chartViewModel.targetBodyWeightEntries.collectAsState()

    Scaffold(topBar = { CustomTopAppBar(stringResource(R.string.chart)) },
        bottomBar = {
            CustomBottomAppBar(appNavigator)
        }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Box(modifier = Modifier.padding(4.dp)) {
                TimeRangeDropdown(timeRange) { chartViewModel.setSelectedRange(it) }
            }
            TabRow(selectedTabIndex = selectedTabIndex) {
                chartTypes.forEachIndexed { index, type ->
                //tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(stringResource(type.labelResId)) }
                    )
                }
            }
            // 選択されたタブに応じて異なるグラフを表示
            when (selectedChartType ) {
                ChartType.BloodPressure -> BloodPressureChart(bpUpperEntries, bpLowerEntries, targetBpUpperEntries, targetBpLowerEntries)
                ChartType.Pulse -> PulseChart(pulseEntries)
                ChartType.BodyWeight -> BodyWeightChart(bodyWeightEntries, targetBodyWeightEntries)
            }
        }
    }
}
////////////
private fun LineChart.setupValueFormatter(datePattern: String){
    val formatter = DateTimeFormatter.ofPattern(datePattern)
    xAxis.valueFormatter = object : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return Instant.ofEpochMilli(value.toLong())
                .atZone(ZoneId.systemDefault()).toLocalDate().format(formatter)
        }
    }
}
//private fun LineChart.setupChart(datePattern: String = "yyyy/M/d") {
//    description.isEnabled = false
//    xAxis.apply {
//        position = XAxis.XAxisPosition.BOTTOM
////        granularity = 86400000f // 1日
////        isGranularityEnabled = true
////        labelRotationAngle = -45f // 見やすさ向上
//    }
//    axisLeft.apply {
//        spaceTop = 40f
//        spaceBottom = 40f
//        //axisMinimum = 0f
//    }
//    axisRight.isEnabled = false
//    setupValueFormatter(datePattern)
//}
fun LineChart.setupChartAdaptive(datePattern: String = "yyyy/M/d", maxLabelCount: Int = 10) {
    description.isEnabled = false

    val start = data?.xMin?.toLong() ?: return
    val end = data?.xMax?.toLong() ?: return
    val totalDays = ((end - start) / 86400000L).coerceAtLeast(1)
    val daysPerLabel = (totalDays / 10).coerceAtLeast(1)
    val granularityMillis = daysPerLabel * 86400000f

    xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        granularity = granularityMillis
        isGranularityEnabled = true
        setLabelCount(maxLabelCount, false)
        labelRotationAngle = -45f
    }

    axisLeft.spaceTop = 40f
    axisLeft.spaceBottom = 40f
    axisRight.isEnabled = false
    setupValueFormatter(datePattern)
}


private fun LineDataSet.applyStyle(color: Int, lineWidth: Float = 2f, circleRadius: Float = 4f) = apply {
    this.color = color
    setCircleColor(color)
    valueTextColor = color
    this.lineWidth = lineWidth
    this.circleRadius = circleRadius
}
private fun LineDataSet.applyTargetStyle(color: Int) = apply {
    this.applyStyle(color, 1f, 1f)
    enableDashedLine(15f, 10f, 0f)
    setDrawValues(false)
}


fun List<DailyItem>.toEntries(takeValue: (DailyItem) -> Double?): List<Entry> {
    return mapNotNull { dailyItem ->
        takeValue(dailyItem)?.toFloat()?.let { value ->
            Entry(dailyItem.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli().toFloat(), value)
        }
    }
}
@Composable
fun HealthChart(updateData: LineData) {
    if (updateData.entryCount == 0) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No data available")
        }
    } else {
        AndroidView(
            factory = { context -> LineChart(context) },
            modifier = Modifier.fillMaxSize(),
            update = { chart ->
                chart.data = updateData
                chart.invalidate()
                chart.setupChartAdaptive()
            }
        )
    }
}

@Composable
fun BloodPressureChart(bpUpperEntries: List<Entry>, bpLowerEntries: List<Entry>, targetBpUpperEntries: List<Entry>, targetBpLowerEntries: List<Entry>){
    val bpUpperColor = MaterialTheme.colorScheme.primary.toArgb()
    val bpLowerColor = MaterialTheme.colorScheme.tertiary.toArgb()

    val lineData = LineData(
        LineDataSet(bpUpperEntries, stringResource(R.string.bpUpper)).applyStyle(bpUpperColor),
        LineDataSet(bpLowerEntries, stringResource(R.string.bpLower)).applyStyle(bpLowerColor),
        LineDataSet(targetBpUpperEntries, stringResource(R.string.targetBpUpper)).applyStyle(bpUpperColor),
        LineDataSet(targetBpLowerEntries, stringResource(R.string.targetBpUpper)).applyStyle(bpLowerColor),
    )
    HealthChart(lineData)
}

@Composable
fun PulseChart(pulseEntries: List<Entry>,){
    val pulseLabel = stringResource(R.string.pulse)
    HealthChart(LineData(LineDataSet(pulseEntries, pulseLabel).applyStyle(MaterialTheme.colorScheme.primary.toArgb())))
}
@Composable
fun BodyWeightChart(bodyWeightEntries: List<Entry>, targetBodyWeightEntries: List<Entry>){
    val bodyWeightLabel = stringResource(R.string.body_weight)
    val targetBodyWeightLabel = stringResource(R.string.target_body_weight)

    val color = MaterialTheme.colorScheme.primary.toArgb()
    val bodyWeightDataSet = LineDataSet(bodyWeightEntries, bodyWeightLabel).applyStyle(color)
    val targetBodyWeightDataSet = LineDataSet(targetBodyWeightEntries, targetBodyWeightLabel).applyTargetStyle(color)
    HealthChart(LineData(bodyWeightDataSet, targetBodyWeightDataSet))
}
