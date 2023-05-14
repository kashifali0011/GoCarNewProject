package com.towsal.towsal.network.serializer.carlist.step5

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel
import java.io.Serializable

/**
 * Response model for Car step 5
 * */
class Step5Model:Serializable {
    @SerializedName(DataKeyValues.CarInfo.CAR_SAFETY_INFO)
    @Expose
    var car_safety_info: CarSafetyInfoModel = null ?: CarSafetyInfoModel()

    @SerializedName(DataKeyValues.CarInfo.CAR_SAFETY_LIST)
    @Expose
    var car_safety_quality_standard_list: ArrayList<CarMakeInfoModel> = null ?: ArrayList()


}