package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for car calendar
 * */
class CalendarModel : Serializable {
    @SerializedName(DataKeyValues.ID)
    @Expose
    var id: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.CAR_ID)
    @Expose
    var car_id: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.DAY)
    @Expose
    var day: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.DATE)
    @Expose
    var date: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.STATUS)
    @Expose
    var status: Int = null ?: 0

}