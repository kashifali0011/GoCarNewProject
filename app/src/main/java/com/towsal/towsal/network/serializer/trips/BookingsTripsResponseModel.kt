package com.towsal.towsal.network.serializer.trips

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for trips with pagination
 * */
class BookingsTripsResponseModel : Serializable {

    @SerializedName(DataKeyValues.Trips.PAGINATION)
    @Expose
    var pagination: PaginationModel = null ?: PaginationModel()

    @SerializedName(DataKeyValues.Trips.BOOKINGS)
    @Expose
    var bookings: ArrayList<TripsModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.Trips.IS_DATA_EXISTS)
    @Expose
    var isDataExists: Boolean = null ?: false

    @SerializedName(DataKeyValues.Trips.BOOKING_HISTORY)
    @Expose
    var bookinghistory: ArrayList<TripsModel> = null ?: ArrayList()


}