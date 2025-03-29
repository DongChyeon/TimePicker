package com.dongchyeon.timepicker.model

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class PickerState(
    val lazyListState: LazyListState,
    var selectedItem: String,
    var startIndex: Int
)

@Composable
fun rememberPickerState(
    lazyListState: LazyListState = rememberLazyListState(),
    selectedItem: String = "",
    startIndex: Int = 0
): PickerState = remember { PickerState(lazyListState, selectedItem, startIndex) }
