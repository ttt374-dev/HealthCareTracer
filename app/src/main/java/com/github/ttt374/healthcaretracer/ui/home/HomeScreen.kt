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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.data.BloodPressure
import com.github.ttt374.healthcaretracer.data.BloodPressureCategory
import com.github.ttt374.healthcaretracer.data.Item
import com.github.ttt374.healthcaretracer.data.bloodPressureFormatted
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.MenuItem
import com.github.ttt374.healthcaretracer.ui.common.rememberDialogState
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    dailyItemsViewModel: DailyItemsViewModel = hiltViewModel(),
    importExportViewModel: BackupDataViewModel = hiltViewModel(),
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
            CustomBottomAppBar(appNavigator,
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
    Row (modifier= Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer),
        verticalAlignment = Alignment.CenterVertically){
        Text(DateTimeFormatter.ofPattern("yyyy-M-d (E) ").format(dailyItem.date),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.weight(1f))
        //Text(BloodPressure(dailyItem.avgBpUpper ?: 0, dailyItem.avgBpLower ?: 0).toAnnotatedString())
        //Text(bloodPressureFormatted(dailyItem.avgBpUpper, dailyItem.avgBpLower))
        Text(Pair(dailyItem.avgBpUpper, dailyItem.avgBpLower).toBloodPressureString())
//        BloodPressureText(dailyItem.avgBp.upper, dailyItem.avgBp.lower,
//            color = MaterialTheme.colorScheme.onSecondaryContainer,)
        Text(dailyItem.avgPulse.toDisplayString().withSubscript("bpm"),
            color = MaterialTheme.colorScheme.onSecondaryContainer,
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
            //Text(BloodPressure(item.bpUpper ?: 0, item.bpLower ?: 0).toAnnotatedString())
            //Text(bloodPressureFormatted(item.bpUpper, item.bpLower))
            Text(Pair(item.bpUpper, item.bpLower).toBloodPressureString())
            //BloodPressureText(item.bp.upper, item.bp.lower, color = MaterialTheme.colorScheme.primary)
            Text(item.pulse.toPulseString())
            Spacer(modifier = Modifier.weight(1f)) // 左右の間に余白を作る
            Text(item.bodyWeight.toBodyWeightString())

        }

        Row {
            //Text(getHypertensionGrade(item.bp.upper, item.bp.lower))
            if (item.bpUpper == null || item.bpLower == null){
                Text("-")
            } else {
                val htnGrade = BloodPressureCategory.getCategory(BloodPressure(item.bpUpper ?: 0, item.bpLower ?: 0)) // fromValues(item.bp.upper, item.bp.lower)
                Text(htnGrade.name, color = htnGrade.color)
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

fun String.withSubscript(subscript: String, textFontSize: TextUnit = 16.sp, subscriptFontSize: TextUnit = 8.sp): AnnotatedString {
    return AnnotatedString.Builder().apply {
        pushStyle(SpanStyle(fontSize = textFontSize)) // 大きめのフォントサイズ
       append(this@withSubscript)

        // 小さな単位部分
        pop()
        pushStyle(SpanStyle(fontSize = subscriptFontSize, baselineShift = BaselineShift.Subscript))
        append(subscript)
    }.toAnnotatedString()
}

fun Number?.toDisplayString(): String = this?.toString() ?: "-"
fun Number?.toBodyWeightString(): AnnotatedString = toDisplayString().withSubscript("kg")
fun Number?.toPulseString(): AnnotatedString = toDisplayString().withSubscript("bpm")
fun Pair<Number?, Number?>.toBloodPressureString(): AnnotatedString = bloodPressureFormatted(first?.toInt(), second?.toInt())


private const val HIGH_BP_THRESHOLD = 140
private const val LOW_BP_THRESHOLD = 90

//
//@Composable
//fun BloodPressureText(bpUpper: Int, bpLower: Int, color: Color) {
//    Text(text = BloodPressure(bpUpper, bpLower).toAnnotatedString(), color = color)
//}
//
