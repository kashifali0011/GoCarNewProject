package com.towsal.towsal.views.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.app.MainApplication
import com.towsal.towsal.databinding.FragmentGuestTripDetailBinding
import com.towsal.towsal.extensions.*
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.cardetails.CalendarDateTimeModel
import com.towsal.towsal.network.serializer.cardetails.CarFullDetailModel
import com.towsal.towsal.network.serializer.carlist.CarInformationModel
import com.towsal.towsal.network.serializer.checkoutcarbooking.CarCheckOutResponseModel
import com.towsal.towsal.network.serializer.checkoutcarbooking.GetCarBookingModel
import com.towsal.towsal.network.serializer.trips.CancellationPolicyModel
import com.towsal.towsal.network.serializer.trips.TripDetailModel
import com.towsal.towsal.network.serializer.trips.TripResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil
import com.towsal.towsal.viewmodel.CheckoutCarBookingViewModel
import com.towsal.towsal.viewmodel.TripsViewModel
import com.towsal.towsal.views.activities.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity class for trip details for user who is guest in the booking
 * */
class
GuestTripDetailFragment : BaseFragment(), OnNetworkResponse, PopupCallback {

    lateinit var binding: FragmentGuestTripDetailBinding
    val tripsViewModel: TripsViewModel by activityViewModels()
    private val carCheckOutViewModel: CheckoutCarBookingViewModel by viewModel()
    var model = TripDetailModel()

    private val checkoutInfoModel =
        GetCarBookingModel()        //model for the checkout screen to make trip editable
    var lat = 0.0
    var lng = 0.0
    var carInfoModel = CarInformationModel()
    var calendarDateTimeModel = CalendarDateTimeModel()

    var responseModel = TripResponseModel()
    private var responseModeless = CarCheckOutResponseModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGuestTripDetailBinding.bind(inflater.inflate(R.layout.fragment_guest_trip_detail, container, false))
        binding.fragment = this
      //  setUiObserver()
        binding.clMainRoot.isVisible = false
        return binding.root
    }

  /*  private fun setUiObserver(){
        tripsViewModel.cancelReason.observe(viewLifecycleOwner){
            var text = it
            Log.d("cancelReasonText" , text)
        }
    }*/

    private fun setGradientTextColors() {
        if (
            model.status == Constants.BookingStatus.IN_PROGRESS && model.drop_off_image_status == Constants.ImagesStatus.PENDING
        ) {
            binding.tvAddDropOff.setGradientTextColor(
                colors = intArrayOf(
                    ContextCompat.getColor(requireActivity(), R.color.send_msg_bg),
                    ContextCompat.getColor(requireActivity(), R.color.text_receive_msg)
                ),
                x0 = -1.283f,
                y0 = -0.921f,
                x1 = 30.5f,
                y1 = 15.455f,
            )
        } else {
            binding.tvAddDropOff.paint.shader = null
            binding.tvAddDropOff.setTextColor(
                Color.parseColor(
                    "#C2C2C2"
                )
            )
        }

        if (
            model.pick_up_image_status == Constants.ImagesStatus.PENDING &&
            model.status == Constants.BookingStatus.FUTURE
            && model.futureTimeCheck == 1
            && model.preTripImagesStatus == Constants.HostBookingImagesStatus.ADDED
        ) {
            binding.tvAddPickUp.setGradientTextColor(
                colors = intArrayOf(
                    requireActivity().getColor(R.color.send_msg_bg),
                    requireActivity().getColor(R.color.text_receive_msg)
                ),
                x0 = -1.283f,
                y0 = -0.921f,
                x1 = 30.5f,
                y1 = 15.455f,
            )
        } else {
            binding.tvAddPickUp.paint.shader = null
            binding.tvAddPickUp.setTextColor(
                Color.parseColor(
                    "#C2C2C2"
                )
            )
        }

        binding.btnWriteReview.setGradientTextColor(
            colors = intArrayOf(
                ContextCompat.getColor(requireActivity(), R.color.send_msg_bg),
                ContextCompat.getColor(requireActivity(), R.color.text_receive_msg),
            ),
            x0 = -4.35f,
            y0 = -4.3f,
            x1 = 50.527f,
            y1 = 55.785f,
            positions = floatArrayOf(
                0f,
                1f
            )
        )
    }

    private fun callApi() {
        tripsViewModel.getBookingDetail(
            tripsViewModel.tripId,
            tripsViewModel.viewType,
            true,
            Constants.API.GET_GUEST_BOOKING_DETAIL,
            this
        )
    }

    /**
     * Function click listeners
     * */
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view.id) {
            binding.tvTermsAndConditions.id -> {
                carCheckOutViewModel.showTermConditionPrivacyPolicyPopup(requireActivity(), this)
            }
            binding.tvFuelPolicy.id -> {
                carCheckOutViewModel.showTermConditionPrivacyPolicyPopup(requireActivity(), this)
            }
            binding.tvAddPickUp.id -> {
                if (model.pick_up_image_status == Constants.ImagesStatus.SUBMITTED) {
                    uiHelper.showLongToastInCenter(
                        requireActivity(),
                        getString(R.string.pending_approval_err_msg)
                    )
                } else {
                    val bundle = Bundle()
                    bundle.putInt(
                        Constants.DataParsing.GOTO_PHOTOS,
                        Constants.CarPhotos.GUEST_PICKUP_PHOTOS
                    )
                    bundle.putInt(
                        Constants.DataParsing.ID,
                        model.id
                    )
                    bundle.putInt(
                        Constants.DataParsing.CAR_ID,
                        model.car_id
                    )
                    bundle.putInt(
                        Constants.DataParsing.CAR_IMAGE_STATUS_PICKUP,
                        model.pick_up_image_status
                    )
                    bundle.putInt(
                        Constants.DataParsing.CAR_IMAGE_STATUS_DROP_OFF,
                        model.pick_up_image_status
                    )
                    bundle.putString(Constants.DataParsing.HEADING, getString(R.string.at_pickup))
                    uiHelper.openActivityForResult(
                        requireActivity(),
                        AddTripDetailImages::class.java,
                        true,
                        Constants.RequestCodes.GUEST_TRIP_DETAIL, bundle
                    )
                }
            }
            binding.tvAddDropOff.id -> {
                if (model.drop_off_image_status == Constants.ImagesStatus.SUBMITTED) {
                    uiHelper.showLongToastInCenter(
                        requireActivity(),
                        getString(R.string.pending_approval_err_msg)
                    )
                } else {
                    val bundle = Bundle()
                    bundle.putInt(
                        Constants.DataParsing.GOTO_PHOTOS,
                        Constants.CarPhotos.GUEST_DROP_OFF_PHOTOS
                    )
                    bundle.putString(Constants.DataParsing.HEADING, getString(R.string.at_dropoff))
                    bundle.putInt(
                        Constants.DataParsing.ID,
                        model.id
                    )
                    bundle.putInt(
                        Constants.DataParsing.CAR_ID,
                        model.car_id
                    )
                    bundle.putInt(
                        Constants.DataParsing.CAR_IMAGE_STATUS_PICKUP,
                        model.pick_up_image_status
                    )
                    bundle.putInt(
                        Constants.DataParsing.CAR_IMAGE_STATUS_DROP_OFF,
                        model.pick_up_image_status
                    )
                    uiHelper.openActivityForResult(
                        requireActivity(),
                        AddTripDetailImages::class.java,
                        true,
                        Constants.RequestCodes.GUEST_TRIP_DETAIL, bundle
                    )
                }
            }
            binding.btnWriteReview.id -> {
                val bundle = Bundle()
                bundle.putInt(
                    Constants.DataParsing.ID,
                    model.id
                )
                bundle.putInt(
                    Constants.DataParsing.CAR_ID,
                    model.car_id
                )
                bundle.putString("viewType", "guest")
                uiHelper.openActivityForResult(
                    requireActivity(),
                    RatingReviewActivity::class.java,
                    true,
                    Constants.RequestCodes.GUEST_TRIP_DETAIL, bundle
                )
            }

            binding.btnCancelTrip.id -> {
                tripsViewModel.cancelBookingReason(
                    1,
                    true,
                    Constants.API.CANCEL_BOOKING_REASON,
                    this
                )
            }
            binding.btnEditTrip.id -> {

//pending to do here .......have to call checkout detail screen with bundle to get the details
                gotoCheckOutScreen()

            }
            binding.tvInvoice.id -> {
                val bundle = Bundle()
                bundle.putInt(Constants.DataParsing.ID, model.id)
                uiHelper.openActivity(requireActivity(), InvoiceActivity::class.java, bundle)
            }
            binding.tvHostName.id -> {
                val bundle = Bundle()
                bundle.putInt(Constants.DataParsing.USER_ID, model.user_id)
                uiHelper.openActivity(requireActivity(), UserProfileActivity::class.java, bundle)
            }

            binding.tvPickUpAndReturn.id ->{

                    if (responseModel.bookingDetails.car_pin_lat != 0.0) {

                        activity?.openMap(
                            responseModel.bookingDetails.car_pin_lat,
                            responseModel.bookingDetails.car_pin_long
                        )

                        /* carInfoModel.fromActivity = true
                         carInfoModel.pin_lat = responseModel.bookingDetails.car_pin_lat
                         carInfoModel.pin_long = responseModel.bookingDetails.car_pin_long


                         carInfoModel.isHideChnageLocation = true
                         val bundle = Bundle()
                         bundle.putSerializable(
                             Constants.DataParsing.MODEL, responseModeless.car_location_delivery
                         )
                         bundle.putSerializable(Constants.DataParsing.CARINFODATAMODEL, carInfoModel)
                         uiHelper.openActivityForResult(
                             requireActivity(),
                             LocationSelectionActivity::class.java,
                             true,
                             Constants.RequestCodes.ACTIVTIY_LOCATION_SELECTION,
                             bundle
                         )*/
                    }

            }
        }

    }

    /**
     * Function for opening checked activity
     * */
    private fun gotoCheckOutScreen() {
        val bundle = Bundle()
        val PERMISSION =
            Manifest.permission.ACCESS_FINE_LOCATION
        if (uiHelper.isPermissionAllowed(requireActivity(), PERMISSION) && gpsTracker != null) {
            checkoutInfoModel.lat = gpsTracker?.getLatitude()!!
            checkoutInfoModel.lng = gpsTracker?.getLongitude()!!
        }
        checkoutInfoModel.city_id =
            preferenceHelper.getIntOther(Constants.DataParsing.CITY_SELECTED_ID, 0)
        checkoutInfoModel.car_id = model.car_id
        checkoutInfoModel.pick_up = model.car_pick_up
        checkoutInfoModel.drop_off = model.car_drop_off


//calculating calendar date times for calendar handling
        val currentDateTime = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:00:ss")
        val dateOnly = SimpleDateFormat("yyyy-MM-dd")

//currentDateTime.add(Calendar.HOUR, noticeTime_ + 1)
        calendarDateTimeModel.minimumDateTime = timeFormat.format(currentDateTime.time)
        calendarDateTimeModel.minimumDateTimeDefault =
            timeFormat.format(currentDateTime.time)
        calendarDateTimeModel.minimumDateOnly = dateOnly.format(currentDateTime.time)
        calendarDateTimeModel.minimumDateOnlyDefault =
            dateOnly.format(currentDateTime.time)
        if (calendarDateTimeModel.pickUpDateServer.isEmpty()) {
            calendarDateTimeModel.pickUpDateServer = timeFormat.format(currentDateTime.time)
            calendarDateTimeModel.pickUpDateComplete = dateOnly.format(currentDateTime.time)
/*currentDateTime.add(
Calendar.HOUR,
model.car_availability.min_trip_dur.name.toInt()
)*/
            calendarDateTimeModel.dropOffDateServer =
                timeFormat.format(currentDateTime.time)
            calendarDateTimeModel.dropOffDateComplete =
                dateOnly.format(currentDateTime.time)
        }
        calendarDateTimeModel.minimumDurationDateTime =
            timeFormat.format(currentDateTime.time)
        calendarDateTimeModel.minimumDurationDateTimeDefault =
            timeFormat.format(currentDateTime.time)

        bundle.putSerializable(Constants.DataParsing.CARINFODATAMODEL, carInfoModel)
        bundle.putSerializable(Constants.DataParsing.MODEL, checkoutInfoModel)
        bundle.putSerializable(Constants.DataParsing.DATE_TIME_MODEL, calendarDateTimeModel)

        bundle.putInt(Constants.DataParsing.ID, tripsViewModel.tripId)

        bundle.putInt("flow", 2)
        bundle.putInt("modelStatus", model.status)
        bundle.putString(Constants.DataParsing.HOST_NAME, model.host_name)

        Log.d("calendarDateTime", "model = " + calendarDateTimeModel.compareDropOffDate)



        uiHelper.openActivityForResult(
            requireActivity(),
            CheckoutCarBookingActivity::class.java,
            bundle,
            Constants.RequestCodes.ACTIVITY_CHECKOUT_CAR_BOOKING
        )
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_GUEST_BOOKING_DETAIL -> {
                 responseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    TripResponseModel::class.java
                )
                binding.clMainRoot.isVisible = true
                binding.model = responseModel.bookingDetails
                this.model = binding.model!!
                tripsViewModel.carId = model.car_id.toString()
                tripsViewModel.receiverId = model.user_id
                tripsViewModel.adminContactNo = model.callNum
                tripsViewModel.threadId = model.threadId ?: 0
                tripsViewModel.messenger = model.messenger
                tripsViewModel.tripStatus = model.status

                tripsViewModel.setBookingStatus(model.status)
                binding.tvStartDate.text = DateUtil.changeDateFormat(
                    Constants.ServerDateTimeFormat,
                    Constants.UIDateFormat,
                    model.car_pick_up
                )
                binding.tvStartTime.text = DateUtil.changeDateFormat(
                    Constants.ServerDateTimeFormat,
                    Constants.UITimeFormat,
                    model.car_pick_up
                )
                binding.tvFinishDate.text = DateUtil.changeDateFormat(
                    Constants.ServerDateTimeFormat,
                    Constants.UIDateFormat,
                    model.car_drop_off
                )
                binding.tvFinishTime.text = DateUtil.changeDateFormat(
                    Constants.ServerDateTimeFormat,
                    Constants.UITimeFormat,
                    model.car_drop_off
                )
                binding.tvTripId.text = getString(R.string.trip_id, model.id.toString())
                binding.tvTripId.setOnLongClickListener {
                    val cm: ClipboardManager? =
                        requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
                    cm?.setPrimaryClip(ClipData.newPlainText("trip id", model.id.toString()))
                    requireActivity().showToast("Copied!")
                    true
                }

                binding.tvCarName.text = "${model.car_make} ${model.car_model} (${model.car_year})"
                binding.tvLocation.text = model.booking_street_address
                binding.tvTotalRent.text =
                    getString(
                        R.string.total_rent,
                        "   " + model.total_rent,
                        getString(R.string.sar)
                    )
                binding.tvHostName.text = model.host_name.trim()
                binding.tvOwnerName.text = getString(R.string.owner_name, model.host_name)
                binding.ivCarImage.loadImage(BuildConfig.IMAGE_BASE_URL + model.car_image)

                setImagesStatus()
                setLayoutAccordingToStatus(responseModel)
            }
            Constants.API.CANCEL_BOOKING -> {
                val intent = Intent()
                requireActivity().setResult(RESULT_OK, intent)
                requireActivity().finish()
            }
            Constants.API.CANCEL_BOOKING_REASON -> {
                val responseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    CancellationPolicyModel::class.java
                )
                var readMore = getString(R.string.read_more)
                tripsViewModel.popupCancellationPolicy(
                    requireActivity(),
                    this,
                    responseModel.cancellationPolicy,
                    responseModel.readMore,
                    readMore,
                    responseModel.cancelReason
                )
            }
            Constants.API.COMPLETE_TRIP -> {
                uiHelper.showLongToastInCenter(
                    requireActivity(),
                    response?.message ?: ""
                )
                callApi()

            }
        }
    }

    /**
     * Function for setting up image status
     * */
    private fun setImagesStatus() {
        binding.tvPickUpStatus.text = when (model.pick_up_image_status) {
            Constants.ImagesStatus.PENDING -> {
                resources.getString(R.string.pending)
            }
            Constants.ImagesStatus.SUBMITTED, Constants.ImagesStatus.APPROVED, Constants.ImagesStatus.REJECTED -> {
                resources.getString(R.string.added)
            }
            else -> {
                ""
            }
        }
        binding.tvDropOffStatus.text = when (model.drop_off_image_status) {
            Constants.ImagesStatus.PENDING -> {
                resources.getString(R.string.pending)
            }
            Constants.ImagesStatus.SUBMITTED, Constants.ImagesStatus.APPROVED, Constants.ImagesStatus.REJECTED -> {
                resources.getString(R.string.added)
            }
            else -> {
                ""
            }
        }
    }

    /**
     * Function for setting up ui according to booking status
     * */
    private fun setLayoutAccordingToStatus(responseModel: TripResponseModel) {

        setVisibilities(responseModel)
        enablingButtons(responseModel)
        val timeRemaining: String

        when (responseModel.bookingDetails.status) {
            Constants.BookingStatus.FUTURE -> {
                timeRemaining =
                    if (responseModel.bookingDetails.timeDifference.contains("ago") || responseModel.bookingDetails.timeDifference.contains(
                            "now"
                        )
                    ) {
                        resources.getString(R.string.pick_up_time_in)
                            .replace(
                                " in",
                                ""
                            ) + " " + responseModel.bookingDetails.timeDifference.lowercase()
                    } else {
                        resources.getString(R.string.pick_up_time_in) + " " + responseModel.bookingDetails.timeDifference.lowercase()
                            .lowercase()
                    }
            }
            Constants.BookingStatus.IN_PROGRESS -> {
                timeRemaining =
                    if (responseModel.bookingDetails.timeDifference.contains("ago") || responseModel.bookingDetails.timeDifference.contains(
                            "now"
                        )
                    ) {
                        resources.getString(R.string.drop_off_time_in)
                            .replace(
                                " in",
                                ""
                            ) + " " + responseModel.bookingDetails.timeDifference.lowercase()
                    } else {
                        resources.getString(R.string.drop_off_time_in) + " " + responseModel.bookingDetails.timeDifference.lowercase()
                    }
            }
            else -> {
                timeRemaining = ""
            }
        }

        binding.tvRemainingTime.setBackgroundResource(
            if (timeRemaining.contains("ago")) R.color.coral else
                R.color.bg_chat_bottom
        )
        val spannable = uiHelper.spanString(
            timeRemaining,
            if (timeRemaining.contains(getString(R.string.pick_up_time_in))) getString(R.string.pick_up_time_in) else if (timeRemaining.contains(
                    getString(R.string.drop_off_time_in)
                )
            ) getString(
                R.string.drop_off_time_in
            ) else if (timeRemaining.contains(
                    getString(R.string.drop_off_time_in).replace(
                        " in",
                        ""
                    )
                )
            ) getString(R.string.drop_off_time_in).replace(" in", "")
            else
                getString(R.string.pick_up_time_in).replace(" in", ""),
            R.font.inter_bold,
            requireActivity()
        )
        binding.tvRemainingTime.text =
            spannable

    }

    private fun enablingButtons(responseModel: TripResponseModel) {

        binding.tvAddPickUp.isEnabled =
            responseModel.bookingDetails.pick_up_image_status == Constants.ImagesStatus.PENDING &&
                    responseModel.bookingDetails.status == Constants.BookingStatus.FUTURE
                    && responseModel.bookingDetails.futureTimeCheck == 1
                    && responseModel.bookingDetails.preTripImagesStatus == Constants.HostBookingImagesStatus.ADDED


        binding.tvAddDropOff.isEnabled =
            responseModel.bookingDetails.status == Constants.BookingStatus.IN_PROGRESS && responseModel.bookingDetails.drop_off_image_status == Constants.ImagesStatus.PENDING
        setGradientTextColors()

    }

    private fun setVisibilities(responseModel: TripResponseModel) {

        binding.tvRemainingTime.isVisible = responseModel.bookingDetails.status in listOf(
            Constants.BookingStatus.FUTURE,
            Constants.BookingStatus.IN_PROGRESS
        )
        binding.btnEditTrip.isVisible = responseModel.bookingDetails.status in listOf(
            Constants.BookingStatus.PENDING,
            Constants.BookingStatus.FUTURE,
            Constants.BookingStatus.IN_PROGRESS
        ) && !responseModel.bookingDetails.isEditRequest
        val bool = if (responseModel.bookingDetails.status == Constants.BookingStatus.CANCELLED) responseModel.bookingDetails.previousStatus == Constants.BookingStatus.PENDING
        else responseModel.bookingDetails.status == Constants.BookingStatus.PENDING
        binding.clUploadPhotos.isVisible = !bool

        if (responseModel.bookingDetails.lateReturnDropOff && responseModel.bookingDetails.status == Constants.BookingStatus.CONFLICTED){
            binding.clUploadPhotos.isVisible = true
        }/*else{
            binding.clUploadPhotos.isVisible = f
        }*/

        binding.btnWriteReview.isVisible =
            responseModel.bookingDetails.status == Constants.BookingStatus.COMPLETED && responseModel.bookingDetails.is_rated == Constants.RatingStatus.NOT_RATED_YET

        binding.btnCancelTrip.isVisible = responseModel.bookingDetails.status in listOf(
            Constants.BookingStatus.PENDING,
            Constants.BookingStatus.FUTURE
        )
       // binding.tvInvoice.isVisible = binding.clUploadPhotos.isVisible
        binding.tvTermsAndConditions.isVisible = binding.clUploadPhotos.isVisible
        binding.tvFuelPolicy.isVisible = binding.clUploadPhotos.isVisible
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(requireActivity(), response?.message)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data?.extras != null) {
            if (resultCode == RESULT_OK) {
                if (requestCode == Constants.RequestCodes.GUEST_TRIP_DETAIL) {
                    if (data.extras != null) {
                        val intent = Intent()
                        requireActivity().setResult(RESULT_OK, intent)
                        tripsViewModel.getBookingDetail(
                            tripsViewModel.tripId,
                            tripsViewModel.viewType,
                            true,
                            Constants.API.GET_GUEST_BOOKING_DETAIL,
                            this
                        )
                    }
                }

                if (requestCode == Constants.RequestCodes.ACTIVITY_CHECKOUT_CAR_BOOKING) {
                    requireActivity().setResult(RESULT_OK)
                    requireActivity().finish()
                }
            }
        }
    }

    override fun popupButtonClick(value: Int) {
        if (value == 1) {
            if (ActivityCompat.checkSelfPermission(
                    MainApplication.applicationContext(),
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                uiHelper.requestPermission(Manifest.permission.CALL_PHONE, requireActivity())
            } else {
                val intent = Intent(
                    Intent.ACTION_CALL,
                    Uri.parse("tel:" + model.admin_contact)
                )
                startActivity(intent)
            }
        }
        if (value == 2) {
            val bundle = Bundle()
            bundle.putInt(
                Constants.DataParsing.GOTO_PHOTOS,
                Constants.CarPhotos.GUEST_ACCIDENT_PHOTOS
            )
            bundle.putInt(
                Constants.DataParsing.ID,
                model.id
            )
            bundle.putInt(
                Constants.DataParsing.CAR_ID,
                model.car_id
            )
            bundle.putString(Constants.DataParsing.HEADING, getString(R.string.report_accident))
            uiHelper.openActivityForResult(
                requireActivity(),
                AddTripDetailImages::class.java,
                true,
                Constants.RequestCodes.GUEST_TRIP_DETAIL, bundle
            )
        }
    }

    override fun popupButtonClick(value: Int, id: Any) {
        if (value == 3) {
            var number = 1
            if (id.toString().toInt() == 11){
                tripsViewModel.cancelReason.observe(viewLifecycleOwner){
                    var reasons = it
                    if (reasons.isNotEmpty()){
                        if (number == 2){
                        tripsViewModel.cancelBookingWithReason(
                            model.id,
                            id.toString().toInt(),
                            reasons,
                            true,
                            Constants.API.CANCEL_BOOKING,
                            this
                        )
                    }}else{
                        if (number == 2){
                            requireActivity().showToast(getString(R.string.other_field_required))
                        }
                    }
                    number++

                }
            }else{
                tripsViewModel.cancelBooking(
                    model.id,
                    id.toString().toInt(),
                    true,
                    Constants.API.CANCEL_BOOKING,
                    this
                )
            }
        } else if (value == 4) {
            requireActivity().showCancelTripImagesPopUp(id) { reasonId, type ->
                val bundle = Bundle()
                bundle.putInt(
                    Constants.DataParsing.FLOW_COMING_FROM,
                    AddTripPhotosActivity.FLOW_FROM_CHECKOUT_SCREEN
                )
                bundle.putString(Constants.DataParsing.CHECKOUT_ID, model.id.toString())
                bundle.putString(Constants.CAR_ID, model.car_id.toString())
                bundle.putInt(Constants.DataParsing.REASON_ID, reasonId.toString().toInt())
                bundle.putInt(Constants.DataParsing.FILE_TYPE, type.toString().toInt())
                requireActivity().intent.putExtras(bundle)
                uiHelper.openActivity(requireActivity(), AddTripPhotosActivity::class.java, bundle)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String?>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(
                Intent.ACTION_CALL,
                Uri.parse("tel:" + model.admin_contact)
            )
            startActivity(intent)
        } else {
            uiHelper.showLongToastInCenter(
                requireActivity(),
                "Call Permission Not Granted Check App Setting and allow permission"
            )
        }
    }

    override fun onResume() {
        super.onResume()
        callApi()
    }
}
