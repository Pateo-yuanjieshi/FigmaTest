package com.porsche.model.hmisdk.v2.qrCode

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.porsche.model.hmisdk.PorscheAnimationStyle.DefaultLoadingRotateDuration
import com.porsche.model.hmisdk.R

@Composable
fun PorscheQRCode(
    content: String,
    size: Dp = 240.dp,
    icon: Bitmap? = null,
    showLoading: Boolean = false,
    foregroundColor: Color = Color.Black,
    backgroundColor: Color = Color.White,
    // 二维码的白变变距
    whiteSpaceMargin: Dp = 8.dp,
    // 二维码容错率
    errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.H
) {
    val fgColorInt = remember(foregroundColor) { foregroundColor.toArgb() }
    val bgColorInt = remember(backgroundColor) { backgroundColor.toArgb() }

    var qrCodeBitmap = remember(content,size,fgColorInt,bgColorInt) {
        generateQRCode(
            content = content,
            size = size,
            foregroundColor = fgColorInt,
            backgroundColor = bgColorInt,
            errorCorrectionLevel = errorCorrectionLevel
        )
    }

    if(icon != null && qrCodeBitmap != null){
        qrCodeBitmap = addIconToQRCode(qrCodeBitmap,icon)
    }

    if (showLoading) {
        val rotationState = remember { Animatable(0f) }

        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(dimensionResource(id = R.dimen.radius_medium)))
                .background(Color(0xCC000000)),
            contentAlignment = Alignment.Center
        ){
            Image(
                painter = painterResource(id = R.drawable.loading_spinner_medium),
                contentDescription = "Loading icon",
                modifier = Modifier
                    .rotate(rotationState.value)
                )

            // 启动一个无限循环的动画，使图片旋转
            LaunchedEffect(Unit) {
                rotationState.animateTo(
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        tween(DefaultLoadingRotateDuration, easing = LinearEasing),
                        RepeatMode.Restart
                    )
                )
            }
        }
    } else if(qrCodeBitmap != null){
        Box(
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_medium)))
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium))
                )
                .padding(whiteSpaceMargin)
        ) {
            Image(
                bitmap = qrCodeBitmap.asImageBitmap(),
                contentDescription = "QR Code",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

// 容错级别枚举（映射到 ZXing 的配置）
enum class ErrorCorrectionLevel(val zxingLevel: com.google.zxing.qrcode.decoder.ErrorCorrectionLevel) {
    L(com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.L),
    M(com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M),
    Q(com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.Q),
    H(com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.H)
}


fun generateQRCode(
    content: String,
    size: Dp,
    foregroundColor: Int,
    backgroundColor: Int,
    errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.H,
    margin: Int = 0,
): Bitmap? {
    val writer = QRCodeWriter()
    try {
        // 设置容错率
        val hints = HashMap<EncodeHintType, Any>()
        hints[EncodeHintType.ERROR_CORRECTION] = errorCorrectionLevel.zxingLevel

        // 设置字符集
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        hints[EncodeHintType.MARGIN] = margin
        // 生成二维码的位矩阵
        val bitMatrix: BitMatrix = writer.encode(content, BarcodeFormat.QR_CODE,  size.value.toInt(), size.value.toInt(), hints)
        val width = bitMatrix.width
        val height = bitMatrix.height

        // 将位矩阵转换为像素数组
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                pixels[y * width + x] = if (bitMatrix.get(x, y)) foregroundColor else backgroundColor
            }
        }

        // 创建Bitmap并设置像素
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun addIconToQRCode(qrCode: Bitmap, icon: Bitmap): Bitmap {
    val qrWidth = qrCode.width
    val qrHeight = qrCode.height
    val combined = Bitmap.createBitmap(qrWidth, qrHeight, qrCode.config)

    val canvas = Canvas(combined)
    canvas.drawBitmap(qrCode, 0f, 0f, null)

    // 绘制图标
    val iconWidth = 66
    val iconHeight = 66
    val scaledIcon = Bitmap.createScaledBitmap(icon, iconWidth, iconHeight, false)
    val left = (qrWidth - iconWidth) / 2
    val top = (qrHeight - iconHeight) / 2
    canvas.drawBitmap(scaledIcon, left.toFloat(), top.toFloat(), null)

    return combined
}
