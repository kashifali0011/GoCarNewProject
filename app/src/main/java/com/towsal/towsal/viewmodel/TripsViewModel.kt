package com.towsal.towsal.viewmodel

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.method.ScrollingMovementMethod
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.databinding.PopupCancelTripBinding
import com.towsal.towsal.databinding.PopupEmergencyContactBinding
import com.towsal.towsal.databinding.PopupRejectionReasonBinding
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.ApiInterface
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.cardetails.ReportReasonDetails
import com.towsal.towsal.network.serializer.carlist.step3.CarPhotoListModel
import com.towsal.towsal.network.serializer.home.CarHomeModel
import com.towsal.towsal.network.serializer.messages.MessagesDetailModel
import com.towsal.towsal.network.serializer.trips.AcceptRejectImageModel
import com.towsal.towsal.network.serializer.trips.CarPhotosDetail
import com.towsal.towsal.network.serializer.trips.CounterImageModel
import com.towsal.towsal.network.serializer.trips.OdoMeterReadingResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.views.activities.AddTripPhotosActivity.Companion.FLOW_FOR_PRE_TRIP_IMAGES
import com.towsal.towsal.views.adapters.CancelReasonAdapter
import com.towsal.towsal.views.adapters.SelectReasonOfReportAdapter
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * View model for trips module
 * */
class TripsViewModel : BaseViewModel() {

    var flowComingFrom: Int = FLOW_FOR_PRE_TRIP_IMAGES
    var carId: String = "-1"
    var checkOutId: String = "-1"
    var adminContactNo: String = ""
    var tripId = 0
    var viewType = 1
    var receiverId = 1
    var threadId = 0
    var messenger: MessengerModel? = null
    var tripStatus = 0

    private val _isLateReturnNeeded = MutableLiveData(false)

    private val _bookingStatus: MutableLiveData<Int> = MutableLiveData(0)
    private val _message: MutableLiveData<MessagesDetailModel> = MutableLiveData()

    val bookingStatus: LiveData<Int> = _bookingStatus

    val message: LiveData<MessagesDetailModel> = _message

    var isLateReturnNeeded: LiveData<Boolean> = _isLateReturnNeeded

    private val _cancelReason: MutableLiveData<String> = MutableLiveData("")
    val cancelReason: LiveData<String> = _cancelReason


