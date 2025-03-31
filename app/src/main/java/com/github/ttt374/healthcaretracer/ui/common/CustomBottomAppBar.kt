package com.github.ttt374.healthcaretracer.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.navigation.Screen

@Composable
fun CustomBottomAppBar (appNavigator: AppNavigator, floatingActionButton: @Composable (() -> Unit)? = null ){
    val actions = listOf(
        BottomAction(Icons.Filled.Home, "home", appNavigator::navigateToHome),
        BottomAction(Icons.AutoMirrored.Filled.ShowChart, "chart", appNavigator::navigateToChart),
        BottomAction(Icons.Filled.CalendarMonth, "calendar", appNavigator::navigateToCalendar),
        BottomAction(Icons.Filled.Analytics, "statics", appNavigator::navigateToStatics)
    )
    BottomAppBar(actions = {
        actions.forEach { action ->
            IconButton(onClick = { action.navigate() }){
                Icon(action.icon, action.description)
            }
        }
    },  floatingActionButton = floatingActionButton)
}
//data class BottomAction (val route: String, val icon: ImageVector, val description: String = "")
data class BottomAction (val icon: ImageVector, val description: String = "", val navigate: () -> Unit = {})