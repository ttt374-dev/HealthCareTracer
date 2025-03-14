package com.github.ttt374.healthcaretracer.ui.chart

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar

@Composable
fun ChartScreen(navController: NavController){
    Scaffold(topBar = { CustomTopAppBar("Chart") },
        bottomBar = {
            CustomBottomAppBar(navController)
        }){ innerPadding ->
            //val modifier = Modifier
            AndroidView(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
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
}

