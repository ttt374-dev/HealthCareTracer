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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.metric.DayPeriod
import com.github.ttt374.healthcaretracer.data.metric.MetricCategory
import com.github.ttt374.healthcaretracer.data.metric.MetricDef
import com.github.ttt374.healthcaretracer.data.metric.MetricDefRegistry
import com.github.ttt374.healthcaretracer.data.metric.StatValue
import com.github.ttt374.healthcaretracer.data.metric.toMeGapStatValue
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.shared.toAnnotatedString
import com.github.ttt374.healthcaretracer.shared.toDisplayString
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.TimeRangeDropdown

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel(), appNavigator: AppNavigator) {
    val timeRange by viewModel.timeRange.collectAsState()
    val statValueStateMap = viewModel.statValueMap.mapValues { (_, flow) ->
        flow.collectAsState()
    }
    val dayPeriodStatValueStateMap = viewModel.dayPeriodStatMap.mapValues { (_, flow) ->
        flow.collectAsState()

    }
    val firstDate by viewModel.firstDate.collectAsState()
    val meGapStatValue by viewModel.meGapStatValue.collectAsState()

    Scaffold(
        topBar = { CustomTopAppBar(stringResource(R.string.statistics)) },
        bottomBar = { CustomBottomAppBar(appNavigator) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding).padding(horizontal = 8.dp)){
            item {
                Row(modifier = Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    TimeRangeDropdown(timeRange, onRangeSelected = { viewModel.setSelectedRange(it) })
                    Text(timeRange.toDisplayString( firstDate))
                }
            }
            items(MetricCategory.entries){ category ->
                MetricDefRegistry.getByCategory(category).forEach { def ->
                    statValueStateMap[def]?.value?.let { allStatValue ->
                        dayPeriodStatValueStateMap[def]?.value?.let { dayPeriodStatValues ->
                            MetricDefStatValueTable(def, allStatValue, dayPeriodStatValues)
                        }
                    }
                }
                if (category == MetricCategory.BLOOD_PRESSURE){
                    StatValueRow(stringResource(R.string.me_gap), meGapStatValue)

                }
            }
        }
    }
}
@Composable
fun MetricDefStatValueTable(metricDef: MetricDef, allStatValue: StatValue, dayPeriodStatValues: Map<DayPeriod, StatValue>){
    CustomDivider()
    StatValueHeadersRow(stringResource(metricDef.resId))
    StatValueRow(stringResource(R.string.all), allStatValue, metricDef.format)
    dayPeriodStatValues.forEach { (period, statValue) ->
        StatValueRow(stringResource(period.resId), statValue, metricDef.format)
    }
}
enum class StatType (val resId: Int, val selector: (StatValue) -> Number?, val weight: Float = 1f){
    Average(R.string.average, { it.avg }),
    Max(R.string.max, { it.max }),
    Min(R.string.min, { it.min }),
    Count(R.string.count, { it.count }, 0.7f);
}
@Composable
fun StatValueHeadersRow(label: String){
    Row {
        val mod = Modifier.weight(1f)
        //val modCount = Modifier.weight(0.7f)
        Text(label, mod, fontWeight = FontWeight.Bold)
        StatType.entries.forEach {
            Text(stringResource(it.resId), Modifier.weight(it.weight))
        }
        //Text(stringResource(StatType.Count.resId), modCount)
    }
}
@Composable
fun StatValueRow(label: String, statValue: StatValue, format: (Number?) -> AnnotatedString = { it.toAnnotatedString("%.1f")}){
    Row {
        Text(label, Modifier.weight(1f))
        StatType.entries.forEach { statType ->
            val mod = Modifier.weight(statType.weight)
            when (statType){
                StatType.Count -> Text(statValue.count.toDisplayString(), mod)
                else -> Text(format(statType.selector(statValue)), mod)
            }
        }
    }
}
@Composable
internal fun CustomDivider(thickness: Dp = 2.dp, color: Color = Color.Gray, paddingTop: Dp = 8.dp){
    HorizontalDivider(thickness = thickness, color = color, modifier = Modifier.padding(top = paddingTop))
}