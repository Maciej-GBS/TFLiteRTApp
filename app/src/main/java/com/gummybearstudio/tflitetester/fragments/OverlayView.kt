package com.gummybearstudio.tflitetester.fragments

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.gummybearstudio.tflitetester.R
import com.gummybearstudio.tflitetester.backend.DetectedObject

class OverlayView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    private var results: List<DetectedObject> = listOf()
    private var boxPaint = Paint()
    private var textBackgroundPaint = Paint()
    private var textPaint = Paint()

    private var bounds = Rect()

    init {
        initPaints()
    }

    private fun clear() {
        textPaint.reset()
        textBackgroundPaint.reset()
        boxPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        textBackgroundPaint.color = Color.BLACK
        textBackgroundPaint.style = Paint.Style.FILL
        textBackgroundPaint.textSize = 50f

        textPaint.color = Color.WHITE
        textPaint.style = Paint.Style.FILL
        textPaint.textSize = 50f

        boxPaint.color = ContextCompat.getColor(context!!, R.color.red)
        boxPaint.strokeWidth = 8F
        boxPaint.style = Paint.Style.STROKE
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        for (result in results) {
            val bBox = result.box.project(width * 1f, height * 1f)
            val drawableRect = RectF(bBox.left, bBox.top, bBox.right, bBox.bottom)
            if (drawableRect.left > drawableRect.right || drawableRect.top > drawableRect.bottom) {
                throw IllegalStateException("Invalid bounding box: $bBox")
            }
            canvas.drawRect(drawableRect, boxPaint)

            val drawableText = String.format("%.2f", result.score)

            textBackgroundPaint.getTextBounds(drawableText, 0, drawableText.length, bounds)
            val textWidth = bounds.width()
            val textHeight = bounds.height()
            canvas.drawRect(
                bBox.left,
                bBox.top,
                bBox.left + textWidth + BOUNDING_RECT_TEXT_PADDING,
                bBox.top + textHeight + BOUNDING_RECT_TEXT_PADDING,
                textBackgroundPaint
            )

            canvas.drawText(drawableText, bBox.left, bBox.top + bounds.height(), textPaint)
        }
    }

    fun setResults(
        detectionResults: List<DetectedObject>
    ) {
        results = detectionResults
        // PreviewView is in FILL_START mode. So we need to scale up the bounding box to match with
        // the size that the captured images will be displayed.
        clear()
    }

    companion object {
        private const val BOUNDING_RECT_TEXT_PADDING = 8
    }
}