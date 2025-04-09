package com.github.ttt374.healthcaretracer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.navigation.compose.rememberNavController
import com.github.ttt374.healthcaretracer.navigation.AppNavHost
import com.github.ttt374.healthcaretracer.ui.theme.HealthCareTracerTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

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

