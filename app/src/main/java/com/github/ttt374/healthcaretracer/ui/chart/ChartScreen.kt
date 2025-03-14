package com.github.ttt374.healthcaretracer.ui.chart

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
//import com.github.tehras.charts.line.LineChart
//import com.github.tehras.charts.line.LineChartData
//import com.github.tehras.charts.line.LineChartPoint
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun ChartScreen(){
    LineChartSample()
}

@Composable
fun LineChartSample() {
    val modifier = Modifier
    AndroidView(
        modifier = modifier,
        factory = { context ->
            // Viewの作成
            val lineChartView = LineChart(context)
            // 表示させるデータ作成
            val entryList = mutableListOf<Entry>()
            for(i in 0..10) {
                entryList.add(Entry(i.toFloat(), (i*i).toFloat()))
            }
            // データ+汎用ラベルのDataSet作成
            val lineDataSet = LineDataSet(entryList, "test chart")
            // LineDataをセット
            lineChartView.data = LineData(lineDataSet)

            lineChartView
        },
        update = { view ->
            // 更新が入った時に呼ばれる
        }
    )

}


@Composable
fun LIneCharSampleRecharts(){
//    val data = listOf(
//        LineChartPoint(1f, 10f),
//        LineChartPoint(2f, 20f),
//        LineChartPoint(3f, 15f),
//        LineChartPoint(4f, 30f),
//        LineChartPoint(5f, 25f)
//    )
//
//    LineChart(
//        lineChartData = LineChartData(
//            points = data,
//            lineColor = Color.Blue
//        )
//    )
}
