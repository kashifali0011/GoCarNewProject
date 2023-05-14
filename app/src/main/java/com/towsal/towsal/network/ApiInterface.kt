package com.towsal.towsal.network

import com.towsal.towsal.network.ApiInterface.Companion.ParamNames.CAR_ID
import com.towsal.towsal.network.ApiInterface.Companion.ParamNames.CHECKOUT_ID
import com.towsal.towsal.network.ApiInterface.Companion.ParamNames.CHECK_OUT_IDS
import com.towsal.towsal.network.ApiInterface.Companion.ParamNames.CIT_ID
import com.towsal.towsal.network.ApiInterface.Companion.ParamNames.RESOURCE_PATH
import com.towsal.towsal.network.ApiInterface.Companion.ParamNames.STEP
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.DataKeyValues.CarInfo.IS_CAR_SAFETY_QUALITY
import com.towsal.towsal.network.serializer.UserModel
import com.towsal.towsal.network.serializer.cardetails.UserInfoStepFourModel
import com.towsal.towsal.network.serializer.cardetails.UserInfoStepThreeModel
import com.towsal.towsal.network.serializer.carlist.step2.CarAvailabilityPostModel
import com.towsal.towsal.network.serializer.checkoutcarbooking.BookingPostModel
import com.towsal.towsal.network.serializer.checkoutcarbooking.GetCarBookingModel
import com.towsal.towsal.network.serializer.messages.SendMessageModel
import com.towsal.towsal.network.serializer.payment.PaymentMethodRequestModel
import com.towsal.towsal.network.serializer.register.RegisterModel
import com.towsal.towsal.network.serializer.register.ResetPasswordModel
import com.towsal.towsal.network.serializer.settings.*
import io.reactivex.Observable
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Interface for api calling
 * */
interface ApiInterface {

