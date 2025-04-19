package com.github.ttt374.healthcaretracer.ui.statics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import com.github.ttt374.healthcaretracer.data.bloodpressure.toBloodPressure
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.TimeRangeDropdown
import com.github.ttt374.healthcaretracer.ui.home.toDisplayString


@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel(), appNavigator: AppNavigator) {
    val selectedRange by viewModel.timeRange.collectAsState()
    val conf by viewModel.config.collectAsState()
    val guideline = conf.bloodPressureGuideline
    val statistics by viewModel.statistics.collectAsState()

    data class StatisticsItem(
        val resId: Int, val statTimeOfDay: StatTimeOfDay<Double>, val takeValue: (Double?) -> AnnotatedString
    )
    val statisticsItems = listOf(
//        StatisticsItem(R.string.blood_pressure, statistics.bloodPressure, { v: BloodPressure? -> v?.toDisplayString() ?: AnnotatedString("-")}),
        StatisticsItem(R.string.pulse, statistics.pulse, { v -> AnnotatedString(v.toDisplayString("%.0f"))}),
        StatisticsItem(R.string.bodyTemperature, statistics.bodyTemperature, { v -> AnnotatedString(v.toDisplayString("%.0f"))}),
        StatisticsItem(R.string.bodyWeight, statistics.bodyWeight, { v -> AnnotatedString(v.toDisplayString("%.1f"))}),
    )
    Scaffold(
        topBar = { CustomTopAppBar(stringResource(R.string.statistics)) },
        bottomBar = { CustomBottomAppBar(appNavigator) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier
            .padding(innerPadding)
            .padding(horizontal = 8.dp)){
            item {
                Box(modifier = Modifier.padding(4.dp)) {
                    TimeRangeDropdown(selectedRange, onRangeSelected = { viewModel.setSelectedRange(it) })
                }
            }
            item {
                HorizontalDivider(thickness = 2.dp, color = Color.Gray, modifier = Modifier.padding(top =  8.dp))
                StatisticsTable(stringResource(R.string.blood_pressure), statistics.bloodPressure,
                    takeValue = { v: BloodPressure? -> v?.toDisplayString() ?: AnnotatedString("-")})
                //StatisticsBpTable(stringResource(R.string.blood_pressure), statistics.bpUpper, statistics.bpLower, guideline)
                Text("${stringResource(R.string.me_gap)}: ${statistics.meGap.maxOrNull()}")
            }
            statisticsItems.forEach { statItem ->
                item {
                    HorizontalDivider(thickness = 2.dp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                    StatisticsTable(stringResource(statItem.resId), statItem.statTimeOfDay, statItem.takeValue)
                }
            }
//            item {
//                HorizontalDivider(thickness = 2.dp, color = Color.Gray, modifier = Modifier.padding(top =  8.dp))
//                StatisticsTable(stringResource(R.string.pulse), statistics.pulse,
//                    takeValue = { v: Double? ->
//                        AnnotatedString(v.toDisplayString("%.0f"))
//                    })
//            }
//            item {
//                HorizontalDivider(thickness = 2.dp, color = Color.Gray, modifier = Modifier.padding(top =  8.dp))
//                StatisticsTable(stringResource(R.string.bodyWeight), statistics.bodyWeight,
//                    takeValue =  { v: Double? ->
//                        AnnotatedString(v.toDisplayString("%.0f"))
//                    })
//            }
//            item {
//                HorizontalDivider(thickness = 2.dp, color = Color.Gray, modifier = Modifier.padding(top =  8.dp))
//                StatisticsTable(stringResource(R.string.bodyTemperature), statistics.bodyTemperature,
//                    takeValue =  { v: Double? ->
//                        AnnotatedString(v.toDisplayString("%.1f"))
//                    })
//            }
        }
    }
}

//@Composable
//fun StatisticsBpTable(title: String, bpUpperStat: StatTimeOfDay<Double>, bpLowerStat: StatTimeOfDay<Double>, guideline: BloodPressureGuideline){
//    Column {
//        StatisticsHeadersRow(title)
//        StatisticsBpRow(stringResource(R.string.all), bpUpperStat.all, bpLowerStat.all, guideline)
//        StatisticsBpRow(stringResource(R.string.morning), bpUpperStat.morning, bpLowerStat.morning, guideline)
//        StatisticsBpRow(stringResource(R.string.afternoon), bpUpperStat.afternoon, bpLowerStat.afternoon, guideline)
//        StatisticsBpRow(stringResource(R.string.evening), bpUpperStat.evening, bpLowerStat.evening, guideline )
//    }
//}
//@Composable
//fun StatisticsBpRow(label: String, bpUpperStatValue: StatValue<Double>, bpLowerStatValue: StatValue<Double> , guideline: BloodPressureGuideline){
//    Row {
//        Text(label, Modifier.weight(1f))
//        Text(Pair(bpUpperStatValue.avg, bpLowerStatValue.avg).toBloodPressure().toDisplayString(guideline, false), modifier=Modifier.weight(1f))
//        Text(Pair(bpUpperStatValue.max, bpLowerStatValue.max).toBloodPressure().toDisplayString(guideline, false), modifier=Modifier.weight(1f))
//        Text(Pair(bpUpperStatValue.min, bpLowerStatValue.min).toBloodPressure().toDisplayString(guideline, false), modifier=Modifier.weight(1f))
//        //Text(BloodPressure(bpUpperStatValue.avg?.toInt(), bpLowerStatValue.avg?.toInt()).toDisplayString(guideline = guideline), Modifier.weight(1f))
//        //Text(BloodPressure(bpUpperStatValue.max?.toInt(), bpLowerStatValue.max?.toInt()).toDisplayString(guideline = guideline), Modifier.weight(1f))
//        //Text(BloodPressure(bpUpperStatValue.min?.toInt(), bpLowerStatValue.min?.toInt()).toDisplayString(guideline = guideline), Modifier.weight(1f))
////        Text(bloodPressureFormatted(bpUpperStatValue.avg?.toInt(), bpLowerStatValue.avg?.toInt(), false, guideline), Modifier.weight(1f))
////        Text(bloodPressureFormatted(bpUpperStatValue.max?.toInt(), bpLowerStatValue.max?.toInt(), false, guideline), Modifier.weight(1f))
////        Text(bloodPressureFormatted(bpUpperStatValue.min?.toInt(), bpLowerStatValue.min?.toInt(), false, guideline), Modifier.weight(1f))
//    }
//}

@Composable
fun <T> StatisticsTable(title: String, stat: StatTimeOfDay<T>, takeValue: (T?) -> AnnotatedString) {
    Column {
        StatisticsHeadersRow(title)
        StatisticsRow(stringResource(R.string.all), stat.all, takeValue)
        StatisticsRow(stringResource(R.string.morning), stat.morning, takeValue)
        StatisticsRow(stringResource(R.string.afternoon), stat.afternoon, takeValue)
        StatisticsRow(stringResource(R.string.evening), stat.evening, takeValue)
    }
}

@Composable
fun <T> StatisticsRow(label: String, statValue: StatValue<T>, takeValue: (T?) -> AnnotatedString){
    Row {
        Text(label, modifier = Modifier.weight(1f))
        Text(takeValue(statValue.avg), Modifier.weight(1f))
        Text(takeValue(statValue.max), Modifier.weight(1f))
        Text(takeValue(statValue.min), Modifier.weight(1f))
    }
}
//@Composable
//fun StatisticsTable(title: String, stat: StatTimeOfDay<Double>, takeValue: (Double?) -> String){
//    Column {
//        StatisticsHeadersRow(title)
//        StatisticsRow(stringResource(R.string.all), stat.all, takeValue)
//        StatisticsRow(stringResource(R.string.morning), stat.morning, takeValue)
//        StatisticsRow(stringResource(R.string.afternoon), stat.afternoon, takeValue)
//        StatisticsRow(stringResource(R.string.evening), stat.evening, takeValue)
//    }
//}
//@Composable
//fun StatisticsRow(label: String, statValue: StatValue<Double>, takeValue: (Double?) -> String){
//    Row {
//        Text(label, modifier = Modifier.weight(1f))
//        Text(takeValue(statValue.avg), Modifier.weight(1f))
//        Text(takeValue(statValue.max), Modifier.weight(1f))
//        Text(takeValue(statValue.min), Modifier.weight(1f))
//    }
//}
@Composable
fun StatisticsHeadersRow(title: String){
    Row {
        //Spacer(Modifier.weight(1f))
        Text(title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        Text(stringResource(R.string.average), Modifier.weight(1f))
        Text(stringResource(R.string.max), Modifier.weight(1f))
        Text(stringResource(R.string.min), Modifier.weight(1f))
    }
    HorizontalDivider(thickness = 1.5.dp, color = Color.LightGray)
}
