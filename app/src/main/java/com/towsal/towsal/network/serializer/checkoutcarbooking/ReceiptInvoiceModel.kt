package com.towsal.towsal.network.serializer.checkoutcarbooking

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues

/**
 * Response model for trip invoice model
 * */
class ReceiptInvoiceModel {

    @SerializedName("make")
    @Expose
    var make: String? = null

    @SerializedName("model")
    @Expose
    var model: String? = null

    @SerializedName("year")
    @Expose
    var year: String? = null

    @SerializedName("car_id")
    @Expose
    var carId: Int? = null

    @SerializedName("car_image")
    @Expose
    var carImage: String? = null

    @SerializedName("checkout_id")
    @Expose
    var checkoutId: Int? = null

    @SerializedName("hosted_id")
    @Expose
    var hostedId: Int? = null

    @SerializedName("hosted_by")
    @Expose
    var hostedBy: String? = null

    @SerializedName("hosted_to")
    @Expose
    var hostedTo: String? = null

    @SerializedName("guest_id")
    @Expose
    var guestId: Int? = null

//    @SerializedName("host_ratings")
//    @Expose
//    var hostRatings: String = null ?: "0.00"
//
//    @SerializedName("host_trips")
//    @Expose
//    var hostTrips: String? = null

    @SerializedName("booked_at")
    @Expose
    var bookedAt: String? = null

    @SerializedName("pick-up")
    @Expose
    var pickUp: String = null ?: ""

    @SerializedName("drop-off")
    @Expose
    var dropOff: String = null ?: ""

    @SerializedName("vat_amount")
    @Expose
    var vatAmount: String? = null

    @SerializedName("host_vat_amount")
    @Expose
    var hostVatAmount: String? = null

    @SerializedName("gocar_fee")
    @Expose
    val gocarFee: String? = null

    @SerializedName("num_of_days")
    @Expose
    var numOfDays: Int = null ?: 1

    @SerializedName("per_day")
    @Expose
    var perDay: String? = null

    @SerializedName(DataKeyValues.CarCheckOut.EARNED)
    @Expose
    var earned: String? = null

    @SerializedName("pick_up_location")
    @Expose
    var pickUpLocation: String? = null

    @SerializedName("total")
    @Expose
    var total: String = null ?: "0.00"

    @SerializedName("distance")
    @Expose
    var distance: Int = null ?: 0

    @SerializedName("discount")
    @Expose
    var discount: String = null ?: "0.00"

    @SerializedName("discount_day_value")
    @Expose
    var discountDayValue: Int = null ?: 0

    @SerializedName("drop_of_location")
    @Expose
    var dropOfLocation: String? = null

    @SerializedName(DataKeyValues.CarCheckOut.IS_EXTENDED)
    @Expose
    var is_extended: Int = null ?: 0

    @SerializedName(DataKeyValues.CarCheckOut.PIN_LAT)
    @Expose
    var pinLat: Double = null ?: 0.0



    @SerializedName(DataKeyValues.CarCheckOut.PIN_LONG)
    @Expose
    var pinLong: Double = null ?: 0.0

    @SerializedName(DataKeyValues.CarCheckOut.NEW_TRIP_TOTAL)
    @Expose
    var newTripTotal: String = null ?: "0.00"

    @SerializedName("sub_total")
    @Expose
    var subTotal: String = null ?: "0.00"

    @SerializedName("protection_fee")
    @Expose
    var protectionFee: String = null ?: "0.00"

    @SerializedName("late_return_fine")
    @Expose
    var lateReturnFine: String = null ?: "0.00"

    @SerializedName("improper_return_fine")
    @Expose
    var improperReturnFine: String = null ?: "0.00"

    @SerializedName(DataKeyValues.CarCheckOut.TRIP_FEE)
    @Expose
    var tripFee: String = null ?: "0.00"

    @SerializedName("total_fine")
    @Expose
    var totalFine: String = null ?: "0.00"

    @SerializedName("delivery_fee")
    @Expose
    var deliveryFee: String = null ?: "0"

    @SerializedName(DataKeyValues.CarCheckOut.TOTAL_PER_DAY)
    @Expose
    var totalPerDay: String = null ?: "0.00"

    @SerializedName(DataKeyValues.VAT_PERCENTAGE)
    @Expose
    var vatPercentage: String = null ?: "0"


    @SerializedName(DataKeyValues.CarCheckOut.GO_CAR_FEE_PERCENTAGE)
    @Expose
    var goCarFeePercentage: String = null ?: "0"

    @SerializedName(DataKeyValues.CarCheckOut.ATTRIBUTES)
    @Expose
    var charges: Charges = null ?: Charges()


    @SerializedName("host_no_show")
    @Expose
    var hostNoShow: String = null ?: "0.00"

    @SerializedName("failure_to_report")
    @Expose
    var failureToReport: String = null ?: "0.00"

    @SerializedName("cancellation_fine")
    @Expose
    var cancellationFine: String = null ?: "0.00"

    @SerializedName("is_company")
    @Expose
    var isCompany: Boolean = null ?: false


}