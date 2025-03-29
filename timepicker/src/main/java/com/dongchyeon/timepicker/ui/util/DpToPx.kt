package com.dongchyeon.timepicker.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
internal fun Dp.toPx(): Float {
    return with(LocalDensity.current) { this@toPx.toPx() }
}
