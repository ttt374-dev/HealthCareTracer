package com.github.ttt374.healthcaretracer.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.github.ttt374.healthcaretracer.data.Item
import com.github.ttt374.healthcaretracer.navigation.Screen
import com.github.ttt374.healthcaretracer.ui.common.ConfirmDialog
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.rememberExpandState
import com.github.ttt374.healthcaretracer.ui.common.rememberItemDialogState
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(homeViewModel: HomeViewModel = hiltViewModel(),
               navController: NavController,
               ){
    val items by homeViewModel.items.collectAsState()

    // dialog
    val deleteDialogState = rememberItemDialogState()
    if (deleteDialogState.isOpen){
        ConfirmDialog(title = { Text("Are you sure to delete ?") },
            text = { Text("") },
            onConfirm = { homeViewModel.deleteItem(deleteDialogState.item) },
            closeDialog = { deleteDialogState.close()})
    }
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
            LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth(), reverseLayout = false) {
                item {
                    Row {  // headers
                        Text("Measured at", modifier = Modifier.weight(2f))
                        Text("High BP", modifier = Modifier.weight(1f))
                        Text("Low BP", modifier = Modifier.weight(1f))
                        Text("Pulse", modifier = Modifier.weight(1f))
                        Text("", modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider(thickness = 1.dp, color = Color.Gray)
                }
                items(items){ item ->
                    ItemRow(item,
                        navigateToEdit = { navController.navigate("${Screen.Edit.route}/${item.id}")},
                        onDeleteItem = { deleteDialogState.open(it) },
                    //    onLongClick = { navController.navigate("${Screen.Edit.route}/${item.id}")}
                    )
                }
            }
        }
    }

}
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemRow(item: Item, navigateToEdit: () -> Unit = {},
            onDeleteItem: (Item) -> Unit = {},
            onLongClick: () -> Unit = {}){
    val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault())
    val menuState = rememberExpandState()

    Row(Modifier.combinedClickable(onLongClick = onLongClick, onClick = {}) ) {
        Text(dateTimeFormatter.format(item.measuredAt), modifier = Modifier.weight(2f))
        Text(item.bpHigh.toString(), modifier = Modifier.weight(1f))
        Text(item.bpLow.toString(), modifier = Modifier.weight(1f))
        Text(item.pulse.toString(), modifier = Modifier.weight(1f))
//        IconButton(onClick = navigateToEdit, modifier = Modifier.weight(0.5f)){
//            Icon(Icons.Filled.Delete, "delete")
//        }
        IconButton(onClick = { menuState.toggle() }, modifier = Modifier.weight(0.5f)){
            Icon(Icons.Filled.MoreVert, "menu")
        }
        Box {
            DropdownMenu(menuState.visible, onDismissRequest = { menuState.fold()}){
                DropdownMenuItem(text = { Icon(Icons.Filled.Edit, "edit")},
                    onClick = { navigateToEdit()})
                DropdownMenuItem(text = { Icon(Icons.Filled.Delete, "delete")},
                    onClick = { onDeleteItem(item)})
            }
        }
    }
    HorizontalDivider(thickness = 1.dp, color = Color.Gray)
}
