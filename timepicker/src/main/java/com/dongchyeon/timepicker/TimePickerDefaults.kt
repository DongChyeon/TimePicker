package com.dongchyeon.timepicker

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
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

    @Composable
    fun itemLabel(
        style: TextStyle = MaterialTheme.typography.titleMedium,
        color: Color = Color.White
    ): ItemLabel {
        return ItemLabel(
            style = style,
            color = color
        )
    }

    val timeFormat: TimeFormat = TimeFormat.DEFAULT
    val visibleItemsCount: Int = 5
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

@Immutable
class ItemLabel(
    val style: TextStyle,
    val color: Color
) {
    fun copy(
        style: TextStyle = this.style,
        color: Color = this.color
    ): ItemLabel {
        return ItemLabel(
            style = style,
            color = color
        )
    }
}

enum class TimeFormat(val is24Hour: Boolean, val localeTimeFormat: LocaleTimeFormat) {
    DEFAULT(false, LocaleTimeFormat.ENGLISH),
    TWELVE_HOUR(false, LocaleTimeFormat.ENGLISH),
    TWELVE_HOUR_KOREAN(false, LocaleTimeFormat.KOREAN),
    TWENTY_FOUR_HOUR(true, LocaleTimeFormat.ENGLISH);
}

enum class LocaleTimeFormat {
    ENGLISH, KOREAN
}

enum class TimePeriod(private val englishLabel: String, private val koreanLabel: String) {
    AM("AM", "오전"),
    PM("PM", "오후");

    fun getLabel(localeTimeFormat: LocaleTimeFormat): String {
        return when (localeTimeFormat) {
            LocaleTimeFormat.ENGLISH -> englishLabel
            LocaleTimeFormat.KOREAN -> koreanLabel
        }
    }
}
