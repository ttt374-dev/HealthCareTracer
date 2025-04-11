package com.github.ttt374.healthcaretracer.ui.chart

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
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
import com.github.ttt374.healthcaretracer.ui.common.TimeRange
import com.github.ttt374.healthcaretracer.ui.common.TimeRangeDropdown
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class ChartColorPalette(
    val primary: Color,
    val secondary: Color
)

enum class ChartType(@StringRes val labelResId: Int, val datasets: (Context, ChartUiState, ChartColorPalette) -> List<LineDataSet>) {
    BloodPressure(
        R.string.blood_pressure,
        { context, uiState, colors ->
            val primary = colors.primary
            val secondary = colors.secondary
            listOf(
                createLineDataSet(context, uiState.actualEntries.bpUpper, R.string.bpUpper, primary),
                createLineDataSet(context, uiState.actualEntries.bpLower, R.string.bpLower, secondary),
                createLineDataSet(context, uiState.targetEntries.bpUpper, R.string.bpUpper, primary, true),
                createLineDataSet(context, uiState.targetEntries.bpLower, R.string.bpLower, secondary, true),
            )
        }
    ),
    Pulse(
        R.string.pulse,
        { context, uiState, colors ->
            listOf(
                createLineDataSet(context, uiState.actualEntries.pulse, R.string.pulse, colors.primary)
            )
        }
    ),
    BodyWeight(
        R.string.body_weight,
        { context, uiState, colors ->
            listOf(
                createLineDataSet(context, uiState.actualEntries.bodyWeight, R.string.body_weight, colors.primary),
                createLineDataSet(context, uiState.targetEntries.bodyWeight, R.string.target_body_weight, colors.primary, true)
            )
        }
    )
//    BloodPressure(R.string.blood_pressure),
//    Pulse(R.string.pulse),
//    BodyWeight(R.string.body_weight)
}

//fun ChartType.createDataSets(context: Context, uiState: ChartUiState, colors: ColorScheme ): List<LineDataSet> {
//    val primaryColor = colors.primary
//    val secondaryColor = colors.secondary
//
//    return when (this) {
//        ChartType.BloodPressure -> listOf(
//            createLineDataSet(context, uiState.actualEntries.bpUpper, R.string.bpUpper, primaryColor),
//            createLineDataSet(context, uiState.actualEntries.bpLower, R.string.bpLower, secondaryColor),
//            createLineDataSet(context, uiState.targetEntries.bpUpper, R.string.bpUpper, primaryColor, true),
//            createLineDataSet(context, uiState.targetEntries.bpLower, R.string.bpLower, secondaryColor, true),
//        )
//        ChartType.Pulse -> listOf(
//            createLineDataSet(context, uiState.actualEntries.pulse, R.string.pulse, primaryColor)
//        )
//        ChartType.BodyWeight -> listOf(
//            createLineDataSet(context, uiState.actualEntries.bodyWeight, R.string.body_weight, primaryColor),
//            createLineDataSet(context, uiState.targetEntries.bodyWeight, R.string.target_body_weight, primaryColor, true)
//        )
//    }
//}


private fun createLineDataSet(context: Context, entries: List<Entry>, labelRes: Int, color: Color, isTarget: Boolean = false): LineDataSet {
    val label = context.getString(labelRes) // stringResource(labelRes)
    val dataSet = LineDataSet(entries, label)
    return if (isTarget) dataSet.applyTargetStyle(color.toArgb())
    else dataSet.applyStyle(color.toArgb())
}

