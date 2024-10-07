package com.example.nlcs

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.RelativeLayout
import androidx.appcompat.app.AlertDialog
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class LineDrawingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    // Neo4j service and database connection
    private var neo4jService: Neo4jService
    private val neo4jUri = "bolt+s://25190e75.databases.neo4j.io"
    private val neo4jUser = "neo4j"
    private val neo4jPassword = "j_j-RCzouI3et4G2bvF0RTW83eXCws-aoQJstrQgUts"

    init {
        neo4jService = Neo4jService(neo4jUri, neo4jUser, neo4jPassword)
    }

    // Data structures to store parent-child relationships, node positions, and dimensions
    private val parentChildMap: MutableMap<String, MutableList<String>> = mutableMapOf()
    private val nodePositions: MutableMap<String, Pair<Float, Float>> = mutableMapOf()
    private val nodeWidths: MutableMap<String, Int> = mutableMapOf()
    private val nodeHeights: MutableMap<String, Int> = mutableMapOf()

    // Paint objects for drawing the lines and arrowheads
    private val linePaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 6f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val arrowPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL_AND_STROKE
        isAntiAlias = true
    }

    private val arrowSize = 20f

    // Paint for the black ball
    private val ballPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    // Method to set relationships, positions, and dimensions
    fun setParentChildMap(
        parentChildMap: Map<String, List<String>>,
        nodes: List<Map<String, Any>>,
        nodeWidths: Map<String, Int>,
        nodeHeights: Map<String, Int>
    ) {
        this.parentChildMap.clear()
        // Convert to mutable lists to allow adding new connections
        for ((key, value) in parentChildMap) {
            this.parentChildMap[key] = value.toMutableList()
        }

        // Store node positions by their IDs
        nodePositions.clear()
        for (node in nodes) {
            val nodeID = node["nodeID"] as String
            val x = (node["x"] as? Float) ?: 0f
            val y = (node["y"] as? Float) ?: 0f
            nodePositions[nodeID] = Pair(x, y)
        }

        // Store node dimensions
        this.nodeWidths.clear()
        this.nodeWidths.putAll(nodeWidths)

        this.nodeHeights.clear()
        this.nodeHeights.putAll(nodeHeights)

        // Trigger a redraw of the view
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for ((parentID, childrenIDs) in parentChildMap) {
            val parentPosition = nodePositions[parentID] ?: continue
            val parentWidth = nodeWidths[parentID] ?: continue
            val parentHeight = nodeHeights[parentID] ?: continue

            val parentCenterX = parentPosition.first + (parentWidth / 2)
            val parentCenterY = parentPosition.second + (parentHeight / 2)

            for (childID in childrenIDs) {
                val childPosition = nodePositions[childID] ?: continue
                val childWidth = nodeWidths[childID] ?: continue
                val childHeight = nodeHeights[childID] ?: continue

                val childEdgeX: Float
                val childEdgeY: Float

                // Determine the horizontal and vertical center of the child node
                val childCenterX = childPosition.first + (childWidth / 2)
                val childCenterY = childPosition.second + (childHeight / 2)

                // Calculate which edge to point to based on the parent's relative position
                if (abs(parentCenterX - childCenterX) > abs(parentCenterY - childCenterY)) {
                    // Parent is more to the left or right -> point to left or right edge
                    if (parentCenterX < childCenterX) {
                        // Point to left edge (middle of the left edge)
                        childEdgeX = childPosition.first - 2f // Left edge
                        childEdgeY = childCenterY  // Midpoint of the left edge
                    } else {
                        // Point to right edge (middle of the right edge)
                        childEdgeX = childPosition.first + childWidth + 2f // Right edge
                        childEdgeY = childCenterY  // Midpoint of the right edge
                    }
                } else {
                    // Parent is more above or below -> point to top or bottom edge
                    if (parentCenterY < childCenterY) {
                        // Point to top edge (middle of the top edge)
                        childEdgeX = childCenterX  // Midpoint of the top edge
                        childEdgeY = childPosition.second - 2f // Top edge
                    } else {
                        // Point to bottom edge (middle of the bottom edge)
                        childEdgeX = childCenterX  // Midpoint of the bottom edge
                        childEdgeY = childPosition.second + childHeight + 2f // Bottom edge
                    }
                }

                // Draw the line from parent center to child edge
                canvas.drawLine(parentCenterX, parentCenterY, childEdgeX, childEdgeY, linePaint)

                // Draw arrowhead at the end of the line
                drawArrowHead(canvas, parentCenterX, parentCenterY, childEdgeX, childEdgeY)

                // Draw the black ball at the midpoint of the line
                val midX = (parentCenterX + childEdgeX) / 2
                val midY = (parentCenterY + childEdgeY) / 2
                canvas.drawCircle(midX, midY, 18f, ballPaint)
            }
        }
    }

    // Method to draw the arrowhead
    private fun drawArrowHead(canvas: Canvas, startX: Float, startY: Float, endX: Float, endY: Float) {
        val angle = atan2((endY - startY).toDouble(), (endX - startX).toDouble()).toFloat()

        // Calculate arrow tip coordinates
        val tipX = endX + arrowSize * 0.2f * cos(angle)
        val tipY = endY + arrowSize * 0.2f * sin(angle)

        val arrowPath = Path()

        arrowPath.moveTo(tipX, tipY)

        arrowPath.lineTo(
            (endX - arrowSize * cos(angle - Math.PI / 6)).toFloat(),
            (endY - arrowSize * sin(angle - Math.PI / 6)).toFloat()
        )
        arrowPath.lineTo(
            (endX - arrowSize * cos(angle + Math.PI / 6)).toFloat(),
            (endY - arrowSize * sin(angle + Math.PI / 6)).toFloat()
        )
        arrowPath.close()

        canvas.drawPath(arrowPath, arrowPaint)
    }

    // Update node position after moving a node
    fun updateNodePosition(nodeID: String, newX: Float, newY: Float) {
        nodePositions[nodeID] = Pair(newX, newY)
        invalidate()
    }

    // Method to store node dimensions
    fun storeNodeDimensions(nodeID: String, width: Int, height: Int) {
        nodeWidths[nodeID] = width
        nodeHeights[nodeID] = height
    }

    // Method to add a new connection line
    fun addConnectionLine(parentID: String, childID: String) {
        // Add the childID to the parent's list of children
        val childrenList = parentChildMap.getOrPut(parentID) { mutableListOf() }
        if (!childrenList.contains(childID)) {
            childrenList.add(childID)
        }
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val touchX = event.x
                val touchY = event.y

                // Loop through the connections to check if the touch is near any of them
                for ((parentID, childrenIDs) in parentChildMap) {
                    val parentPosition = nodePositions[parentID] ?: continue
                    val parentWidth = nodeWidths[parentID] ?: continue
                    val parentHeight = nodeHeights[parentID] ?: continue

                    val parentCenterX = parentPosition.first + (parentWidth / 2)
                    val parentCenterY = parentPosition.second + (parentHeight / 2)

                    for (childID in childrenIDs) {
                        val childPosition = nodePositions[childID] ?: continue
                        val childWidth = nodeWidths[childID] ?: continue
                        val childHeight = nodeHeights[childID] ?: continue

                        val childCenterX = childPosition.first + (childWidth / 2)
                        val childCenterY = childPosition.second + (childHeight / 2)

                        val childEdgeX: Float
                        val childEdgeY: Float

                        // Calculate which edge to point to based on the parent's relative position
                        if (abs(parentCenterX - childCenterX) > abs(parentCenterY - childCenterY)) {
                            // Parent is more to the left or right -> point to left or right edge
                            if (parentCenterX < childCenterX) {
                                // Point to left edge (middle of the left edge)
                                childEdgeX = childPosition.first - 2f // Left edge
                                childEdgeY = childCenterY  // Midpoint of the left edge
                            } else {
                                // Point to right edge (middle of the right edge)
                                childEdgeX = childPosition.first + childWidth + 2f // Right edge
                                childEdgeY = childCenterY  // Midpoint of the right edge
                            }
                        } else {
                            // Parent is more above or below -> point to top or bottom edge
                            if (parentCenterY < childCenterY) {
                                // Point to top edge (middle of the top edge)
                                childEdgeX = childCenterX  // Midpoint of the top edge
                                childEdgeY = childPosition.second - 2f // Top edge
                            } else {
                                // Point to bottom edge (middle of the bottom edge)
                                childEdgeX = childCenterX  // Midpoint of the bottom edge
                                childEdgeY = childPosition.second + childHeight + 2f // Bottom edge
                            }
                        }

                        // Check if the touch is near the ball (midpoint)
                        val midX = (parentCenterX + childEdgeX) / 2
                        val midY = (parentCenterY + childEdgeY) / 2
                        if (isTouchNearBall(touchX, touchY, midX, midY)) {
                            showDeleteDialog(childID)
                            return true
                        }
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    // Check if the touch is near the ball
    private fun isTouchNearBall(touchX: Float, touchY: Float, ballX: Float, ballY: Float): Boolean {
        val buffer = 20f // Allow some buffer distance for touch detection
        return (touchX >= ballX - buffer && touchX <= ballX + buffer &&
                touchY >= ballY - buffer && touchY <= ballY + buffer)
    }

    private fun showDeleteDialog(childID: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete this branch starting from:")

        val childTitle = neo4jService.getTitleByNodeID(childID)

        builder.setMessage("Node title: $childTitle")

        builder.setPositiveButton("Yes") { dialog, _ ->
            (context as MindMapActivity).deleteBranch(childID)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        builder.show()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        neo4jService.close()
    }

    fun removeNodeConnections(nodeID: String) {
        // Remove the connections for the node and its children
        parentChildMap.remove(nodeID) // Remove the node and its connections to children
        nodePositions.remove(nodeID) // Remove the node's position data

        // Optionally remove the node size data as well
        nodeWidths.remove(nodeID)
        nodeHeights.remove(nodeID)

        // If this node is a child of another node, remove it from its parent's list
        for ((_, childrenIDs) in parentChildMap) {
            childrenIDs.remove(nodeID)
        }
    }

    fun getChildIDs(parentID: String): List<String>? {
        return parentChildMap[parentID]
    }
}
