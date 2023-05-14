package com.towsal.towsal.utils

import com.towsal.towsal.R
import java.util.regex.Pattern

/**
 * Class for application constants
 * */
object Constants {

    const val EXIST = 1
    const val NOT_EXIST = 0
    const val BASE_URL = "http://52.59.56.185"
    const val NOTIFICATION_URL = "$BASE_URL:80/notification"
    const val CONNECTION_TIMEOUT = 5000
    const val LOG_TAG = "msdk.demo"
    const val IS_FROM_WITHIN_APP = "IS_FROM_WITHIN_APP"
    const val BROCAST_RECIEVER = "BROCAST_RECIEVER"
    const val TYPE = "type"
    const val CHANGE_LANGUAGE = 1
    const val SHOW_LOADER = 3
    const val HIDE_LOADER = 4
    const val MESSAGE = 2
    const val PREF_NAMES: String = "preference"
    const val PREF_NAMES_OTHER: String = "preference_other"
    const val USER_MODEL: String = "USER_MODEL"
    const val LANGUAGE = "language"
    const val BUNDLE_VALUE: String = "BUNDLE_VALUE"
    const val ENGLISH = "en"
    const val ARABIC = "ar"
    const val USER_TYPE_GUEST = 1
    const val USER_TYPE_HOST = 2
    const val LAT = "lat"
    const val LNG = "lng"
    const val CITY_ID = "city_id"
    const val CAR_ID = "CAR_ID"
    const val MOVE_VIEW_LIST = "move_view_list"
    const val CAR_TYPE_ID = "CAR_TYPE_ID"
    const val ServerDateTimeFormat = "yyyy-MM-dd HH:mm:ss"
    const val ServerDateTimeFormatT = "vyyy-MM-dd'T'HH:mm:ss"
    const val ServerDateTimeFormatOther = "dd-MM-yyyy hh:mm aa"
    const val ServerDateTimeFormatOther1 = "dd-MM-yyyy hh:mm a"
    const val CONST_DEFAULT_IMAGE_WIDTH = 1024


    const val localTimeFormat = "EEE MMM dd hh:mm:ss zzz yyyy"

    const val UIDateTimeFormat = "EEE, MMM dd, hh:mm a"
    const val UIDateTimeWithYearFormat = "EEE, MMM dd yyyy, h:mm a"
    const val UIDateFormat = "EEE, MMM dd"
    const val UIDateTime1Format = "EEE, MMM dd, hh:mm a"
    const val UITimeFormat = "hh:mm a"
    const val UIDateTimeFormat1 = "MMM dd, yyyy hh:mm a"
    const val ServerDateFormat = "yyyy-MM-dd"
    const val ResponseFormat = "MMM, yyyy"
    const val ResponseFormatY = "MMM yyyy"
    const val DayFormat = "EEE"
    const val MonthYearFormat = "MMMM yyyy"
    const val DateFormatWithDay = "dd MMM yyyy"
    const val MonthDayFormat = "MMM dd"
    const val ServerResponseDateTimeFormat = "MMM dd, YYYY"
    const val CUSTOM_DATE_TIME_FORMAT = "MMM dd, yyyy - hh:mm a" // MAR 16, 2022 - 1:22 PM
    const val TIME_FORMAT_24_HOURS = "HH:mm"
    const val AM_PM_TIME_FORMAT = "hh:mm a"


    const val ADDRESS = "address"
    const val ADVANCE_SEARCH_PARAM = "advance_search_param"
    const val TRIGGER_NOTIFICATION = "TriggerNotification"
    const val ICON = "icon"

    val RESULT_CODES: List<String> = listOf(
        "000.000.000",
        "000.000.100",
        "000.100.110",
        "000.100.111"
    )

    val PASSWORD_PATTERN: Pattern = Pattern.compile(
        "^" +
                "(?=.*[a-z])" +  //any letter
                "(?=.*[A-Z])" +  //any letter
                "(?=.*[@$!%_*#?&-])" +  //at least 1 special character
                ".{8,}" +  //at least 6 characters
                "$"
    )

    const val ENABLED = 1
    const val DISABLED = 0

    const val MIME_TYPES = "image/*"

