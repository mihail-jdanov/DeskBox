package org.mikhailzhdanov.deskbox.modules.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import deskbox.composeapp.generated.resources.Res
import deskbox.composeapp.generated.resources.app_icon
import org.jetbrains.compose.resources.painterResource
import org.mikhailzhdanov.deskbox.views.TitledView
import java.awt.Desktop
import java.net.URI

@Composable
fun AboutScreen() {
    TitledView("About") {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(Res.drawable.app_icon),
                    contentDescription = null,
                    modifier = Modifier.size(128.dp)
                )

                Text(
                    text = "DeskBox",
                    modifier = Modifier.padding(top = 16.dp),
                    fontWeight = FontWeight.Medium
                )

                Text(text = System.getProperty("jpackage.app-version") ?: "")

                FilledTonalButton(
                    onClick = {
                        Desktop.getDesktop().browse(URI("https://github.com/mihail-jdanov/DeskBox"))
                    },
                    modifier = Modifier.padding(top = 48.dp)
                ) {
                    Text("Source code on GitHub")
                }
            }
        }
    }
}