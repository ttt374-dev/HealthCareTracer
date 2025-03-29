package com.github.ttt374.healthcaretracer.ui.chart

import android.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.home.DailyItem
import com.github.ttt374.healthcaretracer.ui.home.DailyItemsViewModel
import java.time.Instant
import java.time.ZoneId

@Composable
fun ChartScreen(dailyItemsViewModel: DailyItemsViewModel = hiltViewModel(), appNavigator: AppNavigator){
    val dailyItems by dailyItemsViewModel.dailyItems.collectAsState()
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Blood Pressure", "Pulse", "Body Weight")

    Scaffold(topBar = { CustomTopAppBar("Chart") },
        bottomBar = {
            CustomBottomAppBar(appNavigator)
        }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // 選択されたタブに応じて異なるグラフを表示
            when (selectedTabIndex) {
                0 -> BloodPressureChart(dailyItems)
                1 -> PulseChart(dailyItems)
                2 -> BodyWeightChart(dailyItems)
            }
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
                    .atZone(ZoneId.systemDefault()).toLocalDate().toString()
            }
        }
    }
    axisLeft.apply {
        spaceTop = 40f
        spaceBottom = 40f
        axisMinimum = 0f
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

fun List<DailyItem>.toEntries(takeValue: (DailyItem) -> Float ) = this.map {
    Entry(it.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli().toFloat(), takeValue(it))
}

@Composable
fun HealthChart(update: (LineChart) -> Unit){
    AndroidView(
        factory = { context -> LineChart(context).apply { setupChart() } },
        modifier = Modifier.fillMaxSize(),
        update = { chart -> update(chart) }
    )
}
@Composable
fun BloodPressureChart(dailyItems: List<DailyItem>){
    HealthChart(){ chart ->
        val bpHighEntries = dailyItems.toEntries {it.avgBpHigh.toFloat() }
        val bpLowEntries = dailyItems.toEntries {it.avgBpLow.toFloat() }

        val bpHighDataSet = LineDataSet(bpHighEntries, "BP High").apply { setStyle(Color.BLUE) }
        val bpLowDataSet = LineDataSet(bpLowEntries, "BP Low").apply { setStyle(Color.GREEN) }

        chart.data = LineData(bpHighDataSet, bpLowDataSet)
        chart.invalidate()
    }
}

@Composable
fun PulseChart(dailyItems: List<DailyItem>){
    HealthChart(){ chart ->
        val pulseEntries = dailyItems.toEntries {it.avgPulse.toFloat() }

        val pulseDataSet = LineDataSet(pulseEntries, "Pulse").apply { setStyle(Color.RED) }
        chart.data = LineData(pulseDataSet)
        chart.invalidate()
    }
}
@Composable
fun BodyWeightChart(dailyItems: List<DailyItem>){
    HealthChart(){ chart ->
        val bodyWightEntries = dailyItems.sortedBy { it.date }.filter { it.avgBodyWeight > 0 }.toEntries { it.avgBodyWeight }
        val bodyWeightDataSet = LineDataSet(bodyWightEntries, "Body Weight").apply { setStyle(Color.GREEN) }

        chart.data = LineData(bodyWeightDataSet)
        chart.invalidate()
    }
}