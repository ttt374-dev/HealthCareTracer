package com.github.ttt374.healthcaretracer.ui.home

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomBottomAppBar
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.MenuItem
import com.github.ttt374.healthcaretracer.ui.common.rememberDialogState
import java.time.Instant
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    appNavigator: AppNavigator
){
    val dailyItems by homeViewModel.dailyItems.collectAsState()
    val importFilePickerDialogState = rememberDialogState()
    val exportFilePickerDialogState = rememberDialogState()
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    val config by homeViewModel.config.collectAsState()
    val guideline = config.bloodPressureGuideline //   selectedGuideline
    val zoneId = config.zoneId

    val importFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            selectedFileUri = uri
            Log.d("ImportScreen", "Selected file: $uri")
            selectedFileUri?.let { homeViewModel.importData(it) }
            importFilePickerDialogState.close()
        }
    )
    val exportFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
        onResult = { uri ->
            uri?.let { homeViewModel.exportData(it) }
            exportFilePickerDialogState.close()
        }
    )
    LaunchedEffect(importFilePickerDialogState.isOpen) {
        if (importFilePickerDialogState.isOpen) {
            importFilePickerLauncher.launch(arrayOf("*/*"))
        }
    }
    LaunchedEffect(exportFilePickerDialogState.isOpen) {
        if (exportFilePickerDialogState.isOpen){
            val filenameFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm").withZone(zoneId)
            val defaultFilename = "healthcare-${filenameFormatter.format(Instant.now())}.csv"

            exportFilePickerLauncher.launch(defaultFilename)
        }
    }

    Scaffold(topBar = {
        CustomTopAppBar(
            stringResource(R.string.home),
            menuItems = listOf(
                MenuItem("export", onClick = { exportFilePickerDialogState.open() }),
                MenuItem("import", onClick = { importFilePickerDialogState.open() })
            )
        )
    },
        bottomBar = {
            CustomBottomAppBar(appNavigator,
                floatingActionButton = {
                    FloatingActionButton(onClick = appNavigator::navigateToEntry) {
                        Icon(Icons.Filled.Add, "add")
                    }
                },
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            LazyColumn { // (reverseLayout = true) {
                items(dailyItems.reversed()) { dailyItem ->
                    DailyItemRow(dailyItem, guideline, config.dayPeriodConfig, config.zoneId,
                        appNavigator::navigateToEdit,)
                }
            }
        }
    }
}
