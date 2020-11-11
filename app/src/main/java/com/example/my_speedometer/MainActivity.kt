package com.example.my_speedometer

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.*
import android.widget.Button

class MainActivity : AppCompatActivity() {
    private val button: Button
        get() = findViewById(R.id.button)

    private val speedometer: SpeedometerView
        get() = findViewById(R.id.speedometerView)

    var currencySpeed = 0f
    var maxSpeed = 0f
    var arrowColor = 0

    var isFirstStart = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        maxSpeed = speedometer.maxSpeed.toFloat()
        arrowColor = speedometer.colorArrow

        val arrowColorAnimation = ValueAnimator().apply {
            setIntValues(
                arrowColor,
                Color.RED,
                arrowColor
            )

            setEvaluator(ArgbEvaluator())
            addUpdateListener {
                speedometer.colorArrow = this.animatedValue as Int
            }

            duration = 300L
        }

        val arrowMotionAnimation = ValueAnimator.ofFloat().apply {
            addUpdateListener {
                speedometer.currentSpeed = this.animatedValue as Float

                if (this.animatedValue as Float >= 160f) {
                    if (!arrowColorAnimation.isStarted) {
                        arrowColorAnimation.start()
                    }
                }
            }

            duration = 5000L
            interpolator = DecelerateInterpolator(0.7f)
        }

        button.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action ?: false) {
                    MotionEvent.ACTION_DOWN -> {
                        if (isFirstStart) {
                            if (arrowMotionAnimation.isStarted) {
                                arrowMotionAnimation.setFloatValues(
                                    arrowMotionAnimation.animatedValue as Float, maxSpeed
                                )
                            } else {
                                arrowMotionAnimation.setFloatValues(0f, maxSpeed)
                            }
                        } else {
                            currencySpeed = speedometer.currentSpeed
                            arrowMotionAnimation.setFloatValues(currencySpeed, maxSpeed)
                        }

                        arrowMotionAnimation.start()

                        isFirstStart = true

                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        arrowMotionAnimation
                            .setFloatValues(arrowMotionAnimation.animatedValue as Float, 0f)

                        arrowMotionAnimation.start()

                        return true
                    }
                }

                return false
            }
        })
    }
}