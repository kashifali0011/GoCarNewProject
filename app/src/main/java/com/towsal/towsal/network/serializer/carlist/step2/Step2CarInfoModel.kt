package com.towsal.towsal.network.serializer.carlist.step2

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel
import java.io.Serializable

/**
 * Response model for Car info model
 * */
class Step2CarInfoModel : Serializable {

    @SerializedName(DataKeyValues.CarInfo.CAR_AVAILABILITY)
    @Expose
    var car_availability: CarAvailabilityModel = null ?: CarAvailabilityModel()

    @SerializedName(DataKeyValues.CarInfo.ADVANCE_NOTICE_LIST)
    val carAdvanceNoticeList: ArrayList<CarMakeInfoModel> = ArrayList(emptyList<CarMakeInfoModel>())

    @SerializedName(DataKeyValues.CarInfo.MIN_TRIP_LIST)
    @Expose
    var min_hour_list: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.CarInfo.MAX_TRIP_LIST)
    @Expose
    var max_hour_list: ArrayList<CarMakeInfoModel> = null ?: ArrayList()
}