package com.github.ttt374.healthcaretracer.ui.analysis

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.repository.TimeRange
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.chart.HealthChart
import com.github.ttt374.healthcaretracer.ui.chart.toChartType
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.TimeRangeDropdown
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

enum class DisplayMode { CHART, STATISTICS }

@Composable
fun AnalysisScreen(viewModel: AnalysisViewModel = hiltViewModel(), appNavigator: AppNavigator){
    val chartData by viewModel.chartData.collectAsState()
    val selectedMetricType by viewModel.selectedMetricType.collectAsState()
    val timeRange by viewModel.timeRange.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val onRangeSelected = { range: TimeRange -> viewModel.setSelectedRange(range)}
    val pagerState = rememberPagerState(
        initialPage = MetricType.entries.indexOf(selectedMetricType),
        pageCount = { MetricType.entries.size }
    )
    val displayMode by viewModel.displayMode.collectAsState()
    LaunchedEffect(Unit) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            page.toChartType()?.let { viewModel.setMetricType(it) }
        }
    }
    Scaffold(topBar = { CustomTopAppBar(stringResource(R.string.analysis)) },
        bottomBar = { CustomBottomAppBar(appNavigator) })
    { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TimeRangeDropdown(timeRange, onRangeSelected, modifier = Modifier.padding(4.dp))
                Spacer(modifier = Modifier.weight(1f))
                // Chart / Statistics 切り替えボタン
                ToggleButton(displayMode = displayMode, onModeChange = { viewModel.setDisplayMode(it) })
                //Text(timeRange.toDisplayString(chartData.chartSeriesList.firstDate() ?: Instant.now()))
            }
            TabRow(selectedTabIndex = pagerState.currentPage) {
                MetricType.entries.forEachIndexed { index, type ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(stringResource(type.resId)) }
                    )
                }
            }
            // 選択されたタブに応じて異なるグラフを表示
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) {
                when (displayMode) {
                    DisplayMode.CHART -> HealthChart(chartData.chartSeriesList, timeRange)
                    DisplayMode.STATISTICS -> {}
                }

            }
        }
    }
}

@Composable
fun ToggleButton(displayMode: DisplayMode, onModeChange: (DisplayMode) -> Unit) {
    Row {
        Button(
            onClick = { onModeChange(DisplayMode.CHART) },
            colors = if (displayMode == DisplayMode.CHART) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
        ) {
            Text("Chart")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = { onModeChange(DisplayMode.STATISTICS) },
            colors = if (displayMode == DisplayMode.STATISTICS) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors()
        ) {
            Text("Statistics")
        }
    }
}
