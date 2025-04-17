package com.github.ttt374.healthcaretracer.ui.chart

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ChartScreen(chartViewModel: ChartViewModel = hiltViewModel(), appNavigator: AppNavigator){
    val selectedType by chartViewModel.selectedChartType.collectAsState()
    val pagerState = rememberPagerState(
        initialPage = ChartType.entries.indexOf(selectedType),
        pageCount = { ChartType.entries.size }
    )

    //val uiState by chartViewModel.uiState.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    val onRangeSelected = { range: TimeRange -> chartViewModel.updateTimeRange(range)}


    // ページ変更時に ViewModel に反映
    LaunchedEffect(pagerState.currentPage) {
        chartViewModel.onPageChanged(pagerState.currentPage)
    }
    val chartSeriesList by chartViewModel.chartSeries.collectAsState()

    Scaffold(topBar = { CustomTopAppBar(stringResource(R.string.chart)) },
        bottomBar = { CustomBottomAppBar(appNavigator) })
    { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            //TimeRangeDropdown(uiState.timeRange, onRangeSelected, modifier = Modifier.padding(4.dp))

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
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                //val selectedChartType = ChartType.entries[page]
                HealthChart(chartSeriesList)

//                when (selectedChartType){
//                    ChartType.Pulse -> HealthChart(datasets)
//                    else -> {}
//                }
//                val datasets = remember(uiState, selectedChartType) {
//                    selectedChartType.datasets(context, uiState, chartColors)
//                }
//                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                    HealthChart(datasets)
//                }
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
    }
    axisLeft.spaceTop = 40f
    axisLeft.spaceBottom = 40f
    axisRight.isEnabled = false
    setupValueFormatter(datePattern)
}

@Composable
fun HealthChart(chartSeriesList: List<ChartSeries>) {
//fun HealthChart(lineDataSetList: List<LineDataSet>) {
    val chartColors = ChartColorPalette(
        primary = MaterialTheme.colorScheme.primary,
        secondary = MaterialTheme.colorScheme.tertiary
    )

    val list = chartSeriesList.flatMap { chartSeries ->
        val color = when(chartSeries.seriesDef.seniority){
            Seniority.Primary -> chartColors.primary
            Seniority.Secondary -> chartColors.secondary
        }
        val targetLabel = chartSeries.seriesDef.targetLabelResId?.let { stringResource(it) } ?: ""
        listOfNotNull(
            LineDataSet(chartSeries.entries, stringResource(chartSeries.seriesDef.labelResId)).applyStyle(color.toArgb()),
            LineDataSet(chartSeries.targetEntries, targetLabel).applyStyle(color.toArgb(), isTarget = true)
        )
    }
    AndroidView(
        factory = { context -> LineChart(context) },
        modifier = Modifier.fillMaxSize(),
        update = { chart ->
            chart.data = LineData(*list.toTypedArray())
            //chart.data = LineData(*lineDataSetLit.toTypedArray())
            chart.invalidate()
            chart.setupChartAdaptive()
        }
    )
}
