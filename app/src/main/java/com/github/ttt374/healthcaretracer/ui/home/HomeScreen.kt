package com.github.ttt374.healthcaretracer.ui.home

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.ttt374.healthcaretracer.data.Item
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.navigation.Screen
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.MenuItem
import com.github.ttt374.healthcaretracer.ui.common.rememberDialogState
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
            dailyItemsViewModel: DailyItemsViewModel = hiltViewModel(),
            importExportViewModel: ImportExportViewModel = hiltViewModel(),
            appNavigator: AppNavigator){
    val dailyItems by dailyItemsViewModel.dailyItems.collectAsState()
    val filePickerDialogState = rememberDialogState()
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    //val navigateToEntry = { navController.navigate(Screen.Entry.route) }
    //val navigateToEdit = { id: Long -> navController.navigate("${Screen.Edit.route}/$id")}

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            selectedFileUri = uri
            Log.d("ImportScreen", "Selected file: $uri")
            selectedFileUri?.let { importExportViewModel.importData(it) }
            filePickerDialogState.close()
        }
    )
//    LaunchedEffect(filePickerDialogState.isOpen) {
//        if (filePickerDialogState.isOpen) {
//            filePickerLauncher.launch(arrayOf("*/*"))
//        }
//    }
    Scaffold(topBar = {
        CustomTopAppBar(
            "Home",
            menuItems = listOf(
                MenuItem("export", onClick = { importExportViewModel.exportData() }),
                MenuItem("import", onClick = {
                    filePickerDialogState.open()
                    filePickerLauncher.launch(arrayOf("*/*"))
                })
            )
        )
    },
        bottomBar = {
            CustomBottomAppBar(appNavigator.navController,
                floatingActionButton = {
                    FloatingActionButton(onClick = appNavigator::navigateToEntry) {
                        Icon(Icons.Filled.Add, "add")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            LazyColumn {
                items(dailyItems.asReversed()) { dailyItem ->
                    DailyItemRow(dailyItem, appNavigator::navigateToEdit)
                }
            }
        }
    }
}
@Composable
fun DailyItemRow(dailyItem: DailyItem, navigateToEdit: (Long) -> Unit = {}){
    Row (modifier= Modifier.fillMaxWidth().background(Color.LightGray),
        verticalAlignment = Alignment.CenterVertically){
        Text(DateTimeFormatter.ofPattern("yyyy-M-d (E) ").format(dailyItem.date),
            modifier = Modifier.weight(1f))
        BloodPressureText(dailyItem.avgBpHigh, dailyItem.avgBpLow)
        Text("${dailyItem.avgPulse}".withSubscript("bpm"),
            textAlign = TextAlign.End )
    }
    dailyItem.items.forEach { item ->
        ItemRow(item, navigateToEdit)
    }
}
@Composable
fun ItemRow(item: Item, navigateToEdit: (Long) -> Unit = {}){
    val dateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())

    Column (modifier= Modifier.padding(horizontal = 8.dp, vertical = 4.dp).fillMaxWidth()
        .clickable { navigateToEdit(item.id) }) {
        Row {
            Text(dateTimeFormatter.format(item.measuredAt), fontSize = 14.sp)
            Spacer(modifier = Modifier.width(16.dp))
            BloodPressureText(item.bpHigh, item.bpLow)
            Text(item.pulse.toString().withSubscript("bpm"))
            Spacer(modifier = Modifier.weight(1f)) // 左右の間に余白を作る
            if (item.bodyWeight > 0){
                Text(item.bodyWeight.toString().withSubscript("Kg"))
            }
            Text(item.location, textAlign = TextAlign.Right)
        }
        // 2行目: メモ（もしあれば表示）
        item.memo.takeIf { it.isNotBlank() }?.let { memoText ->
            Text(text = "memo: $memoText")
        }
    }

    HorizontalDivider(thickness = 0.75.dp, color = Color.LightGray)
}
fun String.withSubscript(subscript: String, textFontSize: TextUnit = 16.sp, subscriptFontSize: TextUnit = 8.sp): AnnotatedString {
    //val text = this
    return AnnotatedString.Builder().apply {
        pushStyle(SpanStyle(fontSize = textFontSize)) // 大きめのフォントサイズ
       append(this@withSubscript)

        // 小さな単位部分
        pop()
        pushStyle(SpanStyle(fontSize = subscriptFontSize, baselineShift = BaselineShift.Subscript))
        append(subscript)
    }.toAnnotatedString()
}

private const val HIGH_BP_THRESHOLD = 140
private const val LOW_BP_THRESHOLD = 90

@Composable
fun BloodPressureText(bpHigh: Int, bpLow: Int) {
    val annotatedString = buildAnnotatedString {
        pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = if (bpHigh > HIGH_BP_THRESHOLD) Color.Red else Color.Unspecified))
        append(bpHigh.toString())
        pop()
        append("/")

        pushStyle(SpanStyle(color = if (bpLow > LOW_BP_THRESHOLD) Color.Red else Color.Unspecified))
        append(bpLow.toString())
        pop()

        pushStyle(SpanStyle(fontSize = 8.sp, baselineShift = BaselineShift.Subscript))
        append(" mmHg")
        pop()
    }
    Text(text = annotatedString)
}
