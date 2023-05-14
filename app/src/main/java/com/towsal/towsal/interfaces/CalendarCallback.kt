package com.towsal.towsal.interfaces

import com.towsal.towsal.network.serializer.cardetails.CalendarDateTimeModel

/**
 * Interface for calendar popup callback
 * */
interface CalendarCallback {
    fun onCalendarDateTimeSelected(model: CalendarDateTimeModel)
}