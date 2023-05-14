package com.applandeo.materialcalendarview.extensions

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.applandeo.materialcalendarview.CalendarView
import com.applandeo.materialcalendarview.adapters.CalendarPageAdapter
import com.applandeo.materialcalendarview.utils.CalendarProperties

/**
 * Created by Applandeo Team.
 */

typealias OnCalendarPageChangedListener = (Int) -> Unit

class CalendarViewPager @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

    var swipeEnabled = true
    private var onCalendarPageChangedListener: OnCalendarPageChangedListener? = null
    var completelyVisibleItem: Int = -1
    private var calendarProperties: CalendarProperties? = null

    init {
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (completelyVisibleItem != (layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()) {
                    if (calendarProperties != null) {
                        calendarProperties?.let { calendarProperties ->
                            if (!calendarProperties.isMonthAddable) {
                                Log.e("onScrolled: ", "hello")
                                CalendarProperties.scaleAbleSize =
                                    calendarProperties.maxMonthPosition + 1
                                (adapter as CalendarPageAdapter).notifyDataSetChanged()
                            }


                        }

                    }
                }
            }
        })

    }


    override fun onTouchEvent(event: MotionEvent) = swipeEnabled && super.onTouchEvent(event)

    override fun onInterceptTouchEvent(event: MotionEvent) =
        swipeEnabled && super.onInterceptTouchEvent(event)

    fun onCalendarPageChangedListener(listener: OnCalendarPageChangedListener) {
        onCalendarPageChangedListener = listener
    }

    fun setCurrentPosition(firstVisiblePage: Int) {
        scrollToPosition(firstVisiblePage)
    }

    fun setCalendarProperties(
        newCalendarProperties: CalendarProperties
    ) {
        this.calendarProperties = newCalendarProperties
    }

    fun getCurrentPosition(): Int =
        (layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
}