    companion object {
        const val LOGIN: String = "login"
        const val REGISTER: String = "register"
        const val RESEND_EMAIL = "resend_email"
        const val OTP_VALIDATE: String = "otpValidate"
        const val OTP_RESEND: String = "otpResend"
        const val CITY_LIST: String = "getCityList"
        const val RESET_PASSWORD_REQUEST: String = "password/resetRequest"
        const val RESET_PASSWORD: String = "password/reset"
        const val CARS_LIST: String = "cars/list/{car_id}"
        const val CARS_LIST_: String = "cars/list"
        const val CARS_FEATURES_LIST: String = "cars/features/list"
        const val RECEIPT_INVOICE_DETAIL: String = "booking/receiptInvoiceDetail"
        const val CAR_SAVE: String = "cars/save"
        const val HOME: String = "home"
        const val CAR_DETAILS: String = "carDetails"
        const val VEHICLE_INFO: String = "cars/{car_id}/car-info"
        const val GET_CAR_PRICING: String = "cars/{car_id}/getcarpricing"
        const val SAVE_CAR_PRICING: String = "cars/{car_id}/carpricing"
        const val UPDATE_CAR_STATUS: String = "cars/{car_id}/update-status"
        const val CAR_DELETE_REASON_LIST: String = "cars/getdeletelist"
        const val CAR_DELETE: String = "cars/{car_id}/deletecarstatus"
        const val GET_CAR_CALENDAR: String = "cars/{car_id}/calendar"
        const val SAVE_CALENDAR_DATE: String = "cars/{car_id}/savecarcalender"
        const val GET_CAR_PREFERENCES: String = "cars/{car_id}/getcartriprefrence"
        const val SAVE_CAR_PREFERENCES: String = "cars/{car_id}/savecartriprefrence"
        const val GET_GUIDELINES_BY_HOST: String = "cars/{car_id}/getguidlinebyhost"
        const val SAVE_GUIDELINES_BY_HOST: String = "cars/{car_id}/saveguidlinebyhost"
        const val GET_CAR_DISTANCE_INCLUDED: String = "cars/{car_id}/getcardistanceincluded"
        const val SAVE_CAR_DISTANCE_INCLUDED: String = "cars/{car_id}/saveccardistanceincluded"
        const val GET_CAR_DETAIL: String = "cars/{car_id}/getcardetail"
        const val SAVE_CAR_DETAIL: String = "cars/{car_id}/savecardetail"
        const val SEARCH_CAR_API: String = "home/advance_search"
        const val GET_CAR_LOCATION_DELIVERY: String = "cars/{car_id}/location-and-delivery"
        const val SAVE_CAR_LOCATION_DELIVERY: String = "cars/{car_id}/savecarlocationanddelivery"
        const val GET_USER_INFO: String = "userinfo"
        const val SAVE_USER_INFO: String = "saveuserinfo"
        const val GET_CHECKOUT_DETAIL: String = "booking/getCheckOutDetail"
        const val SAVE_CHECKOUT_DETAIL: String = "booking/saveCheckOutDetail"
        const val LOGOUT: String = "logout"
        const val GET_ALL_NOTIFICATION: String = "getAllNotification"
        const val GET_MY_BOOKINGS: String = "cars/getMyBookings"
        const val GET_MY_HISTORY_BOOKINGS: String = "cars/getBookingHistoryList"
        const val GET_BOOKING_DETAIL: String = "cars/getBookingDetails"
        const val CANCEL_BOOKING: String = "cars/cancelBooking"
        const val ACCEPT_BOOKING: String = "cars/acceptbooking"
        const val ADD_DROP_OFF_IMAGES: String = "cars/adddropoffimages"
        const val HOST_PRE_TRIP_IMAGES: String = "cars/host-pre-trip-images"
        const val ADD_PICKUP_IMAGES: String = "cars/addpickupimages"
        const val REJECT_BOOKING: String = "cars/rejectbooking"
        const val REJECT_BOOKING_REASON: String = "cars/rejectbookingreason"
        const val CANCEL_BOOKING_REASON: String = "cars/cancelbookingreason"
        const val ACCEPT_REJECT_PICKUP_IMAGES: String = "cars/acceptrejectpickupImages"
        const val ACCEPT_REJECT_DROP_OFF_IMAGES: String = "cars/acceptrejectdropoffImages"
        const val VIEW_DROP_OFF_IMAGES: String = "cars/viewdropimage"
        const val VIEW_PICKUP_IMAGES: String = "cars/viewpickimage"
        const val REPORT_ACCIDENT_PHOTOS: String = "cars/reportaccident"
        const val VIEW_ACCIDENT_IMAGE: String = "cars/viewaccidentimage"
        const val GET_PERFORMANCE: String = "cars/getperformance"
        const val GET_HOST_REVIEW: String = "cars/gethostreview"
        const val GET_GUEST_REVIEW: String = "cars/getguestreview"
        const val WRITE_RATING_AND_REVIEW: String = "cars/writeratingandreview"
        const val GET_EARNINGS_REPORTS: String = "cars/getearning"
        const val GET_MESSAGES: String = "cars/getmessage"
        const val GET_USER_THREAD: String = "user-threads"
        const val GET_MESSAGE_DETAIL: String = "cars/getmessagedetail"
        const val GET_THREAD_ID: String = "thread_id"
        const val GET_USER_THREADES: String = "user-thread"
        const val SEND_MESSAGE: String = "cars/sendmessage"
        const val MESSAGE: String = "message"
        const val GET_PROFILE: String = "imageandname"
        const val IMAGE_UPDATE: String = "imageupadte"
        const val ACCOUNT: String = "account"
        const val ACCOUNT_UPDATE: String = "accountupdate"
        const val PASSWORD_CHANGE: String = "changepassword"
        const val ADD_FAVORITE: String = "addFavorite"
        const val GET_FAVORITES = "getFavorites"
        const val SUPPORT = "support"
        const val DELETE_FAVORITE = "deleteFavorite"
        const val GETTRANSACTIONDETAILS = "getTransactionDetails"
        const val GET_TRANSACTION_HISTORY = "getTransactionHistory"
        const val SEEN_MESSAGE = "cars/seenmessage"
        const val UNREAD_NOTIFICATION = "getUnRead"
        const val CAR_ALWAYS_AVAILABLE =
            "cars/always_available"        //car always available in the host section
        const val GET_CAR_AVAILABILITY_INFO = "cars/get_car_availability_info"
        const val CAR_CUSTOM_AVAIABILITY = "cars/custom_availability"
        const val EDIT_CHECKOUT_DETAIL = "booking/editCheckOutDetail"
        const val APPROVE_CHECKOUT_CHANGINGS = "cars/approvecheckoutchanges"
        const val REJECT_CHECKOUT_CHANGINGS = "cars/rejectcheckoutchanges"
        const val VERIFY_EMAIL = "verify/mail"
        const val UPDATE_CHECKOUT_CHANGINGS = "cars/updatecheckoutchange"
        const val USER_DETAILS = "booking/getUsersDetails"
        const val SUGGEST_EXTENSION = "booking/suggestExtension"
        const val CALENDAR_VALIDATOR = "booking/calenderValidator"
        const val REPORT_REASON = "cars/report-reasons"
        const val REPORT_REASON_SAVE = "cars/report-reason/save"
        const val MAKE_SELECTION = "cars/makes"
        const val MODEL_SELECTION = "cars/models"
        const val CARS_DOCS = "cars/{car_id}/docs"
        const val UPDATE_CAR_DOCS = "cars/{car_id}/docUpdate"
        const val PREPARE_CHECKOUT = "prepare-checkout"
        const val PAYMENT_STATUS = "paymentStatus"
        const val CARD_REGISTRATION_STATUS = "card-registration-status"
        const val PAYMENT_METHOD = "payment-method"
        const val PAYMENT_METHODS = "payment-methods"
        const val HOLD_PAYMENT = "hold-payment"
        const val DEFAULT_PAYMENT_METHOD = "payment-methods/set-default"
        const val TRIP_COMPLETE = "cars/trip/complete"
        const val CANCELLATION_IMAGES = "cars/cancelation-images"
        const val UPLOAD_IMAGES = "images/upload"
        const val CAR_ATTRIBUTES = "cars/attributes"
        const val SEARCH_ATTRIBUTES = "search/cars/attributes"
        const val MY_VEHICLES = "cars/my-cars"
        const val FUEL_LEVELS = "fuel-levels"
        const val THREAD = "thread"
        const val LATE_RETURN = "late-return"

        object ParamNames {
            const val MEDIA_FILE: String = "media_file"
            const val FUEL_LEVEL_ID = "trip_fuel_id"
            const val OTP: String = "otp"
            const val USER_ID: String = "user_id"
            const val IS_ACCEPT_TC = "is_accept_tc"
            const val TYPE = "type"
            const val STEP = "step"
            const val STATUS = "status"
            const val SNOOZE_TO = "snooze_start_from"
            const val SNOOZE_END = "snooze_end_to"
            const val REASON_IDS = "reason"
            const val TEXT = "text"
            const val DISTANCE_INCLUDED = "distanceincluded_id"
            const val UNLIMITED_DISTANCE = "unlimited_distance"
            const val PAGE = "page"
            const val FILTER = "filter"
            const val FCM_TOKEN = "fcm_token"
            const val TOKEN = "token"
            const val ID = "id"
            const val VIEW_TYPE = "view_type"
            const val REASON_ID = "reason_id"
            const val REASON = "reason"
            const val CANCELLATION_TYPE = "cancelation_type"
            const val ODOMETER_READING = "odometer_reading"
            const val TRIP_READING = "trip_reading"
            const val CAR_ID = "car_id"
            const val CHECK_OUT_IDS = "checkout_id"
            const val TRIP_ID = "trip_id"
            const val IMAGE_TYPE = "image_type"
            const val IS_ALWAYS_AVAILABLE = "is_always_available"
            const val CAR_CHECKOUT_ID = "car_checkout_id"
            const val CAR_PHOTO = "car_photo"
            const val CAR_PHOTOS = "car_photos"
            const val COUNTER_PHOTO = "counter_photo"
            const val RATING = "rating"
            const val REVIEW = "review"
            const val IS_HOST = "is_host"
            const val CHECKOUT_ID = "car_checkout_id"
            const val ACCEPT_REJECT_FLAG = "accept_reject_flag"
            const val ENUM_TYPE = "enum_type"
            const val SELECT_VALUE = "select_value"
            const val CHECK_OUT_ID = "check_out_id"
            const val THREAD_ID = "thread_id"
            const val FIRST_NAME = "first_name"
            const val MESSAGE = "message"
            const val CHECKOUT_ID_MESSAGE = "checkout_id"
            const val LAST_NAME = "last_name"
            const val OLD_PASSWORD = "old_password"
            const val NEW_PASSWORD = "new_password"
            const val APPROVAL_STATUS = "approval_status"
            const val CHANGE_REQUEST_ID = "change_request_id"
            const val I_AM = "i_am"
            const val PICK_UP = "pick_up"
            const val DROP_OFF = "drop_off"
            const val YEAR_ID = "year_id"
            const val MAKE_ID = "make_id"
            const val AMOUNT = "amount"
            const val LAST_4_DIGITS = "last_4digits"
            const val CURRENCY = "currency"
            const val RESOURCE_PATH = "resourcePath"
            const val CIT_ID = "cit_id"
            const val IS_PHOTO = "is_photo"
            const val CUSTOM_PRICES = "custom_prices"
            const val DEFAULT_PRICE = "default_price"
            const val THREE_DAY_PRICE = "three_day_discount_price"
            const val SEVEN_DAY_PRICE = "seven_day_discount_price"
            const val THIRTY_DAY_PRICE = "thirty_day_discount_price"
            const val RECEIVER_ID = "receiver_id"
        }
    }

