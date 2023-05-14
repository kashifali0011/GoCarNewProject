package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel
import java.io.Serializable

/**
 * Response model for car basic details
 * */
class BasicCarResponseModel : Serializable {

    @SerializedName(DataKeyValues.VehicleInfoSetting.CAR_NUMBER_OF_SEATS)
    @Expose
    var car_number_of_seats: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.VehicleInfoSetting.CAR_NUMBER_OF_DOORS)
    @Expose
    var car_number_of_doors: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.VehicleInfoSetting.CAR_FUEL_TYPES)
    @Expose
    var car_fuel_type: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.VehicleInfoSetting.CAR_FEATURES)
    @Expose
    var car_features: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.VehicleInfoSetting.CAR_DETAILS)
    @Expose
    var car_detail: BasicCarDetailModel = null ?: BasicCarDetailModel()

    var car_featuresID: ArrayList<Int> = null ?: ArrayList()


}