package com.github.ttt374.healthcaretracer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.ttt374.healthcaretracer.navigation.AppNavHost
import com.github.ttt374.healthcaretracer.ui.chart.ChartScreen
import com.github.ttt374.healthcaretracer.ui.entry.EntryScreen
import com.github.ttt374.healthcaretracer.ui.home.HomeScreen
import com.github.ttt374.healthcaretracer.ui.theme.HealthCareTracerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HealthCareTracerTheme {
                val navController = rememberNavController()
                //(appNavigator as? AppNavigatorImpl)?.initialize(navController)
                //appNavigator.initialize(navController)
                AppNavHost(navController)

                //Surface(Modifier.fillMaxSize()){
                    //HomeScreen()
                    //ChartScreen()
                //}
            }
        }
    }
}

