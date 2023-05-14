package com.towsal.towsal.network.serializer.carlist.step2

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel
import java.io.Serializable

/**
 * Request model for Car detail info
 * */
class CarAvailabilityPostModel : Serializable {
    @SerializedName(DataKeyValues.ID)
    @Expose
    var id: Int = null ?: 0

    @SerializedName(DataKeyValues.CarInfo.CAR_ID)
    @Expose
    var car_id: Int = null ?: 0

    @SerializedName(DataKeyValues.CarInfo.ADVANCE_NOTICE)
    @Expose
    var adv_notice_trip_start: CarMakeInfoModel = null ?: CarMakeInfoModel()

    @SerializedName(DataKeyValues.CarInfo.MIN_DUR)
    @Expose
    var min_trip_dur: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.MAX_DUR)
    @Expose
    var max_trip_dur: String = null ?: ""

    @SerializedName(DataKeyValues.CarInfo.STEP)
    @Expose
    var step: Int = null ?: 0

    @SerializedName(DataKeyValues.CarInfo.ADVANCE_NOTICE_TRIP_ID)
    @Expose
    var adv_notice_trip_start_id: Int = null ?: 0

    @SerializedName(DataKeyValues.CarInfo.IS_PICKUP_RETURN)
    @Expose
    var is_pickup_return: Int = null ?: 0

    @SerializedName(DataKeyValues.CarInfo.IS_DELIVERY)
    @Expose
    var is_delivery: Int = null ?: 0

    @SerializedName(DataKeyValues.CarDetails.DELIVERY_STATUS)
    @Expose
    var delivery_status: Int = null ?: 0


}