package com.towsal.towsal.network.serializer.cities

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for city
 * */
class CityModel : Serializable {
    @SerializedName(DataKeyValues.ID)
    @Expose
    var id: Int = null ?: 0

    @SerializedName(DataKeyValues.City.NAME)
    @Expose
    var name: String = null ?: ""

    @SerializedName(DataKeyValues.City.STATUS)
    @Expose
    var status: String = null ?: ""

    @SerializedName(DataKeyValues.City.STATE_ID)
    @Expose
    var state_id: Int = null ?: 0

    @SerializedName(DataKeyValues.LAT)
    @Expose
    var lat: Double = null ?: 0.0

    @SerializedName(DataKeyValues.LNG)
    @Expose
    var lng: Double = null ?: 0.0

    @SerializedName(DataKeyValues.City.CITY)
    @Expose
    var city: String = null ?: ""

    @SerializedName(DataKeyValues.City.COUNTRY)
    @Expose
    var country: String = null ?: ""

    @SerializedName(DataKeyValues.City.ISO2)
    @Expose
    var iso2: String = null ?: ""

    @SerializedName(DataKeyValues.City.ADMIN_NAME)
    @Expose
    var admin_name: String = null ?: ""

    @SerializedName(DataKeyValues.City.CAPITAL)
    @Expose
    var capital: String = null ?: ""

    @SerializedName(DataKeyValues.City.POPULATION)
    @Expose
    var population: String = null ?: ""

    @SerializedName(DataKeyValues.City.POPULATION_PROPER)
    @Expose
    var population_proper: String = null ?: ""


}