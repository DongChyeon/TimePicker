package com.dongchyeon.timepicker.ui

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dongchyeon.timepicker.model.PickerState
import com.dongchyeon.timepicker.model.rememberPickerState
import com.dongchyeon.timepicker.ui.util.toPx
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.math.abs

@Composable
internal fun PickerItem(
    modifier: Modifier = Modifier,
    items: List<String>,
    state: PickerState = rememberPickerState(),
    visibleItemsCount: Int,
    textModifier: Modifier = Modifier,
    infiniteScroll: Boolean = true,
    textStyle: TextStyle,
    textColor: Color,
    itemSpacing: Dp,
    onValueChange: (String) -> Unit
) {
    val visibleItemsMiddle = visibleItemsCount / 2
    val listScrollCount = if (infiniteScroll) Int.MAX_VALUE else items.size + visibleItemsMiddle * 2
    val listScrollMiddle = listScrollCount / 2

    val listState = state.lazyListState
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    var itemHeightPixels by remember { mutableIntStateOf(0) }
    val itemHeightDp = with(LocalDensity.current) { itemHeightPixels.toDp() }

    LaunchedEffect(state.startIndex) {
        val safeStartIndex = state.startIndex
        val listStartIndex = if (infiniteScroll) {
            getStartIndexForInfiniteScroll(itemHeightPixels, listScrollMiddle, visibleItemsMiddle, safeStartIndex)
        } else {
            safeStartIndex
        }
        listState.scrollToItem(listStartIndex, 0)

        if (!infiniteScroll) {
            val selectedItem = items.getOrNull(safeStartIndex) ?: ""
            if (selectedItem != state.selectedItem) {
                state.selectedItem = selectedItem
                onValueChange(selectedItem)
            }
        }
    }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .map { layoutInfo ->
                val centerOffset = layoutInfo.viewportStartOffset +
                    (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
                layoutInfo.visibleItemsInfo.minByOrNull { item ->
                    val itemCenter = item.offset + (item.size / 2)
                    abs(itemCenter - centerOffset)
                }?.index
            }
            .distinctUntilChanged()
            .collect { centerIndex ->
                if (centerIndex != null) {
                    val adjustedIndex = if (infiniteScroll) {
                        centerIndex % items.size
                    } else {
                        centerIndex - visibleItemsMiddle
                    }.coerceIn(0, items.size - 1)

                    val newValue = items[adjustedIndex]

                    if (newValue != state.selectedItem) {
                        state.selectedItem = newValue
                        onValueChange(newValue)
                    }
                }
            }
    }

    val totalItemHeight = itemHeightDp + itemSpacing

    Box(modifier = modifier) {
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .height(totalItemHeight * visibleItemsCount)
                .pointerInput(Unit) { detectVerticalDragGestures { change, _ -> change.consume() } }
        ) {
            items(listScrollCount, key = { index -> index }) { index ->
                val layoutInfo = listState.layoutInfo
                val viewportCenterOffset = layoutInfo.viewportStartOffset +
                    (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2

                val itemInfo = layoutInfo.visibleItemsInfo.find { it.index == index }
                val itemCenterOffset = itemInfo?.offset?.let { it + (itemInfo.size / 2) } ?: 0

                val distanceFromCenter = abs(viewportCenterOffset - itemCenterOffset)
                val maxDistance = totalItemHeight.toPx() * visibleItemsMiddle

                val alpha = if (distanceFromCenter <= maxDistance) {
                    ((maxDistance - distanceFromCenter) / maxDistance).coerceIn(0.2f, 1f)
                } else {
                    0.2f
                }

                val scaleY = 1f - (0.2f * (distanceFromCenter / maxDistance)).coerceIn(0f, 0.4f)

                Text(
                    text = getItemForIndex(index, items, infiniteScroll, visibleItemsMiddle),
                    maxLines = 1,
                    style = textStyle,
                    color = textColor.copy(alpha = alpha),
                    modifier = Modifier
                        .padding(vertical = itemSpacing / 2)
                        .graphicsLayer(scaleY = scaleY)
                        .onSizeChanged { size -> itemHeightPixels = size.height }
                        .then(textModifier)
                )
            }
        }
    }
}

private fun getItemForIndex(
    index: Int,
    items: List<String>,
    infiniteScroll: Boolean,
    visibleItemsMiddle: Int
): String {
    return if (!infiniteScroll) {
        items.getOrNull(index - visibleItemsMiddle) ?: ""
    } else {
        items.getOrNull(index % items.size) ?: ""
    }
}

private fun getStartIndexForInfiniteScroll(
    itemSize: Int,
    listScrollMiddle: Int,
    visibleItemsMiddle: Int,
    startIndex: Int
): Int {
    return listScrollMiddle - listScrollMiddle % itemSize - visibleItemsMiddle + startIndex
}

@Composable
@Preview
private fun PickerItemPreview() {
    PickerItem(
        items = (0..100).map { it.toString() },
        state = rememberPickerState(),
        visibleItemsCount = 5,
        textStyle = MaterialTheme.typography.bodyLarge,
        textColor = Color.White,
        itemSpacing = 8.dp,
        onValueChange = {}
    )
}
