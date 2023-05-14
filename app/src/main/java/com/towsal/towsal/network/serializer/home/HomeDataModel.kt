package com.towsal.towsal.network.serializer.home

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for home screen
 * */
class HomeDataModel : Serializable {
    @SerializedName(DataKeyValues.Home.CAR_TYPES)
    @Expose
    var car_types: ArrayList<CarHomeModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.Home.CAR_AROUND_YOU)
    @Expose
    var car_around_you: ArrayList<CarHomeModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.Home.HOME_SEARCH_PARAMS)
    @Expose
    var home_search_param: ArrayList<CarHomeModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.IS_LISTING)
    @Expose
    var isListing: Int = null ?: 0
}