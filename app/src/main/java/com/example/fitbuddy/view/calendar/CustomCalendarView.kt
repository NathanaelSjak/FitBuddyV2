package com.example.fitbuddy.view.calendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.widget.CalendarView
import com.example.fitbuddy.R
import java.text.SimpleDateFormat
import java.util.*

class CustomCalendarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CalendarView(context, attrs, defStyleAttr) {

    private val completedDates = mutableSetOf<String>()
    private val missedDates = mutableSetOf<String>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    private val completedColor = context.getColor(R.color.light_blue)
    private val missedColor = context.getColor(R.color.pink_accent)
    private val todayColor = context.getColor(R.color.gold_yellow)

    init {
        paint.style = Paint.Style.FILL
    }

    fun setCompletedDates(dates: Set<String>) {
        completedDates.clear()
        completedDates.addAll(dates)
        invalidate()
    }

    fun setMissedDates(dates: Set<String>) {
        missedDates.clear()
        missedDates.addAll(dates)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Get the date cell bounds and draw backgrounds
        val today = dateFormat.format(Date())
        val cellWidth = width / 7f
        val cellHeight = height / 7f
        val startY = cellHeight // Skip the header row

        for (week in 0 until 6) {
            for (day in 0 until 7) {
                val x = day * cellWidth
                val y = startY + (week * cellHeight)
                
                rect.set(x, y, x + cellWidth, y + cellHeight)
                
                val date = getDateForPosition(week, day)
                when {
                    date == today -> {
                        paint.color = todayColor
                        canvas.drawRoundRect(rect, 8f, 8f, paint)
                    }
                    completedDates.contains(date) -> {
                        paint.color = completedColor
                        canvas.drawRoundRect(rect, 8f, 8f, paint)
                    }
                    missedDates.contains(date) -> {
                        paint.color = missedColor
                        canvas.drawRoundRect(rect, 8f, 8f, paint)
                    }
                }
            }
        }
    }

    private fun getDateForPosition(week: Int, day: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.add(Calendar.DAY_OF_MONTH, week * 7 + day)
        return dateFormat.format(calendar.time)
    }
} 