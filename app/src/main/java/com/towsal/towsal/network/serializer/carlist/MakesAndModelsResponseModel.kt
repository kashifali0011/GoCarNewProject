package com.towsal.towsal.network.serializer.carlist

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues

/**
 * Response model for car make and models
 * */
class MakesAndModelsResponseModel {

    @SerializedName(DataKeyValues.CarInfo.CAR_MAKE_LIST)
    @Expose
    var carMakesList: List<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.CarInfo.CAR_MODEL_LIST)
    @Expose
    var carModelsList: List<CarMakeInfoModel> = null ?: ArrayList()

}