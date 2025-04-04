package com.github.ttt374.healthcaretracer.ui.statics

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.data.bloodPressureFormatted
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.TimeRangeDropdown
import com.github.ttt374.healthcaretracer.ui.home.toDisplayString


@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel(), appNavigator: AppNavigator) {
    val selectedRange by viewModel.selectedRange.collectAsState()
//    val statistics by viewModel.statistics.collectAsState()
//    val statisticsMorning by viewModel.statisticsMorning.collectAsState()
//    val statisticsEvening by viewModel.statisticsEvening.collectAsState()


    val bpUpperStat by viewModel.bpUpperStatistics.collectAsState()
    val bpLowerStat by viewModel.bpLowerStatistics.collectAsState()
    val pulseStat by viewModel.pulseStatistics.collectAsState()

    Scaffold(
        topBar = { CustomTopAppBar("Statistics") },
        bottomBar = { CustomBottomAppBar(appNavigator) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)){
            item {
                Box(modifier = Modifier.padding(4.dp)) {
                    TimeRangeDropdown(selectedRange) { viewModel.setSelectedRange(it) }
                }
            }

            item {
                Text("Blood Pressure", Modifier.padding(16.dp))
                StatisticsBpTable(bpUpperStat, bpLowerStat)
                //StatisticsBpTable(statistics, statisticsMorning, statisticsEvening)
                //StatisticsTable(statistics)
            }
            item {
                Text("Pulse", Modifier.padding(16.dp))
                //StatisticsPulseTable(statistics, statisticsMorning, statisticsEvening)
                StatisticsPulseTable(pulseStat)
            }

        }
    }
}
@Composable
fun StatisticsTable(){

}

@Composable
fun StatisticsBpTable(bpUpperStat: StatTimeOfDay, bpLowerStat: StatTimeOfDay){
    Column {
        StatisticsHeadersRow()
        Row {
            Text("All", Modifier.weight(1f))
            Text(bloodPressureFormatted(bpUpperStat.all.avg?.toInt(), bpLowerStat.all.avg?.toInt(), false), Modifier.weight(1f))
            Text(bloodPressureFormatted(bpUpperStat.all.max?.toInt(), bpLowerStat.all.max?.toInt(), false), Modifier.weight(1f))
            Text(bloodPressureFormatted(bpUpperStat.all.min?.toInt(), bpLowerStat.all.min?.toInt(), false), Modifier.weight(1f))
        }
        Row {
            Text("Morning", Modifier.weight(1f))
            Text(bloodPressureFormatted(bpUpperStat.morning.avg?.toInt(), bpLowerStat.morning.avg?.toInt(), false), Modifier.weight(1f))
            Text(bloodPressureFormatted(bpUpperStat.morning.max?.toInt(), bpLowerStat.morning.max?.toInt(), false), Modifier.weight(1f))
            Text(bloodPressureFormatted(bpUpperStat.morning.min?.toInt(), bpLowerStat.morning.min?.toInt(), false), Modifier.weight(1f))
        }
        Row {
            Text("Evening", Modifier.weight(1f))
            Text(bloodPressureFormatted(bpUpperStat.evening.avg?.toInt(), bpLowerStat.evening.avg?.toInt(), false), Modifier.weight(1f))
            Text(bloodPressureFormatted(bpUpperStat.evening.max?.toInt(), bpLowerStat.evening.max?.toInt(), false), Modifier.weight(1f))
            Text(bloodPressureFormatted(bpUpperStat.evening.min?.toInt(), bpLowerStat.evening.min?.toInt(), false), Modifier.weight(1f))
        }
    }
}

