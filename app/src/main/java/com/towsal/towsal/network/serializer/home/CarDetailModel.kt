package com.towsal.towsal.network.serializer.home

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for car details
 * */
class CarDetailModel : Serializable {
    @SerializedName(DataKeyValues.NAME)
    @Expose
    var name: String = null ?: ""

    @SerializedName(DataKeyValues.Home.MODEL)
    @Expose
    var model: String = null ?: ""

    @SerializedName(DataKeyValues.Home.YEAR)
    @Expose
    var year: String = null ?: ""

    @SerializedName(DataKeyValues.Home.RATING)
    @Expose
    var rating: Double = null ?: 0.0

    @SerializedName(DataKeyValues.Home.TRIPS)
    @Expose
    var trips: Int = null ?: 0

    @SerializedName(DataKeyValues.Home.PRICE)
    @Expose
    var price: String = null ?: ""

    @SerializedName(DataKeyValues.Home.DISTANCE)
    @Expose
    var distance: String = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.PICK_UP_STATUS)
    @Expose
    var pick_up_status: Int = null ?: 0

    @SerializedName(DataKeyValues.CarDetails.FAV_FLAG)
    @Expose
    var fav_flag: Int = null ?: 0

    @SerializedName(DataKeyValues.CarDetails.IS_DELIVERY)
    @Expose
    var is_delivery: Int = null ?: 0

    @SerializedName(DataKeyValues.CarDetails.IS_PICKUP_RETURN)
    @Expose
    var is_pickup_return: Int = null ?: 0

    @SerializedName(DataKeyValues.CarDetails.DELIVERY_STATUS)
    @Expose
    var delivery_status: Int = null ?: 0

    @SerializedName(DataKeyValues.IS_FAVOURITE)
    @Expose
    var isFavourite: Int = null ?: 0

    @SerializedName(DataKeyValues.CarDetails.IS_BOOK_IMMEDIATELY)
    @Expose
    var is_book_immediately: Int = null ?: 0

    @SerializedName(DataKeyValues.CarDetails.DESCRIPTION)
    @Expose
    var description: String = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.STATUS)
    @Expose
    var status: Int = null ?: 0


}