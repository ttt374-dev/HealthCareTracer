package com.github.ttt374.healthcaretracer.ui.statics

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.data.bloodPressureFormatted
import com.github.ttt374.healthcaretracer.data.gapME
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.home.ItemsViewModel
import com.github.ttt374.healthcaretracer.ui.home.withSubscript

@SuppressLint("DefaultLocale")
@Composable
fun StaticsScreen(itemsViewModel: ItemsViewModel = hiltViewModel(), appNavigator: AppNavigator) {
    val dailyItems by itemsViewModel.dailyItems.collectAsState()
    val items by itemsViewModel.items.collectAsState()

    Scaffold(topBar = { CustomTopAppBar("Statics") },
        bottomBar = {
            CustomBottomAppBar(appNavigator)
        }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            StaticsHeadersRow()
            val meGapList = dailyItems.map { it.items.gapME()}

            StaticsItemRow("average", 
                items.mapNotNull { it.bpUpper }.average(),
                items.mapNotNull { it.bpLower }.average(),
                meGapList.filterNotNull().average(),
                items.mapNotNull { it.pulse }.average(),
                items.mapNotNull { it.bodyWeight }.average()  )
            StaticsItemRow("max", 
                items.mapNotNull { it.bpUpper }.maxOrNull()?.toDouble(),
                items.mapNotNull { it.bpLower }.maxOrNull()?.toDouble(),
                meGapList.filterNotNull().maxOrNull(),
                items.mapNotNull { it.pulse }.maxOrNull()?.toDouble(),
                items.mapNotNull { it.bodyWeight }.maxOrNull()?.toDouble())
            StaticsItemRow("min",
                items.mapNotNull { it.bpUpper }.minOrNull()?.toDouble(),
                items.mapNotNull { it.bpLower }.minOrNull()?.toDouble(),
                meGapList.filterNotNull().minOrNull(),
                items.mapNotNull { it.pulse }.minOrNull()?.toDouble(),
                items.mapNotNull { it.bodyWeight }.minOrNull()?.toDouble())    

//            StaticsItemRow("min", items.map { it.bp }.min(), items.map { it.pulse.toDouble() }.min(),
//                items.map { it.bodyWeight?.toDouble() ?: 0.0 }.min()  )

            //
            //StaticsHeadingRow()
//            StaticsItemRow("average", { } )
//            StaticsItemRow("Bp Lower", items.map { it.bp.lower.toFloat() } )
//            StaticsItemRow("Pulse", items.map { it.pulse.toFloat() } )
//            StaticsItemRow("Body Weight", items.mapNotNull { it.bodyWeight })
        }
    }
}

@Composable
fun StaticsHeadersRow(){
    Row {
        Text("", Modifier.weight(1f))
        Text("Bp", Modifier.weight(1f))
        Text("Pulse", Modifier.weight(1f))
        Text("Body Weight", Modifier.weight(1f))
    }
    HorizontalDivider(thickness = 1.5.dp, color = Color.LightGray)
}
@SuppressLint("DefaultLocale")
@Composable
fun StaticsItemRow(label: String, bpUpper: Double?, bpLower: Double?, meGap: Double?, pulse: Double?, bodyWeight: Double?){
    Row {
        Text(label, Modifier.weight(1f))
        Text(bloodPressureFormatted(bpUpper?.toInt(), bpLower?.toInt(), meGap?.toInt()))
        //Text(bp.toAnnotatedString(), Modifier.weight(1f))
        Text(String.format("%.1f", pulse).withSubscript("bps"), Modifier.weight(1f))
        Text(String.format("%.1f", bodyWeight).withSubscript("kg"), Modifier.weight(1f))
    }
    HorizontalDivider(thickness = 0.75.dp, color = Color.LightGray)
}

//@SuppressLint("DefaultLocale")
//@Composable
//fun StaticsItemRow(label: String, items: List<Float>){
//    Row {
//        Text(label, modifier = Modifier.weight(1f))
//        Text(String.format("%.1f", items.maxOrNull()), modifier=Modifier.weight(1f))
//        Text(String.format("%.1f", items.minOrNull()), modifier=Modifier.weight(1f))
//        Text(String.format("%.1f", items.average()), modifier=Modifier.weight(1f))
//        //Text(String.format("%.1f", items.count()), modifier=Modifier.weight(1f))
//    }
//}
//
