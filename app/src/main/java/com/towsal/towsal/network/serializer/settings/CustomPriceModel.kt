package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

class CustomPriceModel : Serializable {

    @SerializedName(DataKeyValues.VehicleInfoSetting.PRICE)
    var price: String? = null

    @SerializedName(DataKeyValues.VehicleInfoSetting.DAY)
    var key: String = null ?: "Mon"

    @SerializedName(DataKeyValues.VehicleInfoSetting.ORDER)
    var order: Int = 0

    var dayName = ""
}
