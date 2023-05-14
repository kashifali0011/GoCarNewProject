package com.towsal.towsal.network.serializer.carlist

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for car details model
 * */
class CustomPriceModel : Serializable {

    @SerializedName(DataKeyValues.ORDER)
    @Expose
    var order: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.DAY)
    @Expose
    var day: String = null ?: ""

    @SerializedName("price")
    @Expose
    var price: String = null ?: ""


}