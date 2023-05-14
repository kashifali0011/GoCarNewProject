package com.towsal.towsal.network.serializer.home

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for home screen car types and cars around you
 * */
class CarHomeModel : Serializable {
    @SerializedName(DataKeyValues.ID)
    @Expose
    var id: Int = null ?: 0

    @SerializedName(DataKeyValues.Home.IMAGE_URL)
    @Expose
    var url: String = null ?: ""

    @SerializedName(DataKeyValues.NAME)
    @Expose
    var name: String = null ?: ""

    @SerializedName(DataKeyValues.Home.INFO)
    @Expose
    var carDetailInfo: CarDetailModel = null ?: CarDetailModel()

    @SerializedName(DataKeyValues.LAT)
    @Expose
    var lat: String = null ?: ""

    @SerializedName(DataKeyValues.LONG)
    @Expose
    var long: String = null ?: ""

    var selected = false
}