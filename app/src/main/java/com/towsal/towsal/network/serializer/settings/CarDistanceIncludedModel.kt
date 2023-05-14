package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel
import java.io.Serializable

/**
 * Response model for car distance included
 * */
class CarDistanceIncludedModel : Serializable {
    @SerializedName(DataKeyValues.ID)
    @Expose
    var id: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.CAR_ID)
    @Expose
    var car_id: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.UNLIMITED_DISTANCE)
    @Expose
    var unlimited_distance: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.DISTANCE_INCLUDED)
    @Expose
    var distance_included: CarMakeInfoModel? = null ?: CarMakeInfoModel()
}