package com.github.ttt374.healthcaretracer.ui.common


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SelectableTextField(
    value: String,
    selectableList: List<String>,
    modifier: Modifier = Modifier,
    initialExpanded: Boolean = false,
    labelText: String = "Select or Enter",
    onValueChange: (String) -> Unit,
)  {
    val expandState = rememberExpandState(initialExpanded)

    Column( modifier = modifier ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(labelText) },
            singleLine = true,
            trailingIcon = {
                Icon(
                    imageVector =  if (expandState.expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = "Dropdown Icon",
                    modifier = Modifier
                        .clickable { expandState.toggle() }
                        .padding(8.dp)
                )
            },
        )
        // DropdownMenu for suggestions
        AnimatedVisibility(visible = expandState.expanded) {
            DropdownMenu(expanded = expandState.expanded, onDismissRequest = { expandState.fold() }) {
                selectableList.forEach { tag ->
                    DropdownMenuItem(
                        text = { Text(tag) },
                        onClick = {
                            expandState.fold()
                            onValueChange(tag)
                        }
                    )
                }
            }
        }
    }
}