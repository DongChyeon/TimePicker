package com.dongchyeon.timepicker

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dongchyeon.timepicker.model.PickerState
import com.dongchyeon.timepicker.model.rememberPickerState
import com.dongchyeon.timepicker.ui.PickerItem
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun TimePicker(
    modifier: Modifier = Modifier,
    initialTime: LocalTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time,
    visibleItemsCount: Int = TimePickerDefaults.visibleItemsCount,
    timeFormat: TimeFormat = TimePickerDefaults.timeFormat,
    style: PickerStyle = TimePickerDefaults.pickerStyle(),
    selector: PickerSelector = TimePickerDefaults.pickerSelector(),
    curveEffect: CurveEffect = TimePickerDefaults.curveEffect(),
    onValueChange: (LocalTime) -> Unit
) {
    if (timeFormat.is24Hour) {
        TimePicker24Hour(
            modifier = modifier,
            visibleItemsCount = visibleItemsCount,
            initialTime = initialTime,
            style = style,
            selector = selector,
            curveEffect = curveEffect,
            onValueChange = onValueChange
        )
    } else {
        TimePicker12Hour(
            modifier = modifier,
            visibleItemsCount = visibleItemsCount,
            initialTime = initialTime,
            localeTimeFormat = timeFormat.localeTimeFormat,
            style = style,
            selector = selector,
            curveEffect = curveEffect,
            onValueChange = onValueChange
        )
    }
}

@Composable
private fun TimePicker12Hour(
    modifier: Modifier = Modifier,
    initialTime: LocalTime,
    visibleItemsCount: Int,
    localeTimeFormat: LocaleTimeFormat,
    style: PickerStyle,
    selector: PickerSelector,
    curveEffect: CurveEffect,
    onValueChange: (LocalTime) -> Unit
) {
    val amPmItems = remember {
        listOf(
            TimePeriod.AM.getLabel(localeTimeFormat),
            TimePeriod.PM.getLabel(localeTimeFormat)
        )
    }
    val hourItems = remember { (1..12).toList() }
    val minuteItems = remember { (0..59).toList() }

    val amPmPickerState = rememberPickerState(
        initialIndex = if (initialTime.hour < 12) 0 else 1,
        items = amPmItems
    )
    val hourPickerState = rememberPickerState(
        initialIndex = hourItems.indexOf(if (initialTime.hour % 12 == 0) 12 else initialTime.hour % 12),
        items = hourItems
    )
    val minutePickerState = rememberPickerState(
        initialIndex = minuteItems.indexOf(initialTime.minute),
        items = minuteItems
    )

    var previousHour by remember { mutableIntStateOf(initialTime.hour) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        SelectorBackground(
            style = style,
            selector = selector
        )

        Row(
            modifier = Modifier.padding(horizontal = 50.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PickerItem(
                items = amPmItems,
                state = amPmPickerState,
                visibleItemsCount = visibleItemsCount,
                style = style,
                modifier = Modifier.weight(1f),
                textModifier = Modifier.padding(8.dp),
                infiniteScroll = false,
                curveEffect = curveEffect,
                onValueChange = {
                    onPickerValueChange(
                        amPmPickerState,
                        hourPickerState,
                        minutePickerState,
                        localeTimeFormat,
                        onValueChange
                    )
                }
            )

            PickerItem(
                items = hourItems,
                state = hourPickerState,
                visibleItemsCount = visibleItemsCount,
                style = style,
                modifier = Modifier.weight(1f),
                textModifier = Modifier.padding(8.dp),
                infiniteScroll = true,
                curveEffect = curveEffect,
                onValueChange = {
                    onPickerValueChange(
                        amPmPickerState,
                        hourPickerState,
                        minutePickerState,
                        localeTimeFormat,
                        onValueChange
                    )
                    scope.launch {
                        val currentHour = hourPickerState.selectedItem
                        val currentIndex = amPmPickerState.lazyListState.firstVisibleItemIndex % amPmItems.size
                        val nextIndex = (currentIndex + 1) % amPmItems.size

                        if ((currentHour == 12 && previousHour == 11) ||
                            (currentHour == 11 && previousHour == 12)
                        ) {
                            amPmPickerState.lazyListState.animateScrollToItem(nextIndex)
                        }
                        previousHour = currentHour
                    }
                }
            )

            PickerItem(
                items = minuteItems,
                state = minutePickerState,
                visibleItemsCount = visibleItemsCount,
                style = style,
                modifier = Modifier.weight(1f),
                textModifier = Modifier.padding(8.dp),
                infiniteScroll = true,
                itemFormatter = { it.toString().padStart(2, '0') },
                curveEffect = curveEffect,
                onValueChange = {
                    onPickerValueChange(
                        amPmPickerState,
                        hourPickerState,
                        minutePickerState,
                        localeTimeFormat,
                        onValueChange
                    )
                }
            )
        }
    }
}

@Composable
private fun SelectorBackground(
    modifier: Modifier = Modifier,
    style: PickerStyle,
    selector: PickerSelector
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(with(LocalDensity.current) { style.textStyle.lineHeight.toDp() } + 20.dp)
            .background(color = selector.color, shape = selector.shape)
            .then(
                if (selector.border != null) {
                    Modifier.border(border = selector.border, shape = selector.shape)
                } else {
                    Modifier
                }
            )
    )
}

