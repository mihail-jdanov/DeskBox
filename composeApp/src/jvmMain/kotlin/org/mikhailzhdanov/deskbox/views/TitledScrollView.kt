package org.mikhailzhdanov.deskbox.views

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TitledScrollView(
    title: String,
    scrollState: ScrollState,
    bottomContent: @Composable (() -> Unit)? = null,
    scrollableContent: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            Text(
                text = title,
                modifier = Modifier
                    .padding(8.dp),
                fontWeight = FontWeight.Medium
            )

            HorizontalDivider(modifier = Modifier.alpha(0.5f))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    scrollableContent()
                }

                VerticalScrollbar(
                    adapter = rememberScrollbarAdapter(scrollState),
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            bottomContent?.let { it() }
        }
    }
}