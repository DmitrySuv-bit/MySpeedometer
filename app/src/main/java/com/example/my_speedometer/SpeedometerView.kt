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
    private var borderWidth = 10f
    private var digitsTextSize = 0f
    private var radius = 0f

    private val circle = Path()
    private val line = Path()
    private val matrix1 = Matrix()
    private var generalPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var arrowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var digitsPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var colorBackground = Color.DKGRAY
    private var colorBorder = Color.RED
    private var colorDigits = Color.WHITE


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
            colorBackground =
                typedArray.getInt(R.styleable.SpeedometerView_colorBackground, colorBackground)
            colorBorder = typedArray.getInt(R.styleable.SpeedometerView_colorBorder, colorBorder)
            colorDigits = typedArray.getInt(R.styleable.SpeedometerView_colorDigits, colorDigits)
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

    private fun paintBackground(canvas: Canvas) {
        generalPaint.color = colorBackground
        generalPaint.style = Paint.Style.FILL

        canvas.drawCircle(centerX, centerY, radius, generalPaint)
    }

    private fun paintBackgroundBorder(canvas: Canvas) {
        generalPaint.color = colorBorder
        generalPaint.style = Paint.Style.STROKE
        generalPaint.strokeWidth = borderWidth

        canvas.drawCircle(centerX, centerY, radius - borderWidth / 2f, generalPaint)
    }

    private fun paintDial(canvas: Canvas) {
        canvas.rotate(135f, centerX, centerY)

        val scaleDigits = 0.75f
        val theeForthCircumference = radius * scaleDigits * Math.PI * 1.5
        val increment = 20

        for (i in 0..maxSpeed step increment) {
            val digitText = i.toString()

            digitsPaint.color = colorDigits
            digitsPaint.style = Paint.Style.FILL_AND_STROKE
            digitsPaint.strokeWidth = 2f
            digitsPaint.textSize = digitsTextSize
            digitsPaint.setShadowLayer(6f, 0f, 0f, Color.BLUE)

            val digitTextLength = round(digitsPaint.measureText(digitText))

            circle.addCircle(centerX, centerY, radius * scaleDigits, Path.Direction.CW)

            canvas.drawTextOnPath(
                digitText,
                circle,
                ((i * theeForthCircumference / maxSpeed) - digitTextLength / 2).toFloat(),
                0f,
                digitsPaint
            )
        }
    }

    private fun paintTickDial(canvas: Canvas) {

        canvas.scale(radius, radius)


        matrix1.setTranslate(centerX + 1000, centerY + 1000)

        line.transform(matrix1)

        val scale = 0.8f
        val step = Math.PI * 1.5 / maxSpeed
        val threeQuartersOfCircle = Math.PI * 1.5
        val stepBetweenBigDigits = 20

        for (i in 0..maxSpeed step 10) {
            val angle = threeQuartersOfCircle - (step * i)
            val x1 = (cos(angle)).toFloat()
            val y1 = (sin(angle)).toFloat()

            val x2: Float
            val y2: Float

            x2 = x1 * scale
            y2 = y1 * scale

            generalPaint.color = Color.RED
            generalPaint.style = Paint.Style.FILL_AND_STROKE
            line.moveTo(x1, y1)
            line.lineTo(x2, y2)


            canvas.drawPath(line, generalPaint)
            line.reset()
            if (i % stepBetweenBigDigits == 0) {
                generalPaint.strokeWidth = 0.04f


                //canvas.drawLine( x1,  y1, x2, y2, generalPaint)
            } else {
                generalPaint.strokeWidth = 0.02f

                // canvas.drawLine(x1, y1, x2, y2, generalPaint)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // paintBackground(canvas)

        //paintBackgroundBorder(canvas)

        //paintDial(canvas)

        paintTickDial(canvas)

//        //деление циферблата
//
//
//        //Стрелка
//        val limit = (currentSpeed / maxSpeed.toFloat() * 270 - 270)
//        val scaleOfArrow = radius / 1.5f
//        val scaleOfCircle = radius / 10
//
//        canvas.translate(centerX, centerY)
//        canvas.rotate(limit)
//
//        arrowPaint.color = colorArrow
//        arrowPaint.strokeWidth = digitsTextSize / 4f
//        arrowPaint.style = Paint.Style.FILL_AND_STROKE
//        arrowPaint.setShadowLayer(5f, 0f, 0f, Color.WHITE)
//
//        canvas.drawLine(0f, 0f, scaleOfArrow, scaleOfArrow, arrowPaint)
//
//        arrowPaint.color = Color.BLUE
//        canvas.drawCircle(0f, 0f, scaleOfCircle, arrowPaint)
//
//        arrowPaint.color = Color.BLACK
//        canvas.drawCircle(0f, 0f, scaleOfCircle / 5, arrowPaint)
    }
}