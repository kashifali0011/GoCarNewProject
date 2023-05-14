package com.towsal.towsal.network.serializer.trips

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for car photos
 * */
class CarPhotosResponseModel : Serializable {

    @SerializedName("image_reading")
    @Expose
    val imageReading: OdoMeterReadingResponseModel? = null

    @SerializedName(DataKeyValues.Trips.IMAGE_INFO)
    @Expose
    var image_info: ArrayList<CarPhotosDetail> = null ?: ArrayList()


    @SerializedName(DataKeyValues.Trips.ACCIDENT_IMAGE_INFO)
    @Expose
    var accident_image_info: ArrayList<CarPhotosDetail> = null ?: ArrayList()

    @SerializedName(DataKeyValues.Trips.DROP_OFF_TIME)
    @Expose
    var dropOffTime: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.STATUS)
    @Expose
    var status: Int = null ?: -1

    @SerializedName(DataKeyValues.Trips.IS_ACCEPT)
    @Expose
    var isAccept: Int = null ?: -1

    @SerializedName(DataKeyValues.Trips.IS_REJECT)
    @Expose
    var isReject: Int = null ?: -1


}