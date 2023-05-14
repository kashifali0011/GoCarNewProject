package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for guidelines added by host
 * */
class GuidelinesByHostModel : Serializable {

    @SerializedName(DataKeyValues.VehicleInfoSetting.CAR_ID)
    @Expose
    var car_id: Int = null ?: 0


    @SerializedName(DataKeyValues.ID)
    @Expose
    var id: Int = null ?: 0


    @SerializedName(DataKeyValues.VehicleInfoSetting.TEXT)
    @Expose
    var text: String = null ?: ""

}