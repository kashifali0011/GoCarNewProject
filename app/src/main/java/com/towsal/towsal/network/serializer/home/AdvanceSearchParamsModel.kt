package com.towsal.towsal.network.serializer.home

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel
import java.io.Serializable

/**
 * Response model for Car advance search params
 * */
class AdvanceSearchParamsModel : Serializable {
    @SerializedName(DataKeyValues.Home.SORT_BY)
    @Expose
    var sort_by: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.Home.DISTANCE)
    @Expose
    var distance: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.Home.CAR_LOCATION)
    @Expose
    var car_location: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.Home.TYPES)
    @Expose
    var types: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.Home.YEARS)
    @Expose
    var years: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.Home.MAKES)
    @Expose
    var makes: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.Home.MODELS)
    @Expose
    var models: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.Home.ODOMETERS)
    @Expose
    var odometers: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.Home.ADDRESS)
    @Expose
    var address: String = null ?: ""

    @SerializedName(DataKeyValues.Home.USER_LONG)
    @Expose
    var userLong: Double = null ?: 0.0

    @SerializedName(DataKeyValues.Home.USER_LAT)
    @Expose
    var userLat: Double = null ?: 0.0

}