package org.mikhailzhdanov.deskbox.tools

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import java.awt.image.BufferedImage

object QRGenerator {

    fun generateQrCode(
        text: String,
        width: Int = 512,
        height: Int = 512
    ): BufferedImage {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints)
        return MatrixToImageWriter.toBufferedImage(bitMatrix)
    }

}