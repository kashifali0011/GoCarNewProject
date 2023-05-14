package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for car price
 * */
class CarPriceModel : Serializable {

    @SerializedName(DataKeyValues.VehicleInfoSetting.WEEKEND_DAYS)
    @Expose
    var week_end_days: ArrayList<String> = null ?: ArrayList()

    @SerializedName(DataKeyValues.VehicleInfoSetting.CAR_ID)
    @Expose
    var car_id: Int = null ?: 0

    /*@SerializedName(DataKeyValues.VehicleInfoSetting.DEFAULT_PRICE)
    @Expose
    var defaultPrice: Int = null ?: 0*/

    /*....trying to set the price of the trip.....*/

    @SerializedName(DataKeyValues.VehicleInfoSetting.DEFAULT_PRICE)
    @Expose
    var defaultPrice: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.WEEKEND_PRICE)
    @Expose
    var weekendPrice: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.THREE_DAY_PRICE)
    @Expose
    var threeDayDiscountPrice: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.SEVEN_DAY_PRICE)
    @Expose
    var sevenDayDiscountPrice: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.THIRTY_DAY_PRICE)
    @Expose
    var thirtyDayDiscountPrice: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.CUSTOM_PRICES)
    @Expose
    var customPricingList: ArrayList<CustomPriceModel> = null ?: ArrayList()

}