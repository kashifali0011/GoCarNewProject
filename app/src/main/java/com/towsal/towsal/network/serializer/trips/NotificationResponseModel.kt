package com.towsal.towsal.network.serializer.trips

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for notifications
 * */
class NotificationResponseModel : Serializable {

    @SerializedName(DataKeyValues.Trips.PAGINATION)
    @Expose
    var pagination: PaginationModel = null ?: PaginationModel()


    @SerializedName(DataKeyValues.Trips.NOTIFICATIONS)
    @Expose
    var notificationList: ArrayList<NotificationModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.Trips.FILTERS)
    @Expose
    var filters: ArrayList<FilterTripsModel> = null ?: ArrayList()
}