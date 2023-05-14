package com.towsal.towsal.network.serializer.checkoutcarbooking

import java.io.Serializable

/**
 * Request model for Car booking
 * */
class GetCarBookingModel : Serializable {
    var car_id = 0
    var pick_up = ""
    var drop_off = ""
    var apply_delivery_fee = 0
    var city_id: Int? = null
    var lng = 0.0
    var lat = 0.0
    var checkout_id: Int = null ?: -1

   /* var city_name = ""
    var country_name = ""
    var street_address = ""
    var state_name = ""*/
}