@Composable
private fun TimePicker24Hour(
    modifier: Modifier = Modifier,
    initialTime: LocalTime,
    visibleItemsCount: Int,
    style: PickerStyle,
    selector: PickerSelector,
    curveEffect: CurveEffect,
    onValueChange: (LocalTime) -> Unit
) {
    val hourItems = remember { (0..23).toList() }
    val minuteItems = remember { (0..59).toList() }

    val hourPickerState = rememberPickerState(
        initialIndex = hourItems.indexOf(initialTime.hour),
        items = hourItems
    )
    val minutePickerState = rememberPickerState(
        initialIndex = minuteItems.indexOf(initialTime.minute),
        items = minuteItems
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        SelectorBackground(
            style = style,
            selector = selector
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 50.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PickerItem(
                items = hourItems,
                state = hourPickerState,
                visibleItemsCount = visibleItemsCount,
                style = style,
                modifier = Modifier.weight(1f),
                textModifier = Modifier.padding(8.dp),
                infiniteScroll = true,
                curveEffect = curveEffect,
                onValueChange = {
                    onPickerValueChange(hourPickerState, minutePickerState, onValueChange)
                }
            )

            Text(
                text = ":",
                style = style.textStyle,
                color = style.textColor
            )

            PickerItem(
                items = minuteItems,
                state = minutePickerState,
                visibleItemsCount = visibleItemsCount,
                style = style,
                modifier = Modifier.weight(1f),
                textModifier = Modifier.padding(8.dp),
                infiniteScroll = true,
                itemFormatter = { it.toString().padStart(2, '0') },
                curveEffect = curveEffect,
                onValueChange = {
                    onPickerValueChange(hourPickerState, minutePickerState, onValueChange)
                }
            )
        }
    }
}

private fun onPickerValueChange(
    amPmState: PickerState<String>,
    hourState: PickerState<Int>,
    minuteState: PickerState<Int>,
    localeTimeFormat: LocaleTimeFormat,
    onValueChange: (LocalTime) -> Unit
) {
    val amPm = amPmState.selectedItem
    val hour = hourState.selectedItem
    val minute = minuteState.selectedItem

    val adjustedHour = when (localeTimeFormat) {
        LocaleTimeFormat.ENGLISH -> {
            if (amPm == TimePeriod.AM.getLabel(LocaleTimeFormat.ENGLISH) && hour == 12) {
                0
            } else if (amPm == TimePeriod.PM.getLabel(LocaleTimeFormat.ENGLISH) && hour != 12) {
                hour + 12
            } else {
                hour
            }
        }
        LocaleTimeFormat.KOREAN -> {
            if (amPm == TimePeriod.AM.getLabel(LocaleTimeFormat.KOREAN) && hour == 12) {
                0
            } else if (amPm == TimePeriod.PM.getLabel(LocaleTimeFormat.KOREAN) && hour != 12) {
                hour + 12
            } else {
                hour
            }
        }
    }

    val newTime = LocalTime(adjustedHour, minute)

    onValueChange(newTime)
}

private fun onPickerValueChange(
    hourState: PickerState<Int>,
    minuteState: PickerState<Int>,
    onValueChange: (LocalTime) -> Unit
) {
    val hour = hourState.selectedItem
    val minute = minuteState.selectedItem

    val newTime = LocalTime(hour, minute)

    onValueChange(newTime)
}

@Preview
@Composable
private fun TimePickerPreview() {
    Column(
        modifier = Modifier
            .background(Color.Black),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        TimePicker(
            timeFormat = TimeFormat.TWENTY_FOUR_HOUR
        ) { newTime ->
            Log.d("TimePicker", "Selected Time: $newTime")
        }

        TimePicker(
            selector = TimePickerDefaults.pickerSelector(
                color = Color.Gray.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            ),
            visibleItemsCount = 7,
            timeFormat = TimeFormat.TWELVE_HOUR_KOREAN
        ) { newTime ->
            Log.d("TimePicker", "Selected Time: $newTime")
        }
    }
}
