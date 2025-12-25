package org.mikhailzhdanov.deskbox.modules.profileQR

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.toPainter
import androidx.compose.ui.unit.dp
import org.jetbrains.skia.Image
import org.mikhailzhdanov.deskbox.Profile
import org.mikhailzhdanov.deskbox.views.TitledView
import qrcode.QRCode
import java.awt.Toolkit
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import java.awt.datatransfer.Transferable
import java.awt.image.BufferedImage
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

const val PROFILE_QR_SCREEN_ID = "ProfileQRScreen"

@Composable
fun ProfileQRScreen(
    profile: Profile,
    closeHandler: () -> Unit
) {
    Box(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        TitledView(
            title = profile.name,
            modifier = Modifier.width(250.dp)
        ) {
            val link = getSingBoxLink(profile)
            val imageBytes = QRCode
                .ofRoundedSquares()
                .withSize(20)
                .withMargin(10)
                .withColor(MaterialTheme.colorScheme.primary.toArgb())
                .withBackgroundColor(MaterialTheme.colorScheme.surfaceVariant.toArgb())
                .build(link).render().getBytes()
            val image = Image.makeFromEncoded(imageBytes).toComposeImageBitmap().toAwtImage()

            Column(modifier = Modifier.padding(top = 8.dp)) {
                Image(
                    painter = image.toPainter(),
                    contentDescription = null,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxWidth()
                )

                Button(
                    onClick = { copyImageToClipboard(image) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text("Copy QR code")
                }

                FilledTonalButton(
                    onClick = { copyTextToClipboard(link) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Copy link")
                }
            }
        }

        IconButton(
            onClick = closeHandler,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .size(24.dp)
        ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = null)
        }
    }


}

private fun getSingBoxLink(profile: Profile): String {
    return "sing-box://import-remote-profile?url=${profile.remoteURL}#${encodeUrlComponent(profile.name)}"
}

private fun encodeUrlComponent(input: String): String {
    return URLEncoder
        .encode(input, StandardCharsets.UTF_8.toString())
        .replace("+", "%20")
}

private fun copyImageToClipboard(image: BufferedImage) {
    val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val transferable = object : Transferable {
        override fun getTransferDataFlavors(): Array<DataFlavor> =
            arrayOf(DataFlavor.imageFlavor)

        override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean =
            flavor == DataFlavor.imageFlavor

        override fun getTransferData(flavor: DataFlavor?): Any =
            image
    }
    clipboard.setContents(transferable, null)
}

private fun copyTextToClipboard(text: String) {
    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
    val selection = StringSelection(text)
    clipboard.setContents(selection, null)
}