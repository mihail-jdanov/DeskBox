package org.mikhailzhdanov.deskbox.modules.control

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import deskbox.composeapp.generated.resources.Res
import deskbox.composeapp.generated.resources.arrow_drop_down
import deskbox.composeapp.generated.resources.arrow_drop_up
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.mikhailzhdanov.deskbox.extensions.success
import org.mikhailzhdanov.deskbox.extensions.warning
import org.mikhailzhdanov.deskbox.views.TitledScrollView
import org.mikhailzhdanov.deskbox.views.TitledView

private val logsScrollState = ScrollState(0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ControlScreen() {
    val viewModel = remember { ControlViewModel() }
    val state by viewModel.uiState.collectAsState()
    val logs by viewModel.logs.collectAsState()
    var startEnabled by remember { mutableStateOf(true) }

    if (!startEnabled) {
        LaunchedEffect(Unit) {
            delay(300)
            startEnabled = true
        }
    }

    if (state.isRunning) {
        LaunchedEffect(logs) {
            logsScrollState.scrollTo(logsScrollState.maxValue)
        }
    }

    Column {
        TitledView("Control") {
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (state.isRunning) {
                            viewModel.stop()
                        } else {
                            viewModel.start()
                        }
                        startEnabled = false
                    },
                    enabled = state.isStartAvailable && startEnabled,
                    colors = if (state.isRunning) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Text(if (state.isRunning) "Stop" else "Start")
                }

                ExposedDropdownMenuBox(
                    expanded = state.showProfileDropdown,
                    onExpandedChange = { viewModel.setShowProfileDropdown(it) },
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    FilledTonalButton(
                        onClick = { viewModel.setShowProfileDropdown(true) },
                        modifier = Modifier.width(260.dp),
                        enabled = state.profiles.isNotEmpty()
                    ) {
                        Text(
                            text = state.selectedProfile?.name ?: "No profile selected",
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Icon(
                            painter = painterResource(
                                if (state.showProfileDropdown) {
                                    Res.drawable.arrow_drop_up
                                } else {
                                    Res.drawable.arrow_drop_down
                                }
                            ),
                            contentDescription = null
                        )
                    }

                    ExposedDropdownMenu(
                        expanded = state.showProfileDropdown,
                        onDismissRequest = { viewModel.setShowProfileDropdown(false) }
                    ) {
                        state.profiles.forEach { profile ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = profile.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                onClick = {
                                    viewModel.setShowProfileDropdown(false)
                                    viewModel.selectProfile(profile)
                                },
                                trailingIcon = {
                                    if (state.selectedProfile?.id == profile.id) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = state.version,
                    modifier = Modifier
                        .padding(start = 8.dp),
                    color = if (state.isVersionError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        Color.Unspecified
                    },
                    textAlign = TextAlign.End
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TitledScrollView(
            title = "Logs",
            scrollState = logsScrollState
        ) {
            SelectionContainer {
                Column {
                    logs.forEach { line ->
                        key(line.id) {
                            Text(
                                text = parseAnsiToAnnotatedString(line.value),
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .fillMaxWidth(),
                                style = TextStyle.Default.copy(
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun parseAnsiToAnnotatedString(text: String): AnnotatedString {
    val defaultColor = LocalContentColor.current
    val regex = Regex("\u001B\\[[;\\d]*m")

    return buildAnnotatedString {
        var currentColor: Color? = null
        var lastIndex = 0

        regex.findAll(text).forEach { match ->
            val segment = text.substring(lastIndex, match.range.first)
            if (segment.isNotEmpty()) {
                withStyle(style = SpanStyle(color = currentColor ?: defaultColor)) {
                    append(segment)
                }
            }

            currentColor = when (match.value) {
                "\u001B[31m" -> MaterialTheme.colorScheme.error
                "\u001B[32m" -> Color.success
                "\u001B[33m" -> Color.warning
                else -> defaultColor
            }

            lastIndex = match.range.last + 1
        }

        val tail = text.substring(lastIndex)
        if (tail.isNotEmpty()) {
            withStyle(style = SpanStyle(color = currentColor ?: defaultColor)) {
                append(tail)
            }
        }
    }
}