package com.github.ttt374.healthcaretracer.ui.entry

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester


enum class FocusField { BpUpper, BpLower, Pulse, BodyTemperature, BodyWeight,  }
class FocusRequestMap(private val map: Map<FocusField, FocusRequester>) {
    fun requestFirst() {
        map[FocusField.entries.first()]?.requestFocus()
    }

    fun requestNext(current: FocusField) {
        val nextIndex = current.ordinal + 1
        if (nextIndex < FocusField.entries.size) {
            val next = FocusField.entries[nextIndex]
            map[next]?.requestFocus()
        }
    }
    fun requestNextIf(current: FocusField, condition: () -> Boolean){
        if (condition()) requestNext(current)
    }
    operator fun get(field: FocusField): FocusRequester = map.getValue(field)
}
@Composable
fun rememberFocusRequestMap(): FocusRequestMap {
    val map = remember {
        FocusField.entries.associateWith { FocusRequester() }
    }
    return remember { FocusRequestMap(map) }
}