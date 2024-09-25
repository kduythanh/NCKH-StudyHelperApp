package com.example.nlcs

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.widget.RelativeLayout
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class LineDrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    // Data structure to store parent-child relationships and node positions
    private val parentChildMap: MutableMap<String, List<String>> = mutableMapOf()
    private val nodePositions: MutableMap<String, Pair<Float, Float>> = mutableMapOf()

    // Paint object for drawing the lines and arrowheads
    private val linePaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    // Paint object for arrowhead
    private val arrowPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
    }

    // Buffer space between arrow and node
    private val bufferSpace = 0f
    // Arrowhead size
    private val arrowSize = 20f

    // Method to set the relationships and positions from MindMapActivity
    fun setParentChildMap(parentChildMap: Map<String, List<String>>, nodes: List<Map<String, Any>>) {
        this.parentChildMap.clear()
        this.parentChildMap.putAll(parentChildMap)

        // Store node positions by their IDs
        nodePositions.clear()
        for (node in nodes) {
            val nodeID = node["nodeID"] as String
            val x = (node["x"] as? Float) ?: 0f
            val y = (node["y"] as? Float) ?: 0f
            nodePositions[nodeID] = Pair(x, y)
            Log.d("LineDrawingView", "Node $nodeID: x=$x, y=$y")
        }

        // Trigger a redraw of the view
        invalidate()
    }

    // Override onDraw to handle drawing lines and arrowheads between parent and child nodes
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for ((parentID, childrenIDs) in parentChildMap) {
            val parentPosition = nodePositions[parentID] ?: continue

            for (childID in childrenIDs) {
                val childPosition = nodePositions[childID] ?: continue

                // Log the child node position
                Log.d("LineDrawingView", "Child ID: $childID, Position: $childPosition")

                // Calculate the start and end points with buffer space
                val startX = parentPosition.first + bufferSpace
                val startY = parentPosition.second + bufferSpace
                val endX = childPosition.first - bufferSpace
                val endY = childPosition.second - bufferSpace

                // Log the line drawing start and end coordinates
                Log.d("LineDrawingView", "Drawing line from ($startX, $startY) to ($endX, $endY)")

                // Draw a line from parent to child
                canvas.drawLine(startX, startY, endX, endY, linePaint)

                // Draw arrowhead at the end of the line
                drawArrowHead(canvas, startX, startY, endX, endY)
            }
        }
    }

    // Function to draw an arrowhead at the end of the line
    private fun drawArrowHead(canvas: Canvas, startX: Float, startY: Float, endX: Float, endY: Float) {
        val angle = atan2((endY - startY).toDouble(), (endX - startX).toDouble()).toFloat()

        // Calculate the points for the arrowhead
        val arrowPath = Path()
        arrowPath.moveTo(endX, endY)
        arrowPath.lineTo(
            (endX - arrowSize * cos(angle - Math.PI / 6)).toFloat(),
            (endY - arrowSize * sin(angle - Math.PI / 6)).toFloat()
        )
        arrowPath.lineTo(
            (endX - arrowSize * cos(angle + Math.PI / 6)).toFloat(),
            (endY - arrowSize * sin(angle + Math.PI / 6)).toFloat()
        )
        arrowPath.close()

        // Draw the arrowhead
        canvas.drawPath(arrowPath, arrowPaint)
    }
}
