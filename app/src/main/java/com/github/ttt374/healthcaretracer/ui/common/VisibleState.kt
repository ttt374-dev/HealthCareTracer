package com.github.ttt374.healthcaretracer.ui.common


import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.github.ttt374.healthcaretracer.data.item.Item

interface VisibleState {
    val visible: Boolean
    fun show()
    fun hide()
    fun set(v: Boolean)
    fun toggle()
}
class VisibleStateImpl(initialState: Boolean=false) : VisibleState {
    private var _visible = mutableStateOf(initialState)
    override val visible: Boolean
        get() = _visible.value

    override fun show() {
        _visible.value = true }
    override fun hide() { _visible.value = false }
    override fun set(v: Boolean) { _visible.value = v}
    override fun toggle() { _visible.value = !visible }
}
@Composable
fun rememberVisibleState(initialState: Boolean = false): VisibleState =
    remember { VisibleStateImpl(initialState)}

/////////////////
interface DialogState : VisibleState {
    val isOpen: Boolean
    fun open()
    fun close()
}
class DialogStateImpl(initialState: Boolean = false) : DialogState, VisibleState by VisibleStateImpl(initialState) {
    override val isOpen: Boolean
        get() = visible
    override fun open() = show()
    override fun close() = hide()
}
@Composable
fun rememberDialogState(initialState: Boolean = false): DialogState =
    remember { DialogStateImpl(initialState)}

//////////////////////
interface ItemDialogState: DialogState {
    val item: Item
    fun open(item: Item)
}
class ItemDialogStateImpl(initialState: Boolean = false): ItemDialogState, DialogState by DialogStateImpl(initialState){
    private var _item = mutableStateOf(Item())
    override val item: Item
        get() = _item.value
    override fun open(item: Item){
        _item.value = item
        open()
    }
}
@Composable
fun rememberItemDialogState(initialState: Boolean = false): ItemDialogState =
    remember { ItemDialogStateImpl(initialState)}

/////////////////
interface ExpandState : VisibleState {
    val expanded: Boolean
    fun expand()
    fun fold()
}
class ExpandStateImpl(initialState: Boolean = false) : ExpandState, VisibleState by VisibleStateImpl(initialState) {
    override val expanded: Boolean
        get() = visible
    override fun expand() = show()
    override fun fold() = hide()
}
@Composable
fun rememberExpandState(initialState: Boolean = false): ExpandState =
    remember { ExpandStateImpl(initialState)}
