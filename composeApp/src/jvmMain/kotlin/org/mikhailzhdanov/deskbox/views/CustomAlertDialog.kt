package org.mikhailzhdanov.deskbox.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.mikhailzhdanov.deskbox.managers.AlertData

@Composable
fun CustomAlertDialog(
    onDismissRequest: () -> Unit,
    data: AlertData
) {
    CustomDialog(
        onDismissRequest = {
            onDismissRequest()
            data.cancelButtonData?.handler()
        }
    ) {
        Surface(
            modifier = Modifier
                .width(if (data.text.length >= 100) 450.dp else 350.dp)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(data.text)

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))

                    data.cancelButtonData?.let { cancelButtonData ->
                        TextButton(
                            onClick = {
                                onDismissRequest()
                                cancelButtonData.handler()
                            }
                        ) {
                            Text(cancelButtonData.title)
                        }
                    }

                    TextButton(
                        onClick = {
                            onDismissRequest()
                            data.confirmButtonData.handler()
                        }
                    ) {
                        Text(data.confirmButtonData.title)
                    }
                }
            }
        }
    }
}