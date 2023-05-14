package com.towsal.towsal.network.serializer.home

import java.io.Serializable

/**
 * Request model for searching cars
 * */
class CarSearchModel : Serializable {
    var lat = 0.0
    var lng = 0.0
    var cityID = 0
    var carTypeId = 0
    var sort_by = 0
    var price = ""
    var distance_id = 0
    var car_location = 0
    var delivery_fee = ""
    var year = -1
    var make = 0
    var model = 0
    var odometer = 0
    var bookInstantly = 0
    var date_from = ""
    var date_to = ""
    //    var city_id = 0

}