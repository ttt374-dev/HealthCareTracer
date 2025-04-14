package com.github.ttt374.healthcaretracer.ui.entry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.github.ttt374.healthcaretracer.data.item.MIN_BP
import com.github.ttt374.healthcaretracer.navigation.AppNavigator
import com.github.ttt374.healthcaretracer.ui.common.ConfirmDialog
import com.github.ttt374.healthcaretracer.ui.common.CustomTopAppBar
import com.github.ttt374.healthcaretracer.ui.common.DatePickerDialog
import com.github.ttt374.healthcaretracer.ui.common.SelectableTextField
import com.github.ttt374.healthcaretracer.ui.common.TimePickerDialog
import com.github.ttt374.healthcaretracer.ui.common.rememberDialogState
import com.github.ttt374.healthcaretracer.ui.common.rememberItemDialogState
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
    Scaffold(topBar = { CustomTopAppBar("Edit", navigateBack = appNavigator::navigateBack) }){ innerPadding ->
        Box (modifier = Modifier.padding(innerPadding)) {
//            ButtonAboveIme {  }
            ItemEntryContent(editMode = EditMode.Edit,
                itemUiState = itemUiState,
                updateItemUiState = editViewModel::updateItemUiState,
                locationList = locationList,
                onPost = { itemViewModel.upsertItem(itemUiState.toItem())},
                onDelete = { itemViewModel.deleteItem(itemUiState.toItem())},
            )
        }
    }
}

@Composable
fun ButtonAboveIme(
    onClick: () -> Unit
) {
    val imeInsets = WindowInsets.ime
    val imeBottomPadding = with(LocalDensity.current) {
        imeInsets.getBottom(this).toDp()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = imeBottomPadding)
    ) {
        // 他のUI...
        Column {
            TextField("asdf", {})
            TextField("asdf", {})
            TextField("asdf", {})
            TextField("asdf", {})
            TextField("asdf", {})
            TextField("asdf", {})
            TextField("asdf", {})
        }

        // IMEのすぐ上に固定
        Button(
            onClick = onClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
        ) {
            Text("送信")
        }
    }
}

//class FocusManager (private val focusRequesters: List<FocusRequester>, initialIndex: Int = 0) {
//    private var currentFocusIndex: Int = initialIndex
//
//    private fun shiftFocus(){
//        if (currentFocusIndex < focusRequesters.size - 1) {
//            currentFocusIndex++ // 次のフィールドに移動
//        } else {
//            currentFocusIndex = 0 // もし最後のフィールドなら最初に戻る
//        }
//        focusRequesters[currentFocusIndex].requestFocus() // 次のフィールドにフォーカスを移す
//    }
//    fun shiftFocusIf(condition: () -> Boolean){
//        if (condition()) shiftFocus()
//    }
//}