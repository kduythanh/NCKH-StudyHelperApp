package com.example.nlcs

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.RelativeLayout
import androidx.core.view.ViewCompat

class ZoomableView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    // Initialize ScaleGestureDetector to detect scaling gestures (pinch to zoom)
    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    // Initialize GestureDetector to detect other gestures like scrolling
    private val gestureDetector = GestureDetector(context, GestureListener())
    // Current scale factor, initialized to 1.0 (no zoom)
    private var scaleFactor = 1.0f
    // Matrix to store transformations (scaling, translation)
    private val matrix = Matrix()
    // Variables to track last touch position for panning
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    // ID of the active pointer (finger) for touch events
    private var activePointerId = MotionEvent.INVALID_POINTER_ID
    // Lazily find the content layout that will be zoomed and panned
    private val contentLayout: View by lazy { findViewById(R.id.mindMapContent) }
    // Callback to be invoked when a scaling or scrolling gesture is detected
    var onGestureListener: (() -> Unit)? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // First, let the scale and gesture detectors handle the event
        val scaleHandled = scaleGestureDetector.onTouchEvent(event)
        val gestureHandled = gestureDetector.onTouchEvent(event)

        // Handle different touch events
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // Record the initial touch position and active pointer ID
                val pointerIndex = event.actionIndex
                lastTouchX = event.getX(pointerIndex)
                lastTouchY = event.getY(pointerIndex)
                activePointerId = event.getPointerId(pointerIndex)
            }

            MotionEvent.ACTION_MOVE -> {
                // Handle panning if not currently scaling
                if (!scaleGestureDetector.isInProgress) {
                    val pointerIndex = event.findPointerIndex(activePointerId)
                    val x = event.getX(pointerIndex)
                    val y = event.getY(pointerIndex)

                    // Calculate the distance moved
                    val dx = x - lastTouchX
                    val dy = y - lastTouchY

                    // Apply translation to the content layout
                    matrix.postTranslate(dx, dy)
                    contentLayout.apply {
                        translationX += dx
                        translationY += dy
                    }
                    // Invalidate the content layout to redraw with changes
                    ViewCompat.postInvalidateOnAnimation(contentLayout)
                    // Update last touch position
                    lastTouchX = x
                    lastTouchY = y
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // Reset active pointer ID when touch ends or is canceled
                activePointerId = MotionEvent.INVALID_POINTER_ID
            }

            MotionEvent.ACTION_POINTER_UP -> {
                // Handle the case when a second pointer is lifted
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)
                if (pointerId == activePointerId) {
                    // Update active pointer and last touch position
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    lastTouchX = event.getX(newPointerIndex)
                    lastTouchY = event.getY(newPointerIndex)
                    activePointerId = event.getPointerId(newPointerIndex)
                }
            }
        }

        // Return true if scale or gesture detector handled the event
        if (scaleHandled || gestureHandled) { return true }
        // Otherwise, let the child handle simple taps
        return super.onTouchEvent(event)
    }

    // Inner class to handle scaling gestures
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            // Notify the gesture listener
            onGestureListener?.invoke()

            // Update scale factor based on the detector's scale factor
            scaleFactor *= detector.scaleFactor
            // Clamp scale factor to a reasonable range
            scaleFactor = scaleFactor.coerceIn(0.1f, 5.0f)
            // Apply scaling to the content layout
            contentLayout.scaleX = scaleFactor
            contentLayout.scaleY = scaleFactor
            // Invalidate the content layout to redraw with changes
            ViewCompat.postInvalidateOnAnimation(contentLayout)
            return true
        }
    }

    // Inner class to handle scrolling gestures
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            // Notify the gesture listener
            onGestureListener?.invoke()

            // Apply translation to the content layout based on scroll distance
            contentLayout.apply {
                translationX -= distanceX
                translationY -= distanceY
            }
            // Invalidate the content layout to redraw with changes  
            ViewCompat.postInvalidateOnAnimation(contentLayout)
            return true
        }
    }
}