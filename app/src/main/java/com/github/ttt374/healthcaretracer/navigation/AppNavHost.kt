package com.github.ttt374.healthcaretracer.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.github.ttt374.healthcaretracer.ui.entry.EntryScreen
import com.github.ttt374.healthcaretracer.ui.home.HomeScreen

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    //data object HomeId: Screen("home/{id}")
    //data object Settings : Screen("settings")
    //data object Archived : Screen("archived")
    data object Entry : Screen("entry")
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) { HomeScreen(navigateToEntry = { navController.navigate(Screen.Entry.route)}) }
        //composable(route = Screen.HomeId.route, arguments = listOf(navArgument("id") { type = NavType.LongType })) { HomeScreen() }
        //composable(Screen.Settings.route) { SettingsScreen() }
        //composable(Screen.Archived.route) { ArchivedScreen() }
        composable(Screen.Entry.route) { EntryScreen(navigateBack = { navController.popBackStack()}) }
    }
}