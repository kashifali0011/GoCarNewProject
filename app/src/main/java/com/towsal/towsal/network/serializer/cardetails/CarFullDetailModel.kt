package com.towsal.towsal.network.serializer.cardetails

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel
import com.towsal.towsal.network.serializer.carlist.CustomPriceModel
import com.towsal.towsal.network.serializer.checkoutcarbooking.CarAvailabilityModel
import com.towsal.towsal.network.serializer.settings.CarLocationDeliveryModel
import com.towsal.towsal.network.serializer.settings.CarPriceModel
import com.towsal.towsal.network.serializer.userdetails.Reviews
import java.io.Serializable

/**
 * Response model for Car full detail model
 * */
class CarFullDetailModel : Serializable {
    @SerializedName(DataKeyValues.CarDetails.USER_STEP)
    val userStep: Int = null ?: 0

    @SerializedName(DataKeyValues.CarDetails.CAR_FEATURES)
    @Expose
    var car_feature: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.CarDetails.CAR_BASIC_FEATURES)
    @Expose
    var car_basic_feature: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.CarDetails.FAV_FLAG)
    @Expose
    var fav_flag: Int = null ?: 0

    @SerializedName(DataKeyValues.CarDetails.CAR_DETAILS)
    @Expose
    var car_detail_info_model: CarDetailInfoModel = null ?: CarDetailInfoModel()

    @SerializedName(DataKeyValues.CarDetails.TRIP_DISCOUNT)
    @Expose
    var trip_discount: CarPriceModel = null ?: CarPriceModel()


    @SerializedName(DataKeyValues.CarDetails.CAR_DISTANCE_INCLUDED)
    @Expose
    var distanceIncludedModel: DistanceIncludedModel = null ?: DistanceIncludedModel()

    @SerializedName(DataKeyValues.CarDetails.HOST_BY)
    @Expose
    var host_by: HostByModel = null ?: HostByModel()

    @SerializedName(DataKeyValues.CarDetails.CAR_GUIDELINES_BY_HOST)
    @Expose
    var car_guideline_by_host: String = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.DEPOSIT_AMOUNT)
    @Expose
    var deposit_amount: Int = null ?: 0

    @SerializedName(DataKeyValues.CarDetails.CAR_LOCATION_DELIVERY)
    @Expose
    var car_location_delivery: CarLocationDeliveryModel = null ?: CarLocationDeliveryModel()

    @SerializedName(DataKeyValues.CarDetails.UN_AVAILABLE_DATES)
    @Expose
    var un_available_date: ArrayList<String> = null ?: ArrayList()

    @SerializedName(DataKeyValues.CarDetails.CAR_CUSTOM_PRICE)
    @Expose
    var carCustomPrice : ArrayList<CustomPriceModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.CarDetails.DROP_OFF_DATE_AND_TIME)        //added now for time on the same booked date
    @Expose
    var drop_off_date_and_time: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.CAR_AVAILABILITY_)
    @Expose
    var car_availability: CarAvailabilityModel = null ?: CarAvailabilityModel()

    @SerializedName(DataKeyValues.CarDetails.DAYS_CUSTOM_AVAILABILITIES)
    @Expose
    var daysCustomAvailability: List<CarCustomAvailability> = null ?: emptyList()

    @SerializedName(DataKeyValues.CarDetails.CAR_REVIEWS)
    @Expose
    var carReviewsList: List<Reviews> = null ?: emptyList()

    @SerializedName(DataKeyValues.CarDetails.CAR_SHARING)
    @Expose
    var carSharing: String = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.VEHICLE_INSURANCE_EXPIRY)
    @Expose
    var carInsuranceExpiry: String? = null ?: ""

    @SerializedName(DataKeyValues.CarDetails.IS_OWN_CAR)
    @Expose
    var isOwnCar = null ?: false

}