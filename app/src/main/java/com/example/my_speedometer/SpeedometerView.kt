package com.example.my_speedometer

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sin

class SpeedometerView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
) : View(context, attributeSet) {
    private companion object {
        const val CURRENT_SPEED_KEY = "com.speedometer_view.current_speed"
        const val SUPER_STATE = "com.speedometer_view.super_state"
        const val BORDER_WIDTH = 10f
        const val DIAMETER_DIGITS_RATIO = 0.73f
        const val DIAL_INTERVAL = 20
        const val DIVISION_DIAL_INTERVAL = 10
        const val ARROW_LENGTH_RATIO = 0.35f
        const val LARGE_DIVISION_RATIO = 0.85f
        const val SMALL_DIVISION_RATIO = 0.92f
    }

    var currentSpeed = 0f
        set(value) {
            field = value

            invalidate()
        }

    var maxSpeed = 0
        set(value) {
            field = value

            invalidate()
        }

    var colorArrow = Color.BLUE
        set(value) {
            field = value

            invalidate()
        }

    private var size = 0
    private var centerX = 0f
    private var centerY = 0f
    private var digitsTextSize = 0f
    private var radius = 0f

    private val circle = Path()
    private val line = Path()
    private val centerLine = Path()
    private var generalPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var digitsPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var myBackgroundColor = Color.DKGRAY
    private var myBorderColor = Color.RED
    private var myDigitsColor = Color.WHITE

    init {
        val typedArray = context.obtainStyledAttributes(
            attributeSet,
            R.styleable.SpeedometerView,
        )

        try {
            currentSpeed = typedArray.getFloat(R.styleable.SpeedometerView_currentSpeed, 0f)
            maxSpeed = typedArray.getInt(R.styleable.SpeedometerView_maxSpeed, 220)
            size = typedArray.getInt(R.styleable.SpeedometerView_size, 1000)
            digitsTextSize =
                typedArray.getFloat(R.styleable.SpeedometerView_digitsTextSize, size / 20f)
            myBackgroundColor =
                typedArray.getInt(R.styleable.SpeedometerView_myBackgroundColor, myBackgroundColor)
            myBorderColor = typedArray.getInt(R.styleable.SpeedometerView_myBorderColor, myBorderColor)
            myDigitsColor = typedArray.getInt(R.styleable.SpeedometerView_myDigitsColor, myDigitsColor)
        } finally {
            typedArray.recycle()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = measureDimension(size, widthMeasureSpec)
        val height = measureDimension(size, heightMeasureSpec)

        size = min(width, height)

        centerX = size / 2f
        centerY = size / 2f
        radius = size / 2f

        setMeasuredDimension(size, size)
    }

    private fun measureDimension(minSize: Int, measureSpec: Int): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)

        return when (specMode) {
            MeasureSpec.EXACTLY -> specSize
            MeasureSpec.AT_MOST -> minSize.coerceAtMost(specSize)
            else -> minSize
        }
    }

    override fun onSaveInstanceState(): Parcelable? =
        Bundle().apply {
            putFloat(CURRENT_SPEED_KEY, currentSpeed)
            putParcelable(SUPER_STATE, super.onSaveInstanceState())
        }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var superState = state

        if (state is Bundle) {
            this.currentSpeed = state.getFloat(CURRENT_SPEED_KEY)
            superState = state.getParcelable(SUPER_STATE)
        }

        super.onRestoreInstanceState(superState)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        paintBackground(canvas)
        paintBackgroundBorder(canvas)
        paintDial(canvas)
        paintDivisionDial(canvas)
        paintArrow(canvas)
    }

    private fun paintBackground(canvas: Canvas) {
        generalPaint.color = myBackgroundColor
        generalPaint.style = Paint.Style.FILL

        canvas.drawCircle(centerX, centerY, radius, generalPaint)
    }

    private fun paintBackgroundBorder(canvas: Canvas) {
        generalPaint.color = myBorderColor
        generalPaint.style = Paint.Style.STROKE
        generalPaint.strokeWidth = BORDER_WIDTH

        canvas.drawCircle(centerX, centerY, radius - BORDER_WIDTH / 2f, generalPaint)
    }

    private fun paintDial(canvas: Canvas) {
        canvas.save()
        canvas.rotate(135f, centerX, centerY)

        digitsPaint.color = myDigitsColor
        digitsPaint.style = Paint.Style.FILL_AND_STROKE
        digitsPaint.strokeWidth = 2f
        digitsPaint.textSize = digitsTextSize
        digitsPaint.setShadowLayer(6f, 0f, 0f, Color.BLUE)

        val theeForthCircumference = radius * DIAMETER_DIGITS_RATIO * Math.PI * 1.5

        for (i in 0..maxSpeed step DIAL_INTERVAL) {
            val digitTextLength = round(digitsPaint.measureText(i.toString()))

            circle.addCircle(centerX, centerY, radius * DIAMETER_DIGITS_RATIO, Path.Direction.CW)

            canvas.drawTextOnPath(
                i.toString(),
                circle,
                ((i * theeForthCircumference / maxSpeed) - digitTextLength / 2).toFloat(),
                0f,
                digitsPaint
            )
        }

        canvas.restore()
    }

    private fun paintDivisionDial(canvas: Canvas) {
        canvas.save()
        canvas.rotate(135f, centerX, centerY)

        generalPaint.strokeWidth = 10f
        generalPaint.color = Color.WHITE
        generalPaint.style = Paint.Style.FILL_AND_STROKE

        val angleBetweenNumbers = Math.PI * 1.5 / maxSpeed

        for (i in 0..maxSpeed step DIVISION_DIAL_INTERVAL) {
            val angle = i * angleBetweenNumbers

            val x1 = (radius - BORDER_WIDTH) * (cos(angle)).toFloat()
            val y1 = (radius - BORDER_WIDTH) * (sin(angle)).toFloat()

            val scale = if (i % DIAL_INTERVAL == 0) {
                LARGE_DIVISION_RATIO
            } else {
                SMALL_DIVISION_RATIO
            }

            line.moveTo(x1, y1)
            line.lineTo(x1 * scale, y1 * scale)
        }

        line.offset(centerX, centerY, centerLine)

        canvas.drawPath(centerLine, generalPaint)

        canvas.restore()
    }

    private fun paintArrow(canvas: Canvas) {
        canvas.save()

        val limit = (currentSpeed / maxSpeed.toFloat() * 270 - 90)

        canvas.rotate(limit, centerX, centerY)

        arrowPaint.color = colorArrow
        arrowPaint.strokeWidth = digitsTextSize / 4f
        arrowPaint.style = Paint.Style.FILL_AND_STROKE
        arrowPaint.setShadowLayer(5f, 0f, 0f, Color.WHITE)

        canvas.drawLine(
            radius * ARROW_LENGTH_RATIO,
            radius * ARROW_LENGTH_RATIO,
            radius,
            radius,
            arrowPaint
        )

        arrowPaint.color = Color.BLUE
        canvas.drawCircle(centerX, centerY, radius / 10, arrowPaint)

        arrowPaint.color = Color.BLACK
        canvas.drawCircle(centerX, centerY, radius / 40, arrowPaint)

        canvas.restore()
    }
}