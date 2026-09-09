package com.example.magtipidka.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
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
import androidx.compose.ui.zIndex
import com.example.magtipidka.domain.model.Category
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun Ios26CategoryFilterDropdown(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    var isMenuOpen by remember { mutableStateOf(false) }
    var hoveredCategoryId by remember { mutableStateOf<Long?>(null) }
    val itemBoundsMap = remember { mutableStateMapOf<Long?, Rect>() }
    var buttonWindowTopLeft by remember { mutableStateOf(Offset.Zero) }

    val selectedCategoryName = categories.find { it.id == selectedCategoryId }?.name ?: "Category Filter"

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                buttonWindowTopLeft = coordinates.boundsInWindow().topLeft
            }
            .pointerInput(categories) {
                awaitPointerEventScope {
                    while (true) {
                        val down = awaitFirstDown(requireUnconsumed = false)

                        // Check 150ms timeout: < 150ms = Single Tap, > 150ms = Hold & Drag
                        val gestureResult = withTimeoutOrNull(150L) {
                            while (true) {
                                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                val change = event.changes.firstOrNull() ?: break
                                if (!change.pressed) {
                                    return@withTimeoutOrNull "TAP"
                                }
                            }
                            "DRAG"
                        }

                        if (gestureResult == "TAP") {
                            // Quick Single Tap -> Toggle Menu Open/Close
                            isMenuOpen = !isMenuOpen
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        } else {
                            // Hold & Drag Mode (> 150ms)
                            val currentPointer = currentEvent.changes.firstOrNull()
                            if (currentPointer != null && currentPointer.pressed) {
                                currentPointer.consume()
                                isMenuOpen = true
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                                val initialPos = buttonWindowTopLeft + currentPointer.position
                                val initialMatch = itemBoundsMap.entries.firstOrNull { it.value.contains(initialPos) }?.key
                                if (initialMatch != null) {
                                    hoveredCategoryId = initialMatch
                                }

                                while (true) {
                                    val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                                    val change = event.changes.firstOrNull() ?: break

                                    if (!change.pressed) {
                                        // Release finger to filter
                                        onCategorySelected(hoveredCategoryId)
                                        isMenuOpen = false
                                        hoveredCategoryId = null
                                        break
                                    }

                                    change.consume()
                                    val touchWindowPos = buttonWindowTopLeft + change.position
                                    val matched = itemBoundsMap.entries.firstOrNull { it.value.contains(touchWindowPos) }?.key

                                    if (matched != null && matched != hoveredCategoryId) {
                                        hoveredCategoryId = matched
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                }
                            }
                        }
                    }
                }
            }
    ) {
        Column {
            // Main Category Filter Chip Button
            Surface(
                shape = CircleShape,
                color = if (selectedCategoryId != null || isMenuOpen) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.40f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = if (selectedCategoryId != null || isMenuOpen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = selectedCategoryName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedCategoryId != null || isMenuOpen) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Inline Glassmorphic Hover & Drop Selection Overlay
            AnimatedVisibility(
                visible = isMenuOpen,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .width(240.dp)
                    .padding(top = 6.dp)
                    .zIndex(20f)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                    shadowElevation = 10.dp
                ) {
                    Column(
                        modifier = Modifier
                            .padding(10.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "⚡ Tap or Drag over category:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 6.dp, bottom = 2.dp)
                        )

                        // Option 1: "All Categories" (null key)
                        val isAllHovered = hoveredCategoryId == null
                        val allScale by animateFloatAsState(targetValue = if (isAllHovered) 1.04f else 1.0f, label = "all_scale")

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isAllHovered) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(
                                width = if (isAllHovered) 2.dp else 1.dp,
                                color = if (isAllHovered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(allScale)
                                .clickable {
                                    onCategorySelected(null)
                                    isMenuOpen = false
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                .onGloballyPositioned { coordinates ->
                                    itemBoundsMap[null] = coordinates.boundsInWindow()
                                }
                        ) {
                            Text(
                                text = "All Categories",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isAllHovered) FontWeight.Bold else FontWeight.Medium,
                                color = if (isAllHovered) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }

                        // Options 2..N: Specific Categories
                        categories.forEach { category ->
                            val isHovered = hoveredCategoryId == category.id
                            val scale by animateFloatAsState(targetValue = if (isHovered) 1.04f else 1.0f, label = "cat_scale")

                            Surface(
                                shape = RoundedCornerShape(12.dp),
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
                                        isMenuOpen = false
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                    .onGloballyPositioned { coordinates ->
                                        itemBoundsMap[category.id] = coordinates.boundsInWindow()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CategoryIcon(
                                        iconName = category.iconName,
                                        contentDescription = null,
                                        size = 24.dp,
                                        iconSize = 14.dp,
                                        containerColor = if (isHovered) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.bodyMedium,
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
