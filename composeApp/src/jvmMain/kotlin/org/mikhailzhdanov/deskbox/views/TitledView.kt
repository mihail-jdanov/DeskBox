package org.mikhailzhdanov.deskbox.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TitledView(
    title: String,
    content: @Composable () -> Unit
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
                modifier = Modifier.padding(8.dp)
            ) {
                content()
            }
        }
    }
}