package com.dongchyeon.timepicker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object TimePickerDefaults {
    @Composable
    fun pickerSelector(
        enabled: Boolean = true,
        shape: RoundedCornerShape = RoundedCornerShape(16.dp),
        color: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
        border: BorderStroke? = null
    ): PickerSelector {
        return PickerSelector(
            enabled = enabled,
            shape = shape,
            color = color,
            border = border
        )
    }
}

@Immutable
class PickerSelector(
    val enabled: Boolean,
    val shape: RoundedCornerShape,
    val color: Color,
    val border: BorderStroke?
) {
    fun copy(
        enabled: Boolean = this.enabled,
        shape: RoundedCornerShape = this.shape,
        color: Color = this.color,
        border: BorderStroke? = this.border
    ): PickerSelector {
        return PickerSelector(
            enabled = enabled,
            shape = shape,
            color = color,
            border = border
        )
    }
}
