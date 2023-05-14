package com.applandeo.materialcalendarview.listeners

import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ImageView
import android.widget.TextView
import com.applandeo.materialcalendarview.*
import com.applandeo.materialcalendarview.adapters.CalendarPageAdapter
import com.applandeo.materialcalendarview.utils.*
import java.util.*

/**
 * This class is responsible for handle click events
 *
 *
 * Created by Applandeo Team.
 */
class DayRowClickListener(
    private val calendarPageAdapter: CalendarPageAdapter,
    private val calendarProperties: CalendarProperties,
    pageMonth: Int
) : OnItemClickListener {

    private val pageMonth = if (pageMonth < 0) 11 else pageMonth

    override fun onItemClick(adapterView: AdapterView<*>, view: View, position: Int, id: Long) {

        val pair = (adapterView.getItemAtPosition(position) as Pair<Boolean, Date>)
        val day = GregorianCalendar().apply {
            time = pair.second
        }

        if (calendarProperties.onDayClickListener != null) {
            val currentSelectedDay = Calendar.getInstance()
            currentSelectedDay.time = pair.second
            val foundedCalendarDay = calendarProperties.findDayProperties(
                currentSelectedDay
            )
            if (
                pair.first
            ) {
                var bool = true
                if (calendarProperties.minimumDate != null) {
                    bool = pair.second > calendarProperties.minimumDate?.time
                }
                if (calendarProperties.maximumDate != null && bool) {
                    bool = pair.second < calendarProperties.maximumDate?.time
                }
                if (
                    foundedCalendarDay != null && bool
                ) {
                    bool = foundedCalendarDay.isSelectable
                }
                if (!bool) {
                    return
                }
            }


            if (calendarProperties.selectionDisabled) return


            when (calendarProperties.calendarType) {
                CalendarView.ONE_DAY_PICKER -> {
                    if (pair.first) {
                        selectOneDay(view, day)
                    }
                }
                CalendarView.MANY_DAYS_PICKER -> {
                    if (pair.first) {
                        selectManyDays(view, day, foundedCalendarDay)
                    }
                }
                CalendarView.RANGE_PICKER -> {
                    if (pair.first) {
                        selectRange(view, day, foundedCalendarDay)
                    }
                }
                CalendarView.CLASSIC -> {
                    if (pair.first) {
                        calendarPageAdapter.selectedDay = SelectedDay(day, view)
                    }
                }
            }
        }
    }

    private fun selectOneDay(view: View, day: Calendar) {
        val previousSelectedDay = calendarPageAdapter.selectedDay

        val dayLabel = view.findViewById<TextView>(R.id.dayLabel)

        if (isAnotherDaySelected(previousSelectedDay, day)) {
            selectDay(dayLabel, day)
            reverseUnselectedColor(previousSelectedDay)
            calendarPageAdapter.notifyDataSetChanged()
        }
        onClick(day)
    }

    private fun selectManyDays(view: View, day: Calendar, calendarDay: CalendarDay?) {
        val dayLabel = view.findViewById<TextView>(R.id.dayLabel)

        if (!day.isCurrentMonthDay() || !day.isActiveDay()) return

        val selectedDay = SelectedDay(day, dayLabel)
        if (!calendarPageAdapter.selectedDays.contains(selectedDay)) {
            setSelectedDayColors(dayLabel, day, calendarProperties)
        } else {
            reverseUnselectedColor(selectedDay)
        }
        calendarPageAdapter.addSelectedDay(selectedDay)
        onClick(day)
    }

    private fun selectRange(view: View, day: Calendar, calendarDay: CalendarDay?) {
        val dayLabel = view.findViewById<TextView>(R.id.dayLabel)
        val ivTransparent: ImageView = view.findViewById(R.id.ivTransparent)
        if ((!day.isCurrentMonthDay() && !calendarProperties.selectionBetweenMonthsEnabled) || !day.isActiveDay()) return

        val selectedDays = calendarPageAdapter.selectedDays
        Log.e("selectRange:", "hello")
        when {
            selectedDays.size > 1 -> {
                Log.e("selectRange: ", "clear")
                val bool = calendarDay == null
                if (
                    bool
                ) {
                    Log.e("selectRange: ", "clear")
                    clearAndSelectOne(dayLabel, day)
                } else
                    return
            }
            selectedDays.size == 1 -> {
                val previousSelectedDayCalendar = calendarPageAdapter.selectedDay.calendar
                val selectedRange = previousSelectedDayCalendar.getDatesRange(day)
                if (calendarDay != null) {
                    return
                } else if (calendarProperties.disabledWeekDays.isNotEmpty() && selectedRange.map { it[Calendar.DAY_OF_WEEK] }
                        .containsAnyElement(calendarProperties.disabledWeekDays)
                ) {
                    Log.e("selectRange: ", "${calendarProperties.disabledWeekDays}")
                    return
                } else if (selectedRange.containsAnyElement(
                        calendarProperties.calendarDayProperties.map { it.calendar }
                    )
                ) {
                    Log.e("selectRange: ", "second")
                    return
                } else {
                    selectOneAndRange(dayLabel, day, ivTransparent)

                }
            }
            selectedDays.isEmpty() -> {
                val bool = calendarDay == null
                if (
                    bool
                )
                    selectDay(dayLabel, day, ivTransparent)
                else
                    return
            }
        }
    }

    private fun clearAndSelectOne(dayLabel: TextView, day: Calendar) {

        calendarPageAdapter.selectedDays.forEach { reverseUnselectedColor(it) }
        onClick(day)
        selectDay(dayLabel, day)
        calendarPageAdapter.notifyDataSetChanged()
    }

    private fun selectOneAndRange(dayLabel: TextView, day: Calendar, ivTransparent: ImageView) {
        val previousSelectedDayCalendar = calendarPageAdapter.selectedDay.calendar
        previousSelectedDayCalendar.getDatesRange(day)
            .forEach { calendarPageAdapter.addSelectedDay(SelectedDay(it)) }

        if (isOutOfMaxRange(previousSelectedDayCalendar, day)) return

        setSelectedRangDayColors(
            dayLabel,
            day,
            calendarProperties,
            CalendarView.VERY_FIRST_DATE,
            ivTransparent
        )
        calendarPageAdapter.addSelectedDay(SelectedDay(day, dayLabel))
        onClick(day)
        calendarPageAdapter.notifyDataSetChanged()
    }

    private fun selectDay(dayLabel: TextView, day: Calendar, ivTransparent: ImageView? = null) {
        setSelectedDayColors(dayLabel, day, calendarProperties, ivTransparent)
        onClick(day)
        calendarPageAdapter.selectedDay = SelectedDay(day, dayLabel)
    }

    private fun reverseUnselectedColor(selectedDay: SelectedDay) {
        setCurrentMonthDayColors(
            selectedDay.calendar,
            selectedDay.view as? TextView,
            calendarProperties
        )
    }

    private fun Calendar.isCurrentMonthDay() =
        this[Calendar.MONTH] == pageMonth && this.isBetweenMinAndMax(calendarProperties)

    private fun Calendar.isActiveDay() =
        !calendarProperties.disabledWeekDays.contains(this[Calendar.DAY_OF_WEEK])

    private fun isOutOfMaxRange(firstDay: Calendar, lastDay: Calendar): Boolean {
        // Number of selected days plus one last day
        val numberOfSelectedDays = firstDay.getDatesRange(lastDay).size + 1
        val daysMaxRange: Int = calendarProperties.maximumDaysRange

        return daysMaxRange != 0 && numberOfSelectedDays >= daysMaxRange
    }

    private fun isAnotherDaySelected(selectedDay: SelectedDay, day: Calendar) =
        day != selectedDay.calendar && day.isCurrentMonthDay() && day.isActiveDay()

    private fun onClick(day: Calendar) {
        if (calendarProperties.eventDays.isEmpty()) {
            callOnClickListener(EventDay(day))
            return
        }

        val eventDay = calendarProperties.eventDays.firstOrNull { it.calendar == day }
        callOnClickListener(eventDay ?: EventDay(day))
    }


    private fun callOnClickListener(eventDay: EventDay) {
        val enabledDay = calendarProperties.disabledDays.contains(eventDay.calendar)
                || !eventDay.calendar.isBetweenMinAndMax(calendarProperties)
        eventDay.isEnabled = enabledDay
        calendarProperties.onDayClickListener?.onDayClick(eventDay)
    }
}

fun List<Any>.containsAnyElement(list: List<Any>): Boolean {
    var bool = false
    for (
    calendar in this
    ) {
        bool = list.find {
            calendar == it
        } != null
        if (bool) {
            break
        }
    }

    return bool
}