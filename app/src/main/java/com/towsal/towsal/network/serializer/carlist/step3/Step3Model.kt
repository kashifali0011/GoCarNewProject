package com.towsal.towsal.network.serializer.carlist.step3

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for Car photos step 3
 * */
class Step3Model : Serializable {
    @SerializedName(DataKeyValues.CarInfo.CAR_PHOTOS)
    @Expose
    var car_photos: CarPhotosModel = null ?: CarPhotosModel()

    var finishActivity = null ?: false

}