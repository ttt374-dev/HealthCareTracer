package com.github.ttt374.healthcaretracer.ui.analysis

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.bloodpressure.toAnnotatedString
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.metric.MetricValue
import com.github.ttt374.healthcaretracer.data.metric.toAnnotatedString
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.TimeRangeDropdown
import kotlinx.coroutines.launch

enum class DisplayMode(val resId: Int) { CHART(R.string.chart), STATISTICS(R.string.statistics) ; companion object { val Default = CHART}}

@Composable
fun AnalysisScreen(viewModel: AnalysisViewModel = hiltViewModel(), appNavigator: AppNavigator){
    val chartData by viewModel.chartData.collectAsState()
    val config by viewModel.config.collectAsState()
    val selectedMetricType by viewModel.selectedMetricType.collectAsState()
    val statData by viewModel.statData.collectAsState()
    val meGapStatValue by viewModel.meGapStatValue.collectAsState()

    val timeRange by viewModel.timeRange.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val onRangeSelected = { range: TimeRange -> viewModel.setSelectedRange(range)}
    val pagerState = rememberPagerState(
        initialPage = MetricType.entries.indexOf(selectedMetricType),
        pageCount = { MetricType.entries.size }
    )
    val displayMode by viewModel.displayMode.collectAsState()

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
        .collect { page ->
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
                ToggleDisplayMode(displayMode = displayMode, { viewModel.setDisplayMode(it) })
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
                    DisplayMode.STATISTICS -> {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())){
                            val format = { mv: MetricValue ->
                                when(mv){
                                    is MetricValue.BloodPressure -> { mv.value.toAnnotatedString(config.bloodPressureGuideline, false) }
                                    else -> { mv.format() }
                                }
                            }
                            StatDataTable(selectedMetricType, statData, meGapStatValue, format)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ToggleDisplayMode(
    displayMode: DisplayMode,
    onDisplayModeChange: (DisplayMode) -> Unit
) {
    SegmentedButtonGroup(
        options = DisplayMode.entries,
        selectedOption = displayMode,
        onOptionSelected = onDisplayModeChange,
        optionText = { mode -> stringResource(mode.resId) },
    )
}

@Composable
fun <T> SegmentedButtonGroup(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    optionText: @Composable (T) -> String
) {
    val paddingDp = 3.dp
    Row(
        modifier = Modifier
            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selectedOption
            val shape = when (index) {
                0 -> RoundedCornerShape(topStart = paddingDp, bottomStart = paddingDp)
                options.lastIndex -> RoundedCornerShape(topEnd = paddingDp, bottomEnd = paddingDp)
                else -> RoundedCornerShape(0.dp)
            }
            Button(
                onClick = { onOptionSelected(option) },
                shape = shape,
                colors = if (isSelected) {
                    ButtonDefaults.buttonColors()
                } else {
                    ButtonDefaults.outlinedButtonColors()
                },
                contentPadding = PaddingValues(horizontal = paddingDp, vertical = paddingDp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = optionText(option),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
