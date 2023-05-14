package com.towsal.towsal.network.serializer.trips

import java.io.Serializable

/**
 * Response model for validate calendar
 * */
class CalendarValidateResponseModel : Serializable{
    var availability: Boolean = null ?: false
}