    /**
     * Login API Request
     *@param userModel
     * @param fcmToken
     * */
    @POST(LOGIN)
    fun loginApi(
        @Body userModel: UserModel,
        @Query(ParamNames.FCM_TOKEN) fcmToken: String?
    ): Observable<BaseResponse>

    /**
     * Register user API Request
     *@param userModel
     * */
    @POST(REGISTER)
    fun registerApi(@Body userModel: RegisterModel): Observable<BaseResponse>

    /**
     * Verify user API Request
     *@param otp
     * @param userId
     * @param type
     * */
    @POST(OTP_VALIDATE)
    fun verifyOtp(
        @Query(ParamNames.OTP) otp: String?,
        @Query(ParamNames.USER_ID) userId: Int?,
        @Query(ParamNames.TYPE) type: String?
    ): Observable<BaseResponse>

    /**
     * Resend otp API Request
     *@param userId
     * */
    @POST(OTP_RESEND)
    fun resendOtp(@Query(ParamNames.USER_ID) userId: Int?): Observable<BaseResponse>

    /**
     * City list API Request
     * */
    @GET(CITY_LIST)
    fun cityList(@Query("type") type: Int = 1): Observable<BaseResponse>

    /**
     * Reset password API Request
     * @param map
     * */
    @POST(RESET_PASSWORD_REQUEST)
    fun resetPasswordRequest(@Body map: HashMap<String?, String?>): Observable<BaseResponse>

    /**
     * Reset password API Request
     * @param resetModel
     * */
    @POST(RESET_PASSWORD)
    fun resetPassword(@Body resetModel: ResetPasswordModel): Observable<BaseResponse>

    /**
     * Cars list API Request
     * */
    @GET(CARS_LIST)
    fun getCarsList(@Path(CAR_ID) carId: Int?, @Query("step") step: Int): Observable<BaseResponse>

    /**
     * Cars list API Request
     * */
    @GET(CARS_LIST_)
    fun getCarsList(@Query("step") step: Int): Observable<BaseResponse>

    @POST(CAR_SAVE)
    fun carInformationSave(@Body map: HashMap<String?, Any?>): Observable<BaseResponse>

    /**
     * Car save API Request
     * @param model
     * */
    @POST(CAR_SAVE)
    fun carInformationSave(@Body model: CarAvailabilityPostModel): Observable<BaseResponse>

    /**
     * Car save API Request
     * @param step
     * @param listPhotos
     * */
    @POST(CAR_SAVE)
    fun carInformationSave(
        @Query(STEP) step: Int,
        @Body map: HashMap<String, Any>
    ): Observable<BaseResponse>


    /**
     * Car save API Request
     * @param model
     * */
    @FormUrlEncoded
    @POST(CAR_SAVE)
    fun carInformationSave(
        @Field(STEP) step: Int,
        @Field(IS_CAR_SAFETY_QUALITY) userAgreedToTermsAndConditions: Int,
        @Field(CAR_ID) carId: Int = -1
    ): Observable<BaseResponse>

