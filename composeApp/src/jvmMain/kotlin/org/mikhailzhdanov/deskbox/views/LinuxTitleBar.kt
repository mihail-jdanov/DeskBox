package org.mikhailzhdanov.deskbox.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.FrameWindowScope
import java.awt.Frame

private val titleBarHeight = 32.dp
private val buttonsSize = 24.dp
private val buttonIconSize = 16.dp
private val buttonSpacing = (titleBarHeight - buttonsSize) / 2
private val leftSpacerWidth = (buttonsSize + buttonSpacing) * 2

@Composable
fun FrameWindowScope.LinuxTitleBar(
    title: String,
    closeAction: () -> Unit
) = WindowDraggableArea {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(titleBarHeight)
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(leftSpacerWidth))

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.padding(end = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            WindowButton(
                onClick = {
                    window.extendedState = Frame.ICONIFIED
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = null,
                    modifier = Modifier
                        .size(buttonIconSize)
                        .offset(y = 2.5.dp)
                )
            }

            WindowButton(
                onClick = closeAction
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(buttonIconSize)
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FrameWindowScope.WindowButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    var hovered by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(buttonsSize)
            .background(
                color = if (hovered) {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f)
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(50)
            )
            .onPointerEvent(PointerEventType.Move) { hovered = true }
            .onPointerEvent(PointerEventType.Exit) { hovered = false }
            .onPointerEvent(PointerEventType.Release) { hovered = false }
            .clickable(
                indication = null,
                interactionSource = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}