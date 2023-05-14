package com.applandeo.materialcalendarview.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.R
import com.applandeo.materialcalendarview.listeners.DayRowClickListener
import com.applandeo.materialcalendarview.utils.*
import kotlinx.android.synthetic.main.calendar_view_grid.view.*
import java.util.*

/**
 * This class is responsible for loading a calendar page content.
 *
 *
 * Created by Applandeo team
 */
class CalendarPageAdapter(private val context: Context, private val calendarProperties: CalendarProperties, private val callBack: (position: Int) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var pageMonth = 0

    val selectedDays: List<SelectedDay>
        get() = calendarProperties.selectedDays

    var selectedDay: SelectedDay
        get() = calendarProperties.selectedDays.first()
        set(selectedDay) {
            calendarProperties.setSelectedDay(selectedDay)
            informDatePicker()
        }

    init {
        informDatePicker()
    }

    fun addSelectedDay(selectedDay: SelectedDay) {
        if (selectedDay !in calendarProperties.selectedDays) {
            calendarProperties.selectedDays.add(selectedDay)
            informDatePicker()
            return
        }

        calendarProperties.selectedDays.remove(selectedDay)
        informDatePicker()
    }
    /**
     * This method inform DatePicker about ability to return selected days
     */
    private fun informDatePicker() {
        calendarProperties.onSelectionAbilityListener?.invoke(calendarProperties.selectedDays.size > 0)
    }

    /**
     * This method fill calendar GridView with days
     *
     * @param position Position of current page in ViewPager
     */
    private fun loadMonth(position: Int, viewHolder: ViewHolder) {
        val days = mutableListOf<Pair<Boolean, Date>>()

        // Get Calendar object instance
        val calendar = (calendarProperties.firstPageCalendarDate.clone() as Calendar).apply {
            // Add months to Calendar (a number of months depends on ViewPager position)
            add(Calendar.MONTH, position)

            // Set day of month as 1
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val calendarMonth = calendar.get(Calendar.MONTH)
        val startWeekOfMonth = calendar.get(Calendar.WEEK_OF_MONTH)
        val calendarNextMonth = Calendar.getInstance()
        calendarNextMonth.time = calendar.time
        calendarNextMonth.add(Calendar.MONTH, 1)
        calendarNextMonth.add(Calendar.DATE, -1)
        val endWeekOfMonth = calendarNextMonth.get(Calendar.WEEK_OF_MONTH)
        val noOfWeeksInMonth = (endWeekOfMonth - startWeekOfMonth) + 1
        getPageDaysProperties(calendar)

        // Get a number of the first day of the week
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // Count when month is beginning
        val firstDayOfWeek = calendarProperties.firstDayOfWeek
        val monthBeginningCell =
            (if (dayOfWeek < firstDayOfWeek) 7 else 0) + dayOfWeek - firstDayOfWeek
        Log.e("Library", monthBeginningCell.toString())

        // Subtract a number of beginning days, it will let to load a part of a previous month
        calendar.add(Calendar.DAY_OF_MONTH, -monthBeginningCell)

        /*
        Get all days of one page (42 is a number of all possible cells in one page
        (a part of previous month, current month and a part of next month))
         */
        while (days.size < (noOfWeeksInMonth * 7)) {
            days.add(
                Pair(
                    calendar[Calendar.MONTH] == calendarMonth,
                    calendar.time
                )
            )
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        pageMonth = calendar.get(Calendar.MONTH) - 1
        val calendarDayAdapter =
            CalendarDayAdapter(context, this, calendarProperties, days, pageMonth, position)
        informDatePicker()

        viewHolder.view.calendarGridView.adapter = calendarDayAdapter
        viewHolder.view.calendarGridView.onItemClickListener =
            DayRowClickListener(this, calendarProperties, pageMonth)
    }

    private fun getPageDaysProperties(calendar: Calendar) {
        val pageCalendarDays = calendarProperties.onPagePrepareListener?.invoke(calendar)
        if (pageCalendarDays != null) {
            val diff =
                pageCalendarDays.minus(calendarProperties.calendarDayProperties.toSet()).distinct()
            calendarProperties.calendarDayProperties.addAll(diff)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val calendarView =
            LayoutInflater.from(context).inflate(R.layout.calendar_view_grid, parent, false)
        return ViewHolder(calendarView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        renderHeader(position, viewHolder.view.currentDateLabel)
        loadMonth(position, viewHolder)
    }

    override fun getItemCount() =
        if (calendarProperties.typeOfCalendar == CalendarView.FIXED_SET) CalendarProperties.CALENDAR_SIZE else CalendarProperties.scaleAbleSize

    private fun renderHeader(position: Int, view: TextView) {
        val calendar = calendarProperties.firstPageCalendarDate.clone() as Calendar
        calendar.add(Calendar.MONTH, position)

        if (!isScrollingLimited(calendar, position)) {
            setHeaderName(calendar, view)
        }
    }

    private fun isScrollingLimited(calendar: Calendar, position: Int): Boolean {
        return when {
            calendarProperties.minimumDate.isMonthBefore(calendar) -> {
                callBack(position + 1)
                true
            }
            calendarProperties.maximumDate.isMonthAfter(calendar) -> {
                callBack(position - 1)
                true
            }
            else -> false
        }
    }

    private fun setHeaderName(calendar: Calendar, view: TextView) {
        view.setGradientTextColor(
            intArrayOf(
                Color.parseColor("#001420E4"),
                Color.parseColor("#1420E4"),
                Color.parseColor("#1420E4")
            )
        )
        view.text = calendar.getMonthAndYearDate(context)
    }


    class ViewHolder(var view: View) :
        RecyclerView.ViewHolder(view)

    fun TextView.setGradientTextColor(colors: IntArray) {

        val shader: Shader = LinearGradient(
            0f, 0f, 0f, this.lineHeight.toFloat(),
            colors, null, Shader.TileMode.REPEAT
        )
        this.paint.shader = shader

    }

}