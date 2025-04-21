package com.github.ttt374.healthcaretracer.ui.chart

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.repository.LocalTimeRange
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.TimeOfDayConfig
import com.github.ttt374.healthcaretracer.ui.common.TimeRange
import com.github.ttt374.healthcaretracer.ui.common.TimeRangeDropdown
import com.github.ttt374.healthcaretracer.ui.entry.toLocalTime
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ChartScreen(chartViewModel: ChartViewModel = hiltViewModel(), appNavigator: AppNavigator){
    val chartData by chartViewModel.chartData.collectAsState()
    val selectedChartType by chartViewModel.selectedChartType.collectAsState()
    val timeRange by chartViewModel.timeRange.collectAsState()

    val pagerState = rememberPagerState(
        initialPage = ChartType.entries.indexOf(selectedChartType),
        pageCount = { ChartType.entries.size }
    )

    val coroutineScope = rememberCoroutineScope()
    val onRangeSelected = { range: TimeRange -> chartViewModel.updateTimeRange(range)}

    LaunchedEffect(Unit) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            chartViewModel.onPageChanged(page)
        }
    }

    //////////////////////////////
    Scaffold(topBar = { CustomTopAppBar(stringResource(R.string.chart)) },
        bottomBar = { CustomBottomAppBar(appNavigator) })
    { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TimeRangeDropdown(timeRange, onRangeSelected, modifier = Modifier.padding(4.dp))

            TabRow(selectedTabIndex = pagerState.currentPage) {
                ChartType.entries.forEachIndexed { index, type ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(stringResource(type.labelResId)) }
                    )
                }
            }
            // 選択されたタブに応じて異なるグラフを表示
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) {
                HealthChart(chartData.chartSeriesList)
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
        setAvoidFirstLastClipping(true)
    }

    axisLeft.apply {
        spaceTop = 40f
        spaceBottom = 40f
    }
    axisRight.isEnabled = false
    setupValueFormatter(datePattern)
    invalidate()
}

fun LineDataSet.applyStyle(color: Int? = null, lineWidth: Float = 2f, circleRadius: Float = 4f, isTarget: Boolean = false) = apply {
    if (color != null){
        this.color = color
        setCircleColor(color)
        valueTextColor = color
    }
    this.circleRadius = circleRadius

    if (isTarget) {
        enableDashedLine(15f, 10f, 0f)
        setDrawValues(false)
        setDrawCircles(false)
        this.lineWidth = 1f
    } else {
        this.lineWidth = lineWidth
        //this.circleRadius = circleRadius
    }
}
@Composable
fun HealthChart(chartSeriesList: List<ChartSeries>) {
    val list = chartSeriesList.toLineDataSets()
    val isDarkMode = isSystemInDarkTheme()
    AndroidView(
        factory = { context -> LineChart(context) },
        modifier = Modifier.fillMaxSize(),
        update = { chart ->
            chart.data = LineData(*list.toTypedArray())
            //chart.data = LineData(*lineDataSetLit.toTypedArray())
            chart.apply {
                this.legend.textColor = Color.Black.adjustForMode(isDarkMode).toArgb()
            }
            chart.setupChartAdaptive()
        }
    )
}

@Composable
private fun List<ChartSeries>.toLineDataSets(): List<LineDataSet> {
    val colorList = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.outline,
        MaterialTheme.colorScheme.surfaceVariant
    )


    return this.withIndex().flatMap { (index, series) ->
    //return flatMap { series ->
        //val color = series.seriesDef.color.adjustForMode(isSystemInDarkTheme())
        val label = series.seriesDef.labelResId?.let { stringResource(it) } ?: ""
        val targetLabel = series.seriesDef.targetLabelResId?.let { stringResource(it) } ?: ""
        val color = colorList.getOrElse(index % colorList.size) { MaterialTheme.colorScheme.primary }

        listOfNotNull(
            LineDataSet(series.actualEntries, label).applyStyle(color.toArgb()).apply {
                val colors = series.actualEntries.map { entry ->
                    val instant = Instant.ofEpochMilli(entry.x.toLong())
                    //val timeOfDayConfig = TimeOfDayConfig()
                    if (instant.isMorning()) Color.Blue.toArgb() else
                        if (instant.isEvening()) Color.Red.toArgb() else
                            Color.Black.toArgb()


                }
                //setCircleColors(colors)

                                                                                      },
            LineDataSet(series.targetEntries, targetLabel).applyStyle(color.toArgb(), isTarget = true)
        )
    }
}
//@Composable
//fun adjustForDarkMode(color: Color): Color {
//    return if (isSystemInDarkTheme()) color.toDarkMode() else color
//}

fun Instant.isMorning(timeOfDayConfig: TimeOfDayConfig = TimeOfDayConfig()): Boolean {
    val range = LocalTimeRange(timeOfDayConfig.morning, timeOfDayConfig.afternoon)
    return range.contains(this.toLocalTime())
}
fun Instant.isEvening(timeOfDayConfig: TimeOfDayConfig = TimeOfDayConfig()): Boolean {
    val range = LocalTimeRange(timeOfDayConfig.evening, timeOfDayConfig.morning)
    return range.contains(this.toLocalTime())
}

fun Color.adjustForMode(isDarkMode: Boolean = false): Color {
    return if (isDarkMode) this.toDarkMode() else this
}

fun Color.toDarkMode(): Color {
    val r = 1f - red
    val g = 1f - green
    val b = 1f - blue
    return Color(r, g, b, alpha * 0.5f)
}
//fun Color.toDarkMode(): Color {
//    val hsv = FloatArray(3)
//    // Color を ARGB Int に変換し、HSV に変換
//    android.graphics.Color.colorToHSV(this.toArgb(), hsv)
//
//    // 明度(V)だけを下げる（例: 70% → 40% に）
//    hsv[2] = hsv[2].coerceIn(0.0f, 1.0f) // 安全に範囲制限
//    hsv[2] = max(0f, hsv[2] - 0.3f) // 明度を少し下げる
//
//    val darkColorInt = android.graphics.Color.HSVToColor(hsv)
//    return Color(darkColorInt)
//}
