package com.github.ttt374.healthcaretracer.ui.chart

import android.graphics.Color
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
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ChartScreen(chartViewModel: ChartViewModel = hiltViewModel(), navController: NavController){
    val items by chartViewModel.items.collectAsState()

    Scaffold(topBar = { CustomTopAppBar("Chart") },
        bottomBar = {
            CustomBottomAppBar(navController)
        }){ innerPadding ->
            //val modifier = Modifier
            AndroidView(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                factory = { context ->
                    LineChart(context).apply {
                        description.isEnabled = false
                        setTouchEnabled(true)
                        setPinchZoom(true)
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.textColor = Color.BLACK
                        xAxis.setDrawGridLines(false)
                        axisLeft.textColor = Color.BLACK
                        axisLeft.setDrawGridLines(true)
                        axisLeft.axisMinimum = 50f
                        axisRight.isEnabled = false
//                        axisRight.axisMinimum = 30f
                        xAxis.valueFormatter = object : ValueFormatter() {
                            private val formatter = DateTimeFormatter.ofPattern("M/dd")
                            override fun getFormattedValue(value: Float): String {
                                return Instant.ofEpochMilli(value.toLong())
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                    .format(formatter)
                            }
                        }
                    }
                },
                update = { chart ->
                    val bpHighList = mutableListOf<Entry>()
                    val bpLowList = mutableListOf<Entry>()
                    val pulseList = mutableListOf<Entry>()
                    items.sortedBy { it.measuredAt }.forEach { item ->
                        bpHighList.add(Entry(item.measuredAt.toEpochMilli().toFloat(), item.bpHigh.toFloat()))
                        bpLowList.add(Entry(item.measuredAt.toEpochMilli().toFloat(), item.bpLow.toFloat()))
                        pulseList.add(Entry(item.measuredAt.toEpochMilli().toFloat(), item.pulse.toFloat()))
                    }
                    val lineDataSetBpHigh = LineDataSet(bpHighList, "High BP chart").apply {
                        axisDependency = chart.axisLeft.axisDependency
                        color = Color.BLUE // 濃い青
                        setCircleColor(Color.BLUE)
                    }
                    val lineDataSetBpLow = LineDataSet(bpLowList, "Low BP chart").apply {
                        axisDependency = chart.axisLeft.axisDependency
                        color = Color.parseColor("#87CEFA") // 薄い青（LightSkyBlue）
                        setCircleColor(Color.parseColor("#87CEFA"))
                    }
                    val lineDataSetPulse = LineDataSet(pulseList, "Pulse").apply {
                        axisDependency = chart.axisLeft.axisDependency
                        color = Color.RED // 赤色
                        setCircleColor(Color.RED)
                    }
                    chart.data = LineData(lineDataSetBpHigh, lineDataSetBpLow, lineDataSetPulse)
//                    // 📌 X軸の初期表示範囲を最近1か月に設定
//                    val now = Instant.now().toEpochMilli().toFloat()
//                    val oneMonthAgo = now - Duration.ofDays(30).toMillis()
//
//                    chart.xAxis.axisMinimum = items.minOf { it.measuredAt.toEpochMilli().toFloat() }
//                    chart.xAxis.axisMaximum = now
//
//                    chart.setVisibleXRangeMinimum(Duration.ofDays(30).toMillis().toFloat()) // 1か月分を表示
//                    chart.moveViewToX(oneMonthAgo) // 1か月前のデータを表示開始位置にする
                    chart.invalidate()
                }
            )
    }
}

