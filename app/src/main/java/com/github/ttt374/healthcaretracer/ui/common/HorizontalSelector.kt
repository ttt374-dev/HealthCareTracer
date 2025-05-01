package com.github.ttt374.healthcaretracer.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun <T>HorizontalSelector(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    optionText: @Composable (T) -> String = { it.toString() }
){
    Row {
        options.forEach { option ->
            Text(
                text = "[${optionText(option)}]",
                modifier = Modifier
                    .padding(4.dp)
                    .clickable { onOptionSelected(option) }
                    .background(
                        if (option == selectedOption) Color.LightGray else Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                color = if (option == selectedOption) Color.Black else Color.Gray
            )
        }
    }
}

//@Composable
//fun HorizontalSelector(
//    options: List<String>,
//    selectedOption: String,
//    onOptionSelected: (String) -> Unit
//) {
//    Row {
//        options.forEach { option ->
//            Text(
//                text = "[$option]",
//                modifier = Modifier
//                    .padding(4.dp)
//                    .clickable { onOptionSelected(option) }
//                    .background(
//                        if (option == selectedOption) Color.LightGray else Color.Transparent,
//                        shape = RoundedCornerShape(4.dp)
//                    )
//                    .padding(horizontal = 8.dp, vertical = 2.dp),
//                color = if (option == selectedOption) Color.Black else Color.Gray
//            )
//        }
//    }
//}
//
