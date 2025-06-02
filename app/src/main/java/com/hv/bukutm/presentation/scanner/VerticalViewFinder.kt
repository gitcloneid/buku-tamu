package com.hv.bukutm.presentation.scanner

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import com.journeyapps.barcodescanner.ViewfinderView
import kotlin.math.min

class VerticalViewfinderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewfinderView(context, attrs) {

    private val borderPaint = Paint().apply {
        color = 0xFF3F51B5.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 4f // Diubah dari 8f menjadi 4f
        isAntiAlias = true
    }

    private val cornerPaint = Paint().apply {
        color = 0xFF3F51B5.toInt()
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val cornerSize = 24 // Diubah dari 32 menjadi 24
    private val cornerWidth = 4 // Diubah dari 8 menjadi 4

    override fun onDraw(canvas: Canvas) {
        val frame = framingRect ?: return
        val width = canvas.width
        val height = canvas.height

        // Draw overlay
        canvas.drawRect(0f, 0f, width.toFloat(), frame.top.toFloat(), paint)
        canvas.drawRect(0f, frame.top.toFloat(), frame.left.toFloat(), frame.bottom.toFloat(), paint)
        canvas.drawRect(frame.right.toFloat(), frame.top.toFloat(), width.toFloat(), frame.bottom.toFloat(), paint)
        canvas.drawRect(0f, frame.bottom.toFloat(), width.toFloat(), height.toFloat(), paint)

        // Draw border
        canvas.drawRect(
            frame.left.toFloat(),
            frame.top.toFloat(),
            frame.right.toFloat(),
            frame.bottom.toFloat(),
            borderPaint
        )

        // Draw corners
        drawCorners(canvas, frame)

        postInvalidateDelayed(16, frame.left, frame.top, frame.right, frame.bottom)
    }

    private fun drawCorners(canvas: Canvas, frame: Rect) {
        // Top-left
        canvas.drawRect(
            frame.left.toFloat(),
            frame.top.toFloat(),
            (frame.left + cornerSize).toFloat(),
            (frame.top + cornerWidth).toFloat(),
            cornerPaint
        )
        canvas.drawRect(
            frame.left.toFloat(),
            frame.top.toFloat(),
            (frame.left + cornerWidth).toFloat(),
            (frame.top + cornerSize).toFloat(),
            cornerPaint
        )

        // Top-right
        canvas.drawRect(
            (frame.right - cornerSize).toFloat(),
            frame.top.toFloat(),
            frame.right.toFloat(),
            (frame.top + cornerWidth).toFloat(),
            cornerPaint
        )
        canvas.drawRect(
            (frame.right - cornerWidth).toFloat(),
            frame.top.toFloat(),
            frame.right.toFloat(),
            (frame.top + cornerSize).toFloat(),
            cornerPaint
        )

        // Bottom-left
        canvas.drawRect(
            frame.left.toFloat(),
            (frame.bottom - cornerWidth).toFloat(),
            (frame.left + cornerSize).toFloat(),
            frame.bottom.toFloat(),
            cornerPaint
        )
        canvas.drawRect(
            frame.left.toFloat(),
            (frame.bottom - cornerSize).toFloat(),
            (frame.left + cornerWidth).toFloat(),
            frame.bottom.toFloat(),
            cornerPaint
        )

        // Bottom-right
        canvas.drawRect(
            (frame.right - cornerSize).toFloat(),
            (frame.bottom - cornerWidth).toFloat(),
            frame.right.toFloat(),
            frame.bottom.toFloat(),
            cornerPaint
        )
        canvas.drawRect(
            (frame.right - cornerWidth).toFloat(),
            (frame.bottom - cornerSize).toFloat(),
            frame.right.toFloat(),
            frame.bottom.toFloat(),
            cornerPaint
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Use a square viewfinder
        val size = (min(w, h) * 0.75).toInt()
        framingRect = Rect(
            (w - size) / 2,
            (h - size) / 2,
            (w + size) / 2,
            (h + size) / 2
        )
        invalidate()
    }
}

