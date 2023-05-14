package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for car delivery location
 * */
class CarLocationDeliveryModel : Serializable {
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

    @SerializedName(DataKeyValues.VehicleInfoSetting.DELIVERY_RADIUS)
    @Expose
    var delivery_radius: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.DELIVERY_PRICE)
    @Expose
    var delivery_price: Int? = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.DELIVERY_STATUS)
    @Expose
    var delivery_status: Int? = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.UPDATE_LOCATION)
    @Expose
    var updateLocation: Int? = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.DELIVERY_RADIUS_ID)
    @Expose
    var delivery_radius_id: Int? = null ?: 0


}