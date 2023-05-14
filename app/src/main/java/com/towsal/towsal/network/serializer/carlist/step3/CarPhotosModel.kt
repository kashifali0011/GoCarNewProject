package com.towsal.towsal.network.serializer.carlist.step3

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for Car photos details
 * */
class CarPhotosModel : Serializable {
    @SerializedName(DataKeyValues.CarInfo.FILE_NAMES)
    @Expose
    var fileNamesCarPhotos: ArrayList<String> = null ?: ArrayList()

    @SerializedName(DataKeyValues.CarInfo.LIST)
    @Expose
    var imagesList: ArrayList<CarPhotoListModel> = null ?: ArrayList()

}