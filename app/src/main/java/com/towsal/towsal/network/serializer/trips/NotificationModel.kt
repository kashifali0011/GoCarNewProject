package com.towsal.towsal.network.serializer.trips

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for notifications
 * */
class NotificationModel : Serializable {

    @SerializedName(DataKeyValues.ID)
    @Expose
    var id: Int = null ?: 0

    @SerializedName(DataKeyValues.Trips.NOTIFICATION_TYPE_ID)
    @Expose
    var notification_type_id: Int = null ?: 0

    @SerializedName(DataKeyValues.Trips.TIME)
    @Expose
    var time: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.NOTIFICATION_TITLE)
    @Expose
    var notification_title: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.NOTIFICATION_DETAIL)
    @Expose
    var notification_detail: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.NOTIFICATION_TYPE)
    @Expose
    var not_type: Int = null ?: 0

    @SerializedName(DataKeyValues.Trips.SENDER_PROFILE_IMAGE)
    @Expose
    var sender_profile_image: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.NOTIFICATION_IMAGE)
    @Expose
    var profile_image: String? = null

    @SerializedName(DataKeyValues.Trips.SENDER_ID)
    @Expose
    var sender_id: Int = null ?: 0

    @SerializedName(DataKeyValues.Trips.VIEW_TYPE)
    @Expose
    var view_type: Int = null ?: 0

    @SerializedName(DataKeyValues.Trips.CAR_ID)
    @Expose
    var car_id: Int = null ?: -1


}