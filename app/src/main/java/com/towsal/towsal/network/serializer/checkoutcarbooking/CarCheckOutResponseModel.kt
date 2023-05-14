package com.towsal.towsal.network.serializer.checkoutcarbooking

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.cardetails.CarCustomAvailability
import com.towsal.towsal.network.serializer.home.CarHomeModel
import com.towsal.towsal.network.serializer.settings.CarLocationDeliveryModel
import java.io.Serializable

/**
 * Response model for Car checkout
 * */
class CarCheckOutResponseModel : Serializable {

    @SerializedName(DataKeyValues.CarCheckOut.HOST_VAT_AMOUNT)
    val hostVatAmount: String? = null ?: "0.00"

    @SerializedName(DataKeyValues.CarCheckOut.HOST_EARNED)
    val hostEarned: String? = null ?: "0.00"

    @SerializedName(DataKeyValues.CarCheckOut.SYSTEM_FEE)
    val systemFee: String? = null ?: "0.00"
    @SerializedName(DataKeyValues.CarCheckOut.CAR_DETAILS)
    @Expose
    var carDetail: CarHomeModel = null ?: CarHomeModel()

    @SerializedName(DataKeyValues.CarDetails.DEPOSIT_AMOUNT)
    @Expose
    var deposit_amount: String = null ?: "0.00"

    @SerializedName(DataKeyValues.CarCheckOut.TRIP_DATE)
    @Expose
    var trip_date: TripDatesModel = null ?: TripDatesModel()

    @SerializedName(DataKeyValues.CarDetails.CAR_LOCATION_DELIVERY)
    @Expose
    var car_location_delivery: CarLocationDeliveryModel = null ?: CarLocationDeliveryModel()

    @SerializedName(DataKeyValues.CarCheckOut.RENT)
    @Expose
    var rent: Double = null ?: 0.0

    @SerializedName(DataKeyValues.CarCheckOut.NUMBER_OF_DAYS)
    @Expose
    var number_of_days: Int = null ?: 0

    @SerializedName(DataKeyValues.CarCheckOut.DAY_WISE_RENT)
    @Expose
    var day_wise_rent: String = null ?: "0.00"

    @SerializedName("day_wise_trip_fee")
    @Expose
    var dayWiseTripFee: String = null ?: ""

    @SerializedName(DataKeyValues.CarCheckOut.DISCOUNT_PERCENTAGE)
    @Expose
    var discountPercentage: Int = null ?: 0

    @SerializedName(DataKeyValues.CarCheckOut.DISCOUNT_AMOUNT)
    @Expose
    var discountAmount: String = null ?: "0.00"

    @SerializedName(DataKeyValues.CarCheckOut.VAT_AMOUNT)
    @Expose
    var vatAmount: String = null ?: "0.00"

    @SerializedName(DataKeyValues.CarCheckOut.SUB_TOTAL)
    @Expose
    var sub_total: String = null ?: "0.00"

    @SerializedName(DataKeyValues.CarCheckOut.TOTAL)
    @Expose
    var total: String = null ?: "0.00"

    @SerializedName(DataKeyValues.CarCheckOut.DELIVERY_FEE)
    @Expose
    var delivery_fee: Int = null ?: 0

    @SerializedName(DataKeyValues.CarDetails.TRIP_DISCOUNT)
    @Expose
    var trip_discount: Int = null ?: 0

    @SerializedName(DataKeyValues.CarDetails.UN_AVAILABLE_DATES)
    @Expose
    var un_available_date: ArrayList<String> = null ?: ArrayList()

    @SerializedName(DataKeyValues.CarCheckOut.WEEKEND_RENT)
    @Expose
    var weekend_rent: Double = null ?: 0.0

    @SerializedName(DataKeyValues.CarCheckOut.FINAL_RENT)
    @Expose
    var final_rent: String = null ?: "0.00"

    @SerializedName(DataKeyValues.CarCheckOut.TOTAL_NUMBER_OF_DAYS)
    @Expose
    var total_number_of_days: Int = null ?: 0

    @SerializedName(DataKeyValues.CarCheckOut.WEEKEND_PRICE)
    @Expose
    var weekend_price: Int = null ?: 0

    @SerializedName(DataKeyValues.CarCheckOut.DISCOUNT_DAY_VALUE)
    @Expose
    var discount_day_value: Int = null ?: 0

    @SerializedName(DataKeyValues.CarInfo.CAR_AVAILABILITY_)
    @Expose
    var car_availability: CarAvailabilityModel = null ?: CarAvailabilityModel()

    @SerializedName(DataKeyValues.CarCheckOut.IS_EXTENDED)
    @Expose
    var is_extended: Int = null ?: 0

    @SerializedName(DataKeyValues.CarCheckOut.DIFFERENCE)
    @Expose
    var difference: String = null ?: "0.00"

    @SerializedName(DataKeyValues.CarCheckOut.NEW_TRIP_TOTAL)
    @Expose
    var newTripTotal: String = null ?: "0.00"

    @SerializedName(DataKeyValues.CarCheckOut.TOTAL_TRIP_FEE)
    @Expose
    var toalTripFee: String = null ?: "0.00"

//    @SerializedName(DataKeyValues.CarCheckOut.TRIP_FEE)
//    @Expose
//    var tripFee: String = null ?: "0.00"

    @SerializedName(DataKeyValues.CarCheckOut.TOTAL_PER_DAY)
    @Expose
    var totalPerDay: String = null ?: "0.00"

    @SerializedName(DataKeyValues.VAT_PERCENTAGE)
    @Expose
    var vatPercentage: String = null ?: "0"

    @SerializedName(DataKeyValues.CarCheckOut.ATTRIBUTES)
    @Expose
    var charges: Charges = null ?: Charges()

    @SerializedName(DataKeyValues.CarCheckOut.DAY_WISE_DISTANCE)
    @Expose
    var perDayDistance: String = null ?: "999999"

    @SerializedName(DataKeyValues.CarCheckOut.TOTAL_ALLOWED_DISTANCE)
    @Expose
    var totalAllowedDistance: String = null ?: "999999"

    @SerializedName(DataKeyValues.CarDetails.DAYS_CUSTOM_AVAILABILITIES)
    @Expose
    var daysCustomAvailability: List<CarCustomAvailability> = null ?: emptyList()

    @SerializedName(DataKeyValues.CarCheckOut.IS_FULLY_CHANGEABLE)
    @Expose
    var isFullyChangeable: Int = null ?: 1

    @SerializedName(DataKeyValues.CarCheckOut.VEHICLE_INSURANCE_EXPIRY)
    @Expose
    var carInsuranceExpiry: String = null ?: ""

    @SerializedName(DataKeyValues.CarCheckOut.PROTECTION_FEE)
    @Expose
    var protectionFee: Int = null ?: 0
}