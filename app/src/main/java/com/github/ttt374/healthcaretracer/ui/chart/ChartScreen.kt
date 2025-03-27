package com.github.ttt374.healthcaretracer.ui.chart

import android.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.ttt374.healthcaretracer.data.Item
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.home.DailyItem
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@Composable
fun ChartScreen(chartViewModel: ChartViewModel = hiltViewModel(), navController: NavController){
    val dailyItems by chartViewModel.dailyItems.collectAsState()

    Scaffold(topBar = { CustomTopAppBar("Chart") },
        bottomBar = {
            CustomBottomAppBar(navController)
    }){ innerPadding ->
        Column(modifier=Modifier.padding(innerPadding)){
            BpPulseChart(dailyItems)
        }
    }
}
////////////
private fun LineChart.setupChart() {
    description.isEnabled = false
    xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return Instant.ofEpochMilli(value.toLong())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .toString()
            }
        }
    }
    axisRight.isEnabled = false
    //axisLeft.axisMinimum = 50.0F
}

private fun LineDataSet.setStyle(color: Int) {
    this.color = color
    this.setCircleColor(color)
    this.valueTextColor = color
    this.lineWidth = 2f
    this.circleRadius = 4f
}
fun List<Item>.groupByDateAndAverage(valueSelector: (Item) -> Int): List<Entry> {
    return this.sortedBy { it.measuredAt }
        .groupBy { it.measuredAt.truncatedTo(ChronoUnit.DAYS) }
        .map { (date, items) ->
            val avgValue = items.map(valueSelector).average().toFloat()
            Entry(date.toEpochMilli().toFloat(), avgValue)
        }
}
//fun List<Item>.toEntryList(valueSelector: (Item) -> Int): List<Entry>{
//    return this.sortedBy { it.measuredAt }.map { Entry(it.measuredAt.toEpochMilli().toFloat(), valueSelector(it).toFloat())}
//}
@Composable
fun BpPulseChart(dailyItems: List<DailyItem>){
    val bpHighEntries = dailyItems.map { Entry(it.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli().toFloat(), it.avgBpHigh.toFloat())}
    val bpLowEntries = dailyItems.map { Entry(it.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli().toFloat(), it.avgBpLow.toFloat())}
    val pulseEntries = dailyItems.map { Entry(it.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli().toFloat(), it.avgPulse.toFloat())}
//    val bpHighEntries = dailyItems.groupByDateAndAverage { it.bpHigh }
//    val bpLowEntries = dailyItems.groupByDateAndAverage { it.bpLow }
    AndroidView(
        factory = { context -> LineChart(context).apply { setupChart() } },
        modifier = Modifier.fillMaxSize(),
        update = { chart ->
            val pulseDataSet = LineDataSet(pulseEntries, "Pulse").apply { setStyle(Color.RED) }
            val bpHighDataSet = LineDataSet(bpHighEntries, "BP High").apply { setStyle(Color.BLUE) }
            val bpLowDataSet = LineDataSet(bpLowEntries, "BP Low").apply { setStyle(Color.GREEN) }

            chart.data = LineData(pulseDataSet, bpHighDataSet, bpLowDataSet)
            chart.invalidate()
        }
    )
}