    /**
     * Home API Request
     * @param model
     * */
    @GET(HOME)
    fun homeData(@QueryMap model: HashMap<String, Any>): Observable<BaseResponse>

    /**
     * Car details API Request
     * @param model
     * */
    @POST(CAR_DETAILS)
    fun getCarDetail(@QueryMap model: HashMap<String, Any>): Observable<BaseResponse>

    /**
     * Vehicle info API Request
     * */
    @GET(VEHICLE_INFO)
    fun getVehicleInfo(@Path(CAR_ID) car_id: Int): Observable<BaseResponse>

    /**
     * Get car pricing API Request
     * */
    @GET(GET_CAR_PRICING)
    fun getCarPricing(@Path(CAR_ID) carId: Int): Observable<BaseResponse>

    /**
     * Save car pricing API Request
     * @param hashMap
     * */
    @POST(SAVE_CAR_PRICING)
    fun saveCarPricing(
        @Path(CAR_ID) carId: Int,
        @Body hashMap: HashMap<String, Any?>
    ): Observable<BaseResponse>

    /**
     * Updates car status API Request
     * @param status
     * @param from
     * @param to
     * */
    @PUT(UPDATE_CAR_STATUS)
    fun updateCarStatus(
        @Path(CAR_ID) car_id: String,
        @Query(ParamNames.STATUS) status: Int,
        @Query(ParamNames.SNOOZE_TO) from: String,
        @Query(ParamNames.SNOOZE_END) to: String,
    ): Observable<BaseResponse>

    /**
     * Car delete reason list API Request
     * */
    @GET(CAR_DELETE_REASON_LIST)
    fun getCarDeleteReason(): Observable<BaseResponse>

    /**
     * Car delete API Request
     * @param reasons
     * */
    @PUT(CAR_DELETE)
    fun deleteCarListing(
        @Path(CAR_ID) carId: Int,
        @Query(ParamNames.REASON_IDS) reasons: ArrayList<Int>
    ): Observable<BaseResponse>

    /**
     * Get car calendar API Request
     * */
    @GET(GET_CAR_CALENDAR)
    fun getCarCalendar(@Path(CAR_ID) carId: Int): Observable<BaseResponse>

    /**
     * Save calendar date API Request
     * @param model
     * */
    @POST(SAVE_CALENDAR_DATE)
    fun markUnavailableDate(
        @Path(CAR_ID) carId: Int,
        @Body model: UnAvailableDates
    ): Observable<BaseResponse>

    /**
     * Get car preferences API Request
     * */
    @GET(GET_CAR_PREFERENCES)
    fun getCarPreferences(
        @Path(CAR_ID) carId: Int,
    ): Observable<BaseResponse>

    /**
     * Save car preferences API Request
     * @param model
     * */
    @POST(SAVE_CAR_PREFERENCES)
    fun saveCarTripPreferences(
        @Path(CAR_ID) carId: Int,
        @Body model: CarAvailabilityPostModel
    ): Observable<BaseResponse>

    /**
     * Get guidelines by host API Request
     * */
    @GET(GET_GUIDELINES_BY_HOST)
    fun getGuideLinesByHost(@Path(CAR_ID) carId: Int): Observable<BaseResponse>

    /**
     * Save guidelines by host API Request
     * @param text
     * */
    @POST(SAVE_GUIDELINES_BY_HOST)
    fun saveGuideLines(
        @Path(CAR_ID) carId: Int,
        @Query(ParamNames.TEXT) text: String
    ): Observable<BaseResponse>

    /**
     * Get car distance included API Request
     * */
    @GET(GET_CAR_DISTANCE_INCLUDED)
    fun getDistanceIncluded(@Path(CAR_ID) carId: Int): Observable<BaseResponse>

    /**
     * Save car distance included API Request
     * @param distanceincluded_id
     * @param unlimited_distance
     * */
    @POST(SAVE_CAR_DISTANCE_INCLUDED)
    fun saveDistanceInfo(
        @Path(CAR_ID) carId: Int,
        @Query(ParamNames.DISTANCE_INCLUDED) distanceincluded_id: Int,
        @Query(ParamNames.UNLIMITED_DISTANCE) unlimited_distance: Int
    ): Observable<BaseResponse>

    /**
     * Get car detail API Request
     * */
    @GET(GET_CAR_DETAIL)
    fun getCarDetail(@Path(CAR_ID) carId: Int): Observable<BaseResponse>

    /**
     * Save car detail API Request
     * @param model
     * */
    @POST(SAVE_CAR_DETAIL)
    fun saveCarDetail(
        @Path(CAR_ID) carId: Int,
        @Body model: BasicCarPostModel
    ): Observable<BaseResponse>

    /**
     * Search car API Request
     * @param model
     * */
    @GET(SEARCH_CAR_API)
    fun getCarSearch(@QueryMap model: HashMap<String, Any>): Observable<BaseResponse>

    /**
     * Get car location delivery API Request
     * */
    @GET(GET_CAR_LOCATION_DELIVERY)
    fun getCarLocationDelivery(@Path(CAR_ID) carId: Int): Observable<BaseResponse>

    /**
     * Save car location delivery API Request
     * @param model
     * */
    @POST(SAVE_CAR_LOCATION_DELIVERY)
    fun saveCarLocationDelivery(
        @Path(CAR_ID) carId: Int,
        @Body model: CarLocationDeliveryModel
    ): Observable<BaseResponse>