    object API {
        const val LOGIN = 1
        const val CHANGE_LANGUAGE = 2
        const val REGISTER = 3
        const val OTP_VERIFY = 4
        const val RESEND_OTP = 5
        const val CITIES_LIST = 6
        const val REST_PASS_REQUEST = 7
        const val REST_PASSWORD = 8
        const val GET_CAR_LIST = 9
        const val SEND_CAR_DATA = 10
        const val HOME_SCREEN = 11
        const val CAR_DETAILS = 12
        const val VEHICLE_INFO = 13
        const val GET_CAR_PRICE = 14
        const val SEND_CAR_PRICE = 15
        const val UPDATE_CAR_STATUS = 16
        const val GET_CAR_REASON_LIST = 17
        const val DELETE_CAR_LISTING = 19
        const val GET_CAR_CALENDAR = 20
        const val SEND_CALENDAR_DATE = 21
        const val GET_CAR_PREFERENCES = 22
        const val SAVE_CAR_PREFERENCES = 23
        const val GET_GUIDELINES_BY_HOST = 24
        const val SAVE_GUIDELINES_BY_HOST = 25
        const val GET_DISTANCE_INCLUDED = 26
        const val SAVE_DISTANCE_INCLUDED = 27
        const val GET_CAR_DETAIL = 28
        const val SAVE_CAR_DETAIL = 29
        const val SEARCH_CARS = 30
        const val GET_LOCATION_DELIVERY = 31
        const val SAVE_LOCATION_DELIVERY = 32
        const val GET_USER_INFO = 33
        const val SAVE_USER_INFO = 34
        const val GET_CHECKOUT_DETAIL = 35
        const val SAVE_CHECKOUT_DETAIL = 36
        const val LOGOUT = 37
        const val GET_ALL_NOTIFICATIONS = 38
        const val GET_MY_BOOKINGS = 39
        const val GET_MY_PREVIOUS_BOOKINGS = 40
        const val GET_GUEST_BOOKING_DETAIL = 41
        const val GET_HOST_BOOKING_DETAIL = 42
        const val ACCEPT_BOOKING = 43
        const val REJECT_BOOKING = 44
        const val CANCEL_BOOKING = 45
        const val REJECT_BOOKING_REASON = 46
        const val CANCEL_BOOKING_REASON = 47
        const val ADD_PICKUP_IMAGES = 48
        const val ADD_DROP_OFF_IMAGES = 49
        const val VIEW_PICKUP_IMAGES = 50
        const val ACCEPT_REJECT_PICKUP_IMAGES = 51
        const val ACCEPT_REJECT_DROP_OFF_IMAGES = 52
        const val ADD_ACCIDENT_PHOTOS = 53
        const val VIEW_DROP_OFF_IMAGES = 54
        const val VIEW_ACCIDENT_IMAGES = 55
        const val GET_PERFORMANCE_SETTING = 56
        const val GET_HOST_REVIEW = 57
        const val ADD_REVIEW = 58
        const val GET_EARNING_REPORT = 59
        const val GET_MESSAGES = 60
        const val GET_MESSAGE_DETAIL = 61
        const val SEND_MESSAGE = 62
        const val GET_PROFILE = 63
        const val UPDATE_IMAGE = 64
        const val ACCOUNT_INFO = 65
        const val UPDATE_ACCOUNT_INFO = 66
        const val DELETE_FAV_CAR = 67
        const val ADD_FAV_CAR = 68
        const val GET_FAV_LIST = 69
        const val GET_TRANSACTION_HISTORY = 70
        const val GET_TRANSACTION_DETAIL = 71

