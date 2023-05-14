package com.towsal.towsal.network.serializer.cities

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for cities
 * */
class CitiesListModel : Serializable {

    @SerializedName(DataKeyValues.City.CITY_LIST)
    @Expose
    var cityList: ArrayList<CityModel> = null ?: ArrayList()
}