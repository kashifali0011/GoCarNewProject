package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for car calendars
 * */
class CalendarResponseModel:Serializable {
    @SerializedName(DataKeyValues.VehicleInfoSetting.CAR_CALENDAR)
    @Expose
    var car_calender: ArrayList<CalendarModel> = null ?: ArrayList()
}