        /*...change pass const.....*/
        const val CHANGE_PASSWORD = 72
        const val SEEN_MESSAGE = 73         //Message seen const
        const val READ_NOTIFICATION = 74  //notification read const
        const val GET_Guest_REVIEW = 75
        const val EDIT_TRIP_BOOKING = 76
        const val CAR_IS_ALWAYS_AVAILABLE = 77
        const val GET_CAR_AVAILABILITY_INFO = 78
        const val SET_CAR_UNAVAILABILITY = 79
        const val RESEND_EMAIL = 80
        const val VERIFY_EMAIL = 81
        const val UPDATE_CHECKOUT_CHANGINGS = 82
        const val GET_USER_DETAILS = 83
        const val SUGGEST_EXTENSION = 84
        const val RECEIPT_INVOICE_DETAILS = 85
        const val CALENDAR_VALIDATOR = 86
        const val MAKE_SELECTION = 87
        const val MODEL_SELECTION = 88
        const val CAR_DOCUMENTS = 89
        const val UPDATE_CAR_DOCUMENTS = 90
        const val PREPARE_CHECKOUT = 91
        const val PAYMENT_STATUS = 92
        const val ADD_PAYMENT_METHOD = 93
        const val REMOVE_PAYMENT_METHOD = 94
        const val GET_PAYMENT_METHODS = 95
        const val SET_DEFAULT_PAYMENT_METHOD = 96
        const val PRE_HOST_TRIP_PHOTOS = 97
        const val COMPLETE_TRIP = 98
        const val CARS_FEATURES_LIST = 99
        const val TECHNICAL_REPORT = 100
        const val SNAP_OF_NATIONAL_ID = 101
        const val SNAP_OF_DRIVING_LICENSE = 102
        const val CAR_ATTRIBUTES = 103
        const val UPLOAD_PHOTO = 104
        const val CANCELLATION_IMAGES = 105
        const val GET_MY_VEHICLES = 106
        const val VAT_REGISTRATION = 107
        const val FUEL_LEVEL = 108
        const val CARD_REGISTRATION_STATUS = 109
        const val HOLD_PAYMENT = 110
        const val CREATE_THREAD_ID = 111
        const val LATE_RETURN = 112
        const val GET_REPORT_REASON = 113


    }

    object ResponseCodes {
        const val SUCCESS = 200
        const val UNAUTHORIZED = 401
        const val ALREADY_VERIFIED = 423
        const val NOT_FOUND = 404
        const val ALREADY_VERIFY = 409
    }

    object DataParsing {
        const val REASON_ID = "reason_id"
        const val IS_UPDATE_ONLY = "is_update_only"
        const val IS_API_NEEDED = "is_api_needed"
        const val THANK_YOU_MODEL = "thank_you_model"
        const val HYPER_PAY_CHECKOUT_ID = "hyper_pay_checkout_id"
        const val HOST_NAME = "host_name"
        const val IS_FIRST_RUN = "is_first_run"
        const val USER_ID = "USER_ID"
        const val MOVE_TO_DRIVER_INFO = "DIRECTION"
        const val PHONE_NUMBER = "PHONE_NUMBER"
        const val Email = "Email"
        const val GOTO_LOGIN = "GOTO_LOGIN"
        const val CITY_SELECTED_ID = "CITY_SELECTED_ID"
        const val CITY_SELECTED = "CITY_SELECTED"
        const val CUSTOM_DAYS = "CUSTOM_DAYS"
        const val CITY_LAT = "CITY_LAT"
        const val CITY_LNG = "CITY_LNG"
        const val RESET_PASS_TOKEN = "RESET_PASS_TOKEN"
        const val MODEL = "Model"
        const val CARINFODATAMODEL = "CARINFODATAMODEL"
        const val HIDE_CHANGE_LOCATION = "CARINFODATAMODEL"
        const val LOGIN_RESULT = "LOGIN_RESULT"
        const val DATE = "DATE"
        const val DATECOMPLETEMODEL = "DATECOMPLETEMODEL"
        const val GOTO_PHOTOS = "GOTO_PHOTOS"
        const val DATE_TIME_MODEL = "DATE_TIME_MODEL"
        const val DISABLE_VIEWS = "DISABLE_VIEWS"
        const val LIST = "list"
        const val ID = "id"
        const val THREAD_ID_MESSAGE = ""
        const val HEADING = "heading"
        const val REVIEW = "review"
        const val RATING = "rating"
        const val IS_PICKUP = "pickup"
        const val MODEL_OTHER = "model_other"
        const val VIEW_TYPE = "view_type"
        const val CAR_ID = "car_id"
        const val CAR_IMAGE_STATUS_PICKUP = "car_image_status_pickup"
        const val CAR_IMAGE_STATUS_DROP_OFF = "car_image_status_drop_off"
        const val POSITION = "position"
        const val RECEIVER_ID = "receiver_id"
        const val MESSAGE_TYPE_ID = "message_type_id"
        const val FROM_NOTIFICATION = "from_notification"
        const val SAVE_OBJECT = "save_object"
        const val IMAGE = "image"
        const val IMAGES = "images"
        const val SENDER_PROFILE_IMAGE = "sender_profile_image"
        const val NAME = "name"
        const val SENDER_ID = "sender_id"
        const val MESSAGE_ID = "message_id"
        const val FLOW_COMING_FROM = "flow_coming_from"
        const val CHECKOUT_ID = "checkout_id"
        const val GRAND_TOTAL = "grand_total"
        const val CHANGE_REQ_ID = "change_req_id"
        const val DROP_OFF_DATE_TIME = "drop_off_date_time"
        const val ACTIVITY_NAME = "activity_name"
        const val FAVORITE_FLAG = "favorite_flag"
        const val PAYMENT_METHOD_TYPE = "payment_method_type"
        const val CARD_NUMBER = "card_number"
        const val CARD_HOLDER_NAME = "card_holder_name"
        const val EXPIRY = "expiry_date"
        const val CVV = "cvv"
        const val ACCOUNT_NUMBER = "account_number"
        const val ACCOUNT_TITLE = "account_title"
        const val PAYMENT_BRAND = "payment_brand"
        const val REVIEWS_LIST = "reviews_list"
        const val CAR_LIST_DATA_MODEL = "car_list_data_model"
        const val IS_HOST = "is_host"
        const val FILE_TYPE = "file_type"
        const val THREAD_ID = "threadId"
        const val TEXT_MESSAGE = "message"
        const val USER_THREAD_ID = "user_threat_id"
        const val SUPPORT_NUMBER = "support_number"

    }

