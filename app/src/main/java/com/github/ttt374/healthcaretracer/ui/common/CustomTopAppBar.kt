package com.github.ttt374.healthcaretracer.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class MenuItem(val text: String = "", val onClick: () -> Unit = {}, val enabled: Boolean = true)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(subTitle: String = "",
                    menuItems: List<MenuItem>? = null,
                    navigateBack: (() -> Unit)? = null){

    val menuState = rememberExpandState()

    val appName = "Healthcare Tracer"
    TopAppBar(title = { Text("${appName}: $subTitle") },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.LightGray),
        actions = {
            if (menuItems != null){
                IconButton(onClick = { menuState.expand()}){
                    Icon(Icons.Filled.Menu, contentDescription = "menu")
                }
                DropdownMenu(menuState.expanded, onDismissRequest = { menuState.fold()}){
                    menuItems.forEach { menu ->
                        DropdownMenuItem(text = { Text(menu.text) }, enabled = menu.enabled,
                            onClick = { menu.onClick(); menuState.fold()})
                    }
                }
            }
        },
        navigationIcon = {
            if (navigateBack != null) {
                IconButton(onClick = { navigateBack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Navigate Back")
                }
            }
        }
    )
}