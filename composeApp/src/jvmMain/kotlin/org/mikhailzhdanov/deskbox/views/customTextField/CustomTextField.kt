package org.mikhailzhdanov.deskbox.views.customTextField

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.OutputTransformation
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxLength: Int = Int.MAX_VALUE,
    placeholder: String = "",
    enabled: Boolean = true,
    textStyle: TextStyle = TextStyle.Default.copy(fontSize = 16.sp),
    type: CustomTextFieldType = CustomTextFieldType.SingleLine,
    scrollState: ScrollState = rememberScrollState(),
    outputTransformation: OutputTransformation? = null
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
                val limitedText = if (newText.length > maxLength) {
                    state.edit {
                        replace(
                            start = 0,
                            end = state.text.length,
                            text = newText.take(maxLength)
                        )
                    }
                    newText.take(maxLength)
                } else {
                    newText
                }
                onValueChange(limitedText as String)
            }
    }

    Box {
        BasicTextField(
            state = state,
            modifier = modifier,
            enabled = enabled,
            textStyle = textStyle,
            lineLimits = if (type == CustomTextFieldType.SingleLine) {
                TextFieldLineLimits.SingleLine
            } else {
                TextFieldLineLimits.MultiLine()
            },
            outputTransformation = outputTransformation,
            decorator = TextFieldDecorator { innerTextField ->
                Box(contentAlignment = Alignment.TopStart) {
                    if (state.text.isEmpty()) {
                        Text(
                            text = placeholder,
                            modifier = Modifier.alpha(0.5f),
                            style = textStyle
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