    object PopupCallbackConstants {
        const val PermissionAllow = 1
        const val PermissionDontAllow = 2           //it was set as 2
        const val SelectCity = 3
    }

    object RequestCodes {
        const val ACTIVTIY_LOCATION = 101
        const val ACTIVITY_CAR_INFO = 102
        const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 103
        const val ACTIVITY_CAR_FEATURE = 104
        const val ACTIVITY_LOGIN = 105
        const val ACTIVTIY_PRICING = 106
        const val ACTIVTIY_STATUS = 107
        const val ACTIVTIY_DELETE_LISTING = 108
        const val ACTIVTIY_BOOK_DATE = 109
        const val ACTIVTIY_LOCATION_SELECTION = 110
        const val ACTIVITY_TRIPS = 111
        const val GUEST_TRIP_DETAIL = 112
        const val HOST_TRIP_DETAIL = 113
        const val ACTIVITY_IMAGES = 114

        const val REJECT_BOOKING = 120
        const val FLOW_COMING_FROM = 115      //for redirecting it into history fragment
        const val PICKUP_REJECTION_IMAGES = 116       //giving large view at pickup

        const val ACTIVITY_CHECKOUT_CAR_BOOKING = 117
        const val ACTIVITY_CAR_DETAIL = 118


    }

    object LoginFrom {
        const val SearchHomeFragment = 1
    }

    object ExtensionStatus {
        const val APPROVAL_PENDING = 0
        const val ACCEPT_REQUEST = 1
        const val REJECT_REQUEST = 2
    }

    object CarStep {
        const val STEP_1_COMPLETED = 1
        const val STEP_2_COMPLETED = 2
        const val STEP_3_COMPLETED = 3
        const val STEP_4_COMPLETED = 4
        const val STEP_5_COMPLETED = 5
    }

    object CancellationPolicy {
        const val FLIGHT_DELAY = 10
        const val DISINFECTION = 12
        const val LOST_BAGGAGE = 18
    }

    object CancellationType {
        const val FLIGHT_DELAY = 1
        const val LOST_BAGGAGE = 2
        const val DISINFECTION = 3
    }

    object Container {
        const val SEARCH_CAR_ACTIVITY_CONTAINER = R.id.frameLayout
        const val TRIPS_FRAGMENT_CONTAINER = R.id.frameLayoutTripsFragment
        const val BOOKED_TRIPS_FRAGMENT_CONTAINER = R.id.frameLayoutBookedTripsFragment
        const val HISTORY_TRIPS_FRAGMENT_CONTAINER = R.id.frameLayoutHistoryTripsFragment

    }

    object MoveFromList {
        const val moveFrom = 1
    }

    object CarPricing {
        const val DEFAULT_PRICE = 1
        const val DISCOUNT_PRICE = 2
        const val CUSTOM_PRICE = 3
    }

    object CarPhotos {
        const val GUEST_ACCIDENT_PHOTOS = 1
        const val GUEST_PICKUP_PHOTOS = 2
        const val HOST_PICKUP_PHOTOS = 3
        const val GUEST_DROP_OFF_PHOTOS = 4
        const val HOST_DROP_OFF_PHOTOS = 5
        const val HOST_ACCIDENT_PHOTOS = 6
        const val HOST_REJECT_ALL_IMAGES = 7
        const val FROM_NOTIFICATION = 8
        const val HOST_DROP_OFF_ONE_REJECTED_PHOTO = 9

    }

