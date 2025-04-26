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

//    val pulseDef = MetricDefRegistry.getById("pulse")!!
//    //val statValueMap by viewModel.statValue().collectAsState()
//    //val dayPeriodStatValueMap by viewModel.dayPeriodStatValueMap.collectAsState()

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
                    StatValueRow(stringResource(R.string.all), statValueStateMap[def]?.value)
                    dayPeriodStatValueStateMap[def]?.value?.mapValues { (period, statValue) ->
                        StatValueRow(stringResource(period.resId), statValue)
                    }
                }

            }
        }
    }
}
@Composable
fun StatValueHeadersRow(label: String){
    Row {
        val mod = Modifier.weight(1f)
        val modCount = Modifier.weight(0.7f)
        Text(label, mod, fontWeight = FontWeight.Bold)
        listOf(R.string.average, R.string.max, R.string.min).forEach {
            Text(stringResource(it), mod)
        }
        Text(stringResource(R.string.count), modCount)
    }
}
@Composable
fun StatValueRow(label: String, statValue: StatValue<Double>?, format: (Double?) -> AnnotatedString = { it.toAnnotatedString("%.1f")}){
    Row {
        val mod = Modifier.weight(1f)
        val modCount = Modifier.weight(0.7f)
        Text(label, mod)
        listOf(statValue?.avg, statValue?.max, statValue?.min).forEach {
            Text(format(it), mod)
        }
        Text(statValue?.count.toDisplayString(), modCount)
    }
}
@Composable
internal fun CustomDivider(thickness: Dp = 2.dp, color: Color = Color.Gray, paddingTop: Dp = 8.dp){
    HorizontalDivider(thickness = thickness, color = color, modifier = Modifier.padding(top = paddingTop))
}
//@Composable
//fun <T> StatisticsTable(title: String, stat: StatTimeOfDay<T>, takeValue: (T?) -> AnnotatedString = { v -> toAnnotatedString() } ) {
//    Column {
//        StatisticsHeadersRow(title)
//
//        listOf(
//            R.string.all to stat.all,
//            R.string.morning to stat.morning,
//            R.string.afternoon to stat.afternoon,
//            R.string.evening to stat.evening
//        ).forEach { (labelRes, value) ->
//            StatisticsRow(stringResource(labelRes), value, takeValue)
//        }
//    }
//}
//
//sealed class StatType (val resId: Int){
//    abstract fun <T> getDisplayValue(stat: StatValue<T>): T?
//
//    data object Average: StatType(R.string.average){ override fun <T> getDisplayValue(stat: StatValue<T>) = stat.avg }
//    data object Max: StatType(R.string.max){ override fun <T> getDisplayValue(stat: StatValue<T>) = stat.max }
//    data object Min: StatType(R.string.min){ override fun <T> getDisplayValue(stat: StatValue<T>) = stat.min }
//
//    companion object {
//        val entries = listOf(Average, Max, Min)
//    }
//}
//@Composable
//fun StatisticsBaseRow(label: String, format: @Composable (StatType) -> AnnotatedString, countString: String, fontWeight: FontWeight? = null, ){
//    Row {
//        Text(label, modifier = Modifier.weight(1f), fontWeight = fontWeight)
//        StatType.entries.forEach {
//            Text(format(it), Modifier.weight(1f))
//        }
//        Text(countString, Modifier.weight(0.7f))
//    }
//}
//@Composable
//fun StatisticsHeadersRow(title: String){
//    StatisticsBaseRow(title, { stringResource(it.resId).toAnnotatedString()}, stringResource(R.string.count), FontWeight.Bold)
//    HorizontalDivider(thickness = 1.5.dp, color = Color.LightGray)
//}
//
//@Composable
//fun <T> StatisticsRow(label: String, statValue: StatValue<T>, format: (T?) -> AnnotatedString = { v -> toAnnotatedString() }) {
//    StatisticsBaseRow(label, { format(it.getDisplayValue(statValue))}, statValue.count.toString())
//}
//
////internal fun Number.toAnnotatedString(format: String? = null): AnnotatedString = toAnnotatedString(format)
////internal fun BloodPressure?.toAnnotatedString(): AnnotatedString = toAnnotatedString()
//internal fun toAnnotatedString(): AnnotatedString { throw(UnsupportedOperationException("Unsupported type in toAnnotatedString() "))}// fallback or generic

