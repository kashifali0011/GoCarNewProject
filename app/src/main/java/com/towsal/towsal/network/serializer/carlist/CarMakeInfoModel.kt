package com.towsal.towsal.network.serializer.carlist

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for car details model
 * */
class CarMakeInfoModel : Serializable {

    @SerializedName(DataKeyValues.ID)
    @Expose
    var id: Int = null ?: 0

    @SerializedName(DataKeyValues.CarInfo.MAKE_ID)
    @Expose
    var make_id: Int = null ?: 0

    @SerializedName(DataKeyValues.CarInfo.YEAR_ID)
    @Expose
    var year_id: Int = null ?: 0

    @SerializedName(DataKeyValues.Home.CAR_TYPE_ID)
    @Expose
    var carTypeId: Int = null ?: 0

    @SerializedName(DataKeyValues.CarInfo.NAME)
    @Expose
    var name: String = null ?: ""



    @SerializedName(DataKeyValues.CarInfo.YEAR)
    @Expose
    var year: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.IMAGE_URL)
    @Expose
    var image_url: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.RANGE)
    @Expose
    var range: String = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.VALUE)
    @Expose
    var value: String = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.TEXT)
    @Expose
    var text: String = null ?: ""

    /*....trying to get car detial price ....*/
    @SerializedName(DataKeyValues.CarDetails.PRICE)
    @Expose
    var price: String = null ?: " "

    var checked = false
    var isSelect = false

}