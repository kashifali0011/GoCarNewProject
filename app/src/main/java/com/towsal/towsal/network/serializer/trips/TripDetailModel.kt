package com.towsal.towsal.network.serializer.trips

import android.annotation.SuppressLint
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.MessengerModel
import java.io.Serializable
import java.text.SimpleDateFormat

/**
 * Response model for trip details
 * */
class TripDetailModel : Serializable {

    @SerializedName(DataKeyValues.Trips.FUTURE_TIME_CHECK)
    val futureTimeCheck: Int = null ?: 1

    @SerializedName(DataKeyValues.ID)
    @Expose
    var id: Int = null ?: 0
    @SerializedName(DataKeyValues.THREAD_ID)
    @Expose
    var threadId: Int? = null

    @SerializedName(DataKeyValues.Trips.GUEST_ID)
    @Expose
    var guestId: Int = null ?: 0


    @SerializedName(DataKeyValues.Trips.HOST_ID)
    @Expose
    var hostId: Int = null ?: 0

    @SerializedName(DataKeyValues.Trips.CAR_MAKE)
    @Expose
    var car_make: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.CAR_YEAR)
    @Expose
    var car_year: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.CAR_MODEL)
    @Expose
    var car_model: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.ADMIN_CONTACT)
    @Expose
    var admin_contact: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.NAJM_CONTECT)
    @Expose
    var callNum: String = null ?: ""

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

    @SerializedName(DataKeyValues.Trips.PREVIOUS_STATUS)
    @Expose
    var previousStatus: Int = null ?: status

    @SerializedName(DataKeyValues.Trips.CAR_PIN_LAT)
    @Expose
    var car_pin_lat: Double = null ?: 0.0

    @SerializedName(DataKeyValues.Trips.CAR_PIN_LONG)
    @Expose
    var car_pin_long: Double = null ?: 0.0

    @SerializedName(DataKeyValues.Trips.TOTAL_RENT)
    @Expose
    var total_rent: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.HOST_EARING)
    @Expose
    var hostEaring: String = null ?: ""

    @SerializedName("late_return_dropoff")
    @Expose
    var lateReturnDropOff: Boolean = null ?: false

    @SerializedName(DataKeyValues.Trips.HOST_JOIN)
    @Expose
    var host_join: String = null ?: ""
        @SuppressLint("SimpleDateFormat")
        get() {
            if (field.isNotEmpty()) {
                val simpleDateFormat = SimpleDateFormat(Constants.ServerDateFormat)
                val convertDateFormat = SimpleDateFormat(Constants.ResponseFormat)
                val date = simpleDateFormat.parse(field)
                return try {
                    convertDateFormat.format(date!!)
                } catch (e: Exception) {
                    ""
                }
            }
            return field
        }

    @SerializedName(DataKeyValues.Trips.HOST_CONTACT)
    @Expose
    var host_contact: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.HOST_AVG_RESPONSE)
    @Expose
    var host_avg_response: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.HOST_TOTAL_TRIPS)
    @Expose
    var host_total_trips: Int = null ?: 0

    @SerializedName(DataKeyValues.Trips.HOST_NAME)
    @Expose
    var host_name: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.HOST_PROFILE_IMG)
    @Expose
    var host_profile_img: String = null ?: ""

    var pick_up_image_status_orignal_value = ""

    @SerializedName(DataKeyValues.Trips.PICK_UP_IMAGE_STATUS)
    @Expose
    var pick_up_image_status: Int = null ?: 1           //it was String = null ?: " "

    @SerializedName(DataKeyValues.Trips.DROP_OFF_IMAGE_STATUS)
    @Expose
    var drop_off_image_status: Int = null ?: 1

    @SerializedName(DataKeyValues.Trips.CAR_ID)
    @Expose
    var car_id: Int = null ?: -1

    @SerializedName(DataKeyValues.Trips.ACCIDENT_STATUS)
    @Expose
    var accident_status: Int = null ?: 0

    @SerializedName(DataKeyValues.USER_ID)
    @Expose
    var user_id: Int = null ?: 0

    @SerializedName(DataKeyValues.Trips.IS_RATED)
    @Expose
    var is_rated: Int? = null ?: 0

    @SerializedName(DataKeyValues.Trips.BOOKING_STREET_ADDRESS)
    @Expose
    var booking_street_address: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.BOOKING_PIN_LAT)
    @Expose
    var booking_pin_long: Double = null ?: 0.0

    @SerializedName(DataKeyValues.Trips.BOOKING_PIN_LONG)
    @Expose
    var booking_pin_lat: Double = null ?: 0.0

    //EXTENSION request params
    @SerializedName(DataKeyValues.Trips.EXTENSION_STATUS)
    @Expose
    var extension_status: Int = null ?: -1

    @SerializedName(DataKeyValues.Trips.CHANGE_REQ_ID)
    @Expose
    var change_req_id: Int = null ?: 0

    @SerializedName(DataKeyValues.Trips.EXTENSION_PICKUP)
    @Expose
    var extension_pickup: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.EXTENSION_DROPOFF)
    @Expose
    var extension_dropoff: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.TIME_DIFFERENCE)
    @Expose
    var timeDifference: String = null ?: ""

    @SerializedName(DataKeyValues.Trips.PRE_TRIP_IMAGES_STATUS)
    @Expose
    var preTripImagesStatus: Int = null ?: 0

    @SerializedName(DataKeyValues.Trips.POST_TRIP_IMAGES_STATUS)
    @Expose
    var postTripImagesStatus: Int = null ?: 0

    @SerializedName(DataKeyValues.Trips.IS_POST_IMAGE)
    val isPostImage = null ?: 0

    @SerializedName(DataKeyValues.Trips.IS_PRE_IMAGE)
    val isPreImage: Boolean = null ?: false

    @SerializedName(DataKeyValues.Trips.LATE_RETURN)
    @Expose
    var lateReturn: Boolean = null ?: false

    @SerializedName(DataKeyValues.Trips.IS_EDIT_REQUEST)
    @Expose
    var isEditRequest: Boolean = null ?: false

    @SerializedName(DataKeyValues.Trips.IS_DROP_OFF_EXCEED)
    val isDropOffExceed: Boolean? = null ?: false

    @SerializedName(DataKeyValues.Trips.SHOW_EXTENSION_BTN)
    @Expose
    var showExtensionBttn: Boolean? = null ?: false

    @SerializedName(DataKeyValues.MESSENGER)
    val messenger: MessengerModel? = null
}