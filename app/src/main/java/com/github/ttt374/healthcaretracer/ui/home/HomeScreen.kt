package com.github.ttt374.healthcaretracer.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.ttt374.healthcaretracer.data.Item
import com.github.ttt374.healthcaretracer.navigation.Screen
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(homeViewModel: HomeViewModel = hiltViewModel(),
               //navigateToEntry: () -> Unit = {},
               navController: NavController,
               ){
//    val items = listOf(
//        Item(0, 120, 80, 78),
//            Item(1, 132, 102, 82)
//    )
    val items by homeViewModel.items.collectAsState()

    Scaffold(topBar = { CustomTopAppBar("Home") },
        bottomBar = {
            CustomBottomAppBar(navController,
                floatingActionButton = {
                FloatingActionButton(onClick = { navController.navigate(Screen.Entry.route) }){
                    Icon(Icons.Filled.Add, "add")
                }
            })
        }){ innerPadding ->
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
