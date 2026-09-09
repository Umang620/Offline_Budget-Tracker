package com.example.magtipidka.presentation.components

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback

class DragToSelectState<Key> {
    val boundsMap = mutableStateMapOf<Key, Rect>()
    var currentHoveredKey by mutableStateOf<Key?>(null)
}

@Composable
fun <Key> rememberDragToSelectState(): DragToSelectState<Key> {
    return remember { DragToSelectState() }
}

@Composable
fun <Key> DragToSelectContainer(
    state: DragToSelectState<Key>,
    onItemSelected: (Key) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var containerWindowOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                containerWindowOffset = coordinates.boundsInWindow().topLeft
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { localOffset ->
                        val windowOffset = containerWindowOffset + localOffset
                        val matched = state.boundsMap.entries.firstOrNull { it.value.contains(windowOffset) }?.key
                        if (matched != null && matched != state.currentHoveredKey) {
                            state.currentHoveredKey = matched
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onItemSelected(matched)
                        }
                    },
                    onDrag = { change, _ ->
                        val windowOffset = containerWindowOffset + change.position
                        val matched = state.boundsMap.entries.firstOrNull { it.value.contains(windowOffset) }?.key
                        if (matched != null && matched != state.currentHoveredKey) {
                            state.currentHoveredKey = matched
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onItemSelected(matched)
                        }
                    },
                    onDragEnd = {
                        state.currentHoveredKey?.let { onItemSelected(it) }
                    },
                    onDragCancel = {
                        state.currentHoveredKey = null
                    }
                )
            }
    ) {
        content()
    }
}

@Composable
fun <Key> DragToSelectItem(
    key: Key,
    state: DragToSelectState<Key>,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.onGloballyPositioned { coordinates ->
            state.boundsMap[key] = coordinates.boundsInWindow()
        }
    ) {
        content()
    }
}
