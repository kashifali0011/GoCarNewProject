package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.home.CarHomeModel
import java.io.Serializable

/**
 * Response model for Car reasons
 * */
class DeleteReasonListModel : Serializable {
    @SerializedName(DataKeyValues.VehicleInfoSetting.DELETE_CAR_REASON_LIST)
    @Expose
    var delete_car_reason_list: ArrayList<CarHomeModel> = null ?: ArrayList()
}