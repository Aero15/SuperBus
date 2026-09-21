package xyz.doocode.superbus.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Rect
import android.graphics.RectF

fun createMarkerBitmap(context: Context, color: Int, sizeDp: Float = 28f): Bitmap {
    val density = context.resources.displayMetrics.density
    val size = (sizeDp * density).toInt().coerceAtLeast(24)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }
    val radius = size * 0.45f
    canvas.drawCircle(size / 2f, size / 2f, radius, paint)

    val border = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        this.color = Color.WHITE
        strokeWidth = density * 2f
    }
    canvas.drawCircle(size / 2f, size / 2f, radius - border.strokeWidth / 2f, border)
    return bitmap
}

fun createTextMarkerBitmap(
    context: Context,
    text: String,
    backgroundColor: Int,
    textColor: Int = Color.WHITE,
    sizeDp: Float = 30f
): Bitmap {
    val density = context.resources.displayMetrics.density
    val baseSize = (sizeDp * density).toInt().coerceAtLeast(28)

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = textColor
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
        textSize = when {
            text.length <= 2 -> baseSize * 0.46f
            text.length == 3 -> baseSize * 0.38f
            else -> baseSize * 0.32f
        }
    }

    val textWidth = textPaint.measureText(text)
    val horizontalPadding = 8f * density
    val totalWidth = maxOf(baseSize.toFloat(), textWidth + horizontalPadding * 2).toInt()
    val totalHeight = baseSize

    val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = backgroundColor
    }

    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = density * 2f
    }

    val strokeInset = borderPaint.strokeWidth / 2f
    val cornerRadius = totalHeight / 2f
    val rect = RectF(
        strokeInset,
        strokeInset,
        totalWidth - strokeInset,
        totalHeight - strokeInset
    )

    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, bgPaint)
    canvas.drawRoundRect(rect, cornerRadius, cornerRadius, borderPaint)

    val textBounds = Rect()
    textPaint.getTextBounds(text, 0, text.length, textBounds)
    val x = totalWidth / 2f
    val y = (totalHeight / 2f) - textBounds.exactCenterY()
    canvas.drawText(text, x, y, textPaint)

    return bitmap
}

fun createUserLocationMarkerBitmap(
    context: Context,
    progress: Float = 0f,
    sizeDp: Float = 44f
): Bitmap {
    val density = context.resources.displayMetrics.density
    val size = (sizeDp * density).toInt().coerceAtLeast(36)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val center = size / 2f
    val clampedProgress = progress.coerceIn(0f, 1f)

    // Halo animation: scale between 0.65 and 1.0 of max radius, alpha between 0.15 and 0.45
    val maxHaloRadius = size * 0.46f
    val haloRadius = maxHaloRadius * (0.65f + 0.35f * clampedProgress)
    val haloAlpha = (25 + (85 * clampedProgress)).toInt().coerceIn(0, 255)

    // Color transition from vibrant blue (#2979FF) to dark slate/black (#1A1A1A)
    val blueR = 0x29
    val blueG = 0x79
    val blueB = 0xFF

    val darkR = 0x12
    val darkG = 0x12
    val darkB = 0x12

    val coreR = (blueR + (darkR - blueR) * clampedProgress).toInt().coerceIn(0, 255)
    val coreG = (blueG + (darkG - blueG) * clampedProgress).toInt().coerceIn(0, 255)
    val coreB = (blueB + (darkB - blueB) * clampedProgress).toInt().coerceIn(0, 255)

    val haloColor = Color.argb(haloAlpha, coreR, coreG, coreB)
    val coreColor = Color.rgb(coreR, coreG, coreB)

    // Outer accuracy halo
    val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = haloColor
    }
    canvas.drawCircle(center, center, haloRadius, haloPaint)

    // Core circle
    val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = coreColor
    }
    val coreRadius = size * 0.22f
    canvas.drawCircle(center, center, coreRadius, corePaint)

    // White border around core
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = density * 2f
    }
    canvas.drawCircle(center, center, coreRadius, borderPaint)

    return bitmap
}