    /**
     * Get user info API Request
     * @param car_id
     * */
    @GET(GET_USER_INFO)
    fun getUserInfo(@Query(ParamNames.ID) userId: Int): Observable<BaseResponse>

    /**
     * Save user info API Request
     * @param profileImage
     * @param map
     * */
    @FormUrlEncoded
    @POST(SAVE_USER_INFO)
    fun saveUserProfileImage(@FieldMap map: HashMap<String?, String?>): Observable<BaseResponse>

    /**
     * Save user info API Request
     * @param stepThreeModel
     * */
    @POST(SAVE_USER_INFO)
    fun saveLicenseInformation(@Body stepThreeModel: UserInfoStepThreeModel): Observable<BaseResponse>

    /**
     * Save user info API Request
     * @param model
     * */
    @POST(SAVE_USER_INFO)
    fun savePaymentInfo(@Body model: UserInfoStepFourModel): Observable<BaseResponse>

    /**
     * Get checkout detail API Request
     * @param map
     * */
    @POST(GET_CHECKOUT_DETAIL)
    fun getCheckoutDetail(@Body map: GetCarBookingModel): Observable<BaseResponse>

    /**
     * Save checkout detail API Request
     * @param model
     * */
    @POST(SAVE_CHECKOUT_DETAIL)
    fun saveCheckOutDetail(@Body model: BookingPostModel): Observable<BaseResponse>

    /**
     * Edit checkout detail API Request
     * @param model
     * */
    @POST(EDIT_CHECKOUT_DETAIL)
    fun editCheckoutDetail(@Body model: BookingPostModel): Observable<BaseResponse>

    /**
     * Logout API Request
     * */
    @POST(LOGOUT)
    fun logout(): Observable<BaseResponse>

    /**
     * Get all notification API Request
     * @param map
     * */
    @GET(GET_ALL_NOTIFICATION)
    fun getAllNotifications(@QueryMap map: HashMap<String, Any>): Observable<BaseResponse>

    /**
     * Get my bookings API Request
     * @param map
     * */
    @GET(GET_MY_BOOKINGS)
    fun getMyBookings(@QueryMap map: HashMap<String, Any>): Observable<BaseResponse>

    /**
     * Get my history bookings API Request
     * @param map
     * */
    @GET(GET_MY_HISTORY_BOOKINGS)
    fun getMyHistoryBookings(@QueryMap map: HashMap<String, Any>): Observable<BaseResponse>

    /**
     * Get booking detail API Request
     * @param id
     * @param viewType
     * */
    @GET(GET_BOOKING_DETAIL)
    fun getBookingDetail(
        @Query(ParamNames.ID) id: Int,
        @Query(ParamNames.VIEW_TYPE) viewType: Int
    ): Observable<BaseResponse>

    /**
     * Accept booking API Request
     * @param id
     * */
    @POST(ACCEPT_BOOKING)
    fun acceptBooking(@Query(ParamNames.ID) id: Int): Observable<BaseResponse>

    /**
     * Cancel booking API Request
     * @param id
     * @param reason
     * */
    @POST(CANCEL_BOOKING)
    fun cancelBooking(
        @Query(ParamNames.ID) id: Int,
        @Query(ParamNames.REASON_ID) reason: Int
    ): Observable<BaseResponse> // reason String


    /**
     * Cancel booking API Request
     * @param id
     * @param reason
     * */
    @POST(CANCEL_BOOKING)
    fun cancelBookingWithId(
        @Query(ParamNames.ID) id: Int,
        @Query(ParamNames.REASON_ID) reason: Int,
        @Query(ParamNames.REASON) cancelReason: String
    ): Observable<BaseResponse> // reason String


    /**
     * Reject booking API Request
     * @param id
     * @param reason
     * */
    @POST(REJECT_BOOKING)
    fun rejectBooking(
        @Query(ParamNames.ID) id: Int,
        @Query(ParamNames.REASON_ID) reason: Int
    ): Observable<BaseResponse>

    /**
     * Add pickUp images API Request
     * @param map
     * */
    @FormUrlEncoded
    @POST(ADD_PICKUP_IMAGES)
    fun addPickupImages(
        @FieldMap map: HashMap<String?, String?>,
    ): Observable<BaseResponse>

    /**
     * Add dropOff images API Request
     * @param map
     * */
    @FormUrlEncoded
    @POST(ADD_DROP_OFF_IMAGES)
    fun addDropOffImages(@FieldMap map: HashMap<String?, String?>?): Observable<BaseResponse>

    /**
     * Reject booking reason API Request
     * */
    @GET(REJECT_BOOKING_REASON)
    fun getRejectBookingReason(): Observable<BaseResponse>

    /**
     * Cancel booking reason API Request
     * @param iAm
     * */
    @GET(CANCEL_BOOKING_REASON)
    fun getCancelBookingReason(@Query(ParamNames.I_AM) iAm: Int, @Query(ParamNames.CAR_CHECKOUT_ID) checkOutId: Int): Observable<BaseResponse> // I_am int 1 for guest , 2 for host

    /**
     * View pickup images API Request
     * @param id
     * @param viewType
     * */
    @GET(VIEW_PICKUP_IMAGES)
    fun viewPickupImages(
        @Query(CAR_ID) id: Int,
        @Query(ParamNames.CAR_CHECKOUT_ID) viewType: Int
    ): Observable<BaseResponse>

    /**
     * View drop off images API Request
     * @param id
     * @param viewType
     * */
    @GET(VIEW_DROP_OFF_IMAGES)
    fun viewDropOffImages(
        @Query(CAR_ID) id: Int,
        @Query(ParamNames.CAR_CHECKOUT_ID) viewType: Int
    ): Observable<BaseResponse>

