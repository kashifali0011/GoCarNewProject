package com.towsal.towsal.network.serializer.carlist

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for car data model
 * */
class CarListDataModel : Serializable {


    var isStepOneApiRequired: Boolean = false
    var isStepThreeApiRequired: Boolean = false
    var isStepFourApiRequired: Boolean = false

    @SerializedName(DataKeyValues.CarInfo.CURRENT_STEP)
    @Expose
    var current_step: Int = null ?: 0

    @SerializedName(DataKeyValues.HOST_TYPE)
    @Expose
    var hostType = null ?: 1

    @SerializedName(DataKeyValues.CarInfo.CAR_EXIST)
    @Expose
    var carExist = null ?: 0

    @SerializedName(DataKeyValues.CarInfo.CAR_MODEL_LIST)
    @Expose
    var car_model: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.CarInfo.CAR_MAKE_LIST)
    @Expose
    var car_make: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.CarInfo.CAR_YEAR_LIST)
    @Expose
    var car_year: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.CarInfo.CAR_TYPES_LIST)
    @Expose
    var carTypes: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.CarInfo.CAR_FEATURES)
    @Expose
    var car_feature: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.CarInfo.CAR_ODOMETER_LIST)
    @Expose
    var odometer: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    var finishActivity = false
    var disableViews = false

}