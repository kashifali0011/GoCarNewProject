package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel
import java.io.Serializable

/**
 * Response model for Car basic detail
 * */
class BasicCarDetailModel : Serializable {
    @SerializedName(DataKeyValues.ID)
    @Expose
    var id: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.CAR_ID)
    @Expose
    var car_id: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.NUMBER_OF_SEAT)
    @Expose
    var number_of_seat: CarMakeInfoModel = null ?: CarMakeInfoModel()

    @SerializedName(DataKeyValues.VehicleInfoSetting.NUMBER_OF_DOOR)
    @Expose
    var number_of_door: CarMakeInfoModel = null ?: CarMakeInfoModel()

    @SerializedName(DataKeyValues.VehicleInfoSetting.FUEL_TYPE)
    @Expose
    var fuel_type: CarMakeInfoModel = null ?: CarMakeInfoModel()

    @SerializedName(DataKeyValues.VehicleInfoSetting.KMP)
    @Expose
    var kmp: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.CAR_FEATURE_SELECTED)
    @Expose
    val car_feature: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

}