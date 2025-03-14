package com.github.ttt374.healthcaretracer.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.ttt374.healthcaretracer.data.Item
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(){
    val items = listOf(
        Item(0, 120, 80, 78),
            Item(1, 132, 102, 82)
    )
    Scaffold(topBar = { CustomTopAppBar("Home") }){ innerPadding ->
        Column(modifier= Modifier.padding(innerPadding)){
            LazyColumn {
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
        Text(dateTimeFormatter.format(item.measuredAt), modifier = Modifier.weight(1f))
        Text(item.bpHigh.toString(), modifier = Modifier.weight(1f))
        Text(item.bpLow.toString(), modifier = Modifier.weight(1f))
        Text(item.pulse.toString(), modifier = Modifier.weight(1f))
    }
}