    /**
     * Function for getting all user notifications
     * */
    fun getAllNotifications(
        page: Int,
        filter: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        val hashmap = HashMap<String, Any>()
        hashmap[ApiInterface.Companion.ParamNames.PAGE] = page
        hashmap[ApiInterface.Companion.ParamNames.FILTER] = if (filter != -1) filter else 1
        dataRepository.callApi(
            dataRepository.network.apis()!!.getAllNotifications(hashmap),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting all user bookings that are in pending, future and in progress status
     * */
    fun getMyBookings(
        page: Int, filter: Int, status: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        val hashmap = HashMap<String, Any>()
        hashmap[ApiInterface.Companion.ParamNames.PAGE] = page
        hashmap[ApiInterface.Companion.ParamNames.FILTER] = if (filter != -1) filter else 1
        hashmap[ApiInterface.Companion.ParamNames.STATUS] = status
        dataRepository.callApi(
            dataRepository.network.apis()!!.getMyBookings(hashmap),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting all user bookings that are in completed, cancelled, expired and in_review status
     * */
    fun getMyHistoryBookings(
        page: Int, filter: Int, status: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        val hashmap = HashMap<String, Any>()
        hashmap[ApiInterface.Companion.ParamNames.PAGE] = page
        hashmap[ApiInterface.Companion.ParamNames.FILTER] = if (filter != -1) filter else 1

        hashmap[ApiInterface.Companion.ParamNames.STATUS] = status
        dataRepository.callApi(
            dataRepository.network.apis()!!.getMyHistoryBookings(hashmap),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting user booking details
     * */
    fun getBookingDetail(
        id: Int, viewType: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getBookingDetail(id, viewType),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for sending request for updating checkout details of the booking
     * */
    fun updateCheckoutChangings(
        change_req_id: Int, approval_status: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.updateCheckoutChangings(change_req_id, approval_status),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for accept booking
     * */
    fun acceptBooking(
        id: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.acceptBooking(id),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for cancelling booking
     * */
    fun cancelBooking(
        id: Int, reason: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.cancelBooking(id, reason),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for cancelling booking
     * */
    fun cancelBookingWithReason(
        id: Int, reason: Int,
        cancelReason: String,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.cancelBookingWithId(id, reason , cancelReason),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting rejectBooking reasons
     * */
    fun rejectBookingReason(
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getRejectBookingReason(),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting cancel booking reasons
     * */
    fun cancelBookingReason(
        iAm: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getCancelBookingReason(iAm, tripId),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for rejecting booking
     * */
    fun rejectBooking(
        id: Int, reason: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.rejectBooking(id, reason),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for creating request body
     * */
    fun requestBody(value: String?): RequestBody? {
        return (value ?: ""
                ).toRequestBody("text/plain".toMediaTypeOrNull())
    }

    /**
     * Function for uploading pickup images
     * */
    fun uploadPickupImages(
        listPhotos: ArrayList<CarPhotosDetail>,
        odometerReading: String,
        id: Int,
        car_id: Int,
        showLoader: Boolean,
        tag: Int,
        needsToSendOdometerReading: Boolean,
        callback: OnNetworkResponse
    ) {
        val hashMap: HashMap<String?, String?> = HashMap()
        var isImageAdded = false
        for (
        photo in listPhotos
        ) {
            if (photo.index != -1) {
                isImageAdded = true
                hashMap[ApiInterface.Companion.ParamNames.CAR_PHOTO + "[${photo.index}]"] =
                    photo.file_name
            }
        }
        if (
            !isImageAdded
        )
            hashMap[ApiInterface.Companion.ParamNames.IS_PHOTO] = "0"
        else
            hashMap[ApiInterface.Companion.ParamNames.IS_PHOTO] = "1"
        if (needsToSendOdometerReading)
            hashMap[ApiInterface.Companion.ParamNames.ODOMETER_READING] = odometerReading

        hashMap[ApiInterface.Companion.ParamNames.CAR_CHECKOUT_ID] = id.toString()
        hashMap[ApiInterface.Companion.ParamNames.CAR_ID] = car_id.toString()
        dataRepository.callApi(
            dataRepository.network.apis()!!.addPickupImages(
                hashMap
            ),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for uploading drop off images
     * */
    fun uploadDropOffImages(
        listPhotos: ArrayList<CarPhotosDetail>,
        odometerReading: String, id: Int, car_id: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        val hashMap: HashMap<String?, String?> = HashMap()
        for (
        photo in listPhotos
        ) {
            if (photo.index != -1)
                hashMap[ApiInterface.Companion.ParamNames.CAR_PHOTO + "[${photo.index}]"] =
                    photo.file_name
        }
        hashMap[ApiInterface.Companion.ParamNames.ODOMETER_READING] =
            odometerReading
        hashMap[ApiInterface.Companion.ParamNames.CAR_CHECKOUT_ID] = id.toString()
        hashMap[ApiInterface.Companion.ParamNames.CAR_ID] = car_id.toString()
        dataRepository.callApi(
            dataRepository.network.apis()!!.addDropOffImages(
                hashMap
            ),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for accept or reject images
     * */
    fun acceptRejectImages(
        model: AcceptRejectImageModel,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {

        var containImage = false
        for (i in model.images.indices) {
            containImage = true
            break
        }
        if (containImage) {
            var statusMap: HashMap<String, Int> = HashMap()
            for (i in 0 until model.status.size) {
                statusMap["$i"] = model.status[i]
            }

            var reasonMap: HashMap<String, String> = HashMap()
            for (i in 0 until model.reason.size) {
                reasonMap["$i"] = model.reason[i]
            }

            val hashMap: HashMap<String, RequestBody> = HashMap()
            hashMap[ApiInterface.Companion.ParamNames.CAR_CHECKOUT_ID] =
                requestBody(model.car_checkout_id.toString())!!
            hashMap[ApiInterface.Companion.ParamNames.CAR_ID] =
                requestBody(model.cardId.toString())!!
            hashMap[ApiInterface.Companion.ParamNames.STATUS] =
                requestBody(statusMap.toString())!!
            hashMap[ApiInterface.Companion.ParamNames.REASON_IDS] =
                requestBody(reasonMap.toString())!!
            dataRepository.callApi(
                dataRepository.network.apis()!!.acceptRejectImages(
                    model.images, hashMap
                ),
                tag,
                callback,
                showLoader
            )
        } else {

            val statusMap: HashMap<String, Int> = HashMap()
            for (i in 0 until model.status.size) {
                statusMap["$i"] = model.status[i]
            }

            val reasonMap: HashMap<String, String> = HashMap()
            for (i in 0 until model.reason.size) {
                reasonMap["$i"] = model.reason[i]
            }
            val hashMap: HashMap<String, Any> = HashMap()
            hashMap[ApiInterface.Companion.ParamNames.CAR_CHECKOUT_ID] = model.car_checkout_id
            hashMap[ApiInterface.Companion.ParamNames.CAR_ID] = model.cardId
            hashMap[ApiInterface.Companion.ParamNames.STATUS] = statusMap
            hashMap[ApiInterface.Companion.ParamNames.REASON_IDS] = reasonMap

            //trying to simply add odometer reading
            dataRepository.callApi(
                dataRepository.network.apis()!!.acceptRejectImages(
                    hashMap
                ),
                tag,
                callback,
                showLoader
            )
        }
    }

    /**
     * Function for accept reject images with odometer
     * */
    fun acceptRejectImagesWithOdMeter(
        listPhotos: ArrayList<CarPhotosDetail>,
        odometerModel: OdoMeterReadingResponseModel,
        carCheckoutId: Int,
        carId: Int,
        acceptRejectFlag: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap[ApiInterface.Companion.ParamNames.CAR_CHECKOUT_ID] = carCheckoutId.toString()
        hashMap[ApiInterface.Companion.ParamNames.CAR_ID] = carId

        val statusList = ArrayList(
            listPhotos.map {
                it.status
            }
        )
        val reasonsList = ArrayList(
            listPhotos.map {
                it.reason
            }
        )
        hashMap[ApiInterface.Companion.ParamNames.STATUS] = statusList
        hashMap[ApiInterface.Companion.ParamNames.REASON_IDS] = reasonsList

        hashMap[ApiInterface.Companion.ParamNames.ACCEPT_REJECT_FLAG] =
            acceptRejectFlag
        hashMap[ApiInterface.Companion.ParamNames.ODOMETER_READING] =
            odometerModel

        dataRepository.callApi(
            dataRepository.network.apis()!!.acceptRejectImages(
                hashMap
            ),
            tag,
            callback,
            showLoader
        )

    }

    /**
     * Function for accept or reject drop off images
     * */
    fun acceptRejectDropOffImages(
        model: AcceptRejectImageModel,
        odometer: String,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {

        var containImage = false
        for (i in model.images.indices) {
            containImage = true
            break
        }

        if (containImage) {
            val hashMap: HashMap<String, RequestBody> = HashMap()
            hashMap[ApiInterface.Companion.ParamNames.CAR_CHECKOUT_ID] =
                requestBody(model.car_checkout_id.toString())!!
            hashMap[ApiInterface.Companion.ParamNames.CAR_ID] =
                requestBody(model.cardId.toString())!!
            if (model.accept_reject_flag != -1) {
                hashMap[ApiInterface.Companion.ParamNames.ACCEPT_REJECT_FLAG] =
                    requestBody(model.accept_reject_flag.toString())!!
            }



            for (i in model.reason.indices) {
                if (model.position != -1) {
                    hashMap[ApiInterface.Companion.ParamNames.REASON_IDS + "[${model.position}]"] =
                        requestBody(model.reason[i])!!
                } else {
                    hashMap[ApiInterface.Companion.ParamNames.REASON_IDS + "[$i]"] =
                        requestBody(model.reason[i])!!
                }

            }
            for (i in model.status.indices) {
                if (model.position != -1) {
                    hashMap[ApiInterface.Companion.ParamNames.STATUS + "[${model.position}]"] =
                        requestBody(if (model.status[i] == -1) "1" else model.status[i].toString())!!
                } else {
                    hashMap[ApiInterface.Companion.ParamNames.STATUS + "[$i]"] =
                        requestBody(if (model.status[i] == -1) "1" else model.status[i].toString())!!
                }

            }

            if (odometer.isNotEmpty()) {
                hashMap[ApiInterface.Companion.ParamNames.COUNTER_PHOTO + "[5]"] =
                    requestBody(odometer)!!
            }

            dataRepository.callApi(
                dataRepository.network.apis()!!.acceptRejectDropOffImages(
                    model.images, hashMap
                ),
                tag,
                callback,
                showLoader
            )
        } else {

            val hashMap: HashMap<String, Any> = HashMap()
            hashMap[ApiInterface.Companion.ParamNames.CAR_CHECKOUT_ID] =
                model.car_checkout_id
            hashMap[ApiInterface.Companion.ParamNames.CAR_ID] = model.cardId
            /*    hashMap[ApiInterface.Companion.ParamNames.STATUS] = model.status
                hashMap[ApiInterface.Companion.ParamNames.REASON_IDS] = model.reason*/


            hashMap[ApiInterface.Companion.ParamNames.STATUS] = model.status
            hashMap[ApiInterface.Companion.ParamNames.REASON_IDS] = model.reason
            if (model.accept_reject_flag != -1) {
                hashMap[ApiInterface.Companion.ParamNames.ACCEPT_REJECT_FLAG] =
                    model.accept_reject_flag
            }
            if (odometer.isNotEmpty()) {
                val hashMapOther: HashMap<String, Any> = HashMap()
                hashMapOther["5"] = odometer
                hashMap[ApiInterface.Companion.ParamNames.COUNTER_PHOTO] =
                    hashMapOther
            }

            dataRepository.callApi(
                dataRepository.network.apis()!!.acceptRejectDropOffImages(
                    hashMap
                ),
                tag,
                callback,
                showLoader
            )
        }
    }

    /**
     * Function for getting pickup images
     * */
    fun getPickupImages(
        carId: Int, carCheckoutId: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.viewPickupImages(carId, carCheckoutId),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting drop off images
     * */
    fun getDropOffImages(
        carId: Int, carCheckoutId: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.viewDropOffImages(carId, carCheckoutId),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting accident images
     * */
    fun viewAccidentImages(
        carId: Int, carCheckoutId: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.viewAccidentImages(carId, carCheckoutId),
            tag,
            callback,
            showLoader
        )
    }

    fun lateReturn(
        id: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ){
        dataRepository.callApi(
            dataRepository.network.apis()!!.lateReturn(id),
            tag,
            callback,
            showLoader
        )
    }


    /**
     * Function for adding accident images
     * */
    fun addAccidentImages(
        listPhotos: ArrayList<CarPhotosDetail>, id: Int, car_id: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        val hashMap: HashMap<String?, String?> = HashMap()
        for (
        photo in listPhotos
        ) {
            if (photo.index != -1)
                hashMap[ApiInterface.Companion.ParamNames.CAR_PHOTO + "[${photo.index}]"] =
                    photo.file_name
        }
        hashMap[ApiInterface.Companion.ParamNames.CAR_CHECKOUT_ID] = id.toString()
        hashMap[ApiInterface.Companion.ParamNames.CAR_ID] = car_id.toString()
        dataRepository.callApi(
            dataRepository.network.apis()!!.addAccidentPhotos(
                hashMap
            ),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for adding review
     * */
    fun addReview(
        id: Int, car_id: Int, rating: Float, review: String, is_host: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {

        dataRepository.callApi(
            dataRepository.network.apis()!!.addRatingReview(
                car_id, id, rating, review, is_host
            ),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for showing emergency contact popup
     * */
    fun popupEmergencyContact(
        activity: Activity,
        callBack: PopupCallback,
        isInProgress: Boolean,
        isLateReturnNeeded: Boolean = false,
        dismissCallback: () -> Unit
    ) {
        Log.d("tripStatus" ,tripStatus.toString())

        val accidentPopup = Dialog(activity, R.style.full_screen_dialog)
        val binding = PopupEmergencyContactBinding.bind(
            LayoutInflater.from(activity).inflate(R.layout.popup_emergency_contact, null)
        )
        accidentPopup.requestWindowFeature(Window.FEATURE_NO_TITLE)
        accidentPopup.setCancelable(false)
        accidentPopup.setContentView(binding.root)
        accidentPopup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        accidentPopup.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        if (tripStatus == Constants.BookingStatus.IN_PROGRESS || tripStatus == Constants.BookingStatus.FUTURE){
            binding.btnCallNajam.isVisible = true
            binding.btnReportAccident.isVisible = true
            binding.btnReportLate.isVisible = true
            binding.btnNotSupportYet.isVisible = false
        }else{
            binding.btnCallNajam.isVisible = false
            binding.btnReportAccident.isVisible = false
            binding.btnReportLate.isVisible = false
            binding.btnNotSupportYet.isVisible = true
        }


        accidentPopup.setOnDismissListener {
            dismissCallback()
        }
        try {
            accidentPopup.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.cross.setOnClickListener {
            try {
                accidentPopup.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.btnCallNajam.setOnClickListener {
            try {
                if (viewType == Constants.TripUserType.GUEST_TYPE){
                    accidentPopup.dismiss()
                    callBack.popupButtonClick(1)
                }else{
                    accidentPopup.dismiss()
                    callBack.popupButtonClick(4)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.btnCallNajam.text =
            if (viewType == Constants.TripUserType.GUEST_TYPE) activity.getString(R.string.call_najam) else activity.getString(
                R.string.contact_gocar
            )

        binding.btnReportAccident.isVisible = viewType == Constants.TripUserType.GUEST_TYPE && isInProgress
        binding.btnReportLate.isVisible = viewType == Constants.TripUserType.HOST_TYPE && isLateReturnNeeded

        binding.btnReportLate.setOnClickListener {
            try {
                callBack.popupButtonClick(3)
                accidentPopup.dismiss()
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        binding.btnReportAccident.setOnClickListener {
            try {
                callBack.popupButtonClick(2)
                accidentPopup.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    /**
     * Function for showing the cancellation popup
     * */
    fun popupCancellationPolicy(
        activity: Activity,
        callBack: PopupCallback,
        cancellationPolicy: String,
        readMoreLink : String,
        readMore: String,
        cancelReason: ArrayList<CarHomeModel>
    ) {
        val cancellationPopup = Dialog(activity, R.style.full_screen_dialog_2)
        val binding = PopupCancelTripBinding.bind(
            LayoutInflater.from(activity).inflate(
                R.layout.popup_cancel_trip, null
            )
        )
        cancellationPopup.requestWindowFeature(Window.FEATURE_NO_TITLE)
        cancellationPopup.setCancelable(false)
        cancellationPopup.setContentView(binding.root)
        cancellationPopup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        cancellationPopup.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        val adapter = CancelReasonAdapter(activity, uiHelper, cancelReason){ reasonId ->
            binding.etReason.setText("")
            binding.llReason.isVisible = reasonId == 11 || reasonId == 6
        }

        binding.recyclerView.adapter = adapter
        var lenghtCancellationPolicy = cancellationPolicy.length
        var lenghtReadMore = readMore.length
        var getFinalLength = lenghtCancellationPolicy + lenghtReadMore
        val WordtoSpan: Spannable = SpannableString(cancellationPolicy + readMore)
        WordtoSpan.setSpan(ForegroundColorSpan(Color.BLUE), lenghtCancellationPolicy, getFinalLength, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        WordtoSpan.setSpan(UnderlineSpan(), lenghtCancellationPolicy, getFinalLength, 0)
        binding.guidlines.text = WordtoSpan
     //   binding.guidlines.text = cancellationPolicy + readMore
        binding.guidlines.movementMethod = ScrollingMovementMethod()

        binding.guidlines.setOnClickListener {
            if (readMoreLink.isNotEmpty()) {
                var builder = CustomTabsIntent.Builder()
                val customTabsIntent: CustomTabsIntent = builder.build()
                customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                customTabsIntent.intent.`package` = "com.android.chrome"
                customTabsIntent.launchUrl(activity, readMoreLink.toUri())
            }
        }

        try {
            cancellationPopup.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        binding.layoutToolBar.toolbarTitle.text = activity.getString(R.string.cancellation_policy_)
        binding.layoutToolBar.ivArrowBack.setOnClickListener {
            try {
                cancellationPopup.dismiss()
                callBack.popupButtonClick(0, "hello")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        binding.btnSubmit.setOnClickListener {
            var checkTest = false
            try {
                var reasonId = -1
                for (i in adapter.getList().indices) {
                    if (adapter.getList()[i].selected) {
                        reasonId = adapter.getList()[i].id
                        break
                    }
                }
                if (reasonId == -1) {
                    uiHelper.showLongToastInCenter(
                        activity,
                        activity.getString(R.string.select_reasons_for_booking_cancellation)
                    )
                    return@setOnClickListener
                }
                if (reasonId in listOf(
                        Constants.CancellationPolicy.FLIGHT_DELAY,
                        Constants.CancellationPolicy.DISINFECTION,
                        Constants.CancellationPolicy.LOST_BAGGAGE
                    )
                ) {
                    callBack.popupButtonClick(4, reasonId.toString() )
                } else {
                    if (reasonId == 11|| reasonId == 6){
                        checkTest = true
                        var cancelText = binding.etReason.text
                        checkTest = !binding.etReason.text.isNotEmpty()
                        setCacelReason(cancelText.toString())
                    }
                    callBack.popupButtonClick(3, reasonId.toString())
                }
                if (!checkTest){
                    cancellationPopup.dismiss()
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }

    /**
     * Function for showing popup rejection image
     * */
    fun popupRejectionImage(
        activity: Activity,
        callBack: PopupCallback,
        rejectionIdxValue: Int
    ) {
        val popupRejectShow = Dialog(activity, R.style.full_screen_dialog)
        val binding = PopupRejectionReasonBinding.bind(
            LayoutInflater.from(activity).inflate(R.layout.popup_rejection_reason, null)
        )
        popupRejectShow.requestWindowFeature(Window.FEATURE_NO_TITLE)
        popupRejectShow.setCancelable(true)
        popupRejectShow.setContentView(binding.root)
        popupRejectShow.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupRejectShow.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        try {
            popupRejectShow.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        binding.btnSubmit.setOnClickListener {
            try {
                if (binding.descriptionEdt.text.toString().isEmpty()) {
                    uiHelper.showLongToastInCenter(
                        activity,
                        activity.getString(R.string.enter_reason_for_rejection)
                    )
                }
                val counterImageModel = CounterImageModel()
                counterImageModel.position = rejectionIdxValue
                counterImageModel.reason = binding.descriptionEdt.text.toString()
                callBack.popupButtonClick(rejectionIdxValue, counterImageModel)
                popupRejectShow.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Function for sending extension request
     * */
    fun sendExtensionRequest(
        id: String,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.sendExtensionRequest(id),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for showing rejection popup information
     * */
    fun popupRejectionInfo(
        activity: Activity,
        reasonText: String
    ) {
        val popupRejectShow: Dialog? = Dialog(activity, R.style.full_screen_dialog)
        popupRejectShow?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        popupRejectShow?.setCancelable(false)
        popupRejectShow?.setContentView(R.layout.popup_reject_info)
        popupRejectShow?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupRejectShow?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val cross: ImageButton? = popupRejectShow?.findViewById(R.id.cross)

        val reason: TextView? = popupRejectShow?.findViewById(R.id.reason)
        reason?.text = reasonText
        reason?.movementMethod = ScrollingMovementMethod()
        if (popupRejectShow != null) {
            try {
                popupRejectShow.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        cross?.setOnClickListener {
            try {
                popupRejectShow.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


    }

    fun addTripPhotos(
        list: List<CarPhotoListModel>,
        tripId: String,
        carId: String,
        imageType: Int,
        onNetworkResponse: OnNetworkResponse,
        tag: Int,
        odometerReading: String,
        fuelLevelId: Int
    ) {
        val hashMap = HashMap<String?, Any?>()
        hashMap[ApiInterface.Companion.ParamNames.CAR_ID] = carId
        hashMap[ApiInterface.Companion.ParamNames.TRIP_ID] = tripId
        hashMap[ApiInterface.Companion.ParamNames.IMAGE_TYPE] = imageType.toString()
        hashMap[ApiInterface.Companion.ParamNames.CAR_PHOTOS] = list.map { it.url }
        hashMap[ApiInterface.Companion.ParamNames.TRIP_READING] = odometerReading
        hashMap[ApiInterface.Companion.ParamNames.FUEL_LEVEL_ID] = fuelLevelId
        dataRepository.callApi(
            dataRepository.network.apis()!!.addPreTripPhotos(
                hashMap
            ),
            tag,
            onNetworkResponse,
            true
        )
    }

    fun addCancellationImages(
        list: List<CarPhotoListModel>,
        tripId: String,
        carId: String,
        reasonId: Int,
        onNetworkResponse: OnNetworkResponse,
        tag: Int
    ) {
        val hashMap = HashMap<String?, Any?>()
        hashMap[ApiInterface.Companion.ParamNames.CAR_ID] = carId
        hashMap[ApiInterface.Companion.ParamNames.TRIP_ID] = tripId
        hashMap[ApiInterface.Companion.ParamNames.REASON_ID] = reasonId.toString()
        hashMap[ApiInterface.Companion.ParamNames.CANCELLATION_TYPE] =
            when (reasonId) {
                Constants.CancellationPolicy.FLIGHT_DELAY -> Constants.CancellationType.FLIGHT_DELAY.toString()
                Constants.CancellationPolicy.LOST_BAGGAGE -> Constants.CancellationType.LOST_BAGGAGE.toString()
                else -> {
                    Constants.CancellationType.DISINFECTION.toString()
                }
            }

        hashMap[ApiInterface.Companion.ParamNames.MEDIA_FILE] = ArrayList(
            list.map {
                it.url
            }
        )
        dataRepository.callApi(
            dataRepository.network.apis()!!.addCancellationImages(
                hashMap
            ),
            tag,
            onNetworkResponse,
            true
        )
    }

    fun completeTrip(
        checkOutId: Int,
        tag: Int,
        onNetworkResponse: OnNetworkResponse,
        showLoader: Boolean
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.completeTrip(checkOutId),
            tag,
            onNetworkResponse,
            showLoader
        )
    }

    fun setBookingStatus(bookingStatus: Int) {
        _bookingStatus.postValue(bookingStatus)
    }

    fun setMessage(messagesDetailModel: MessagesDetailModel){
        _message.postValue(messagesDetailModel)
    }

    fun setIsLateReturnNeeded(isLateReturnNeeded: Boolean){
        _isLateReturnNeeded.postValue(isLateReturnNeeded)
    }
    fun setCacelReason(cancelReason: String){
        _cancelReason.postValue(cancelReason)
    }

    /**
     * Function for showing report listing error
     * */
    @SuppressLint("SimpleDateFormat")
    fun showFullImage(
        activity: Activity,
        imageUrl: String
    ) {
        val calendarPopup = Dialog(activity, R.style.full_screen_dialog_2)
        calendarPopup.requestWindowFeature(Window.FEATURE_NO_TITLE)
        calendarPopup.setCancelable(false)
        calendarPopup.setContentView(R.layout.full_image_show_layout)
        calendarPopup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        calendarPopup.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        val ivBack: ImageView? = calendarPopup.findViewById(R.id.ivBack)
        val ivFullPicShow: ImageView = calendarPopup.findViewById(R.id.ivFullPicShow)
        uiHelper.glideLoadImage(
            activity,
            imageUrl,
            ivFullPicShow
        )
        ivBack?.setOnClickListener {
            try {
                calendarPopup.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (calendarPopup != null) {
            try {
                calendarPopup.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    /**
     * Function for showing report listing error
     * */
    @SuppressLint("SimpleDateFormat")
    fun showFullWebView(
        activity: Activity,
        imageUrl: String
    ) {

        val calendarPopup = Dialog(activity, R.style.full_screen_dialog_2)
        calendarPopup.requestWindowFeature(Window.FEATURE_NO_TITLE)
        calendarPopup.setCancelable(false)
        calendarPopup.setContentView(R.layout.full_web_view_show_layout)
        calendarPopup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        calendarPopup.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        val ivBack: ImageView? = calendarPopup.findViewById(R.id.ivBack)
        val wvPdfLoader: WebView = calendarPopup.findViewById(R.id.wvPdfLoader)


        Handler(Looper.getMainLooper()).postDelayed({
            wvPdfLoader.webViewClient = WebViewClient()
            wvPdfLoader.settings.setSupportZoom(false)
            wvPdfLoader.settings.javaScriptEnabled = true
            wvPdfLoader.settings.builtInZoomControls = false

            //showLoaderDialog()
            Log.e("onCreate: ", BuildConfig.IMAGE_BASE_URL + imageUrl)
            wvPdfLoader.loadUrl("https://docs.google.com/gview?embedded=true&url=${BuildConfig.IMAGE_BASE_URL + imageUrl}")
            wvPdfLoader.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                 //   hideLoaderDialog()
                }
            }
        }, 500)

        ivBack?.setOnClickListener {
            try {
                calendarPopup.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        if (calendarPopup != null) {
            try {
                calendarPopup.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

}