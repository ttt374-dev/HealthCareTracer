package com.github.ttt374.healthcaretracer.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.mikephil.charting.BuildConfig
import com.github.ttt374.healthcaretracer.data.selectedGuideline
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar

@Composable
fun SettingsScreen(appNavigator: AppNavigator) {
    Scaffold(
        topBar = { CustomTopAppBar("Settings") },
        bottomBar = { CustomBottomAppBar(appNavigator) }
    ) { innerPadding ->
        Column (Modifier.padding(innerPadding).padding(16.dp)){
            SettingsRow("HTN Category Type") { Text(selectedGuideline.name) }
            SettingsRow("Version") { Text(BuildConfig.VERSION_NAME) }
        }
    }
}
@Composable
fun SettingsRow(label: String, content: @Composable () -> Unit ){
    Row {
        Text(label, Modifier.weight(1f))
        content()
    }
}