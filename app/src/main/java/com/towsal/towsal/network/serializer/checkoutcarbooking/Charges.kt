package com.towsal.towsal.network.serializer.checkoutcarbooking

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for trips additional charges
 * */
class Charges : Serializable {
    @SerializedName(DataKeyValues.CarCheckOut.UPDATED_DAYS)
    @Expose
    val updatedDays: Int? = null

    @SerializedName(DataKeyValues.CarCheckOut.UPDATED_TRIP_PRICE)
    @Expose
    val updatedTripPrice: String? = null

    @SerializedName(DataKeyValues.CarCheckOut.ADDITION_CHARGES)
    @Expose
    var additionCharges: String = null ?: "0.0"

    @SerializedName(DataKeyValues.CarCheckOut.ALREADY_PAID_TRIP_PRICE)
    @Expose
    val alreadyPaidTripPrice: String? = null

    @SerializedName(DataKeyValues.CarCheckOut.UPDATED_VAT)
    @Expose
    val updatedVat: String? = null

    @SerializedName(DataKeyValues.CarCheckOut.EXCEEDING_KILOMETERS)
    @Expose
    val exceedingKilometers: Int? = null

    @SerializedName(DataKeyValues.CarCheckOut.EXCEEDING_CHARGES)
    @Expose
    val exceedingCharges: String? = null

    @SerializedName(DataKeyValues.CarCheckOut.GO_CAR_FEE_PERCENTAGE)
    @Expose
    val goCarFeePercentage: Int? = null

    @SerializedName(DataKeyValues.CarCheckOut.PREVIOUSLY_EARNED)
    @Expose
    val previouslyEarned: String? = null

    @SerializedName(DataKeyValues.VAT_PERCENTAGE)
    @Expose
    val vatPercentage: String? = null

    @SerializedName(DataKeyValues.CarCheckOut.GO_CAR_FEE)
    @Expose
    val goCarFee: String? = null

    @SerializedName(DataKeyValues.CarCheckOut.TOTAL)
    @Expose
    val total: String? = null

    @SerializedName(DataKeyValues.CarCheckOut.EARNED)
    @Expose
    val earned: String? = null

    @SerializedName(DataKeyValues.CarCheckOut.IS_EXTENDED)
    @Expose
    val is_extended: Int = 0

    @SerializedName(DataKeyValues.CarCheckOut.REMAINING_AMOUNT)
    @Expose
    val remainingAmount: String = null ?: "0.00"
}
