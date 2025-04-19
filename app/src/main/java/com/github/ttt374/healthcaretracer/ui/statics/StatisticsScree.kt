package com.github.ttt374.healthcaretracer.ui.statics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.TimeRangeDropdown
import com.github.ttt374.healthcaretracer.ui.common.toAnnotatedString
import com.github.ttt374.healthcaretracer.ui.common.toDisplayString

@Composable
fun StatisticsScreen(viewModel: StatisticsViewModel = hiltViewModel(), appNavigator: AppNavigator) {
    val selectedRange by viewModel.timeRange.collectAsState()
    val config by viewModel.config.collectAsState()
    val guideline = config.bloodPressureGuideline
    val statistics by viewModel.statistics.collectAsState()

    Scaffold(
        topBar = { CustomTopAppBar(stringResource(R.string.statistics)) },
        bottomBar = { CustomBottomAppBar(appNavigator) }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding).padding(horizontal = 8.dp)){
            item {
                Box(modifier = Modifier.padding(4.dp)) {
                    TimeRangeDropdown(selectedRange, onRangeSelected = { viewModel.setSelectedRange(it) })
                }
            }
            item {
                HorizontalDivider(thickness = 2.dp, color = Color.Gray, modifier = Modifier.padding(top =  8.dp))
                StatisticsTable(stringResource(R.string.blood_pressure), statistics.bloodPressure,
                    takeValue = { v: BloodPressure? -> v?.toAnnotatedString(guideline, false) ?: AnnotatedString("-")})
                val meGapStatValue = statistics.meGap.toStatValue()
                StatisticsRow(stringResource(R.string.me_gap), meGapStatValue, { v -> v.toAnnotatedString("%.1f")})
            }
            items(listOf(
                R.string.pulse to statistics.pulse,
                R.string.bodyTemperature to statistics.bodyTemperature,
                R.string.bodyWeight to statistics.bodyWeight))
            { (resId, stat) ->
                HorizontalDivider(thickness = 2.dp, color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                StatisticsTable(stringResource(resId), stat,  { v -> AnnotatedString(v.toDisplayString("%.1f"))})
            }
        }
    }
}

@Composable
fun <T> StatisticsTable(title: String, stat: StatTimeOfDay<T>, takeValue: (T?) -> AnnotatedString = { v -> v.toAnnotatedString()} ) {
    Column {
        StatisticsHeadersRow(title)

        listOf(
            R.string.all to stat.all,
            R.string.morning to stat.morning,
            R.string.afternoon to stat.afternoon,
            R.string.evening to stat.evening
        ).forEach { (labelRes, value) ->
            StatisticsRow(stringResource(labelRes), value, takeValue)
        }
    }
}

@Composable
fun <T> StatisticsRow(label: String, statValue: StatValue<T>, takeValue: (T?) -> AnnotatedString = { v -> v.toAnnotatedString()}) {
    Row {
        val statTextModifier = Modifier.weight(1f)
        Text(label, modifier = statTextModifier)
        listOf(statValue.avg, statValue.max, statValue.min).forEach { value ->
            Text(takeValue(value), modifier = statTextModifier)
        }
    }
}

internal fun Number.toAnnotatedString(format: String? = null): AnnotatedString = toAnnotatedString(format)
internal fun BloodPressure.toAnnotatedString(): AnnotatedString = toAnnotatedString()
internal fun <T> T?.toAnnotatedString(): AnnotatedString { throw(UnsupportedOperationException("Unsupported type in toAnnotatedString() "))}// fallback or generic

@Composable
fun StatisticsHeadersRow(title: String){
    Row {
        val statTextModifier = Modifier.weight(1f)

        Text(title, fontWeight = FontWeight.Bold, modifier = statTextModifier)
        listOf(R.string.average, R.string.max, R.string.min).forEach {
            Text(stringResource(it), statTextModifier)
        }
    }
    HorizontalDivider(thickness = 1.5.dp, color = Color.LightGray)
}
