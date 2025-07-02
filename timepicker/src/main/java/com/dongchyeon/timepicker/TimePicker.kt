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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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
    itemSpacing: Dp = 2.dp,
    visibleItemsCount: Int = TimePickerDefaults.visibleItemsCount,
    itemLabel: ItemLabel = TimePickerDefaults.itemLabel(),
    initialTime: LocalTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time,
    timeFormat: TimeFormat = TimePickerDefaults.timeFormat,
    selector: PickerSelector = TimePickerDefaults.pickerSelector(),
    onValueChange: (LocalTime) -> Unit
) {
    if (timeFormat.is24Hour) {
        TimePicker24Hour(
            modifier = modifier,
            itemSpacing = itemSpacing,
            visibleItemsCount = visibleItemsCount,
            textStyle = itemLabel.style,
            textColor = itemLabel.color,
            initialTime = initialTime,
            selector = selector,
            onValueChange = onValueChange
        )
    } else {
        TimePicker12Hour(
            modifier = modifier,
            itemSpacing = itemSpacing,
            visibleItemsCount = visibleItemsCount,
            textStyle = itemLabel.style,
            textColor = itemLabel.color,
            initialTime = initialTime,
            localeTimeFormat = timeFormat.localeTimeFormat,
            selector = selector,
            onValueChange = onValueChange
        )
    }
}

@Composable
private fun TimePicker12Hour(
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 2.dp,
    visibleItemsCount: Int = 5,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    textColor: Color = Color.White,
    initialTime: LocalTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time,
    localeTimeFormat: LocaleTimeFormat,
    selector: PickerSelector,
    onValueChange: (LocalTime) -> Unit
) {
    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Box(
                modifier = modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
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
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                                .padding(horizontal = 20.dp)
                                .height(with(LocalDensity.current) { textStyle.lineHeight.toDp() } + 20.dp)
                                .background(
                                    color = selector.color,
                                    shape = selector.shape
                                )
                                .then(
                                    if (selector.border != null) {
                                        Modifier.border(
                                            border = selector.border,
                                            shape = selector.shape
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 50.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PickerItem(
                                state = amPmPickerState,
                                items = amPmItems,
                                visibleItemsCount = 3,
                                itemSpacing = itemSpacing,
                                textStyle = textStyle,
                                textColor = textColor,
                                modifier = Modifier.weight(1f),
                                textModifier = Modifier.padding(8.dp),
                                infiniteScroll = false,
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
                                state = hourPickerState,
                                items = hourItems,
                                visibleItemsCount = visibleItemsCount,
                                itemSpacing = itemSpacing,
                                textStyle = textStyle,
                                textColor = textColor,
                                modifier = Modifier.weight(1f),
                                textModifier = Modifier.padding(8.dp),
                                infiniteScroll = true,
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

                                        if (currentHour == 12 && previousHour == 11) {
                                            val currentIndex = amPmPickerState.lazyListState.firstVisibleItemIndex % amPmItems.size
                                            val nextIndex = (currentIndex + 1) % amPmItems.size
                                            amPmPickerState.lazyListState.animateScrollToItem(nextIndex)
                                        } else if (currentHour == 11 && previousHour == 12) {
                                            val currentIndex = amPmPickerState.lazyListState.firstVisibleItemIndex % amPmItems.size
                                            val nextIndex = (currentIndex + 1) % amPmItems.size
                                            amPmPickerState.lazyListState.animateScrollToItem(nextIndex)
                                        }

                                        previousHour = currentHour
                                    }
                                }
                            )

                            PickerItem(
                                state = minutePickerState,
                                items = minuteItems,
                                visibleItemsCount = visibleItemsCount,
                                itemSpacing = itemSpacing,
                                textStyle = textStyle,
                                textColor = textColor,
                                modifier = Modifier.weight(1f),
                                textModifier = Modifier.padding(8.dp),
                                infiniteScroll = true,
                                itemFormatter = { item ->
                                    item.toString().padStart(2, '0')
                                },
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
            }
        }
    }
}

@Composable
private fun TimePicker24Hour(
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 2.dp,
    visibleItemsCount: Int = 5,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    textColor: Color = Color.White,
    initialTime: LocalTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time,
    selector: PickerSelector,
    onValueChange: (LocalTime) -> Unit
) {
    val hourItems = remember { (1..23).toList() }
    val minuteItems = remember { (0..59).toList() }

    val hourPickerState = rememberPickerState(
        initialIndex = hourItems.indexOf(if (initialTime.hour % 12 == 0) 12 else initialTime.hour % 12),
        items = hourItems
    )
    val minutePickerState = rememberPickerState(
        initialIndex = minuteItems.indexOf(initialTime.minute),
        items = minuteItems
    )

    Box(modifier = modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Bottom
        ) {
            Box(
                modifier = modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.Center)
                                .padding(horizontal = 20.dp)
                                .height(with(LocalDensity.current) { textStyle.lineHeight.toDp() } + 20.dp)
                                .background(
                                    color = selector.color,
                                    shape = selector.shape
                                )
                                .then(
                                    if (selector.border != null) {
                                        Modifier.border(
                                            border = selector.border,
                                            shape = selector.shape
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 50.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PickerItem(
                                state = hourPickerState,
                                items = hourItems,
                                visibleItemsCount = visibleItemsCount,
                                itemSpacing = itemSpacing,
                                textStyle = textStyle,
                                textColor = textColor,
                                modifier = Modifier.weight(1f),
                                textModifier = Modifier.padding(8.dp),
                                infiniteScroll = true,
                                onValueChange = {
                                    onPickerValueChange(
                                        hourPickerState,
                                        minutePickerState,
                                        onValueChange
                                    )
                                }
                            )

                            Text(
                                text = ":",
                                style = textStyle,
                                color = textColor
                            )

                            PickerItem(
                                state = minutePickerState,
                                items = minuteItems,
                                visibleItemsCount = visibleItemsCount,
                                itemSpacing = itemSpacing,
                                textStyle = textStyle,
                                textColor = textColor,
                                modifier = Modifier.weight(1f),
                                textModifier = Modifier.padding(8.dp),
                                infiniteScroll = true,
                                itemFormatter = { item ->
                                    item.toString().padStart(2, '0')
                                },
                                onValueChange = {
                                    onPickerValueChange(
                                        hourPickerState,
                                        minutePickerState,
                                        onValueChange
                                    )
                                }
                            )
                        }
                    }
                }
            }
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
