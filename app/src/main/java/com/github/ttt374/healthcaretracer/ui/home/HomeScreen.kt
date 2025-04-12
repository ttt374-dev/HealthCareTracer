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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressure
import com.github.ttt374.healthcaretracer.data.bloodpressure.BloodPressureGuideline
import com.github.ttt374.healthcaretracer.data.datastore.LocalTimeRange
import com.github.ttt374.healthcaretracer.data.item.DailyItem
import com.github.ttt374.healthcaretracer.data.item.Item
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.MenuItem
import com.github.ttt374.healthcaretracer.ui.common.rememberDialogState
import com.github.ttt374.healthcaretracer.ui.entry.toLocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(dailyItemsViewModel: ItemsViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    appNavigator: AppNavigator
){
    val dailyItems by dailyItemsViewModel.dailyItems.collectAsState()
    val filePickerDialogState = rememberDialogState()
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    val config by homeViewModel.config.collectAsState()
    val guideline = config.bloodPressureGuideline //   selectedGuideline

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            selectedFileUri = uri
            Log.d("ImportScreen", "Selected file: $uri")
            selectedFileUri?.let { homeViewModel.importData(it) }
            filePickerDialogState.close()
        }
    )
    LaunchedEffect(filePickerDialogState.isOpen) {
        if (filePickerDialogState.isOpen) {
            filePickerLauncher.launch(arrayOf("*/*"))
        }
    }
    Scaffold(topBar = {
        CustomTopAppBar(
            stringResource(R.string.home),
            menuItems = listOf(
                MenuItem("export", onClick = { homeViewModel.exportData() }),
                MenuItem("import", onClick = { filePickerDialogState.open() })
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
                    DailyItemRow(dailyItem, guideline,
                        config.morningRange, config.eveningRange,
                        appNavigator::navigateToEdit,)
                }
            }
        }
    }
}
@Composable
fun DailyItemRow(dailyItem: DailyItem, guideline: BloodPressureGuideline = BloodPressureGuideline.Default,
                 morningTimeRange: LocalTimeRange = LocalTimeRange(), eveningTimeRange: LocalTimeRange = LocalTimeRange(),
                 navigateToEdit: (Long) -> Unit = {}){
    Row (modifier= Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically){
        val bp = BloodPressure(dailyItem.avgBpUpper?.toInt(), dailyItem.avgBpLower?.toInt())
        Text(DateTimeFormatter.ofPattern("yyyy-M-d (E) ").format(dailyItem.date), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
        //CustomText(bp.toDisplayString(guideline = guideline))
        Text(bp.toDisplayString(guideline = guideline), fontWeight = FontWeight.Bold)
        Text(dailyItem.avgPulse?.toInt().toPulseString(), textAlign = TextAlign.End)
        Text(dailyItem.avgBodyWeight.toBodyWeightString(), textAlign = TextAlign.End)
    }
    dailyItem.items.forEach { item ->
        ItemRow(item, guideline, morningTimeRange, eveningTimeRange, navigateToEdit, )
    }
}
//@Composable
//fun CustomText(
//    text: String,
//    modifier: Modifier = Modifier,
//    color: Color = MaterialTheme.colorScheme.onSecondaryContainer,
//    fontWeight: FontWeight = FontWeight.Bold,
//    textAlign: TextAlign? = null
//) {
//    Text(
//        text = text,
//        color = color,
//        fontWeight = fontWeight,
//        modifier = modifier,
//        textAlign = textAlign
//    )
//}
//@Composable
//fun CustomText(
//    text: AnnotatedString,
//    modifier: Modifier = Modifier,
//    color: Color = MaterialTheme.colorScheme.onSecondaryContainer,
//    fontWeight: FontWeight = FontWeight.Bold,
//    textAlign: TextAlign? = null
//) {
//    CustomText(
//        text = text.toString(),
//        color = color,
//        fontWeight = fontWeight,
//        modifier = modifier,
//        textAlign = textAlign
//    )
//}
@Composable
fun ItemRow(item: Item, guideline: BloodPressureGuideline = BloodPressureGuideline.Default,
            morningTimeRange: LocalTimeRange, eveningTimeRange: LocalTimeRange,
            navigateToEdit: (Long) -> Unit = {}){
    val dateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
    val bp = BloodPressure(item.bpUpper, item.bpLower)

    Column (modifier= Modifier.padding(horizontal = 8.dp, vertical = 4.dp).fillMaxWidth().clickable { navigateToEdit(item.id) }) {
        Row {
            Text(dateTimeFormatter.format(item.measuredAt), fontSize = 14.sp)

            item.measuredAt.toLocalTime().let {
                when {
                    morningTimeRange.contains(it) -> Icon(Icons.Filled.WbSunny, "morning", modifier = Modifier.size(12.dp))
                    eveningTimeRange.contains(it) -> Icon(Icons.Filled.DarkMode, "evening", modifier = Modifier.size(12.dp))
                    else -> Text("")
                }
            }

            Spacer(modifier = Modifier.width(16.dp))
            Text(bp.toDisplayString(guideline = guideline))
            Text(item.pulse.toPulseString())
            Spacer(modifier = Modifier.weight(1f)) // 左右の間に余白を作る
            Text(item.bodyWeight.toBodyWeightString())
        }

        Row {
            if (item.bpUpper == null || item.bpLower == null){
                Text("-")
            } else {
                //val htnGrade = BloodPressureCategory.getCategory(item.bpUpper, item.bpLower)
                //with (bp.htnCategory()){
                guideline.getCategory(bp.upper, bp.lower).let {
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

fun Number?.toDisplayString(format: String? = null): String {
    return this?.let  {
        if (format != null)
            String.format(format, this)
        else
            this.toString()
    }   ?: "-"
}
fun Number?.toBodyWeightString(): AnnotatedString = toDisplayString("%.1f").withSubscript("kg")
fun Number?.toPulseString(): AnnotatedString = toDisplayString().withSubscript("bpm")
//fun Pair<Number?, Number?>.toBloodPressureString(): AnnotatedString = bloodPressureFormatted(first?.toInt(), second?.toInt())