@Composable
fun StatisticsPulseTable(stat: StatTimeOfDay){
    Column {
        StatisticsHeadersRow()
        val takeValue = { v: Double? -> v.toDisplayString("%.0f")}
        StatisticsRow("All", stat.all, takeValue)
        StatisticsRow("Morning", stat.morning, takeValue)
        StatisticsRow("Evening", stat.evening, takeValue)
    }
}
@Composable
fun StatisticsRow(label: String, statValue: StatValue, takeValue: (Double?) -> String = { it.toDisplayString() }){
    Row {
        Text(label, Modifier.weight(1f))
        Text(takeValue(statValue.avg), Modifier.weight(1f))
        Text(takeValue(statValue.max), Modifier.weight(1f))
        Text(takeValue(statValue.min), Modifier.weight(1f))
//        Text(statValue.avg.toDisplayString(format), Modifier.weight(1f))
//        Text(statValue.max.toDisplayString(format), Modifier.weight(1f))
//        Text(statValue.min.toDisplayString(format), Modifier.weight(1f))
    }
}
//@Composable
//fun StatisticsPulseTable(statistics: StatisticsData, statisticsMorning: StatisticsData, statisticsEvening: StatisticsData){
//    Column {
//        StatisticsHeadersRow()
//        Row {
//            Text("All", Modifier.weight(1f))
//            Text(statistics.pulse.avg.toDisplayString("%.0f"), Modifier.weight(1f))
//            Text(statistics.pulse.max.toDisplayString("%.0f"), Modifier.weight(1f))
//            Text(statistics.pulse.min.toDisplayString("%.0f"), Modifier.weight(1f))
//        }
//        Row {
//            Text("Morning", Modifier.weight(1f))
//            Text(statisticsMorning.pulse.avg.toDisplayString("%.0f"), Modifier.weight(1f))
//            Text(statisticsMorning.pulse.max.toDisplayString("%.0f"), Modifier.weight(1f))
//            Text(statisticsMorning.pulse.min.toDisplayString("%.0f"), Modifier.weight(1f))
//        }
//        Row {
//            Text("Evening", Modifier.weight(1f))
//            Text(statisticsEvening.pulse.avg.toDisplayString("%.0f"), Modifier.weight(1f))
//            Text(statisticsEvening.pulse.max.toDisplayString("%.0f"), Modifier.weight(1f))
//            Text(statisticsEvening.pulse.min.toDisplayString("%.0f"), Modifier.weight(1f))
//        }
//    }
//}
//@Composable
//fun StatisticsTable(statistics: StatisticsData){
//    HorizontalDivider(thickness = 1.5.dp, color = Color.LightGray)
//    StatisticsHeadersRow()
//    StatisticsItemRow("average",
//        statistics.bpUpper.avg,
//        statistics.bpLower.avg,
//        //statistics.meGap.avg,
//        statistics.pulse.avg,
//        statistics.bodyWeight.avg
//    )
//    StatisticsItemRow("max",
//        statistics.bpUpper.max,
//        statistics.bpLower.max,
//        //statistics.meGap.max,
//        statistics.pulse.max,
//        statistics.bodyWeight.max
//    )
//    StatisticsItemRow("min",
//        statistics.bpUpper.min,
//        statistics.bpLower.min,
//        //statistics.meGap.min,
//        statistics.pulse.min,
//        statistics.bodyWeight.min
//    )
//}

@Composable
fun StatisticsHeadersRow(){
    Row {
        Spacer(Modifier.weight(1f))
        Text("avg", Modifier.weight(1f))
        Text("max", Modifier.weight(1f))
        Text("min", Modifier.weight(1f))
//        StatisticsHeaderField ("Blood Pressure", "mmHg", modifier = Modifier.weight(1f))
//        //StatisticsHeaderField ("ME Gap", "mmHg", modifier = Modifier.weight(1f))
//        StatisticsHeaderField ("Pulse", "bpm", modifier = Modifier.weight(1f))
//        StatisticsHeaderField ("Body Weight", "Kg", modifier = Modifier.weight(1f))
    }
    HorizontalDivider(thickness = 1.5.dp, color = Color.LightGray)
}
@Composable
fun StatisticsHeaderField(name: String, unit: String, modifier: Modifier = Modifier){
    val unitFontSize = 12.sp
    Column(modifier = modifier) {
        Text(name)
        Text("($unit)", fontSize = unitFontSize)
    }
}
//fun String.withUnit(unit: String, unitFontSize: TextUnit = 12.sp) =
//    buildAnnotatedString {
//        append(this@withUnit)
//        withStyle(SpanStyle(fontSize = unitFontSize)) {
//            append(unit)
//        }
//    }

@Composable
fun StatisticsItemRow(label: String, bpUpper: Double?, bpLower: Double?, pulse: Double?, bodyWeight: Double?){
    Row(horizontalArrangement = Arrangement.Center) {
        Text(label, Modifier.weight(1f))
        Text(bloodPressureFormatted(bpUpper?.toInt(), bpLower?.toInt(), false), Modifier.weight(1f))
        //Text(meGap.toDisplayString("%.0f"), Modifier.weight(1f))
        Text(pulse.toDisplayString("%.0f"), Modifier.weight(1f))
        Text(bodyWeight.toDisplayString("%.1f"), Modifier.weight(1f))
        //Text(pulse?.toInt().toPulseString(), Modifier.weight(1f))
        //Text(bodyWeight.toBodyWeightString(), Modifier.weight(1f))
    }
    HorizontalDivider(thickness = 0.75.dp, color = Color.LightGray)
}

//fun List<Item>.filterByRange(range: TimeRange): List<Item> {
//    val cutoffDate = Instant.now().minus(range.days.toLong(), ChronoUnit.DAYS)
//    return filter { it.measuredAt.isAfter(cutoffDate) }
//}