    /**
     * Accept reject pickUp images API Request
     * @param map
     * @param tech
     * */
    @Multipart
    @POST(ACCEPT_REJECT_PICKUP_IMAGES)
    fun acceptRejectImages(
        @Part tech: ArrayList<MultipartBody.Part>,
        @PartMap map: HashMap<String, RequestBody>
    ): Observable<BaseResponse>

    /**
     * Accept reject pickUp images API Request
     * @param map
     * */
    @POST(ACCEPT_REJECT_PICKUP_IMAGES)
    fun acceptRejectImages(@Body map: HashMap<String, Any>): Observable<BaseResponse>

    /**
     * Report accident photos API Request
     * @param map
     * */
    @FormUrlEncoded
    @POST(REPORT_ACCIDENT_PHOTOS)
    fun addAccidentPhotos(@FieldMap map: HashMap<String?, String?>?): Observable<BaseResponse>

    /**
     * Accept reject drop off images API Request
     * @param map
     * @param tech
     * */
    @Multipart
    @POST(ACCEPT_REJECT_DROP_OFF_IMAGES)
    fun acceptRejectDropOffImages(
        @Part tech: List<MultipartBody.Part>,
        @PartMap map: HashMap<String, RequestBody>
    ): Observable<BaseResponse>

    /**
     * Accept reject drop off images API Request
     * @param map
     * */
    @POST(ACCEPT_REJECT_DROP_OFF_IMAGES)
    fun acceptRejectDropOffImages(@Body map: HashMap<String, Any>): Observable<BaseResponse>

    /**
     * View accident images API Request
     * @param id
     * @param viewType
     * */
    @GET(VIEW_ACCIDENT_IMAGE)
    fun viewAccidentImages(
        @Query(CAR_ID) id: Int,
        @Query(ParamNames.CAR_CHECKOUT_ID) viewType: Int
    ): Observable<BaseResponse>

    /**
     * View accident images API Request
     * @param id
     * @param viewType
     * */
    @POST(LATE_RETURN)
    fun lateReturn(
        @Query(CHECK_OUT_IDS) id: Int,
    ): Observable<BaseResponse>

    /**
     * Get performance API Request
     * @param map
     * */
    @GET(GET_PERFORMANCE)
    fun getPerformanceData(): Observable<BaseResponse>

    /**
     * Get host review API Request
     * */
    @GET(GET_HOST_REVIEW)
    fun getHostReview(): Observable<BaseResponse>

    /**
     * Get guest review API Request
     * */
    @GET(GET_GUEST_REVIEW)
    fun getGuestReview(): Observable<BaseResponse>

    /**
     * Write rating and review API Request
     * @param id
     * @param viewType
     * @param rating
     * @param review
     * @param is_host
     * */
    @POST(WRITE_RATING_AND_REVIEW)
    fun addRatingReview(
        @Query(CAR_ID) id: Int,
        @Query(CHECKOUT_ID) viewType: Int,
        @Query(ParamNames.RATING) rating: Float,
        @Query(ParamNames.REVIEW) review: String,
        @Query(ParamNames.IS_HOST) is_host: Int
    ): Observable<BaseResponse>

    /**
     * Get host review API Request
     * @param userId
     * */
    @GET(GET_HOST_REVIEW)
    fun getHostReview(@Query(ParamNames.USER_ID) userId: Int): Observable<BaseResponse>

    /**
     * Get guest review API Request
     * @param userId
     * */
    @GET(GET_GUEST_REVIEW)
    fun getGuestReview(@Query(ParamNames.USER_ID) userId: Int): Observable<BaseResponse>

    /**
     * Get earnings reports API Request
     * @param userId
     * @param date
     * */
    @GET(GET_EARNINGS_REPORTS)
    fun getEarningReport(
        @Query(ParamNames.ENUM_TYPE) userId: Int,
        @Query(ParamNames.SELECT_VALUE) date: String
    ): Observable<BaseResponse>

    /**
     * Get messages API Request
     * */
    @GET(GET_MESSAGES)
    fun getMessages(): Observable<BaseResponse>

    /**
     * Get messages API Request
     * */
    @GET(GET_USER_THREAD)
    fun getMessagesNew(@Query("page") page: Int): Observable<BaseResponse>

    /**
     * Seen messages API Request
     * @param messageId
     * */
    @GET("${SEEN_MESSAGE}/{booking_id}")
    fun seenMessage(@Path("booking_id") messageId: Int): Observable<BaseResponse>

    /**
     * Unread notifications API Request
     * */
    @GET(UNREAD_NOTIFICATION)
    fun unReadNotification(): Observable<BaseResponse>

    /**
     * Get message detail API Request
     * @param id
     * */
    @GET(GET_MESSAGE_DETAIL)
    fun getMessageDetail(@Query(ParamNames.CHECK_OUT_ID) id: Int): Observable<BaseResponse>

    /**
     * Get message detail API Request
     * @param id
     * */
    @GET(GET_USER_THREADES)
    fun getMessageDetailNew(@Query("page") page: Int , @Query(ParamNames.THREAD_ID) id: Int): Observable<BaseResponse>

    /**
     * Send message API Request
     * @param model
     * */
    @POST(MESSAGE)
    fun sendMessage(@Body model: SendMessageModel): Observable<BaseResponse>

    /**
     * Send message API Request
     * @param model
     * */
    @POST(MESSAGE)
    fun sendMessageThread(@Body model: SendMessageModel): Observable<BaseResponse>

