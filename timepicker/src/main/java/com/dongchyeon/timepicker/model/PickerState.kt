package com.dongchyeon.timepicker.model

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PickerState<T>(
    val lazyListState: LazyListState,
    initialIndex: Int,
    private val items: List<T>
) {
    private val _selectedIndex = MutableStateFlow(initialIndex)
    val selectedIndex: StateFlow<Int>
        get() = _selectedIndex

    val selectedItem: T
        get() = items.getOrElse(_selectedIndex.value) { items.first() }

    fun updateSelectedIndex(newIndex: Int) {
        _selectedIndex.value = newIndex.coerceIn(0, items.size - 1)
    }
}

@Composable
fun <T> rememberPickerState(
    lazyListState: LazyListState = rememberLazyListState(),
    initialIndex: Int = 0,
    items: List<T>
): PickerState<T> = remember { PickerState(lazyListState, initialIndex, items) }
