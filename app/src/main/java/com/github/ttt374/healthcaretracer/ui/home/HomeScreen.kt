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
import androidx.navigation.NavController
import com.github.ttt374.healthcaretracer.data.Item
import com.github.ttt374.healthcaretracer.navigation.Screen
import com.github.ttt374.healthcaretracer.ui.common.ConfirmDialog
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.MenuItem
import com.github.ttt374.healthcaretracer.ui.common.rememberDialogState
import com.github.ttt374.healthcaretracer.ui.common.rememberItemDialogState
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.withSign

@Composable
fun HomeScreen(homeViewModel: HomeViewModel = hiltViewModel(),
               navController: NavController,
               ){
    //val items by homeViewModel.items.collectAsState()
    val dailyItems by homeViewModel.dailyItems.collectAsState()

    val filePickerDialogState = rememberDialogState()
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            selectedFileUri = uri
            Log.d("ImportScreen", "Selected file: $uri")
            selectedFileUri?.let { homeViewModel.importData(it) }
            filePickerDialogState.close()
        }
    )
//    LaunchedEffect(filePickerDialogState.isOpen) {
//        if (filePickerDialogState.isOpen) {
//            filePickerLauncher.launch(arrayOf("*/*"))
//        }
//    }
    if (filePickerDialogState.isOpen)
        filePickerLauncher.launch(arrayOf("*/*"))


    Scaffold(topBar = { CustomTopAppBar("Home",
        menuItems = listOf(
            MenuItem("export", onClick = { homeViewModel.exportData()}),
            MenuItem("import", onClick = { filePickerDialogState.open()}))

        ) },
        bottomBar = {
            CustomBottomAppBar(navController,
                floatingActionButton = {
                FloatingActionButton(onClick = { navController.navigate(Screen.Entry.route) }){
                    Icon(Icons.Filled.Add, "add")
                }
            })
        }){ innerPadding ->
        Column(modifier= Modifier.padding(innerPadding)){
            LazyColumn() {
                dailyItems.reversed().forEach { groupedItem ->
                    item {
                        Row (modifier=Modifier.fillMaxWidth().background(Color.LightGray),
                            verticalAlignment = Alignment.CenterVertically){
                            Text(DateTimeFormatter.ofPattern("yyyy/M/d(E) ").format(groupedItem.date),
                                modifier = Modifier.weight(1f))
                            Text("${groupedItem.avgBpHigh}/${groupedItem.avgBpLow}".withSubscript("mmHg"),
                                textAlign = TextAlign.End )
                            Text("${groupedItem.avgPulse}".withSubscript("bpm"),
                                textAlign = TextAlign.End )
                        }
                    }
                    items(groupedItem.items){ item ->
                        ItemRow(item,
                            navigateToEdit = { navController.navigate("${Screen.Edit.route}/${item.id}")},
                            //onDeleteItem = { deleteDialogState.open(it) },
                        )
                    }
                }
//                items(items){ item ->
//                    ItemRow(item,
//                        navigateToEdit = { navController.navigate("${Screen.Edit.route}/${item.id}")},
//                        onDeleteItem = { deleteDialogState.open(it) },
//                    )
//                }
            }
        }
    }
}
//@Composable
//fun ItemHeaderRow() {
//    Row {
//        Text("Measured at", modifier = Modifier.weight(2f))
//        Text("High BP", modifier = Modifier.weight(1f))
//        Text("Low BP", modifier = Modifier.weight(1f))
//        Text("Pulse", modifier = Modifier.weight(1f))
//        Text("Location", modifier = Modifier.weight(1f))
//        Text("", modifier = Modifier.weight(.5f))
//    }
//    HorizontalDivider(thickness = 1.dp, color = Color.Gray)
//}
@Composable
fun ItemRow(item: Item, navigateToEdit: () -> Unit = {},
            onDeleteItem: (Item) -> Unit = {}){
    val dateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault())
    //val menuState = rememberExpandState()

    Column  (modifier=Modifier.padding(horizontal = 8.dp, vertical = 4.dp).clickable { navigateToEdit() }) {
        Row {
            Text(dateTimeFormatter.format(item.measuredAt))
            //Text(" ${item.bpHigh}/${item.bpLow}_${item.pulse}", textAlign = TextAlign.Left, modifier=Modifier.weight(1f))
            Spacer(modifier = Modifier.width(16.dp))
            Text("${item.bpHigh}/${item.bpLow}".withSubscript("mmHg"))
            Text(item.pulse.toString().withSubscript("bpm"))
            //Text(item.bodyWeight.toString().withSubscript("kg"), textAlign = TextAlign.Right)
            Spacer(modifier = Modifier.weight(1f)) // 左右の間に余白を作る
            if (item.bodyWeight > 0){
                Text(item.bodyWeight.toString().withSubscript("Kg"))
            }
            Text(item.location, textAlign = TextAlign.Right)
        }
        // 2行目: メモ（もしあれば表示）
        item.memo.takeIf { it.isNotBlank() }?.let { memoText ->
            //Spacer(modifier = Modifier.height(4.dp))
            Text(text = "memo: $memoText")
        }
    }

    HorizontalDivider(thickness = 0.75.dp, color = Color.LightGray)
}
fun String.withSubscript(subscript: String, textFontSize: TextUnit = 16.sp, subscriptFontSize: TextUnit = 8.sp): AnnotatedString {
    val text = this
    return AnnotatedString.Builder().apply {
        pushStyle(SpanStyle(fontSize = textFontSize)) // 大きめのフォントサイズ
        append(text)

        // 小さな単位部分
        pop()
        pushStyle(SpanStyle(fontSize = subscriptFontSize, baselineShift = BaselineShift.Subscript))
        append(subscript)
    }.toAnnotatedString()
}

fun Float.asBodyWeightString() =
    if (this == 0.0F) "" else this.toString().withSubscript("Kg").toString()




