package com.towsal.towsal.network.serializer.trips

import android.net.Uri
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for car photos details
 * */
class CarPhotosDetail : Serializable {
    @SerializedName(DataKeyValues.ID)
    @Expose
    var id: Int = null ?: 0

    @SerializedName(DataKeyValues.Trips.CAR_ID)
    @Expose
    var car_id: Int = null ?: 0

    @SerializedName(DataKeyValues.Trips.CAR_CHECKOUT_ID)
    @Expose
    var car_checkout_id: Int = null ?: 0

    @SerializedName(DataKeyValues.Trips.ODOMETER_READING)
    @Expose
    var odometer_reading: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.INDEX_ID)
    @Expose
    var index_id: Int = null ?: -1

    @SerializedName(DataKeyValues.Trips.FILE_NAME)
    @Expose
    var file_name: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.COUNTER_PHOTO)
    @Expose
    var counter_photo: String = null ?: ""


    @SerializedName(DataKeyValues.Trips.UPLOAD_BY)
    @Expose
    var upload_by: Int = null ?: 0

    @SerializedName(DataKeyValues.Trips.APPROVED_REJECT_BY)
    @Expose
    var approved_reject_by: Int = null ?: 0

    @SerializedName(DataKeyValues.Trips.STATUS)
    @Expose
    var status: Int = null ?: -1

    @SerializedName(DataKeyValues.Trips.REASON)
    @Expose
    var reason: String = null ?: ""




    @Transient
    var url: String = null ?: ""
    @Transient
    var uri: Uri = null ?: Uri.EMPTY
    @Transient
    var name: String = null ?: ""
    @Transient
    var index = null ?: -1

}