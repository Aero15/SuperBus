package xyz.doocode.superbus.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color

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

fun createUserLocationMarkerBitmap(context: Context, sizeDp: Float = 32f): Bitmap {
    val density = context.resources.displayMetrics.density
    val size = (sizeDp * density).toInt().coerceAtLeast(32)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Outer accuracy halo
    val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#4D2979FF")
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, haloPaint)

    // Blue core
    val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2979FF")
    }
    val coreRadius = size * 0.35f
    canvas.drawCircle(size / 2f, size / 2f, coreRadius, corePaint)

    // White border around blue core
    val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = density * 2f
    }
    canvas.drawCircle(size / 2f, size / 2f, coreRadius, borderPaint)

    return bitmap
}
