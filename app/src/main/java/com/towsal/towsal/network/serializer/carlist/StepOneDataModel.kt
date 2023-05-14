package com.towsal.towsal.network.serializer.carlist

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for car details after first step
 * */
class StepOneDataModel : Serializable {
    @SerializedName(DataKeyValues.CarInfo.STEP)
    @Expose
    var step: Int = null ?: 0

    @SerializedName(DataKeyValues.CarInfo.USER_ADDRESS)
    @Expose
    var user_address: UserAddress = null ?: UserAddress()

    @SerializedName(DataKeyValues.CarInfo.CAR_INFO)
    @Expose
    var carInfoModel: UserCarInfoModel = null ?: UserCarInfoModel()

    @SerializedName(DataKeyValues.CarInfo.YEAR_DATA)
    @Expose
    var year: CarMakeInfoModel = null ?: CarMakeInfoModel()

    @SerializedName(DataKeyValues.CarInfo.MAKE_DATA)
    @Expose
    var make: CarMakeInfoModel = null ?: CarMakeInfoModel()

    @SerializedName(DataKeyValues.CarInfo.MODEL_DATA)
    @Expose
    var model: CarMakeInfoModel = null ?: CarMakeInfoModel()

    @SerializedName(DataKeyValues.CarInfo.ODOMETER)
    @Expose
    var odometer: CarMakeInfoModel = null ?: CarMakeInfoModel()

    @SerializedName(DataKeyValues.CarInfo.CAR_FEATURE_SELECTED)
    @Expose
    val car_feature: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.User.CR_NUMBER)
    var crNumber: String? = null ?: ""

    @SerializedName(DataKeyValues.VAT_REGISTRATION_SNAP)
    var vat_registration_snap: String? = null

    @SerializedName(DataKeyValues.COMPANY_REGISTRATION_SNAP)
    var company_registration_snap: String? = null


    @SerializedName(DataKeyValues.CarInfo.NATIONAL_ID)
    @Expose
    var national_id: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.SNAP_OF_NATIONAL_ID)
    @Expose
    var snap_of_national_id: String? = null


}