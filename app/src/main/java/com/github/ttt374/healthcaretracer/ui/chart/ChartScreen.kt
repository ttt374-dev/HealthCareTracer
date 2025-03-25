package com.github.ttt374.healthcaretracer.ui.chart

import android.graphics.Color
import androidx.compose.foundation.layout.Box
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
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

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
fun List<Item>.toEntryList(valueSelector: (Item) -> Int): List<Entry>{
    return this.sortedBy { it.measuredAt }.map { Entry(it.measuredAt.toEpochMilli().toFloat(), valueSelector(it).toFloat())}
}
@Composable
fun BpPulseChart(items: List<Item>){
//    val bpHighEntries = mutableListOf<Entry>()
//    val bpLowEntries = mutableListOf<Entry>()
//    val pulseEntries = mutableListOf<Entry>()
//
//    items.sortedBy { it.measuredAt }.forEach { item ->
//        bpHighEntries.add(Entry(item.measuredAt.toEpochMilli().toFloat(), item.bpHigh.toFloat()))
//        bpLowEntries.add(Entry(item.measuredAt.toEpochMilli().toFloat(), item.bpLow.toFloat()))
//        pulseEntries.add(Entry(item.measuredAt.toEpochMilli().toFloat(), item.pulse.toFloat()))
//    }
    val pulseEntries = items.groupByDateAndAverage { it.pulse }
    val bpHighEntries = items.groupByDateAndAverage { it.bpHigh }
    val bpLowEntries = items.groupByDateAndAverage { it.bpLow }

//    val bpHighEntries = items.toEntryList { it.bpHigh }
//    val bpLowEntries = items.toEntryList { it.bpLow }
//    val pulseEntries = items.toEntryList { it.pulse }
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
@Composable
fun ChartScreen(chartViewModel: ChartViewModel = hiltViewModel(), navController: NavController){
    val items by chartViewModel.items.collectAsState()

    Scaffold(topBar = { CustomTopAppBar("Chart") },
        bottomBar = {
            CustomBottomAppBar(navController)
        }){ innerPadding ->
            Column(modifier=Modifier.padding(innerPadding)){
                BpPulseChart(items)
            }
    }
}

