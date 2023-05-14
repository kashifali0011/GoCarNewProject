package com.towsal.towsal.network.serializer.cardetails

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.home.CarDetailModel
import java.io.Serializable

/**
 * Response model for Car detail info
 * */
class CarDetailInfoModel : Serializable {
    @SerializedName(DataKeyValues.ID)
    @Expose
    var id: Int = null ?: 0
    @SerializedName(DataKeyValues.CarDetails.IMAGES_URL)
    @Expose
    var images: ArrayList<String> = null ?: ArrayList()

    @SerializedName(DataKeyValues.Home.INFO)
    @Expose
    var carDetail: CarDetailModel = null ?: CarDetailModel()

    @SerializedName(DataKeyValues.USER_ID)
    @Expose
    var user_id: Int = null ?: 0

}