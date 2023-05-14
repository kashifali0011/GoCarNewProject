package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel
import java.io.Serializable

/**
 * Response model for Car distance included
 * */
class CarDistanceIncludedResponseModel : Serializable {
    @SerializedName(DataKeyValues.VehicleInfoSetting.DISTANCE_INCLUDED)
    @Expose
    var distance_included: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.VehicleInfoSetting.CAR_DISTANCE_INCLUDED)
    @Expose
    var car_distance_included: CarDistanceIncludedModel = null ?: CarDistanceIncludedModel()

    var car_features: ArrayList<Int> = null ?: ArrayList()
}