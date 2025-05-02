 package com.github.ttt374.healthcaretracer.ui.entry

import androidx.compose.foundation.layout.Box
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

const val MIN_PULSE = 40

@Composable
fun EditScreen(editViewModel: EditViewModel = hiltViewModel(), itemViewModel: ItemViewModel = hiltViewModel(), appNavigator: AppNavigator) {
    val itemUiState by editViewModel.itemUiState.collectAsState()
    val locationList by itemViewModel.locationList.collectAsState()
    val saveState by itemViewModel.saveState.collectAsState()

    LaunchedEffect(saveState) {
        if (saveState) {
            appNavigator.navigateBack()
        }
    }
    Scaffold(contentWindowInsets = WindowInsets(0), topBar = { CustomTopAppBar(stringResource(R.string.edit), navigateBack = appNavigator::navigateBack) }){ innerPadding ->
        Box (modifier = Modifier.padding(innerPadding)) {
            ItemEntryContent(editMode = EditMode.Edit(itemUiState.toItem() ),
                itemUiState = itemUiState,
                updateItemUiState = editViewModel::updateItemUiState,
                locationList = locationList,
                onPost = { itemViewModel.upsertItem(itemUiState.toItem())},
                onDelete = { itemViewModel.deleteItem(itemUiState.toItem())},
            )
        }
    }
}
