package com.github.ttt374.healthcaretracer.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.ui.calendar.CalendarScreen
import com.github.ttt374.healthcaretracer.ui.chart.ChartScreen
import com.github.ttt374.healthcaretracer.ui.entry.EditScreen
import com.github.ttt374.healthcaretracer.ui.entry.EntryScreen
import com.github.ttt374.healthcaretracer.ui.home.HomeScreen
import com.github.ttt374.healthcaretracer.ui.metric.MetricScreen
import com.github.ttt374.healthcaretracer.ui.settings.SettingsScreen
import com.github.ttt374.healthcaretracer.ui.statics.StatisticsScreen
import java.time.LocalDate

sealed class Screen(val route: String, val routeWithArgs: String = "") {
    data object Home : Screen("home")
    data object Entry: Screen("entry")
    data object EntryWithDate : Screen("entry", "entry/{date}")  // date is optional
    data object Edit : Screen("edit", "edit/{itemId}")
    data object Chart: Screen("chart")
    data object Calendar: Screen("calendar")
    data object Statistics: Screen("statistics")
    data object Settings: Screen("settings")
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    val appNavigator = AppNavigator(navController)

    NavHost(navController = navController, startDestination = Screen.Home.route) {
    //NavHost(navController = navController, startDestination = "metric") {
        composable(Screen.Home.route) { HomeScreen(appNavigator = appNavigator) }
        composable(Screen.Entry.route) { EntryScreen(appNavigator = appNavigator)}
        composable(Screen.EntryWithDate.routeWithArgs, arguments = listOf(navArgument("date") { type = NavType.StringType })){
            EntryScreen(appNavigator = appNavigator)
        }
        composable(Screen.Edit.routeWithArgs, arguments = listOf(navArgument("itemId"){ type = NavType.LongType})) {
            EditScreen(appNavigator=appNavigator)
        }
        composable(Screen.Chart.route) { ChartScreen(appNavigator=appNavigator)}
        composable(Screen.Calendar.route) { CalendarScreen(appNavigator=appNavigator)}
        composable(Screen.Statistics.route) { StatisticsScreen(appNavigator=appNavigator)}
        composable(Screen.Settings.route) { SettingsScreen(appNavigator=appNavigator)}
        composable("metric"){ MetricScreen()}
    }
}
class AppNavigator(private val navController: NavHostController){
    fun navigateBack() = navController.popBackStack()
    fun navigateTo(route: String) = navController.navigate(route)

    fun navigateToHome() = navigateTo(Screen.Home.route)
    fun navigateToEntry(date: LocalDate? = null) =
        navigateTo(date?.let { "${Screen.EntryWithDate.route}/$date" } ?: Screen.Entry.route)
    fun navigateToEdit(itemId: Long) = navigateTo("${Screen.Edit.route}/$itemId")
    fun navigateToChart() = navigateTo(Screen.Chart.route)
    fun navigateToCalendar() = navigateTo(Screen.Calendar.route)
    fun navigateToStatistics() = navigateTo(Screen.Statistics.route)
    fun navigateToSettings() = navigateTo(Screen.Settings.route)
}