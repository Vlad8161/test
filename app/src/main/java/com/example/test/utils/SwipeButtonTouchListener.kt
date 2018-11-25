package com.example.test.utils

import android.content.Context
import android.support.v4.view.animation.LinearOutSlowInInterpolator
import android.view.MotionEvent
import android.view.View

/**
 * Created by vlad on 24.11.18.
 */

class SwipeButtonTouchListener(context: Context, swipeButtonWidthDp: Int = 86) : View.OnTouchListener {
    private val QUEUE_SIZE = 5
    private val EPS = 0.001f

    private val swipeButtonWidth = context.resources.displayMetrics.density * swipeButtonWidthDp
    private val oldXCoords = ArrayList<Float>()
    private val oldYCoords = ArrayList<Float>()
    private var moved = false
    private var canSwipe = false

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                oldXCoords.clear()
                oldXCoords.add(event.x)
                oldYCoords.clear()
                oldYCoords.add(event.y)
                moved = false
                v.parent.requestDisallowInterceptTouchEvent(true)
                canSwipe = true
            }
            MotionEvent.ACTION_MOVE -> {
                val oldX = oldXCoords.average().toFloat()
                val oldY = oldYCoords.average().toFloat()
                oldXCoords.add(event.x)
                if (oldXCoords.size > QUEUE_SIZE) {
                    oldXCoords.removeAt(0)
                }
                oldYCoords.add(event.y)
                if (oldYCoords.size > QUEUE_SIZE) {
                    oldYCoords.removeAt(0)
                }
                val deltaX = event.x - oldX
                val deltaY = event.y - oldY
                if (Math.abs(deltaX) > EPS || Math.abs(deltaY) > EPS) {
                    if (!moved) {
                        if (Math.abs(deltaX) < Math.abs(deltaY)) {
                            v.parent.requestDisallowInterceptTouchEvent(false)
                            canSwipe = false
                        }
                        moved = true
                    } else {
                        if (canSwipe) {
                            var translation = v.translationX + deltaX
                            translation = Math.min(translation, 0f)
                            translation = Math.max(translation, -swipeButtonWidth)
                            v.translationX = translation
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                val distanceToLeft = Math.abs(v.translationX - (-swipeButtonWidth))
                val distanceToRight = Math.abs(v.translationX)
                v.parent.requestDisallowInterceptTouchEvent(false)
                v.animate().cancel()
                v.animate().translationX(if (distanceToLeft < distanceToRight) -swipeButtonWidth else 0f)
                        .setDuration(100L)
                        .setInterpolator(LinearOutSlowInInterpolator())
                        .start()

                if (!moved) {
                    v.performClick()
                }
            }
        }
        return true
    }
}