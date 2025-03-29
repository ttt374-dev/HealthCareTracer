package com.github.ttt374.healthcaretracer.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavArgs
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.github.ttt374.healthcaretracer.ui.calendar.CalendarScreen
import com.github.ttt374.healthcaretracer.ui.chart.ChartScreen
import com.github.ttt374.healthcaretracer.ui.entry.EditScreen
import com.github.ttt374.healthcaretracer.ui.entry.EntryScreen
import com.github.ttt374.healthcaretracer.ui.home.HomeScreen

sealed class Screen(val route: String, val routeWithArgs: String = "") {
    data object Home : Screen("home")
    data object Entry : Screen("entry", "entry/{date}")  // date is optional
    data object Edit : Screen("edit", "edit/{itemId}")
    data object Chart: Screen("chart")
    data object Calendar: Screen("calendar")
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { HomeScreen(
            navController=navController) }
        //composable(Screen.Entry.route) { EntryScreen(navigateBack = { navController.popBackStack()}) }
        composable(Screen.Entry.routeWithArgs, arguments = listOf(navArgument("date") { type = NavType.StringType })){
            EntryScreen(navigateBack = { navController.navigateUp()})
        }
//        composable("${Screen.Edit.route}/{itemId}") {
//            EditScreen(navigateBack = { navController.popBackStack()})
//        }
        composable(Screen.Edit.routeWithArgs, arguments = listOf(navArgument("itemId"){ type = NavType.LongType})) {
            EditScreen(navigateBack = { navController.navigateUp()})
        }
        composable(Screen.Chart.route) { ChartScreen(navController=navController)}
        composable(Screen.Calendar.route) { CalendarScreen(navController=navController)}
    }
}