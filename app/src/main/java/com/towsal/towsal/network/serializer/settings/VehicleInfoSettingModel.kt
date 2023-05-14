package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for vehicle settings
 * */
class VehicleInfoSettingModel : Serializable {
    /*@SerializedName(DataKeyValues.VehicleInfoSetting.FILE_NAME)
    @Expose
    var file_name: String = null ?: ""*/

    //adding six images here
    @SerializedName(DataKeyValues.VehicleInfoSetting.IMAGES_URL)
    @Expose
    var images: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.RATING)
    val rating: String = null ?: "0.0"

    @SerializedName(DataKeyValues.ID)
    @Expose
    var id: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.STATUS)
    @Expose
    var status: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.ACTIVATION)
    @Expose
    var isCarLive: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.ACTIVATION_MESSAGE)
    @Expose
    var carStatusMessage: String? = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.ACC_STATUS)
    @Expose
    var acc_status: Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.SNOOZE_START)
    @Expose
    var snooze_start_from: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.SNOOZE_END)
    @Expose
    var snooze_end_to: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.YEAR_DATA)
    @Expose
    var year: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.MAKE_DATA)
    @Expose
    var make: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.CAR_MODEL)
    @Expose
    var model: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.PLATE_NUMBER)
    @Expose
    var plateNumber: String = null ?: ""

    @SerializedName(DataKeyValues.VehicleInfoSetting.PICK_UP)
    @Expose
    var pickUp: String? = null

    @SerializedName(DataKeyValues.VehicleInfoSetting.DROP_OFF)
    @Expose
    var dropOff: String? = null

    @SerializedName(DataKeyValues.VehicleInfoSetting.CAR_DETAILS_COMPLETE)
    @Expose
    var car_details_complete : Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.CAR_PRICING_COMPLETE)
    @Expose
    var car_pricing_complete : Int = null ?: 0

    @SerializedName(DataKeyValues.VehicleInfoSetting.IS_PROTECTION)
    @Expose
    var isProtection : Int = null ?: 0


}