    /**
     * Account update API Request
     * @param receiverId
     * */
    @POST(THREAD)
    fun createThread(
        @Query(ParamNames.RECEIVER_ID) receiverId: Int,
    ): Observable<BaseResponse>
    /**
     * Get profile API Request
     * */
    @GET(GET_PROFILE)
    fun getProfile(): Observable<BaseResponse>

    /**
     * Image update API Request
     * @param image
     * */
    @Multipart
    @POST(IMAGE_UPDATE)
    fun updateImage(@Part image: MultipartBody.Part?): Observable<BaseResponse>

    /**
     * Account API Request
     * */
    @GET(ACCOUNT)
    fun getAccountInfo(): Observable<BaseResponse>

    /**
     * Account update API Request
     * @param firstName
     * @param lastName
     * */
    @POST(ACCOUNT_UPDATE)
    fun accountUpdate(
        @Query(ParamNames.FIRST_NAME) firstName: String?,
        @Query(ParamNames.LAST_NAME) lastName: String?
    ): Observable<BaseResponse>


    /**
     * Account update API Request
     * @param firstName
     * */
    @POST(SUPPORT)
    fun sendMessage(
        @Query(ParamNames.MESSAGE) message: String?
    ): Observable<BaseResponse>
    /**
     * Account update API Request
     * @param firstName
     * */
    @POST(SUPPORT)
    fun sendMessageWithId(
        @Query(ParamNames.MESSAGE) message: String?,
        @Query(ParamNames.CHECKOUT_ID_MESSAGE) checkOutId: String?
    ): Observable<BaseResponse>

    /**
     * Change password API Request
     * @param oldPassword
     * @param newPassword
     * */
    @POST(PASSWORD_CHANGE)
    fun changePassword(
        @Query(ParamNames.OLD_PASSWORD) oldPassword: String?,
        @Query(ParamNames.NEW_PASSWORD) newPassword: String?
    ): Observable<BaseResponse>

    /**
     * Add favorite API Request
     * @param carID
     * */
    @POST(ADD_FAVORITE)
    fun addToFavCar(@Query(CAR_ID) carID: Int): Observable<BaseResponse>

    /**
     * Get favorites API Request
     * @param model
     * */
    @GET(GET_FAVORITES)
    fun getFavoritesCarList(@QueryMap model: HashMap<String, Any>): Observable<BaseResponse>

    /**
     * Delete favorite API Request
     * @param carID
     * */
    @POST(DELETE_FAVORITE)
    fun deleteFavCar(@Query(CAR_ID) carID: Int): Observable<BaseResponse>

    /**
     * Get transaction history API Request
     * */
    @GET(GET_TRANSACTION_HISTORY)
    fun getTransactionHistory(): Observable<BaseResponse>

    /**
     * Get transaction details API Request
     * @param id
     * @param view_type
     * */
    @GET(GETTRANSACTIONDETAILS)
    fun getTransactionDetail(
        @Query(ParamNames.ID) id: Int,
        @Query(ParamNames.VIEW_TYPE) view_type: Int
    ): Observable<BaseResponse>

    /**
     * Car always available API Request
     * @param car_id
     * @param is_always_available
     * */
    @POST(CAR_ALWAYS_AVAILABLE)
    @FormUrlEncoded
    fun carAlwaysAvailable(
        @Field(CAR_ID) car_id: Int,
        @Field(ParamNames.IS_ALWAYS_AVAILABLE) is_always_available: Int
    ): Observable<BaseResponse>

    /**
     * Get car availability info API Request
     * @param car_id
     * */
    @POST(GET_CAR_AVAILABILITY_INFO)
    @FormUrlEncoded
    fun getCarAvailabilityInfo(@Field(CAR_ID) car_id: Int): Observable<BaseResponse>

    /**
     * Car custom availability API Request
     * @param model
     * */
    @POST(CAR_CUSTOM_AVAIABILITY)
    fun setCarUnAvailability(@Body model: CarAvailabilityModel): Observable<BaseResponse>

    /**
     * Resend email API Request
     * @param user_id
     * */
    @POST(RESEND_EMAIL)
    @FormUrlEncoded
    fun resendEmail(@Field(ParamNames.USER_ID) user_id: Int): Observable<BaseResponse>

    /**
     * Verify email API Request
     * @param token
     * @param fcm_token
     * */
    @GET(VERIFY_EMAIL)
    fun verifyEmail(
        @Query(ParamNames.FCM_TOKEN) fcm_token: String,
        @Query(ParamNames.TOKEN) token: String
    ): Observable<BaseResponse>

    /**
     * Update checkout changes API Request
     * @param changeRequestId
     * @param approvalStatus
     * */
    @POST(UPDATE_CHECKOUT_CHANGINGS)
    @FormUrlEncoded
    fun updateCheckoutChangings(
        @Field(ParamNames.CHANGE_REQUEST_ID) changeRequestId: Int,
        @Field(ParamNames.APPROVAL_STATUS) approvalStatus: Int
    ): Observable<BaseResponse>

    /**
     * User details API Request
     * @param userId
     * */
    @GET(USER_DETAILS)
    fun getUserDetails(
        @QueryMap map: HashMap<String, Any>
    ): Observable<BaseResponse>

    /**
     * Suggest extension API Request
     * @param checkOutId
     * */
    @FormUrlEncoded
    @POST(SUGGEST_EXTENSION)
    fun sendExtensionRequest(@Field(ParamNames.CHECK_OUT_ID) checkOutId: String): Observable<BaseResponse>

