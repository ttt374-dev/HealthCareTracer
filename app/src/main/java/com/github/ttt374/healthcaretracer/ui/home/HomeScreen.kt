package com.github.ttt374.healthcaretracer.ui.home

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarState
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.data.Item
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(homeViewModel: HomeViewModel = hiltViewModel(),
               navigateToEntry: () -> Unit = {},
               ){
//    val items = listOf(
//        Item(0, 120, 80, 78),
//            Item(1, 132, 102, 82)
//    )
    val items by homeViewModel.items.collectAsState()

    Scaffold(topBar = { CustomTopAppBar("Home") },
        bottomBar = { BottomAppBar(actions = {}, floatingActionButton = {
            FloatingActionButton(onClick = { navigateToEntry() }){
                Icon(Icons.Filled.Add, "add")
            }}) }
        ){ innerPadding ->
        Column(modifier= Modifier.padding(innerPadding)){
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
                item {
                    Row {  // headers
                        Text("measured at", modifier = Modifier.weight(2f))
                        Text("High BP", modifier = Modifier.weight(1f))
                        Text("Low BP", modifier = Modifier.weight(1f))
                        Text("Pulse", modifier = Modifier.weight(1f))
                    }
                }
                items(items){ item ->
                    ItemRow(item)
                }
            }
        }

    }
}
@Composable
fun ItemRow(item: Item){
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())

    Row {
        Text(dateTimeFormatter.format(item.measuredAt), modifier = Modifier.weight(2f))
        Text(item.bpHigh.toString(), modifier = Modifier.weight(1f))
        Text(item.bpLow.toString(), modifier = Modifier.weight(1f))
        Text(item.pulse.toString(), modifier = Modifier.weight(1f))
    }
}
