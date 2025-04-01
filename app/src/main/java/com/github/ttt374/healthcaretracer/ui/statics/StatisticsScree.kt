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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.data.averageOrNull
import com.github.ttt374.healthcaretracer.data.bloodPressureFormatted
import com.github.ttt374.healthcaretracer.data.gapME
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.home.ItemsViewModel
import com.github.ttt374.healthcaretracer.ui.home.toBodyWeightString
import com.github.ttt374.healthcaretracer.ui.home.toPulseString
import com.github.ttt374.healthcaretracer.ui.home.withSubscript


@Composable
fun StatisticsScreen(itemsViewModel: ItemsViewModel = hiltViewModel(), appNavigator: AppNavigator) {
    val dailyItems by itemsViewModel.dailyItems.collectAsState()
    val items by itemsViewModel.items.collectAsState()

    val bpUpperList = remember(items) { items.mapNotNull { it.bpUpper?.toDouble() } }
    val bpLowerList = remember(items) { items.mapNotNull { it.bpLower?.toDouble()  } }
    val pulseList = remember(items) { items.mapNotNull { it.pulse?.toDouble()  } }
    val bodyWeightList = remember(items) { items.mapNotNull { it.bodyWeight?.toDouble()  } }
    val meGapList = remember(dailyItems) { dailyItems.mapNotNull { it.items.gapME() } }

    Scaffold(topBar = { CustomTopAppBar("Statics") },
        bottomBar = { CustomBottomAppBar(appNavigator) }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            StatisticsHeadersRow()

            StatisticsItemRow("average",
                bpUpperList.averageOrNull(),
                bpLowerList.averageOrNull(),
                meGapList.averageOrNull(),
                pulseList.averageOrNull(),
                bodyWeightList.averageOrNull() )
            StatisticsItemRow("max",
                bpUpperList.maxOrNull(),
                bpLowerList.maxOrNull(),
                meGapList.maxOrNull(),
                pulseList.maxOrNull(),
                bodyWeightList.maxOrNull() )     
            StatisticsItemRow("min",
                bpUpperList.minOrNull(),
                bpLowerList.minOrNull(),
                meGapList.minOrNull(),
                pulseList.minOrNull(),
                bodyWeightList.minOrNull())
        }
    }
}

@Composable
fun StatisticsHeadersRow(){
    Row {
        Text("", Modifier.weight(1f))
        Text("Blood Pressure (ME gap)", Modifier.weight(1f))
        Text("Pulse", Modifier.weight(1f))
        Text("Body Weight", Modifier.weight(1f))
    }
    HorizontalDivider(thickness = 1.5.dp, color = Color.LightGray)
}

@Composable
fun StatisticsItemRow(label: String, bpUpper: Double?, bpLower: Double?, meGap: Double?, pulse: Double?, bodyWeight: Double?){
    Row {
        Text(label, Modifier.weight(1f))
        Text(bloodPressureFormatted(bpUpper?.toInt(), bpLower?.toInt(), meGap?.toInt()), Modifier.weight(1f))
        Text(pulse.toPulseString(), Modifier.weight(1f))
        Text(bodyWeight.toBodyWeightString(), Modifier.weight(1f))
    }
    HorizontalDivider(thickness = 0.75.dp, color = Color.LightGray)
}

