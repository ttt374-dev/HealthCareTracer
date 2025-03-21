package com.github.ttt374.healthcaretracer.ui.entry

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar

@Composable
fun EntryScreen(viewModel: EditViewModel = hiltViewModel(),
                navigateBack: () -> Unit = {}){
    val uiState = viewModel.uiState
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            navigateBack()
        }
    }
    Scaffold(topBar = { CustomTopAppBar("Entry", navigateBack = navigateBack) }){ innerPadding ->
        ItemEntryContent(entryUiState = uiState,
            updateItemUiState = { itemUiState -> viewModel.updateItemUiState(itemUiState)},
            onPost = viewModel::upsertItem,
            isEditing = false,
            modifier = Modifier.padding(innerPadding))
    }
}