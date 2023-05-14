package com.towsal.towsal.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.ActionMode
import androidx.appcompat.widget.AppCompatEditText
import com.towsal.towsal.R


/**
 * File for enums. Enum class for trip extension
 * */
enum class ExtensionEnum(val value: Int) {
    NO_EXTENSION(0),
    TRIP_EXTENDED(1),
    TRIP_REDUCED(2),
    KILOMETERS_EXCEEDED(3),
    KILOMETERS_EXCEEDED_AND_TRIP_EXTENDED(4),
    KILOMETERS_EXCEEDED_AND_TRIP_REDUCED(5)
}

/**
 *  Enum class for user availability
 * */
enum class Availability(val value: Int) {
    ALWAYS_AVAILABLE(0),
    UNAVAILABLE(1),
    CUSTOM_HOURS(2)
}

/**
 *  Enum class for days
 * */
enum class Days(val value: String) {
    SUNDAY("sunday"),
    MONDAY("monday"),
    TUESDAY("tuesday"),
    WEDNESDAY("wednesday"),
    THURSDAY("thursday"),
    FRIDAY("friday"),
    SATURDAY("saturday")
}

/**
 *  Enum class for Card Brands
 * */
enum class CardBrand(val value: String) {
    AMEX("amex"),
    MADA("mada"),
    MASTER("master"),
    VISA("visa")
}

enum class ImagePicker(val value: Int) {
    CAMERA(1),
    GALLERY(2)
}
enum class MoveFrom(val value: Int ){
    DriverLicenseInfoActivity(1),
    OTHER_ACTIVITY(2),

}

enum class DocType(val value: String) {
    TECHNICAL_LATEST_REPORT("latest_techical_report"),
    VEHICLE_REGISTRATION_CARD("snap_of_istimara"),
    NATIONAL_ID("snap_of_national_id")
}