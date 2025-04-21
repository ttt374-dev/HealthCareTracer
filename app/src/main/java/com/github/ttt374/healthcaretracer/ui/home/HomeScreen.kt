package com.github.ttt374.healthcaretracer.ui.home

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import com.github.ttt374.healthcaretracer.data.bloodpressure.toAnnotatedString
import com.github.ttt374.healthcaretracer.data.bloodpressure.toBloodPressure
import com.github.ttt374.healthcaretracer.data.item.DailyItem
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.MenuItem
import com.github.ttt374.healthcaretracer.ui.common.TimeOfDay
import com.github.ttt374.healthcaretracer.ui.common.TimeOfDayConfig
import com.github.ttt374.healthcaretracer.ui.common.rememberDialogState
import com.github.ttt374.healthcaretracer.ui.common.toBodyTemperatureString
import com.github.ttt374.healthcaretracer.ui.common.toBodyWeightString
import com.github.ttt374.healthcaretracer.ui.common.toPulseString
import com.github.ttt374.healthcaretracer.ui.common.toTimeOfDay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(dailyItemsViewModel: ItemsViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    appNavigator: AppNavigator
){
    val dailyItems by dailyItemsViewModel.dailyItems.collectAsState()
    val importFilePickerDialogState = rememberDialogState()
    val exportFilePickerDialogState = rememberDialogState()
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    val config by homeViewModel.config.collectAsState()
    val guideline = config.bloodPressureGuideline //   selectedGuideline

    val importFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            selectedFileUri = uri
            Log.d("ImportScreen", "Selected file: $uri")
            selectedFileUri?.let { homeViewModel.importData(it) }
            importFilePickerDialogState.close()
        }
    )
    val exportFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri ->
            uri?.let { homeViewModel.exportData(it) }
            exportFilePickerDialogState.close()
        }
    )
    LaunchedEffect(importFilePickerDialogState.isOpen) {
        if (importFilePickerDialogState.isOpen) {
            importFilePickerLauncher.launch(arrayOf("*/*"))
        }
    }
    LaunchedEffect(exportFilePickerDialogState.isOpen) {
        if (exportFilePickerDialogState.isOpen){
            val filenameFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm").withZone(ZoneId.systemDefault())
            val defaultFilename = "healthcare-${filenameFormatter.format(Instant.now())}.csv"

            exportFilePickerLauncher.launch(defaultFilename)
        }
    }

    Scaffold(topBar = {
        CustomTopAppBar(
            stringResource(R.string.home),
            menuItems = listOf(
                MenuItem("export", onClick = { exportFilePickerDialogState.open() }),
                MenuItem("import", onClick = { importFilePickerDialogState.open() })
            )
        )
    },
        bottomBar = {
            CustomBottomAppBar(appNavigator,
                floatingActionButton = {
                    FloatingActionButton(onClick = appNavigator::navigateToEntry) {
                        Icon(Icons.Filled.Add, "add")
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            LazyColumn { // (reverseLayout = true) {
                items(dailyItems.reversed()) { dailyItem ->
                    DailyItemRow(dailyItem, guideline, config.timeOfDayConfig,
                        appNavigator::navigateToEdit,)
                }
            }
        }
    }
}
@Composable
fun DailyItemRow(dailyItem: DailyItem, guideline: BloodPressureGuideline = BloodPressureGuideline.Default,
                 timeOfDayConfig: TimeOfDayConfig = TimeOfDayConfig(),
                 navigateToEdit: (Long) -> Unit = {}){
    Row (modifier= Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically){
        Text(DateTimeFormatter.ofPattern("yyyy-M-d (E) ").format(dailyItem.date), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        Text(dailyItem.vitals.bp.toAnnotatedString(guideline = guideline), fontWeight = FontWeight.Bold)
        Text(dailyItem.vitals.pulse?.toInt().toPulseString(), textAlign = TextAlign.End)
    }
    dailyItem.items.forEach { item ->
        ItemRow(item, guideline, timeOfDayConfig, navigateToEdit, )
    }
}
@Composable
fun ItemRow(item: Item, guideline: BloodPressureGuideline = BloodPressureGuideline.Default,
            timeOfDayConfig: TimeOfDayConfig = TimeOfDayConfig(),
            navigateToEdit: (Long) -> Unit = {}){
    val dateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
    val bp = item.vitals.bp

    Column (modifier= Modifier.padding(horizontal = 8.dp, vertical = 4.dp).fillMaxWidth().clickable { navigateToEdit(item.id) }) {
        Row {
            Text(dateTimeFormatter.format(item.measuredAt), fontSize = 14.sp)
            when (item.measuredAt.toTimeOfDay(config = timeOfDayConfig)){
                TimeOfDay.Morning -> Icon(Icons.Filled.WbSunny, "morning", modifier = Modifier.size(12.dp))
                TimeOfDay.Evening -> Icon(Icons.Filled.DarkMode, "evening", modifier = Modifier.size(12.dp))
                else -> {}
            }
//            item.measuredAt.toLocalTime().let {
//                when {
//                    morningTimeRange.contains(it) -> Icon(Icons.Filled.WbSunny, "morning", modifier = Modifier.size(12.dp))
//                    eveningTimeRange.contains(it) -> Icon(Icons.Filled.DarkMode, "evening", modifier = Modifier.size(12.dp))
//                    else -> Text("")
//                }
//            }

            Spacer(modifier = Modifier.width(16.dp))
            Text(bp.toAnnotatedString(guideline = guideline))
            Text(item.vitals.pulse.toPulseString())
            Spacer(modifier = Modifier.weight(1f)) // 左右の間に余白を作る
            Text(item.vitals.bodyWeight.toBodyWeightString())
            Text(item.vitals.bodyTemperature.toBodyTemperatureString())
        }

        Row {
            if (item.vitals.bp == null){
                Text("-")
            } else {
                guideline.getCategory(bp).let {
                    Text(stringResource(it.nameLabel), color=it.color)
                    //Text(it.name, color=it.color)
                }
            }
            Spacer(modifier = Modifier.weight(1f)) // 左右の間に余白を作る
            Text(item.location, textAlign = TextAlign.Right)
        }
        Row {
            item.memo.takeIf { it.isNotBlank() }?.let { memoText ->
                Text(text = "memo: $memoText", fontSize = 12.sp)
            }
        }
    }
    HorizontalDivider(thickness = 0.75.dp, color = Color.LightGray)
}
