package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.utils.Constants
import java.io.Serializable

/**
 * Response model for car availability
 * */
class CarAvailabilityModel :Serializable {
    @SerializedName(DataKeyValues.VehicleInfoSetting.CAR_ID)
    @Expose
    var car_id: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.DAY)
    @Expose
    var day: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.FROM)
    @Expose
    var from: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.TO)
    @Expose
    var to: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.IS_UNAVAILABLE)
    @Expose
    var isUnavailable: Int = null ?: Constants.Availability.AVAILABLE
}