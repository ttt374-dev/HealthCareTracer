package com.github.ttt374.healthcaretracer.ui.chart

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ColorScheme
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
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.TimeRange
import com.github.ttt374.healthcaretracer.ui.common.TimeRangeDropdown
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

@Composable
fun HealthChart(chartSeriesList: List<ChartSeries>, colorScheme: ColorScheme = MaterialTheme.colorScheme) {
    val list = chartSeriesList.toLineDataSets(colorScheme)

    AndroidView(
        factory = { context -> LineChart(context) },
        modifier = Modifier.fillMaxSize(),
        update = { chart ->
            chart.data = LineData(*list.toTypedArray())
            //chart.data = LineData(*lineDataSetLit.toTypedArray())

            chart.setupChartAdaptive()
        }
    )
}

@Composable
private fun List<ChartSeries>.toLineDataSets(colorScheme: ColorScheme): List<LineDataSet> {
    return flatMap { series ->
        val color = when (series.seriesDef.seriesPriority) {
            SeriesPriority.Primary -> colorScheme.primary
            SeriesPriority.Secondary -> colorScheme.secondary
        }
        val label = series.seriesDef.labelResId?.let { stringResource(it) } ?: ""
        val targetLabel = series.seriesDef.targetLabelResId?.let { stringResource(it) } ?: ""
        listOfNotNull(
            LineDataSet(series.actualEntries, label).applyStyle(color.toArgb()),
            LineDataSet(series.targetEntries, targetLabel).applyStyle(color.toArgb(), isTarget = true)
        )
    }
}
