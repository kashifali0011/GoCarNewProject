package com.towsal.towsal.network.serializer.checkoutcarbooking

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Request model for booking
 * */
class BookingPostModel : Serializable {

    @SerializedName(DataKeyValues.CarDetails.CAR_ID)
    @Expose
    var car_id: Int = null ?: 0

    @SerializedName(DataKeyValues.CarCheckOut.PICK_UP)
    @Expose
    var pick_up: String = null ?: ""

    @SerializedName(DataKeyValues.CarCheckOut.DROP_OFF)
    @Expose
    var drop_off: String = null ?: ""

    @SerializedName(DataKeyValues.CarCheckOut.IS_BOOKED)
    @Expose
    var is_booked: Int = null ?: 0

    @SerializedName(DataKeyValues.CarCheckOut.IS_ACCEPT)
    @Expose
    var is_accept: Int = null ?: 0

    @SerializedName(DataKeyValues.CarCheckOut.USER_ID)
    @Expose
    var user_id: Int = null ?: 0

    @SerializedName(DataKeyValues.CarCheckOut.DAY_WISE_RENT)
    @Expose
    var rent: String = null ?: "0.00"

    @SerializedName(DataKeyValues.CarCheckOut.NUM_OF_DAYS)
    @Expose
    var number_of_days: Int = null ?: 0


    @SerializedName(DataKeyValues.CarCheckOut.DISCOUNT_AMOUNT)
    @Expose
    var discountAmount: String = null ?: "0.00"


    @SerializedName(DataKeyValues.CarCheckOut.VAT_AMOUNT)
    @Expose
    var vat_amount: String = null ?: "0.0"

    @SerializedName(DataKeyValues.CarCheckOut.SUB_TOTAL)
    @Expose
    var sub_total: String = null ?: "0.00"

    @SerializedName(DataKeyValues.CarCheckOut.TOTAL)
    @Expose
    var total: String = null ?: "0.00"

    @SerializedName(DataKeyValues.CarCheckOut.DELIVERY_FEE)
    @Expose
    var delivery_fee: Int = null ?: 0

    @SerializedName(DataKeyValues.CarCheckOut.DEPOSIT)
    @Expose
    var deposit: String = null ?: "0.00"

    @SerializedName(DataKeyValues.CarCheckOut.DELIVERY_STATUS)
    @Expose
    var delivery_status: Int = null ?: 0


    @SerializedName(DataKeyValues.CarCheckOut.APPLY_DELIVERY_FEE)
    @Expose
    var apply_delivery_fee: Int = null ?: 0

    @SerializedName(DataKeyValues.CarInfo.CITY_NAME)
    @Expose
    var city_name: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.STATE_NAME)
    @Expose
    var state_name: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.COUNTRY_NAME)
    @Expose
    var country_name: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.PIN_LAT)
    @Expose
    var pin_lat: Double = null ?: 0.0

    @SerializedName(DataKeyValues.CarInfo.PIN_LNG)
    @Expose
    var pin_long: Double = null ?: 0.0

    @SerializedName(DataKeyValues.CarInfo.PIN_ADDRESS)
    @Expose
    var pin_address: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.ZIP_CODE)
    @Expose
    var zip_code: String? = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.STREET_ADDRESS)
    @Expose
    var street_address: String = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.IS_BOOK_IMMEDIATELY)
    @Expose
    var is_book_immediately: Int = null ?: 0

    @SerializedName(DataKeyValues.CarDetails.CHECKOUT_ID)
    @Expose
    var checkout_id: Int = null ?: 0

    @SerializedName(DataKeyValues.CarCheckOut.IS_EXTENDED)
    @Expose
    var isExtended: Int = null ?: 0


    @SerializedName(DataKeyValues.CarCheckOut.UPDATED_DAY)
    @Expose
    var updateDays: Int = null ?: 0

    @SerializedName(DataKeyValues.CarCheckOut.REMAINING_AMOUNT)
    @Expose
    var remainingAmount: String = null ?: ""


    @SerializedName(DataKeyValues.CarCheckOut.DAY_WISE_DISTANCE)
    @Expose
    var perDayDistance: String = null ?: "999999"

    @SerializedName("day_wise_trip_fee")
    @Expose
    var dayWiseTripFee: String = null ?: ""

    @SerializedName(DataKeyValues.CarCheckOut.TOTAL_ALLOWED_DISTANCE)
    @Expose
    var totalAllowedDistance: String = null ?: "999999"

    @SerializedName(DataKeyValues.CarCheckOut.TOTAL_TRIP_FEE)
    @Expose
    var totalTripFee: String = null ?: "0.00"

    @SerializedName(DataKeyValues.VAT_PERCENTAGE)
    @Expose
    var vatPercentage: String = null ?: "0"


    @SerializedName(DataKeyValues.CarCheckOut.DISCOUNT_PERCENTAGE)
    @Expose
    var discountPercentage: Int = null ?: 0

    @SerializedName(DataKeyValues.CarCheckOut.HOST_VAT_AMOUNT)
    var hostVatAmount: String? = null ?: "0.00"

    @SerializedName(DataKeyValues.CarCheckOut.HOST_EARNED)
    var hostEarned: String? = null ?: "0.00"

    @SerializedName(DataKeyValues.CarCheckOut.SYSTEM_FEE)
    var systemFee: String? = null ?: "0.00"

    @SerializedName(DataKeyValues.CarCheckOut.PROTECTION_FEE)
    @Expose
    var protectionFee: Int = null ?: 0

}