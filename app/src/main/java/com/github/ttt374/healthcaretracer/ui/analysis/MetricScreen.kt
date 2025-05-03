package com.github.ttt374.healthcaretracer.ui.analysis

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun MetricScreen(viewModel: MetricViewModel = hiltViewModel(), appNavigator: AppNavigator) {
    val selectedMetricType by viewModel.selectedMetricType.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = MetricType.entries.indexOf(selectedMetricType),
        pageCount = { MetricType.entries.size }
    )
    val displayMode by viewModel.displayMode.collectAsState()
//    LaunchedEffect(selectedMetricType) {
//        pagerState.animateScrollToPage(selectedMetricType.ordinal)
//    }
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

    LaunchedEffect(selectedMetricType) {
        val targetPage = MetricType.entries.indexOf(selectedMetricType)
        if (pagerState.currentPage != targetPage) {
            pagerState.scrollToPage(targetPage)
        }
    }

    Scaffold(topBar = { CustomTopAppBar(stringResource(R.string.analysis)) },
        bottomBar = { CustomBottomAppBar(appNavigator) }
        )
    { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            ToggleDisplayMode(displayMode = displayMode, { viewModel.setDisplayMode(it) })
            TabRow(selectedTabIndex = pagerState.currentPage) {
                MetricType.entries.forEachIndexed { index, type ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
//                                pagerState.currentPage.toChartType()?.let {
//                                    viewModel.setMetricType(it)
//                                }
                            }
                        },
                        text = { Text(stringResource(type.resId)) }
                    )
                }
            }
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) {

            }
        }
    }

}