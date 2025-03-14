package com.github.ttt374.healthcaretracer.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.ttt374.healthcaretracer.ui.chart.ChartScreen
import com.github.ttt374.healthcaretracer.ui.entry.EntryScreen
import com.github.ttt374.healthcaretracer.ui.home.HomeScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    //data object Settings : Screen("settings")

    data object Entry : Screen("entry")
    data object Chart: Screen("chart")
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { HomeScreen(navController=navController) }
        //composable(route = Screen.HomeId.route, arguments = listOf(navArgument("id") { type = NavType.LongType })) { HomeScreen() }
        //composable(Screen.Settings.route) { SettingsScreen() }
        //composable(Screen.Archived.route) { ArchivedScreen() }
        composable(Screen.Entry.route) { EntryScreen(navigateBack = { navController.popBackStack()}) }
        composable(Screen.Chart.route) { ChartScreen(navController=navController)}
    }}