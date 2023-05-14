package com.towsal.towsal.network.serializer.cardetails

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel
import java.io.Serializable

/**
 * Response model for Car distance included by host
 * */
class DistanceIncludedModel : Serializable {

    @SerializedName(DataKeyValues.CarDetails.KM)
    @Expose
    var km_included: String = null ?: ""


    @SerializedName(DataKeyValues.CarDetails.SAR_CHARGE)
    @Expose
    var charge: String = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.CAR_ID)
    @Expose
    var car_id: Int = null ?: 0


    @SerializedName(DataKeyValues.CarDetails.UNLIMITED_DISTANCE)
    @Expose
    var unlimited_distance: Int = null ?: 0

    @SerializedName(DataKeyValues.CarDetails.DISTANCEINCLUDED)
    @Expose
    var distanceincluded:CarMakeInfoModel = null ?: CarMakeInfoModel()

}