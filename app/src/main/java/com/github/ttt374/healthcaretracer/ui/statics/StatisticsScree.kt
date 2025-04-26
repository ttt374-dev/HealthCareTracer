package com.github.ttt374.healthcaretracer.ui.statics

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.metric.MetricCategory
import com.github.ttt374.healthcaretracer.data.metric.MetricDefRegistry
import com.github.ttt374.healthcaretracer.data.metric.StatValue
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.shared.toAnnotatedString
import com.github.ttt374.healthcaretracer.shared.toDisplayString
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.TimeRangeDropdown

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel(), appNavigator: AppNavigator) {
    val timeRange by viewModel.timeRange.collectAsState()
    val config by viewModel.config.collectAsState()
//    val guideline = config.bloodPressureGuideline
//    val statisticsData by viewModel.statisticsData.collectAsState()

    val statValueStateMap = viewModel.statValueMap.mapValues { (_, flow) ->
        flow.collectAsState()
    }
    val dayPeriodStatValueStateMap = viewModel.dayPeriodStatMap.mapValues { (_, flow) ->
        flow.collectAsState()

    }
    val firstDate by viewModel.firstDateFlow.collectAsState()

    Scaffold(
        topBar = { CustomTopAppBar(stringResource(R.string.statistics)) },
        bottomBar = { CustomBottomAppBar(appNavigator) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding).padding(horizontal = 8.dp)){
            item {
                Row(modifier = Modifier.padding(4.dp)) {
                    TimeRangeDropdown(timeRange, onRangeSelected = { viewModel.setSelectedRange(it) })
                    Text(timeRange.toDisplayString( firstDate))
                }
            }
            items(MetricCategory.entries){ category ->
                MetricDefRegistry.getByCategory(category).forEach { def ->
                    CustomDivider()
                    StatValueHeadersRow(stringResource(def.resId))
                    StatValueRow(stringResource(R.string.all), statValueStateMap[def]?.value!!) // TODO
                    dayPeriodStatValueStateMap[def]?.value?.mapValues { (period, statValue) ->
                        StatValueRow(stringResource(period.resId), statValue)
                    }
                }
            }
        }
    }
}
enum class StatType (val resId: Int, val selector: (StatValue) -> Double?){
    Average(R.string.average, { it.avg }),
    Max(R.string.max, { it.max }),
    Min(R.string.min, { it.min }),
    Count(R.string.count, { it.count.toDouble() })

}
@Composable
fun StatValueHeadersRow(label: String){
    Row {
        val mod = Modifier.weight(1f)
        val modCount = Modifier.weight(0.7f)
        Text(label, mod, fontWeight = FontWeight.Bold)
        StatType.entries.forEach {
            Text(stringResource(it.resId), mod)
        }
        Text(stringResource(StatType.Count.resId), modCount)
    }
}
@Composable
fun StatValueRow(label: String, statValue: StatValue, format: (Double?) -> AnnotatedString = { it.toAnnotatedString("%.1f")}){
    Row {
        val mod = Modifier.weight(1f)
        val modCount = Modifier.weight(0.7f)
        Text(label, mod)
        StatType.entries.forEach { statType ->
            Text(format(statType.selector(statValue)))
        }
        Text(statValue.count.toDisplayString(), modCount)
    }
}
@Composable
internal fun CustomDivider(thickness: Dp = 2.dp, color: Color = Color.Gray, paddingTop: Dp = 8.dp){
    HorizontalDivider(thickness = thickness, color = color, modifier = Modifier.padding(top = paddingTop))
}