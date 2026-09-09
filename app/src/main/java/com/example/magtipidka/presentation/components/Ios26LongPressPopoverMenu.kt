package com.example.magtipidka.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.magtipidka.domain.model.Category

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Ios26CategoryLongPressSelector(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isPopoverActive by remember { mutableStateOf(false) }
    var hoveredCategoryId by remember { mutableStateOf<Long?>(null) }
    val itemBoundsMap = remember { mutableStateMapOf<Long, Rect>() }
    var containerWindowTopLeft by remember { mutableStateOf(Offset.Zero) }

    val selectedCategory = categories.find { it.id == selectedCategoryId } ?: categories.firstOrNull()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                containerWindowTopLeft = coordinates.boundsInWindow().topLeft
            }
            .pointerInput(categories) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { localOffset ->
                        isPopoverActive = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val touchWindowPos = containerWindowTopLeft + localOffset
                        val matched = itemBoundsMap.entries.firstOrNull { it.value.contains(touchWindowPos) }?.key
                        if (matched != null) {
                            hoveredCategoryId = matched
                        }
                    },
                    onDrag = { change, _ ->
                        val touchWindowPos = containerWindowTopLeft + change.position
                        val matched = itemBoundsMap.entries.firstOrNull { it.value.contains(touchWindowPos) }?.key
                        if (matched != null && matched != hoveredCategoryId) {
                            hoveredCategoryId = matched
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    },
                    onDragEnd = {
                        hoveredCategoryId?.let { onCategorySelected(it) }
                        isPopoverActive = false
                        hoveredCategoryId = null
                    },
                    onDragCancel = {
                        isPopoverActive = false
                        hoveredCategoryId = null
                    }
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Main Category Selector Bar (Combined Clickable: 1-Tap Click OR Long Press Drag)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
                shadowElevation = 0.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .combinedClickable(
                        onClick = {
                            isPopoverActive = !isPopoverActive
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        },
                        onLongClick = {
                            isPopoverActive = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        selectedCategory?.let { cat ->
                            CategoryIcon(
                                iconName = cat.iconName,
                                contentDescription = null,
                                size = 32.dp,
                                iconSize = 18.dp,
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Tap or Hold",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Inline Glassmorphic Popover Overlay Menu
            AnimatedVisibility(
                visible = isPopoverActive,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .zIndex(10f)
            ) {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "⚡ Tap or Drag over category:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                        )

                        categories.forEach { category ->
                            val isHovered = hoveredCategoryId == category.id || (hoveredCategoryId == null && selectedCategoryId == category.id)
                            val scale by animateFloatAsState(
                                targetValue = if (isHovered) 1.03f else 1.0f,
                                label = "scale_anim"
                            )

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isHovered) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(
                                    width = if (isHovered) 2.dp else 1.dp,
                                    color = if (isHovered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .scale(scale)
                                    .clickable {
                                        onCategorySelected(category.id)
                                        isPopoverActive = false
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    .onGloballyPositioned { coordinates ->
                                        itemBoundsMap[category.id] = coordinates.boundsInWindow()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CategoryIcon(
                                        iconName = category.iconName,
                                        contentDescription = null,
                                        size = 28.dp,
                                        iconSize = 16.dp,
                                        containerColor = if (isHovered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isHovered) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isHovered) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
