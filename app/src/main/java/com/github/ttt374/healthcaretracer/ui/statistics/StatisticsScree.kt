package com.github.ttt374.healthcaretracer.ui.statistics

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
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.bloodpressure.toAnnotatedString
import com.github.ttt374.healthcaretracer.data.bloodpressure.toBloodPressure
import com.github.ttt374.healthcaretracer.data.metric.DayPeriod
import com.github.ttt374.healthcaretracer.data.metric.MetricCategory
import com.github.ttt374.healthcaretracer.data.metric.MetricDef
import com.github.ttt374.healthcaretracer.data.metric.MetricDefRegistry
import com.github.ttt374.healthcaretracer.data.metric.StatValue
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.shared.toAnnotatedString
import com.github.ttt374.healthcaretracer.shared.toDisplayString
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.TimeRangeDropdown

data class StatValueData (val all: StatValue, val byPeriod: Map<DayPeriod, StatValue>)

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel(), appNavigator: AppNavigator) {
    val config by viewModel.config.collectAsState()
    val timeRange by viewModel.timeRange.collectAsState()
    val statValueStateMap = viewModel.statValueMap.mapValues { (_, flow) ->
        flow.collectAsState().value
    }
    val dayPeriodStatValueStateMap = viewModel.dayPeriodStatMap.mapValues { (_, flow) ->
        flow.collectAsState().value
    }

    val firstDate by viewModel.firstDate.collectAsState()
    val meGapStatValue by viewModel.meGapStatValue.collectAsState()

    val statValueDataMap: Map<MetricDef, StatValueData> = statValueStateMap.mapValues { (key, all) ->
        val byPeriod = dayPeriodStatValueStateMap[key] ?: emptyMap()
        StatValueData(all = all, byPeriod = byPeriod)
    }

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
                when (category){
                    MetricCategory.BLOOD_PRESSURE -> {
                        val statUpper = statValueDataMap[MetricDefRegistry.getById("bp_upper")]
                        val statLower = statValueDataMap[MetricDefRegistry.getById("bp_lower")]
                        if (statUpper != null && statLower != null){
                            BloodPressureStatValueTable(statUpper, statLower, format = { it.toAnnotatedString(config.bloodPressureGuideline, false)})
                        }
                        StatValueRow(stringResource(R.string.me_gap), meGapStatValue, { it.toAnnotatedString("%.0f")})
                    }
                    else -> {
                        MetricDefRegistry.getByCategory(category).forEach { def ->
                            statValueDataMap[def]?.let { statValueData ->
                                MetricDefStatValueTable(def, statValueData) }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun BloodPressureStatValueTable(statUpperData: StatValueData, statLowerData: StatValueData, format: (BloodPressure) -> AnnotatedString ){
    CustomDivider()
    StatValueHeadersRow(stringResource(R.string.blood_pressure))
    StatValueBpRow(stringResource(R.string.all), statUpperData.all, statLowerData.all, format)
    statUpperData.byPeriod.forEach { (period, statUpper) ->
        statLowerData.byPeriod[period]?.let { statLower ->
            StatValueBpRow(stringResource(period.resId), statUpper, statLower, format)
        }

    }

}
@Composable
fun StatValueBpRow(label: String, statUpper: StatValue, statLower: StatValue, format: (BloodPressure) -> AnnotatedString ){
    Row {
        Text(label, Modifier.weight(1f))
        StatType.entries.forEach { statType ->
            //val mod = Modifier.weight(statType.weight)
            val bp = (statType.selector(statUpper)?.toInt() to statType.selector(statLower)?.toInt()).toBloodPressure()
            //Text(format(bp!!)) // TODO
            bp?.let { Text(format(it), Modifier.weight(1f))}
        }
        Text(statUpper.count.toString(), Modifier.weight(.7f))
    }
}
//enum class StatTypeBp (val resId: Int, val selector: (StatValue) -> Number?){
//    Average(R.string.average, { u, l -> u.avg?.let { upper -> l.avg?.let { lower -> BloodPressure(upper.toInt(), lower.toInt()) }}}),
//    Max(R.string.max,  { u, l -> u.max?.let { upper -> l.max?.let { lower -> BloodPressure(upper.toInt(), lower.toInt()) }}}),
//    Min(R.string.min,  { u, l -> u.min?.let { upper -> l.min?.let { lower -> BloodPressure(upper.toInt(), lower.toInt()) }}}),
//    //Count(R.string.count, { u, l -> u.count }, 0.7f);
//}
@Composable
fun MetricDefStatValueTable(metricDef: MetricDef, statValueData: StatValueData){
    CustomDivider()
    StatValueHeadersRow(stringResource(metricDef.resId))
    StatValueRow(stringResource(R.string.all), statValueData.all, metricDef.format)
    statValueData.byPeriod.forEach { (period, statValue) ->
        StatValueRow(stringResource(period.resId), statValue, metricDef.format)
    }
}

enum class StatType (val resId: Int, val selector: (StatValue) -> Number?, val weight: Float = 1f){
    Average(R.string.average, { it.avg }),
    Max(R.string.max, { it.max }),
    Min(R.string.min, { it.min }),
    //Count(R.string.count, { it.count }, 0.7f);
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
        Text(stringResource(R.string.count), Modifier.weight(0.7f))
    }
}
@Composable
fun StatValueRow(label: String, statValue: StatValue, format: (Number?) -> AnnotatedString){
    Row {
        Text(label, Modifier.weight(1f))
        StatType.entries.forEach { statType ->
            val mod = Modifier.weight(statType.weight)
            Text(format(statType.selector(statValue)), mod)
        }
        Text(statValue.count.toDisplayString(), Modifier.weight(0.7f))
    }
}
@Composable
internal fun CustomDivider(thickness: Dp = 2.dp, color: Color = Color.Gray, paddingTop: Dp = 8.dp){
    HorizontalDivider(thickness = thickness, color = color, modifier = Modifier.padding(top = paddingTop))
}