@Composable
fun ChartScreen(chartViewModel: ChartViewModel = hiltViewModel(), appNavigator: AppNavigator){
    val uiState by chartViewModel.uiState.collectAsState()
    val selectedChartType = ChartType.entries[uiState.selectedTabIndex]
    val onRangeSelected = { range: TimeRange -> chartViewModel.updateTimeRange(range)}
    val onClickTab = { index: Int -> chartViewModel.updateSelectedTabIndex(index) }
    val context = LocalContext.current
    val chartColors = ChartColorPalette(
        primary = MaterialTheme.colorScheme.primary,
        secondary = MaterialTheme.colorScheme.secondary
    )
    Scaffold(topBar = { CustomTopAppBar(stringResource(R.string.chart)) },
        bottomBar = { CustomBottomAppBar(appNavigator) })
    { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Box(modifier = Modifier.padding(4.dp)) {
                TimeRangeDropdown(uiState.timeRange, onRangeSelected)
            }
            TabRow(selectedTabIndex = uiState.selectedTabIndex) {
                ChartType.entries.forEachIndexed { index, type ->
                    Tab(
                        selected = uiState.selectedTabIndex == index,
                        //onClick = { selectedTabIndex = index },
                        onClick = { onClickTab(index) },
                        text = { Text(stringResource(type.labelResId)) }
                    )
                }
            }
            // 選択されたタブに応じて異なるグラフを表示
            HealthChart(selectedChartType.datasets(context, uiState, chartColors))
//            val primaryColor = MaterialTheme.colorScheme.primary
//            val secondaryColor = MaterialTheme.colorScheme.tertiary

//            when (selectedChartType) {
//                ChartType.BloodPressure -> {
//                    HealthChart(listOf(
//                        LineDataSet(uiState.actualEntries.bpUpper, stringResource(R.string.bpUpper)).applyStyle(primaryColor.toArgb()),
//                        LineDataSet(uiState.actualEntries.bpLower, stringResource(R.string.bpLower)).applyStyle(secondaryColor.toArgb()),
//                        LineDataSet(uiState.targetEntries.bpUpper, stringResource(R.string.targetBpUpper)).applyTargetStyle(primaryColor.toArgb()),
//                        LineDataSet(uiState.targetEntries.bpLower, stringResource(R.string.targetBpLower)).applyTargetStyle(secondaryColor.toArgb()),
//                    ))
//                }
//                ChartType.Pulse -> {
//                    HealthChart(
//                        LineDataSet(uiState.actualEntries.pulse, stringResource(R.string.pulse)).applyStyle(primaryColor.toArgb())
//                    )
//                }
//                ChartType.BodyWeight -> {
//                    HealthChart(listOf(
//                        LineDataSet(uiState.actualEntries.bodyWeight, stringResource(R.string.body_weight)).applyStyle(primaryColor.toArgb()),
//                        LineDataSet(uiState.targetEntries.bodyWeight, stringResource(R.string.target_body_weight)).applyTargetStyle(primaryColor.toArgb())
//                    ))
//                }
//            }
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
fun LineChart.setupChartAdaptive(datePattern: String = "yyyy/M/d", maxLabelCount: Int = 10) {
    description.isEnabled = false

    val start = data?.xMin?.toLong() ?: return
    val end = data?.xMax?.toLong() ?: return
    val totalDays = ((end - start) / 86400000L).coerceAtLeast(1)
    val daysPerLabel = (totalDays / 10).coerceAtLeast(1)
    val granularityMillis = daysPerLabel * 86400000f
    val labelRotationAngle = -45f

    xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        granularity = granularityMillis
        isGranularityEnabled = true
        setLabelCount(maxLabelCount, false)
        this.labelRotationAngle = labelRotationAngle
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
    setDrawCircles(false)
}
//@Composable
//fun LineDataSet.labelFrom(@StringRes labelId: Int): LineDataSet =
//    apply { label = stringResource(labelId) } // Composable で実行

fun LineDataSet.applyColor(color: Color): LineDataSet =
    apply { this.color = color.toArgb() }


//
//@Composable
//@JvmName("HealthChartSingle")
//fun HealthChart(lineDataSet: LineDataSet){
//    HealthChart(listOf(lineDataSet))
//}

@Composable
fun HealthChart(lineDataSetList: List<LineDataSet>) {
    AndroidView(
        factory = { context -> LineChart(context) },
        modifier = Modifier.fillMaxSize(),
        update = { chart ->
            chart.data = LineData(*lineDataSetList.toTypedArray())
            chart.invalidate()
            chart.setupChartAdaptive()
        }
    )
}
//data class EntryData (val entries: List<Entry>, val resLabel: Int, val color: Color, ){
//    @Composable
//    fun lineDataSet(): LineDataSet {
//        return LineDataSet(entries, stringResource(resLabel)).applyStyle(color.toArgb())
//    }
//}
//
//@Composable
//fun BloodPressureChart(bpUpperEntries: List<Entry>, bpLowerEntries: List<Entry>, targetBpUpperEntries: List<Entry>, targetBpLowerEntries: List<Entry>){
//    val bpUpperColor = MaterialTheme.colorScheme.primary
//    val bpLowerColor = MaterialTheme.colorScheme.tertiary
//
//    val lineDataSetList = listOf(
//        EntryData(bpUpperEntries, R.string.bpUpper, bpUpperColor).lineDataSet(),
//        EntryData(bpLowerEntries, R.string.bpLower, bpLowerColor).lineDataSet(),
//        //LineDataSet(bpUpperEntries, stringResource(R.string.bpUpper)).applyStyle(bpUpperColor),
////        LineDataSet(bpLowerEntries, stringResource(R.string.bpLower)).applyStyle(bpLowerColor.toArgb()),
////        LineDataSet(targetBpUpperEntries, stringResource(R.string.targetBpUpper)).applyStyle(bpUpperColor.toArgb()),
////        LineDataSet(targetBpLowerEntries, stringResource(R.string.targetBpUpper)).applyStyle(bpLowerColor.toArgb()),
//    )
//    HealthChart(lineDataSetList)
//}

//@Composable
//fun PulseChart(pulseEntries: List<Entry>,){
//    val pulseLabel = stringResource(R.string.pulse)
//    HealthChart(LineData(LineDataSet(pulseEntries, pulseLabel).applyStyle(MaterialTheme.colorScheme.primary.toArgb())))
//}
//@Composable
//fun BodyWeightChart(bodyWeightEntries: List<Entry>, targetBodyWeightEntries: List<Entry>){
//    val bodyWeightLabel = stringResource(R.string.body_weight)
//    val targetBodyWeightLabel = stringResource(R.string.target_body_weight)
//
//    val color = MaterialTheme.colorScheme.primary.toArgb()
//    val bodyWeightDataSet = LineDataSet(bodyWeightEntries, bodyWeightLabel).applyStyle(color)
//    val targetBodyWeightDataSet = LineDataSet(targetBodyWeightEntries, targetBodyWeightLabel).applyTargetStyle(color)
//    HealthChart(LineData(bodyWeightDataSet, targetBodyWeightDataSet))
//}
