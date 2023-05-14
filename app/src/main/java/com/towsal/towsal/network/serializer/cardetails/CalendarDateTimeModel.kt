package com.towsal.towsal.network.serializer.cardetails

import java.io.Serializable

/**
 * Model for calendar screen handling
 * */
class CalendarDateTimeModel : Serializable {
    var pickupDate = ""
    var pickupTime = ""
    var dropOffDate = ""
    var dropOffTime = ""
    var dropOffTimeServer = ""
    var pickupTimeServer = ""
    var pickUpDateServer = ""
    var dropOffDateServer = ""
    var comparePickupDate = ""
    var compareDropOffDate = ""
    var pickUpDateComplete = ""
    var dropOffDateComplete = ""
    var minimumDateTime = ""
    var minimumDurationDateTime = ""
    var minimumDateOnly = ""
    var minimumDateTimeDefault = ""
    var minimumDurationDateTimeDefault = ""
    var minimumDateOnlyDefault = ""
    var carId = -1

}