package com.towsal.towsal.network.serializer.settings

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel
import java.io.Serializable

/**
 * Response model for location delivery setting
 * */
class LocationDeliveryResponseModel : Serializable {
    @SerializedName(DataKeyValues.VehicleInfoSetting.LOCATION_DELIVERY_RADIUS)
    @Expose
    var location_delivery_redius: ArrayList<CarMakeInfoModel> = null ?: ArrayList()

    @SerializedName(DataKeyValues.VehicleInfoSetting.CAR_LOCATION_DELIVERY_MODEL)
    @Expose
    var car_location_delivery: CarLocationDeliveryModel = null ?: CarLocationDeliveryModel()

}