package com.github.ttt374.healthcaretracer.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.github.ttt374.healthcaretracer.navigation.Screen

@Composable
fun CustomBottomAppBar (navController: NavController, floatingActionButton: @Composable (() -> Unit)? = null ){
    val actions = listOf(
        BottomAction(Screen.Home.route, Icons.Filled.Home),
        BottomAction(Screen.Chart.route, Icons.AutoMirrored.Filled.ShowChart)
    )
    BottomAppBar(actions = {
        actions.forEach { action ->
            IconButton(onClick = { navController.navigate(action.route)}){
                Icon(action.icon, action.description)
            }
        }
    },  floatingActionButton = floatingActionButton)
}
data class BottomAction (val route: String, val icon: ImageVector, val description: String = "")