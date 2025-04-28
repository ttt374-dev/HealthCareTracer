package com.github.ttt374.healthcaretracer.ui.statistics

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import com.github.ttt374.healthcaretracer.data.metric.MetricType
import com.github.ttt374.healthcaretracer.data.metric.StatData
import com.github.ttt374.healthcaretracer.data.metric.StatValue
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.shared.toAnnotatedString
import com.github.ttt374.healthcaretracer.shared.toDisplayString
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.TimeRangeDropdown

fun <T> List<T>.firstAndSecondOrNull(): Pair<T?, T?> {
    return if (this.size > 1) {
        this[0] to this[1]  // 1番目と2番目の要素をペアで返す
    } else {
        null to null  // 要素が2つ未満の場合はnullを返す
    }
}

fun <A, B> Pair<A?, B?>.forEachNonNull(action: (A, B) -> Unit) {
    val (first, second) = this
    if (first != null && second != null) {
        action(first, second)
    }
}

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel(), appNavigator: AppNavigator) {
    val config by viewModel.config.collectAsState()
    val timeRange by viewModel.timeRange.collectAsState()
    val metricType = MetricType.BLOOD_PRESSURE

    val statDataList by viewModel.getStatDataListForMetricType(metricType).collectAsState()

    val statValueStateMap = viewModel.statValueMap.mapValues { (_, flow) ->
        flow.collectAsState().value
    }
    val dayPeriodStatValueStateMap = viewModel.dayPeriodStatMap.mapValues { (_, flow) ->
        flow.collectAsState().value
    }

    val firstDate by viewModel.firstDate.collectAsState()
    val meGapStatValue by viewModel.meGapStatValue(MetricType.BLOOD_PRESSURE.defs.first()).collectAsState()  // TODO first check

//    val statDataMap: Map<MetricDef, StatData> = statValueStateMap.mapValues { (key, all) ->
//        val byPeriod = dayPeriodStatValueStateMap[key] ?: emptyMap()
//        StatData(all = all, byPeriod = byPeriod)
//    }

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
            item {
                BloodPressureStatDataTable(statDataList, meGapStatValue) { it.toAnnotatedString(config.bloodPressureGuideline, false) }

            }
//            statDataList.firstAndSecondOrNull().forEachNonNull { upper, lower ->
//                item {
//                    BloodPressureStatValueTable(upper, lower) { it.toAnnotatedString(showUnit = false)}
//                }
//            }

//                statDataList.forEach { statData ->
//                    MetricDefStatDataTable(statData)
//                }


            //items(MetricType.entries){ category ->
//                when (metricType){
//                    MetricType.BLOOD_PRESSURE -> {
//                        val statUpper = statValueDataMap[MetricDefRegistry.getById("bp_upper")]
//                        val statLower = statValueDataMap[MetricDefRegistry.getById("bp_lower")]
//                        if (statUpper != null && statLower != null){
//                            BloodPressureStatValueTable(statUpper, statLower, format = { it.toAnnotatedString(config.bloodPressureGuideline, false)})
//                        }
//                        StatValueRow(stringResource(R.string.me_gap), meGapStatValue, { it.toAnnotatedString("%.0f")})
//                    }
//                    else -> {
//                        MetricDefRegistry.getByCategory(metricType).forEach { def ->
//                            statValueDataMap[def]?.let { statValueData ->
//                                MetricDefStatValueTable(def, statValueData) }
//                        }
//                    }
//                }
            //}
        }
    }
}
@Composable
fun BloodPressureStatDataTable(statDataList: List<StatData>, meGapStatValue: StatValue, format: (BloodPressure) -> AnnotatedString ){
    val (statUpperData, statLowerData) = statDataList.firstAndSecondOrNull()
    CustomDivider()
    if (statUpperData != null && statLowerData != null){
        StatValueHeadersRow(stringResource(R.string.blood_pressure))
        StatValueBpRow(stringResource(R.string.all), statUpperData.all, statLowerData.all, format)
        statUpperData.byPeriod.forEach { (period, statUpper) ->
            statLowerData.byPeriod[period]?.let { statLower ->
                StatValueBpRow(stringResource(period.resId), statUpper, statLower, format)
            }
        }
        StatValueRow(stringResource(R.string.me_gap), meGapStatValue, { it.toAnnotatedString("%.0f")})
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
fun MetricDefStatDataTable(statData: StatData){
    CustomDivider()
    with(statData){
        StatValueHeadersRow(stringResource(metricDef.resId))
        StatValueRow(stringResource(R.string.all), all, metricDef.format)
        byPeriod.forEach { (period, statValue) ->
            StatValueRow(stringResource(period.resId), statValue, metricDef.format)
        }
    }
}


enum class StatType (val resId: Int, val selector: (StatValue) -> Number?, val format: ((Number?) -> AnnotatedString)? = null){
    Average(R.string.average, { it.avg }, { it.toAnnotatedString("%.1f")} ),
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
            Text(stringResource(it.resId), Modifier.weight(1f))
        }
        Text(stringResource(R.string.count), Modifier.weight(0.7f))
    }
}
@Composable
fun StatValueRow(label: String, statValue: StatValue, format: (Number?) -> AnnotatedString){
    Row {
        Text(label, Modifier.weight(1f))
        StatType.entries.forEach { statType ->
            val mod = Modifier.weight(1f)
            statType.format?.let { statFormat ->
                Text(statFormat(statType.selector(statValue)), mod)
            } ?:  Text(format(statType.selector(statValue)), mod)
        }
        Text(statValue.count.toDisplayString(), Modifier.weight(0.7f))
    }
}
@Composable
internal fun CustomDivider(thickness: Dp = 2.dp, color: Color = Color.Gray, paddingTop: Dp = 8.dp){
    HorizontalDivider(thickness = thickness, color = color, modifier = Modifier.padding(top = paddingTop))
}