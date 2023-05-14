package com.towsal.towsal.network.serializer.home

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for cars search by user
 * */
class CarSearchResponseModel : Serializable {
    @SerializedName(DataKeyValues.Home.CAR_DETAILS)
    @Expose
    var car_details: ArrayList<SearchCarDetailModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.Home.ADVANCE_SEARCH_PARAMS)
    @Expose
    var advance_search_param: AdvanceSearchParamsModel = null ?: AdvanceSearchParamsModel()

    @SerializedName(DataKeyValues.Home.HOME_SEARCH_PARAMS)
    @Expose
    var home_search_param: ArrayList<CarHomeModel> = null ?: ArrayList()

}