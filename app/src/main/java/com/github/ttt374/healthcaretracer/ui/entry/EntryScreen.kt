package com.github.ttt374.healthcaretracer.ui.entry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar

@Composable
fun EntryScreen(viewModel: EditViewModel = hiltViewModel(),
                navigateBack: () -> Unit = {}){
    val uiState by viewModel.itemUiState.collectAsState()
    val locationList by viewModel.locationList.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navigateBack()
        }
    }
    Scaffold(topBar = { CustomTopAppBar("Entry", navigateBack = navigateBack) }){ innerPadding ->
        Column (modifier = Modifier.padding(innerPadding)) {
            ItemEntryContent(itemUiState = uiState,
                updateItemUiState = viewModel::updateItemUiState,
                locationList = locationList,
                onPost = viewModel::upsertItem)
        }
    }
}
