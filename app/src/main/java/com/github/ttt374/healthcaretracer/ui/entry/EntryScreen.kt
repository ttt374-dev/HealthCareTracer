package com.github.ttt374.healthcaretracer.ui.entry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.R
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar

@Composable
fun EntryScreen(viewModel: EntryViewModel = hiltViewModel(), itemViewModel: ItemViewModel = hiltViewModel(), appNavigator: AppNavigator){
    val uiState by viewModel.itemUiState.collectAsState()
    val locationList by itemViewModel.locationList.collectAsState()
    val saveState by itemViewModel.saveState.collectAsState()
    val config by viewModel.config.collectAsState()

    LaunchedEffect(saveState) {
        if (saveState) {
            appNavigator.navigateBack()
        }
    }
//    val imeInsets = WindowInsets.ime
//    val imeBottomPadding = with(LocalDensity.current) {
//        imeInsets.getBottom(this).toDp()
//    }
    //contentWindowInsets = WindowInsets(0),
    Scaffold(contentWindowInsets = WindowInsets(0), topBar = { CustomTopAppBar(stringResource(R.string.entry), navigateBack = appNavigator::navigateBack) }){ innerPadding ->
        Column (modifier = Modifier.padding(innerPadding)) {
            ItemEntryContent(itemUiState = uiState,
                updateItemUiState = viewModel::updateItemUiState,
                locationList = locationList,
                onPost = { itemViewModel.upsertItem(uiState.toItem())},
                onCancel = { appNavigator.navigateBack() },
                zoneId = config.zoneId,
                )
        }
    }
}
