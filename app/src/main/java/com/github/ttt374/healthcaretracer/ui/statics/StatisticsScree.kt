package com.github.ttt374.healthcaretracer.ui.statics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import com.github.ttt374.healthcaretracer.data.bloodpressure.bloodPressureFormatted
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.TimeRangeDropdown
import com.github.ttt374.healthcaretracer.ui.home.toDisplayString


@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel(), appNavigator: AppNavigator) {
    val selectedRange by viewModel.timeRange.collectAsState()

    val bpUpperStat by viewModel.bpUpperStatistics.collectAsState()
    val bpLowerStat by viewModel.bpLowerStatistics.collectAsState()
    val pulseStat by viewModel.pulseStatistics.collectAsState()
    val bodyWeightStat by viewModel.bodyWeightStatistics.collectAsState()
    val meGapList by viewModel.meGapList.collectAsState()
    val conf by viewModel.config.collectAsState()
    val guideline = conf.bloodPressureGuideline

    Scaffold(
        topBar = { CustomTopAppBar(stringResource(R.string.statistics)) },
        bottomBar = { CustomBottomAppBar(appNavigator) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)){
            item {
                Box(modifier = Modifier.padding(4.dp)) {
                    TimeRangeDropdown(selectedRange) { viewModel.setSelectedRange(it) }
                }
            }

            item {
                HorizontalDivider(thickness = 2.dp, color = Color.Gray)
                Text(stringResource(R.string.blood_pressure), Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                StatisticsBpTable(bpUpperStat, bpLowerStat, guideline)
                Text("ME Gap: ${meGapList.maxOrNull()}")
            }
            item {
                HorizontalDivider(thickness = 2.dp, color = Color.Gray)
                Text(stringResource(R.string.pulse), Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                StatisticsTable(pulseStat, takeValue = { v: Double? -> v.toDisplayString("%.0f")})
            }
            item {
                HorizontalDivider(thickness = 2.dp, color = Color.Gray)
                Text(stringResource(R.string.body_weight), Modifier.padding(8.dp), fontWeight = FontWeight.Bold)
                StatisticsTable(bodyWeightStat, takeValue = { v: Double? -> v.toDisplayString("%.1f")})
            }

        }
    }
}

@Composable
fun StatisticsBpTable(bpUpperStat: StatTimeOfDay, bpLowerStat: StatTimeOfDay, guideline: BloodPressureGuideline){
    Column {
        StatisticsHeadersRow()
        StatisticsBpRow(stringResource(R.string.all), bpUpperStat.all, bpLowerStat.all, guideline)
        StatisticsBpRow(stringResource(R.string.morning), bpUpperStat.morning, bpLowerStat.morning, guideline)
        StatisticsBpRow(stringResource(R.string.evening), bpUpperStat.evening, bpLowerStat.evening, guideline )
    }
}
@Composable
fun StatisticsBpRow(label: String, bpUpperStatValue: StatValue, bpLowerStatValue: StatValue , guideline: BloodPressureGuideline){
    Row {
        Text(label, Modifier.weight(1f))
        Text(bloodPressureFormatted(bpUpperStatValue.avg?.toInt(), bpLowerStatValue.avg?.toInt(), false, guideline), Modifier.weight(1f))
        Text(bloodPressureFormatted(bpUpperStatValue.max?.toInt(), bpLowerStatValue.max?.toInt(), false, guideline), Modifier.weight(1f))
        Text(bloodPressureFormatted(bpUpperStatValue.min?.toInt(), bpLowerStatValue.min?.toInt(), false, guideline), Modifier.weight(1f))
    }
}

@Composable
fun StatisticsTable(stat: StatTimeOfDay, takeValue: (Double?) -> String){
    Column {
        StatisticsHeadersRow()

        StatisticsRow(stringResource(R.string.all), stat.all, takeValue)
        StatisticsRow(stringResource(R.string.morning), stat.morning, takeValue)
        StatisticsRow(stringResource(R.string.evening), stat.evening, takeValue)
    }
}
@Composable
fun StatisticsRow(label: String, statValue: StatValue, takeValue: (Double?) -> String){
    Row {
        Text(label, Modifier.weight(1f))
        Text(takeValue(statValue.avg), Modifier.weight(1f))
        Text(takeValue(statValue.max), Modifier.weight(1f))
        Text(takeValue(statValue.min), Modifier.weight(1f))
    }
}
@Composable
fun StatisticsHeadersRow(){
    Row {
        Spacer(Modifier.weight(1f))
        Text(stringResource(R.string.average), Modifier.weight(1f))
        Text(stringResource(R.string.max), Modifier.weight(1f))
        Text(stringResource(R.string.min), Modifier.weight(1f))
    }
    HorizontalDivider(thickness = 1.5.dp, color = Color.LightGray)
}