    object BookingStatus {
        const val PENDING = 0
        const val FUTURE = 1
        const val IN_PROGRESS = 2
        const val COMPLETED = 3
        const val ACCEPTED = 4
        const val REJECTED = 5
        const val CANCELLED = 6
        const val CONFLICTED = 7
        const val CANCELLATION_PENDING = 9
        const val EXPIRED = -7
    }

    object ImagesStatus {
        const val PENDING = 1
        const val SUBMITTED = 2
        const val REJECTED = 3
        const val APPROVED = 4
    }

    object PaymentMethodTypes {
        const val CARD = 2
        const val BANK_ACCOUNT = 3
    }

    object Availability {
        const val UNAVAILABLE = 1
        const val AVAILABLE = 0
        const val CUSTOM_AVAILABILITY = 2
    }

    object DeliveryFeeStatus {
        const val APPLIED = 1
        const val NOT_APPLIED = 2
    }

    object CarDeliveryStatus {
        const val AVAILABLE = 1
        const val NOT_AVAILABLE = 0
    }

    object HostType {
        const val INDIVIDUAL = 1
        const val COMPANY = 2
    }

    object CarStatus {
        const val LISTED = 1
        const val SNOOZED = 3
    }


    object HostBookingImagesStatus {
        const val ADDED = 1
        const val NOT_ADDED = 0
    }

    object RatingStatus {
        const val RATED = 1
        const val NOT_RATED_YET = 0
    }

    object TripUserType {
        const val HOST_TYPE = 1
        const val GUEST_TYPE = 2
    }

    object HostAllStarStatus {
        const val YES = 1
        const val NO = 0
    }

    const val AMOUNT = "1"
    const val CURRENCY = "USD"

    /* The payment brands for Ready-to-Use UI and Payment Button */
    val PAYMENT_BRANDS = linkedSetOf("VISA", "MASTER", "PAYPAL", "GOOGLEPAY", "MADA")

    /* The default payment brand for payment button */
    const val PAYMENT_BUTTON_BRAND = "GOOGLEPAY"

    /* The card info for SDK & Your Own UI */
    const val CARD_BRAND = "VISA"
    const val CARD_HOLDER_NAME = "JOHN DOE"
    const val CARD_EXPIRY_MONTH = "12"
    const val CARD_NUMBER = "4111111111111111"
    const val CARD_EXPIRY_YEAR = "22"
    const val CARD_CVV = "123"


    val VISA_PATTERN: Pattern = Pattern.compile("^4[0-9]{6,}\$")
    val MASTER_PATTERN: Pattern =
        Pattern.compile("^5[1-5][0-9]{5,}|222[1-9][0-9]{3,}|22[3-9][0-9]{4,}|2[3-6][0-9]{5,}|27[01][0-9]{4,}|2720[0-9]{3,}\$")
    val AMEX_PATTERN: Pattern =
        Pattern.compile("^3[47][0-9]{5,}\$")
    val MADA_PATTERN: Pattern =
        Pattern
            .compile("^(4(0(0861|1757|3024|6136|6996|7(197|395)|9201)|1(2565|0621|0685|7633|9593)|2(0132|1141|281(7|8|9)|689700|8(331|67(1|2|3)))|3(1361|2328|4107|9954)|4(0(533|647|795)|5564|6(393|404|672))|5(5(036|708)|7865|7997|8456)|6(2220|854(0|1|2|3))|7(4491)|8(301(0|1|2)|4783|609(4|5|6)|931(7|8|9))|93428)|5(0(4300|6968|8160)|13213|2(0058|1076|4(130|514)|9(415|741))|3(0(060|906)|1(095|196)|2013|5(825|989)|6023|7767|9931)|4(3(085|357)|9760)|5(4180|7606|8563|8848)|8(5265|8(8(4(5|6|7|8|9)|5(0|1))|98(2|3))|9(005|206)))|6(0(4906|5141)|36120)|9682(0(1|2|3|4|5|6|7|8|9)|1(0|1)))\\d{10}\$")

    object FileTypes {
        const val PDF = 1
        const val IMAGES = 2
    }
}