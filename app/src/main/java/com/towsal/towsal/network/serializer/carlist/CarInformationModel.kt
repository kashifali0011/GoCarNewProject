package com.towsal.towsal.network.serializer.carlist

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.utils.Constants
import okhttp3.MultipartBody
import java.io.Serializable

/**
 * Response model for car information model
 * */
class CarInformationModel : Serializable {

    @SerializedName(DataKeyValues.USER_ID)
    @Expose
    var user_id: Int = null ?: 0

    @SerializedName(DataKeyValues.CarInfo.YEAR_ID)
    @Expose
    var year_id: Int = null ?: -1

    @SerializedName(DataKeyValues.CarInfo.MAKE_ID)
    @Expose
    var make_id: Int = null ?: -1

    @SerializedName(DataKeyValues.CarInfo.MODEL_ID)
    @Expose
    var model_id: Int = null ?: -1

    var modelName: String = ""
    var makeName: String = ""
    var year: String = ""

    @SerializedName(DataKeyValues.CarInfo.TYPE_ID)
    @Expose
    var carTypeId: Int = null ?: -1

    @SerializedName(DataKeyValues.CarInfo.ODOMETER_ID)
    @Expose
    var odometer: Int = null ?: -1

    @SerializedName(DataKeyValues.CarInfo.ZIP_CODE)
    @Expose
    var zip_code: String? = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.STREET_ADDRESS)
    @Expose
    var street_address: String = null ?: ""

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

    @SerializedName(DataKeyValues.CarInfo.NATIONAL_ID)
    @Expose
    var national_id: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.CAR_FEATURES)
    @Expose
    var car_features: ArrayList<Int> = null ?: ArrayList()
    var car_features_list: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.CarInfo.VIN_CHASIS_NUM)
    @Expose
    var vin_chasis_num: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.TRANSMISSION)
    @Expose
    var transmission: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.CAR_REGISTRATION_CITY_ID)
    @Expose
    var car_registration_city_id: Int = null ?: 0


    @SerializedName(DataKeyValues.CarInfo.DESCRIPTION)
    @Expose
    var description: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.LICENCE_PLATE_NUM)
    @Expose
    var licence_plate_num: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.PIN_ADDRESS)
    @Expose
    var pin_address: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.NEVER_MAJOR_ACCIDENT)
    @Expose
    var never_major_accident: Int = null ?: 0

    @SerializedName(DataKeyValues.CarInfo.LIST_PERSONAL_CAR)
    @Expose
    var list_personal_car: Int = null ?: 0

    @SerializedName(DataKeyValues.CarInfo.STEP)
    @Expose
    var step: Int = null ?: 0


    @SerializedName(DataKeyValues.VAT_REGISTRATION_SNAP)
    var vat_registration_snap: String = null ?: ""

    @SerializedName(DataKeyValues.COMPANY_REGISTRATION_SNAP)
    var company_registration_snap: String = null ?: ""

    @SerializedName(DataKeyValues.HOST_TYPE)
    var hostType: Int = null ?: 0

    @SerializedName(DataKeyValues.User.CR_NUMBER)
    var crNumber: String? = null ?: ""

    @Transient
    var istamarImage: MultipartBody.Part? = null

    @Transient
    var nationalIDImage: MultipartBody.Part? = null

    var istamarImagePath: String = ""
    var vatRegistrationPath = ""
    var companyRegistrationPath = ""

    var nationalIDImagePath: String = ""
    var technicalReportPath: String = ""

    @Transient
    var technicalReport: MultipartBody.Part? = null

    var radius = 0
    var locationSelected = Constants.DeliveryFeeStatus.NOT_APPLIED
    var hideData = false
    var isSelectionMade = false
    var isHideChnageLocation = false
    var fromActivity = false
}