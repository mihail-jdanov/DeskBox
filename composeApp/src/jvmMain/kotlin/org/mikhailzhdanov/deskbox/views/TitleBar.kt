package org.mikhailzhdanov.deskbox.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.FrameWindowScope
import org.mikhailzhdanov.deskbox.darkColorScheme
import java.awt.Frame

private val crossHoverColor = Color(0xFFC22B1D)

@Composable
fun FrameWindowScope.TitleBar(
    title: String,
    icon: Painter,
    closeAction: () -> Unit
) = WindowDraggableArea {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(MaterialTheme.colorScheme.surface),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = icon,
            contentDescription = null,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .size(16.dp)
        )

        Text(
            text = title,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp
        )

        WindowButton(
            onClick = {
                window.extendedState = Frame.ICONIFIED
            },
            hoverColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(Icons.Default.Remove, null)
        }

        WindowButton(
            onClick = closeAction,
            hoverColor = crossHoverColor
        ) {
            Icon(Icons.Default.Close, null)
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FrameWindowScope.WindowButton(
    onClick: () -> Unit,
    hoverColor: Color,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable () -> Unit
) {
    var hovered by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(32.dp)
            .background(if (hovered) hoverColor else Color.Transparent)
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
        val color = if (hovered && hoverColor == crossHoverColor) {
            darkColorScheme.onSurface
        } else {
            contentColor
        }

        CompositionLocalProvider(
            LocalContentColor provides color
        ) {
            content()
        }
    }
}