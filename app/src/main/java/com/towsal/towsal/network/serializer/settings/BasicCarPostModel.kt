package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Request model for car feature and interior details
 * */
class BasicCarPostModel : Serializable {
    @SerializedName(DataKeyValues.VehicleInfoSetting.NUMBER_OF_SEAT_ID)
    @Expose
    var number_of_seat: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.NUMBER_OF_DOORS_ID)
    @Expose
    var number_of_door: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.FUEL_TYPE_ID)
    @Expose
    var fuel_type: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.KMP)
    @Expose
    var kmp: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.CAR_FEATURES)
    @Expose
    var car_feature: ArrayList<Int> = null ?: ArrayList()
}