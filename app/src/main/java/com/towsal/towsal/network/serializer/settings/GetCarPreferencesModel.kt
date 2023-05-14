package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel
import com.towsal.towsal.network.serializer.carlist.step2.CarAvailabilityModel
import java.io.Serializable

/**
 * Response model for car preferences
 * */
class GetCarPreferencesModel : Serializable {

    @SerializedName(DataKeyValues.VehicleInfoSetting.TRIP_PREFERENCE)
    @Expose
    var advance_notice_list: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.VehicleInfoSetting.CAR_TRIP_PREFERENCE)
    @Expose
    var car_availability: CarAvailabilityModel = null ?: CarAvailabilityModel()

    @SerializedName(DataKeyValues.CarInfo.MIN_TRIP_LIST)
    @Expose
    var min_hour_list: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.CarInfo.MAX_TRIP_LIST)
    @Expose
    var max_hour_list: ArrayList<CarMakeInfoModel> = null ?: ArrayList()
}