    /**
     * Receipt invoice details API Request
     * @param id
     * @param type
     * */
    @GET(RECEIPT_INVOICE_DETAIL)
    fun getReceiptInvoiceDetails(
        @Query(ParamNames.ID) id: Int,
        @Query(ParamNames.TYPE) type: Int = 0
    ): Observable<BaseResponse>

    /**
     * Calendar validator API Request
     * @param id
     * @param pickup
     * @param dropOff
     * */
    @GET(CALENDAR_VALIDATOR)
    fun validateCalendar(
        @Query(CAR_ID) id: Int,
        @Query(ParamNames.PICK_UP) pickup: String,
        @Query(ParamNames.DROP_OFF) dropOff: String
    ): Observable<BaseResponse>



    /**
     * get Report Reason
     * */
    @GET(REPORT_REASON)
    fun getReportReason(): Observable<BaseResponse>


    /**
     * Car always available API Request
     * @param car_id
     * @param is_always_available
     * */
    @POST(REPORT_REASON_SAVE)
    fun saveReportReason(
        @Query("report_id") id: Int,
        @Query("car_id") reason: Int,
        @Query("reason") cancelReason: String
    ): Observable<BaseResponse>

    /**
     * Make selection API Request
     * @param id
     * */
    @GET(MAKE_SELECTION)
    fun makeSelection(@Query(ParamNames.YEAR_ID) id: Int): Observable<BaseResponse>

    /**
     * Model selection API Request
     * @param id
     * */
    @GET(MODEL_SELECTION)
    fun modelSelection(
        @Query(ParamNames.YEAR_ID) yearId: Int,
        @Query(ParamNames.MAKE_ID) makeId: Int
    ): Observable<BaseResponse>

    /**
     * Car docs API Request
     * */
    @GET(CARS_DOCS)
    fun getCarDocs(@Path(CAR_ID) carId: Int): Observable<BaseResponse>

    /**
     * Update car docs API Request
     * @param natId
     * @param tech
     * @param istamara
     * */
    @POST(UPDATE_CAR_DOCS)
    fun updateCarDocs(
        @Path(CAR_ID) carId: Int,
        @Body map: HashMap<String, String>
    ): Observable<BaseResponse>


    /**
     * Update car docs API Request
     * @param map
     * */
    @POST(HOST_PRE_TRIP_IMAGES)
    fun addPreTripPhotos(
        @Body map: HashMap<String?, Any?>?,
    ): Observable<BaseResponse>


    /**
     * Prepare checkout API Request
     * @param map
     * */
    @FormUrlEncoded
    @POST(PREPARE_CHECKOUT)
    fun prepareCheckout(@FieldMap map: HashMap<String, String>): Observable<BaseResponse>

    /**
     * Check payment status API Request
     * @param citId
     * @param checkOutId
     * */
    @FormUrlEncoded
    @POST(PAYMENT_STATUS)
    fun getPaymentStatus(
        @Field(CIT_ID) citId: String,
        @Field(CHECKOUT_ID) checkOutId: String?
    ): Observable<BaseResponse>

    /**
     * Check payment status API Request
     * @param resourcePath
     * */
    @FormUrlEncoded
    @POST(CARD_REGISTRATION_STATUS)
    fun getCardRegistrationStatus(
        @Field(RESOURCE_PATH) resourcePath: String,
    ): Observable<BaseResponse>

    /**
     * Add payment method API Request
     * @param paymentMethod
     * */
    @POST(PAYMENT_METHOD)
    fun addPaymentMethod(@Body paymentMethod: PaymentMethodRequestModel): Observable<BaseResponse>

    /**
     * Remove payment method API Request
     * @param paymentMethodId
     * */
    @DELETE("$PAYMENT_METHOD/{id}")
    fun removePaymentMethod(@Path("id") paymentMethodId: Int , @Query("payment_method_type") paymentMethodType: Int): Observable<BaseResponse>

    /**
     * Get payment methods API Request
     * */
    @GET(PAYMENT_METHODS)
    fun getPaymentMethods(): Observable<BaseResponse>

    /**
     * Update payment method API Request
     * @param paymentMethodId
     * */
    @PUT("$DEFAULT_PAYMENT_METHOD/{id}")
    fun setDefaultPaymentMethod(@Path("id") paymentMethodId: Int): Observable<BaseResponse>

    @GET("$TRIP_COMPLETE/{id}")
    fun completeTrip(@Path("id") tripId: Int): Observable<BaseResponse>

    @POST(CANCELLATION_IMAGES)
    fun addCancellationImages(
        @Body map: HashMap<String?, Any?>?,
    ): Observable<BaseResponse>


    @GET(CARS_FEATURES_LIST)
    fun getCarFeaturesList(): Observable<BaseResponse>

    @Multipart
    @POST(UPLOAD_IMAGES)
    fun uploadImage(@Part listPhotos: ArrayList<MultipartBody.Part>?): Observable<BaseResponse>

    @GET(CAR_ATTRIBUTES)
    fun getCarAttributes(): Observable<BaseResponse>

    @GET(SEARCH_ATTRIBUTES)
    fun getSearchCarAttributes(): Observable<BaseResponse>


    /**
     * Vehicle info API Request
     * */
    @GET(MY_VEHICLES)
    fun getMyVehicles(): Observable<BaseResponse>

    @GET(FUEL_LEVELS)
    fun getFuelLevels(): Observable<BaseResponse>

    @POST(HOLD_PAYMENT)
    fun holdPayment(
        @Body map: HashMap<String, Any?>
    ): Observable<BaseResponse>
}