package com.dongchyeon.timepicker

import android.util.Log
import androidx.compose.foundation.background
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
import java.util.Locale

@Composable
fun TimePicker(
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 2.dp,
    visibleItemsCount: Int = 5,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    textColor: Color = Color.White,
    initialTime: LocalTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time,
    is24HourFormat: Boolean = false,
    onValueChange: (LocalTime) -> Unit
) {
    if (is24HourFormat) {
        TimePicker24Hour(
            modifier = modifier,
            itemSpacing = itemSpacing,
            visibleItemsCount = visibleItemsCount,
            textStyle = textStyle,
            textColor = textColor,
            initialTime = initialTime,
            onValueChange = onValueChange
        )
    } else {
        TimePicker12Hour(
            modifier = modifier,
            itemSpacing = itemSpacing,
            visibleItemsCount = visibleItemsCount,
            textStyle = textStyle,
            textColor = textColor,
            initialTime = initialTime,
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
                    val amPmItems = remember { listOf("AM", "PM") }
                    val hourItems = remember { (1..12).map { it.toString() } }
                    val minuteItems = remember { (0..59).map { String.format(Locale.ROOT, "%02d", it) } }

                    val amPmPickerState = rememberPickerState(
                        selectedItem = amPmItems.indexOf(if (initialTime.hour < 12) "AM" else "PM").toString(),
                        startIndex = if (initialTime.hour < 12) 0 else 1
                    )
                    val hourPickerState = rememberPickerState(
                        selectedItem = hourItems.indexOf((if (initialTime.hour % 12 == 0) 12 else initialTime.hour % 12).toString()).toString(),
                        startIndex = hourItems.indexOf((if (initialTime.hour % 12 == 0) 12 else initialTime.hour % 12).toString())
                    )
                    val minutePickerState = rememberPickerState(
                        selectedItem = minuteItems.indexOf(initialTime.minute.toString().padStart(2, '0')).toString(),
                        startIndex = minuteItems.indexOf(initialTime.minute.toString().padStart(2, '0'))
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
                                .background(Color.Gray, shape = RoundedCornerShape(12.dp))
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
                                        onValueChange
                                    )

                                    scope.launch {
                                        val currentHour = hourPickerState.selectedItem.toIntOrNull() ?: 0

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
                                onValueChange = {
                                    onPickerValueChange(
                                        amPmPickerState,
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

@Composable
private fun TimePicker24Hour(
    modifier: Modifier = Modifier,
    itemSpacing: Dp = 2.dp,
    visibleItemsCount: Int = 5,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    textColor: Color = Color.White,
    initialTime: LocalTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).time,
    onValueChange: (LocalTime) -> Unit
) {
    val hourItems = remember { (1..23).map { it.toString() } }
    val minuteItems = remember { (0..59).map { String.format(Locale.ROOT, "%02d", it) } }

    val hourPickerState = rememberPickerState(
        selectedItem = hourItems.indexOf((if (initialTime.hour % 12 == 0) 12 else initialTime.hour % 12).toString()).toString(),
        startIndex = hourItems.indexOf((if (initialTime.hour % 12 == 0) 12 else initialTime.hour % 12).toString())
    )
    val minutePickerState = rememberPickerState(
        selectedItem = minuteItems.indexOf(initialTime.minute.toString().padStart(2, '0')).toString(),
        startIndex = minuteItems.indexOf(initialTime.minute.toString().padStart(2, '0'))
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
                                .background(Color.Gray, shape = RoundedCornerShape(12.dp))
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
    amPmState: PickerState,
    hourState: PickerState,
    minuteState: PickerState,
    onValueChange: (LocalTime) -> Unit
) {
    val amPm = amPmState.selectedItem
    val hour = hourState.selectedItem.toIntOrNull() ?: 0
    val minute = minuteState.selectedItem.toIntOrNull() ?: 0

    val adjustedHour = if (amPm == "AM" && hour == 12) {
        0
    } else if (amPm == "PM" && hour != 12) {
        hour + 12
    } else {
        hour
    }

    val newTime = LocalTime(adjustedHour, minute)

    onValueChange(newTime)
}

private fun onPickerValueChange(
    hourState: PickerState,
    minuteState: PickerState,
    onValueChange: (LocalTime) -> Unit
) {
    val hour = hourState.selectedItem.toIntOrNull() ?: 0
    val minute = minuteState.selectedItem.toIntOrNull() ?: 0

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
            is24HourFormat = true
        ) { newTime ->
            Log.d("TimePicker", "Selected Time: $newTime")
        }

        TimePicker(
            is24HourFormat = false
        ) { newTime ->
            Log.d("TimePicker", "Selected Time: $newTime")
        }
    }
}
