package com.towsal.towsal.network.serializer.profile

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.home.SearchCarDetailModel
import java.io.Serializable

/**
 * Response model for user favorite cars
 * */
class FavouriteResponseModel : Serializable {
    @SerializedName(DataKeyValues.Profile.FAVORITE_CARS)
    @Expose
    var fav_car_list: ArrayList<SearchCarDetailModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.FIRST_NAME)
    @Expose
    var firstName: String = null ?: ""

}