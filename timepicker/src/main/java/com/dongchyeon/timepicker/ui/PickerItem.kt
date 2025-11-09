package com.dongchyeon.timepicker.ui

import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import com.dongchyeon.timepicker.CurveEffect
import com.dongchyeon.timepicker.PickerStyle
import com.dongchyeon.timepicker.TimePickerDefaults
import com.dongchyeon.timepicker.model.PickerState
import com.dongchyeon.timepicker.model.rememberPickerState
import com.dongchyeon.timepicker.ui.util.toPx
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlin.math.abs

@Composable
internal fun <T> PickerItem(
    modifier: Modifier = Modifier,
    items: List<T>,
    state: PickerState<T> = rememberPickerState(items = items),
    visibleItemsCount: Int,
    style: PickerStyle,
    textModifier: Modifier = Modifier,
    itemFormatter: (T) -> String = { it.toString() },
    infiniteScroll: Boolean,
    curveEffect: CurveEffect,
    onValueChange: (T) -> Unit
) {
    val visibleItemsMiddle = visibleItemsCount / 2
    val listScrollCount = if (infiniteScroll) Int.MAX_VALUE else items.size + visibleItemsMiddle * 2
    val listScrollMiddle = listScrollCount / 2

    val listState = state.lazyListState
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    var itemHeightPixels by remember { mutableIntStateOf(0) }
    val itemHeightDp = with(LocalDensity.current) { itemHeightPixels.toDp() }

    LaunchedEffect(state.initialIndex) {
        val safeStartIndex = state.initialIndex
        val listStartIndex = if (infiniteScroll) {
            getStartIndexForInfiniteScroll(itemHeightPixels, listScrollMiddle, visibleItemsMiddle, safeStartIndex)
        } else {
            safeStartIndex
        }
        listState.scrollToItem(listStartIndex, 0)

        if (!infiniteScroll) {
            val selectedItem = items.getOrNull(listStartIndex) ?: items.first()
            if (listStartIndex != state.selectedIndex.value) {
                state.updateSelectedIndex(listStartIndex)
            }
            onValueChange(selectedItem)
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
            .map { centerIndex ->
                centerIndex?.let { index ->
                    if (infiniteScroll) {
                        index % items.size
                    } else {
                        (index - visibleItemsMiddle).coerceIn(0, items.size - 1)
                    }
                }
            }
            .distinctUntilChanged()
            .collect { adjustedIndex ->
                if (adjustedIndex != null && adjustedIndex != state.selectedIndex.value) {
                    state.updateSelectedIndex(adjustedIndex)
                    onValueChange(items[adjustedIndex])
                }
            }
    }

    val totalItemHeight = itemHeightDp + style.itemSpacing
    val totalItemHeightPx = totalItemHeight.toPx()

    val layoutInfo by rememberUpdatedState(listState.layoutInfo)

    val itemInfoMap = remember(layoutInfo) {
        layoutInfo.visibleItemsInfo.associateBy { it.index }
    }

    val viewportCenterOffset = layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2

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
                val item = getItemForIndex(
                    index = index,
                    items = items,
                    infiniteScroll = infiniteScroll,
                    visibleItemsMiddle = visibleItemsMiddle
                )

                Text(
                    text = item?.let { itemFormatter(it) } ?: "",
                    maxLines = 1,
                    style = style.textStyle,
                    color = style.textColor,
                    modifier = Modifier
                        .padding(vertical = style.itemSpacing / 2)
                        .curvedPickerEffect(
                            index = index,
                            viewportCenterOffset = viewportCenterOffset,
                            itemInfoMap = itemInfoMap,
                            totalItemHeightPx = totalItemHeightPx,
                            visibleItemsMiddle = visibleItemsMiddle,
                            curveEffect = curveEffect
                        ).onSizeChanged { size -> itemHeightPixels = size.height }
                        .then(textModifier)
                )
            }
        }
    }
}

private fun <T> getItemForIndex(
    index: Int,
    items: List<T>,
    infiniteScroll: Boolean,
    visibleItemsMiddle: Int
): T? {
    require(items.isNotEmpty()) { "Items list cannot be empty." }

    return if (!infiniteScroll) {
        items.getOrNull(index - visibleItemsMiddle)
    } else {
        items.getOrNull(index % items.size)
    }
}

private fun getStartIndexForInfiniteScroll(
    itemSize: Int,
    listScrollMiddle: Int,
    visibleItemsMiddle: Int,
    startIndex: Int
): Int {
    if (itemSize == 0) {
        return listScrollMiddle - visibleItemsMiddle + startIndex
    }

    return listScrollMiddle - listScrollMiddle % itemSize - visibleItemsMiddle + startIndex
}

fun Modifier.curvedPickerEffect(
    index: Int,
    viewportCenterOffset: Int,
    itemInfoMap: Map<Int, LazyListItemInfo>,
    totalItemHeightPx: Float,
    visibleItemsMiddle: Int,
    curveEffect: CurveEffect
): Modifier = graphicsLayer {
    val itemInfo = itemInfoMap[index]
    val itemCenterOffset = itemInfo?.let { it.offset + (it.size / 2) } ?: 0

    val distanceFromCenter = abs(viewportCenterOffset - itemCenterOffset).toFloat()
    val maxDistance = totalItemHeightPx * visibleItemsMiddle

    val alpha = curveEffect.calculateAlpha(distanceFromCenter, maxDistance)
    val scaleY = curveEffect.calculateScaleY(distanceFromCenter, maxDistance)

    this.alpha = alpha
    this.scaleY = scaleY
}

@Composable
@Preview
private fun PickerItemPreview() {
    PickerItem<Int>(
        items = (0..100).map { it },
        visibleItemsCount = 5,
        style = TimePickerDefaults.pickerStyle(),
        curveEffect = TimePickerDefaults.curveEffect(),
        infiniteScroll = true,
        onValueChange = {}
    )
}
