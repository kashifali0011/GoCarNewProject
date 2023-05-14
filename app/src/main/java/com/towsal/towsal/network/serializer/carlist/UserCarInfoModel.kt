package com.towsal.towsal.network.serializer.carlist

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.cities.CityModel
import java.io.Serializable

/**
 * Response model for user car info model
 * */
class UserCarInfoModel : Serializable {

    @SerializedName(DataKeyValues.ID)
    @Expose
    var id: Int = null ?: 0

    @SerializedName(DataKeyValues.CarInfo.VIN_CHASIS_NUM)
    @Expose
    var vin_chasis_num: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.LICENCE_PLATE_NUM)
    @Expose
    var licence_plate_num: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.TRANSMISSION)
    @Expose
    var transmission: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.NEVER_MAJOR_ACCIDENT)
    @Expose
    var never_major_accident: Boolean = null ?: false

    @SerializedName(DataKeyValues.CarInfo.LIST_PERSONAL_CAR)
    @Expose
    var list_personal_car: Boolean = null ?: false

    @SerializedName(DataKeyValues.CarInfo.CAR_REGISTRATION_CITY)
    @Expose
    var car_registration_city: CityModel = null ?: CityModel()

    @SerializedName(DataKeyValues.CarInfo.DESCRIPTION)
    @Expose
    var description: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.LATEST_TECHNICAL_REPORT)
    @Expose
    var latest_techical_report: String = null ?: ""


    @SerializedName(DataKeyValues.CarInfo.NATIONAL_ID)
    @Expose
    var national_id: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.SNAP_OF_NATIONAL_ID)
    @Expose
    var snap_of_national_id: String? = null

    @SerializedName(DataKeyValues.CarInfo.SNAP_OF_ISTAMARA)
    @Expose
    var snap_of_istimara: String = null ?: ""

}