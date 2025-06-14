package com.github.ttt374.healthcaretracer.ui.analysis

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import com.github.ttt374.healthcaretracer.data.bloodpressure.toAnnotatedString
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.metric.MetricValue
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.HorizontalSelector
import com.github.ttt374.healthcaretracer.ui.common.TimeRangeDropdown
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

enum class DisplayMode(val resId: Int) { CHART(R.string.chart), STATISTICS(R.string.statistics) ; companion object { val Default = CHART}}

internal fun Int.toChartType(): MetricType? {
    return MetricType.entries.getOrNull(this)
}

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
            .distinctUntilChanged()
            .collect { page ->
                val newMetricType = MetricType.entries[page]
                if (newMetricType != selectedMetricType) {
                    viewModel.setMetricType(newMetricType)
                }
            }
    }
//    LaunchedEffect(Unit) {
//        viewModel.selectedMetricType.collect { type ->
//            val targetPage = MetricType.entries.indexOf(type)
//            if (pagerState.currentPage != targetPage) {
//                pagerState.scrollToPage(targetPage)
//            }
//        }
//    }

    LaunchedEffect(selectedMetricType) {
        val targetPage = MetricType.entries.indexOf(selectedMetricType)
        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
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
                    DisplayMode.CHART -> {
                        HealthChart(chartData.chartSeriesList, timeRange, config.zoneId)
                    }
                    DisplayMode.STATISTICS -> {
                        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())){
                            StatDataTable(selectedMetricType, statData, meGapStatValue, createFormatter(config.bloodPressureGuideline))
                        }
                    }
                }
            }
        }
    }
}
fun createFormatter(guideline: BloodPressureGuideline): MetricValueFormatter {
    val format = { mv: MetricValue ->
        when(mv){
            is MetricValue.BloodPressure -> { mv.value.toAnnotatedString(guideline, false) }
            else -> { mv.format() }
        }
    }
    return format
}

@Composable
fun ToggleDisplayMode(
    displayMode: DisplayMode,
    onDisplayModeChange: (DisplayMode) -> Unit
) {
    HorizontalSelector(
        options = DisplayMode.entries,
        selectedOption = displayMode,
        onOptionSelected = onDisplayModeChange,
        optionText = { mode -> stringResource(mode.resId)}
    )
}