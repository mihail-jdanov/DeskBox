package org.mikhailzhdanov.deskbox.views

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxLength: Int = Int.MAX_VALUE,
    placeholder: String = "",
    enabled: Boolean = true,
    fontSize: TextUnit = 16.sp,
    fontFamily: FontFamily = FontFamily.Default,
    leftAligned: Boolean = true,
    type: CustomTextFieldType = CustomTextFieldType.SingleLine,
    scrollState: ScrollState = rememberScrollState(),
    inputFilter: (String) -> String = { it }
) {
    val state = remember { TextFieldState(value) }

    LaunchedEffect(value) {
        if (state.text != value) {
            state.edit {
                replace(
                    start = 0,
                    end = state.text.length,
                    text = value
                )
            }
        }
    }

    LaunchedEffect(state) {
        snapshotFlow { state.text }
            .collect { newText ->
                var text = newText as String
                if (text.length > maxLength) {
                    text = text.take(maxLength)
                }
                text = inputFilter(text)
                if (text != newText) {
                    state.edit {
                        replace(
                            start = 0,
                            end = state.text.length,
                            text = text
                        )
                    }
                }
                onValueChange(text)
            }
    }

    Box {
        BasicTextField(
            state = state,
            modifier = modifier,
            enabled = enabled,
            textStyle = TextStyle.Default.copy(
                color = LocalContentColor.current,
                fontSize = fontSize,
                fontFamily = fontFamily,
                textAlign = if (leftAligned) TextAlign.Start else TextAlign.End
            ),
            lineLimits = if (type == CustomTextFieldType.SingleLine) {
                TextFieldLineLimits.SingleLine
            } else {
                TextFieldLineLimits.MultiLine()
            },
            cursorBrush = SolidColor(LocalContentColor.current),
            decorator = TextFieldDecorator { innerTextField ->
                Box(
                    contentAlignment = if (leftAligned) Alignment.TopStart else Alignment.TopEnd
                ) {
                    if (state.text.isEmpty()) {
                        Text(
                            text = placeholder,
                            modifier = Modifier.alpha(0.5f),
                            style = TextStyle.Default.copy(
                                fontSize = fontSize,
                                fontFamily = fontFamily
                            )
                        )
                    }
                    innerTextField()
                }
            },
            scrollState = scrollState
        )

        if (type == CustomTextFieldType.MultilineWithScrollbar) {
            VerticalScrollbar(
                adapter = rememberScrollbarAdapter(scrollState),
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

enum class CustomTextFieldType {
    SingleLine,
    Multiline,
    MultilineWithScrollbar
}