package com.example.nlcs

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.data.BarEntry

// Tạo độ cong cho biểu đồ cột
class CylinderBarChartRenderer(
    chart: BarChart, animator: ChartAnimator, viewPortHandler: ViewPortHandler
) : BarChartRenderer(chart, animator, viewPortHandler) {

    private val cylinderPaint: Paint = Paint()

    override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
        val trans = mChart.getTransformer(dataSet.axisDependency)
        cylinderPaint.color = dataSet.color

        // Tạo hình trụ cho mỗi cột
        for (i in 0 until dataSet.entryCount) {
            val entry = dataSet.getEntryForIndex(i) as BarEntry
            val barRect = RectF(entry.x - 0.3f, 0f, entry.x + 0.3f, entry.y)  // RectF tùy chỉnh
            trans.rectToPixelPhase(barRect, mAnimator.phaseY)

            // Chiều cao của phần bo góc trên cùng
            val roundRadius = 10f

            // Vẽ phần dưới của cột (góc vuông)
            c.drawRect(barRect.left, barRect.top + roundRadius, barRect.right, barRect.bottom, cylinderPaint)

            // Vẽ phần trên của cột (bo góc trên)
            val topRect = RectF(barRect.left, barRect.top, barRect.right, barRect.top + (barRect.height() / 2))
            c.drawRoundRect(topRect, roundRadius, roundRadius, cylinderPaint)
        }
    }
}







