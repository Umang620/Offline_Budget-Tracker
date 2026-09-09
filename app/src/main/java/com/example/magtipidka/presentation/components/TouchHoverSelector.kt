package com.example.magtipidka.presentation.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <Key> TouchHoverSelectorGrid(
    items: List<Key>,
    selectedKey: Key?,
    onKeySelected: (Key) -> Unit,
    getKeyLabel: (Key) -> String,
    modifier: Modifier = Modifier,
    getKeyIcon: @Composable ((Key) -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    var hoveredKey by remember { mutableStateOf<Key?>(null) }
    val boundsMap = remember { mutableStateMapOf<Key, Rect>() }
    var containerWindowOffset by remember { mutableStateOf(Offset.Zero) }

    val activeKey = hoveredKey ?: selectedKey

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                containerWindowOffset = coordinates.boundsInWindow().topLeft
            }
            .pointerInput(items) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown(requireUnconsumed = false)

                        // Match initial touch down
                        val initialPos = containerWindowOffset + down.position
                        val initialMatch = boundsMap.entries.firstOrNull { it.value.contains(initialPos) }?.key
                        if (initialMatch != null) {
                            hoveredKey = initialMatch
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onKeySelected(initialMatch)
                        }

                        // Drag loop across scattered chips
                        while (true) {
                            val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                            val currentPointer = event.changes.firstOrNull() ?: break

                            if (!currentPointer.pressed) {
                                // Finger lifted / dropped!
                                hoveredKey?.let { onKeySelected(it) }
                                hoveredKey = null
                                break
                            }

                            currentPointer.consume()
                            val touchWindowPos = containerWindowOffset + currentPointer.position
                            val matched = boundsMap.entries.firstOrNull { it.value.contains(touchWindowPos) }?.key

                            if (matched != null && matched != hoveredKey) {
                                hoveredKey = matched
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onKeySelected(matched)
                            }
                        }
                    }
                }
            }
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items.forEach { key ->
                val isSelected = activeKey == key
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.08f else 1.0f,
                    label = "scale_anim"
                )

                Surface(
                    shape = CircleShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier
                        .scale(scale)
                        .onGloballyPositioned { coordinates ->
                            boundsMap[key] = coordinates.boundsInWindow()
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        getKeyIcon?.let { icon ->
                            icon(key)
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = getKeyLabel(key),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
