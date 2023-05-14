package com.towsal.towsal.network.serializer.trips

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for trips
 * */
class TripsModel : Serializable {
    @SerializedName(DataKeyValues.ID)
    @Expose
    var id: Int = null ?: 0

    @SerializedName(DataKeyValues.Trips.CAR_MAKE)
    @Expose
    var car_make: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.CAR_YEAR)
    @Expose
    var car_year: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.CAR_MODEL)
    @Expose
    var car_model: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.CAR_PICK_UP)
    @Expose
    var car_pick_up: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.CAR_DROP_OFF)
    @Expose
    var car_drop_off: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.CAR_STREET_ADDRESS)
    @Expose
    var car_street_address: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.CAR_IMAGE)
    @Expose
    var car_image: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.USER_IMAGE)
    @Expose
    var user_image: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.BOOKED_BY)
    @Expose
    var booked_by: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.TOTAL_TRIPS)
    @Expose
    var total_trips: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.RATINGS)
    @Expose
    var ratings: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.VIEW_TYPE)
    @Expose
    var viewType = 0

    @SerializedName(DataKeyValues.Trips.STATUS)
    @Expose
    var status: Int = null ?: -1

    @SerializedName(DataKeyValues.Trips.CAR_PIN_LAT)
    @Expose
    var car_pin_lat: Double = null ?: 0.0

    @SerializedName(DataKeyValues.Trips.CAR_PIN_LONG)
    @Expose
    var car_pin_long: Double = null ?: 0.0

    @SerializedName(DataKeyValues.USER_ID)
    @Expose
    var user_id: Int = null ?: 0

    @SerializedName(DataKeyValues.Trips.TOTAL)
    @Expose
    var total: Double = null ?: 0.0             //it was int before

    @SerializedName(DataKeyValues.Trips.BOOKING_STREET_ADDRESS)
    @Expose
    var booking_street_address: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.BOOKING_PIN_LAT)
    @Expose
    var booking_pin_long: Double = null ?: 0.0

    @SerializedName(DataKeyValues.Trips.BOOKING_PIN_LONG)
    @Expose
    var booking_pin_lat: Double = null ?: 0.0

    @SerializedName(DataKeyValues.Trips.HOST_NAME)
    @Expose
    var host_name: String = null ?: ""

    @SerializedName(DataKeyValues.CREATED_AT)
    @Expose
    var createdAt: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.IS_ACCIDENT)
    @Expose
    var isAccident: Boolean? = null ?: false

}