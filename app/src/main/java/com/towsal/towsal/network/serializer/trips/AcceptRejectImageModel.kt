package com.towsal.towsal.network.serializer.trips

import okhttp3.MultipartBody
import java.io.Serializable

/**
 * Custom model for accept or reject image
 * */
class AcceptRejectImageModel : Serializable {
    var status: ArrayList<Int> = null ?: ArrayList()
    var reason: ArrayList<String> = null ?: ArrayList()
    var images: ArrayList<MultipartBody.Part> = null ?: ArrayList()
    var cardId = 0
    var car_checkout_id = 0
    var odometer = ""
    var position = -1
    var accept_reject_flag = -1
}