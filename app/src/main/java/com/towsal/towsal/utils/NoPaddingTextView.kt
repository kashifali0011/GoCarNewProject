package com.towsal.towsal.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

/**
* Custom text view class
* */
class NoPaddingTextView : AppCompatTextView {
    private val boundsRect = Rect()
    private val textParams = calculateTextParams()

    constructor(context: Context)
            : super(context)

    constructor(context: Context, attrs: AttributeSet?)
            : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr)

    override fun onDraw(canvas: Canvas) {
        with(boundsRect) {
            paint.isAntiAlias = true
            paint.color = currentTextColor
            canvas.drawText(
                textParams,
                -left.toFloat(),
                (-top - bottom).toFloat(),
                paint
            )
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        calculateTextParams()
        setMeasuredDimension(boundsRect.width() + 1, -boundsRect.top + 1)
    }

    private fun calculateTextParams(): String {
        return text.toString()
            .also { text ->
                text.length.let { textLength ->
                    paint.textSize = textSize
                    paint.getTextBounds(text, 0, textLength, boundsRect)
                    if (textLength == 0) boundsRect.right = boundsRect.left
                }
            }
    }
}