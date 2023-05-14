package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model car status listing
 * */
class StatusListingResponseModel:Serializable{
    @SerializedName(DataKeyValues.VehicleInfoSetting.STATUS)
    @Expose
    var status: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.SNOOZE_START)
    @Expose
    var snooze_start_from: String = null ?: " "

    @SerializedName(DataKeyValues.VehicleInfoSetting.SNOOZE_END)
    @Expose
    var snooze_end_to: String = null ?: " "
}