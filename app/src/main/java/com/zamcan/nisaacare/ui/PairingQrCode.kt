package com.zamcan.nisaacare.ui

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.QRCodeWriter

/**
 * Renders a pairing QR code locally.
 *
 * The code is generated on the device. Nothing about the pairing is sent to a
 * server to produce the image, which matters because the QR carries a
 * short-lived pairing credential.
 *
 * Only the ZXing core encoder is used. It is a plain Java library with no
 * Android dependency, so the app gains no AndroidX surface and no camera
 * permission for a feature that only draws an image.
 */
object PairingQrCode {

    /**
     * Encodes [content] as a QR bitmap.
     *
     * @param content the payload to encode, normally a pairing URI.
     * @param size the width and height in pixels.
     */
    fun create(content: String, size: Int = 720): Bitmap {
        val hints = mapOf(
            // Level M tolerates a small amount of damage, which matters for a
            // code that may be read from a phone screen at an angle.
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 2,
            EncodeHintType.CHARACTER_SET to "UTF-8"
        )
        val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
        return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888).apply {
            for (x in 0 until size) {
                for (y in 0 until size) {
                    setPixel(x, y, if (matrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
        }
    }
}
