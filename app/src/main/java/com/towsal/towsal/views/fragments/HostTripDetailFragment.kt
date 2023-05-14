package com.towsal.towsal.views.fragments

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.app.MainApplication
import com.towsal.towsal.databinding.FragmentHostTripDetailBinding
import com.towsal.towsal.extensions.*
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.carlist.CarInformationModel
import com.towsal.towsal.network.serializer.checkoutcarbooking.CarCheckOutResponseModel
import com.towsal.towsal.network.serializer.trips.CancellationPolicyModel
import com.towsal.towsal.network.serializer.trips.TripDetailModel
import com.towsal.towsal.network.serializer.trips.TripResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil
import com.towsal.towsal.viewmodel.TripsViewModel
import com.towsal.towsal.views.activities.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity class for trip details for user who is host in the booking
 * */
class HostTripDetailFragment : BaseFragment(), OnNetworkResponse, PopupCallback {

    lateinit var binding: FragmentHostTripDetailBinding
    val tripsViewModel: TripsViewModel by activityViewModels()
    var model = TripDetailModel()
    var flow_coming_from = ""
    var change_req_id = 0

    var dropoffDate = ""
    var dropOffDateWithoutParse = ""
    var isAccepted = false


    var responseModel = TripResponseModel()
    var carInfoModel = CarInformationModel()
    private var responseModeless = CarCheckOutResponseModel()

    override fun onResume() {
        super.onResume()
        binding.clMainRoot.isVisible = false
        setData()
        setUiObserver()
    }

    private val rejectBookingLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        if(it.resultCode == RESULT_OK) {
            requireActivity().finish()
        }
    }
    private fun setUiObserver(){
        tripsViewModel.cancelReason.observe(viewLifecycleOwner){

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            FragmentHostTripDetailBinding.bind(
                inflater.inflate(
                    R.layout.fragment_host_trip_detail,
                    container,
                    false
                )
            )
        binding.fragment = this
        return binding.root
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        tripsViewModel.getBookingDetail(
            tripsViewModel.tripId,
            tripsViewModel.viewType,
            true,
            Constants.API.GET_GUEST_BOOKING_DETAIL,
            this
        )
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view) {
            binding.tvAddPre -> {
                val bundle = Bundle()
                bundle.putInt(
                    Constants.DataParsing.FLOW_COMING_FROM,
                    AddTripPhotosActivity.FLOW_FOR_PRE_TRIP_IMAGES
                )
                bundle.putString(Constants.CAR_ID, model.car_id.toString())
                bundle.putString(Constants.DataParsing.CHECKOUT_ID, model.id.toString())
                uiHelper.openActivity(
                    requireActivity(),
                    AddTripPhotosActivity::class.java,
                    bundle
                )
            }
            binding.tvAddPostTrip -> {
                val bundle = Bundle()
                bundle.putInt(
                    Constants.DataParsing.FLOW_COMING_FROM,
                    AddTripPhotosActivity.FLOW_FOR_POST_TRIP_IMAGES
                )
                bundle.putString(Constants.CAR_ID, model.car_id.toString())
                bundle.putString(Constants.DataParsing.CHECKOUT_ID, model.id.toString())
                uiHelper.openActivity(
                    requireActivity(),
                    AddTripPhotosActivity::class.java,
                    bundle
                )
            }

            binding.btnRejectTrip -> {
                val bundle = Bundle()
                bundle.putInt(Constants.DataParsing.ID, model.id)
                rejectBookingLauncher.launch(
                    Intent(
                        requireActivity(),
                        RejectionReasonActivity::class.java
                    ).apply {
                        putExtras(bundle)
                    })
            }
            binding.btnAcceptTrip -> {
                tripsViewModel.acceptBooking(model.id, true, Constants.API.ACCEPT_BOOKING, this)
            }
            binding.btnCancelTrip -> {
                tripsViewModel.cancelBookingReason(
                    2, true, Constants.API.CANCEL_BOOKING_REASON, this
                )
            }

            binding.btnWriteReview -> {
                val bundle = Bundle()
                bundle.putInt(
                    Constants.DataParsing.ID, model.id
                )
                bundle.putInt(
                    Constants.DataParsing.CAR_ID, model.car_id
                )
                bundle.putString("viewType", "host")
                uiHelper.openActivityForResult(
                    requireActivity(),
                    RatingReviewActivity::class.java,
                    true,
                    Constants.RequestCodes.HOST_TRIP_DETAIL,
                    bundle
                )
            }
            binding.btnAcceptTripExtension -> {
                isAccepted = true

                tripsViewModel.updateCheckoutChangings(
                    change_req_id,
                    Constants.ExtensionStatus.ACCEPT_REQUEST,
                    true,
                    Constants.API.UPDATE_CHECKOUT_CHANGINGS,
                    this
                )
            }
            binding.btnRejectTripExtension -> {
                isAccepted = false

                tripsViewModel.updateCheckoutChangings(
                    change_req_id,
                    Constants.ExtensionStatus.REJECT_REQUEST,
                    true,
                    Constants.API.UPDATE_CHECKOUT_CHANGINGS,
                    this
                )
            }

            binding.tvRequestExtension -> {
                tripsViewModel.sendExtensionRequest(
                    model.id.toString(), true, Constants.API.SUGGEST_EXTENSION, this
                )
            }

            binding.tvReceipt -> {
                val bundle = Bundle()
                bundle.putInt(Constants.DataParsing.ID, model.id)
                uiHelper.openActivity(requireActivity(), ReceiptActivity::class.java, bundle)
            }

            binding.tvViewAccidentPhotos -> {
                val bundle = Bundle()
                bundle.putInt(
                    Constants.DataParsing.GOTO_PHOTOS,
                    Constants.CarPhotos.HOST_ACCIDENT_PHOTOS
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
                bundle.putString(
                    Constants.DataParsing.HEADING,
                    getString(R.string.reported_accident)
                )
                uiHelper.openActivityForResult(
                    requireActivity(),
                    AddTripDetailImages::class.java,
                    true,
                    Constants.RequestCodes.HOST_TRIP_DETAIL, bundle
                )
            }

            binding.tvGuestName -> {
                val bundle = Bundle()
                bundle.putInt(Constants.DataParsing.USER_ID, model.user_id)
                uiHelper.openActivity(requireActivity(), UserProfileActivity::class.java, bundle)
            }

            binding.tvPickUpAndReturn ->{

                if (responseModel.bookingDetails.car_pin_lat != 0.0) {
                    activity?.openMap(
                        responseModel.bookingDetails.car_pin_lat,
                        responseModel.bookingDetails.car_pin_long
                    )

                  /*  carInfoModel.fromActivity = true
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
                dropOffDateWithoutParse = model.car_drop_off
                tripsViewModel.carId = model.car_id.toString()
                tripsViewModel.receiverId = model.user_id
                tripsViewModel.adminContactNo = model.admin_contact
                tripsViewModel.setBookingStatus(model.status)
                tripsViewModel.setIsLateReturnNeeded(model.lateReturn)
                tripsViewModel.tripStatus = model.status

                tripsViewModel.threadId = model.threadId ?: 0
                tripsViewModel.messenger = model.messenger

                binding.executePendingBindings()
                binding.bookingDetail.visibility = View.VISIBLE
                loadTripDates()
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
                binding.tvGuestName.text = model.booked_by.trim()
                binding.tvOwnerName.text = getString(R.string.owner_name, model.host_name)
                binding.ivCarImage.loadImage(BuildConfig.IMAGE_BASE_URL + model.car_image)
                binding.tvAddPre.setTextColor(
                    Color.parseColor(
                        "#C2C2C2"
                    )
                )
                setLayoutAccordingToStatus(responseModel)
                setImagesStatus()


                if (responseModel.bookingDetails.extension_status == Constants.ExtensionStatus.APPROVAL_PENDING) {
                    change_req_id = responseModel.bookingDetails.change_req_id
                    binding.tvExtendedStartDate.text = DateUtil.changeDateFormat(
                        Constants.ServerDateTimeFormat,
                        Constants.UIDateFormat,
                        responseModel.bookingDetails.extension_pickup
                    )
                    binding.tvExtendedStartTime.text = DateUtil.changeDateFormat(
                        Constants.ServerDateTimeFormat,
                        Constants.UITimeFormat,
                        responseModel.bookingDetails.extension_pickup
                    )
                    binding.tvExtendedFinishDate.text = DateUtil.changeDateFormat(
                        Constants.ServerDateTimeFormat,
                        Constants.UIDateFormat,
                        responseModel.bookingDetails.extension_dropoff
                    )
                    binding.tvExtendedFinishTime.text = DateUtil.changeDateFormat(
                        Constants.ServerDateTimeFormat,
                        Constants.UITimeFormat,
                        responseModel.bookingDetails.extension_dropoff
                    )
                }
            }
            Constants.API.ACCEPT_BOOKING -> {
                uiHelper.showLongToastInCenter(requireActivity(), response?.message)
                val intent = Intent()
                requireActivity().setResult(RESULT_OK, intent)
                tripsViewModel.getBookingDetail(
                    model.id,
                    tripsViewModel.viewType,
                    true,
                    Constants.API.GET_GUEST_BOOKING_DETAIL,
                    this
                )
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
            Constants.API.UPDATE_CHECKOUT_CHANGINGS -> {
                uiHelper.showLongToastInCenter(requireActivity(), response?.message)
                binding.clExtendedTripDateDetails.isVisible = false
                if (isAccepted) {
                    model.car_drop_off = model.extension_dropoff
                    model.car_pick_up = model.extension_pickup
                    model.extension_status = Constants.ExtensionStatus.ACCEPT_REQUEST
                }
                loadTripDates()
                setData()
            }

            Constants.API.SUGGEST_EXTENSION -> {
                uiHelper.showLongToastInCenter(requireActivity(), response!!.message)
                binding.tvRequestExtension.isVisible = false
            }
        }
    }

    private fun loadTripDates() {
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
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(requireActivity(), response?.message)
    }

    /**
     * Function for ui according to booking status
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

    private fun setGradientTextColors() {
        if (
            model.status == Constants.BookingStatus.COMPLETED &&
            model.postTripImagesStatus == Constants.HostBookingImagesStatus.NOT_ADDED &&
            model.isPostImage == Constants.ENABLED
        ) {
            binding.tvAddPostTrip.setGradientTextColor(
                colors = intArrayOf(
                    ContextCompat.getColor(requireActivity(), R.color.send_msg_bg),
                    ContextCompat.getColor(requireActivity(), R.color.text_receive_msg),
                ),
                x0 = -1.283f,
                y0 = -0.921f,
                x1 = 30.5f,
                y1 = 15.455f,
                positions = floatArrayOf(
                    0f,
                    1f
                )
            )
        } else {
            binding.tvAddPre.paint.shader = null
            binding.tvAddPostTrip.setTextColor(
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

        if (
            model.status == Constants.BookingStatus.FUTURE && model.preTripImagesStatus == Constants.HostBookingImagesStatus.NOT_ADDED && model.isPreImage
        ) {
            binding.tvAddPre.setGradientTextColor(
                colors = intArrayOf(
                    ContextCompat.getColor(requireActivity(), R.color.send_msg_bg),
                    ContextCompat.getColor(requireActivity(), R.color.text_receive_msg),
                ),
                x0 = -1.283f,
                y0 = -0.921f,
                x1 = 30.5f,
                y1 = 15.455f,
                positions = floatArrayOf(
                    0f,
                    1f
                )
            )
        } else {

            binding.tvAddPre.paint.shader = null
            binding.tvAddPre.setTextColor(
                Color.parseColor(
                    "#C2C2C2"
                )
            )
        }

    }

    private fun enablingButtons(responseModel: TripResponseModel) {
        binding.tvAddPostTrip.isEnabled =
            responseModel.bookingDetails.status == Constants.BookingStatus.COMPLETED &&
                    responseModel.bookingDetails.postTripImagesStatus == Constants.HostBookingImagesStatus.NOT_ADDED &&
                    responseModel.bookingDetails.isPostImage == Constants.ENABLED

        binding.tvAddPre.isEnabled =
            responseModel.bookingDetails.status == Constants.BookingStatus.FUTURE && responseModel.bookingDetails.preTripImagesStatus == Constants.HostBookingImagesStatus.NOT_ADDED && responseModel.bookingDetails.isPreImage

        setGradientTextColors()
    }

    private fun setVisibilities(responseModel: TripResponseModel) {

        binding.tvReceipt.isVisible = model.status in listOf(
            Constants.BookingStatus.FUTURE,
            Constants.BookingStatus.PENDING,
            Constants.BookingStatus.IN_PROGRESS,
            Constants.BookingStatus.COMPLETED
        )
        binding.tvRemainingTime.isVisible = responseModel.bookingDetails.status in listOf(
            Constants.BookingStatus.FUTURE, Constants.BookingStatus.IN_PROGRESS
        )

        binding.clExtendedTripDateDetails.isVisible = responseModel.bookingDetails.status in listOf(
            Constants.BookingStatus.FUTURE,
            Constants.BookingStatus.IN_PROGRESS
        ) && responseModel.bookingDetails.extension_status == 0

        if (responseModel.bookingDetails.isDropOffExceed == true){
            if (responseModel.bookingDetails.showExtensionBttn == true){
                binding.tvRequestExtension.isVisible = true

            }
        }

       /* binding.tvRequestExtension.isVisible =
            responseModel.bookingDetails.extension_status != 0 && responseModel.bookingDetails.status == Constants.BookingStatus.IN_PROGRESS && responseModel.bookingDetails.isDropOffExceed ?: false*/

        binding.clAcceptRejectBooking.isVisible =
            responseModel.bookingDetails.status == Constants.BookingStatus.PENDING
        binding.btnWriteReview.isVisible =
            responseModel.bookingDetails.status == Constants.BookingStatus.COMPLETED && responseModel.bookingDetails.is_rated == Constants.RatingStatus.NOT_RATED_YET
        binding.llStatuses.isVisible =
            responseModel.bookingDetails.status != Constants.BookingStatus.PENDING
        binding.clTripDateDetails.isVisible = !binding.clExtendedTripDateDetails.isVisible
        binding.clAcceptRejectExtension.isVisible = binding.clExtendedTripDateDetails.isVisible

        binding.btnCancelTrip.isVisible =
            responseModel.bookingDetails.status == Constants.BookingStatus.FUTURE && !binding.clExtendedTripDateDetails.isVisible

        val bool = if (responseModel.bookingDetails.status == Constants.BookingStatus.CANCELLED)
            responseModel.bookingDetails.previousStatus == Constants.BookingStatus.PENDING
        else responseModel.bookingDetails.status == Constants.BookingStatus.PENDING

        binding.clUploadPhotos.isVisible = !bool

        binding.tvReceipt.isVisible = binding.clUploadPhotos.isVisible
        binding.tvTermsAndConditions.isVisible = binding.clUploadPhotos.isVisible
        binding.tvFuelPolicy.isVisible = binding.clUploadPhotos.isVisible

        binding.clAccidentPhotos.isVisible =
            responseModel.bookingDetails.accident_status == 1 && responseModel.bookingDetails.status == Constants.BookingStatus.CONFLICTED
    }

    override fun popupButtonClick(value: Int) {

    }

    override fun popupButtonClick(value: Int, id: Any) {
        if (value == 3) {
            var number = 1
            tripsViewModel.cancelReason.observe(viewLifecycleOwner){
                if (id.toString().toInt() == 6){
                    tripsViewModel.cancelReason.observe(viewLifecycleOwner){ reasons ->
                        if (reasons.isNotEmpty()){
                            if (number == 2) {
                            tripsViewModel.cancelBookingWithReason(
                            model.id,
                            id.toString().toInt(),
                            reasons,
                            true,
                            Constants.API.CANCEL_BOOKING,
                            this
                        )}}else{
                            if (number == 2) {
                                requireActivity().showToast(getString(R.string.other_field_required))
                            }
                        }
                        number++

                    }
                }else{
                    tripsViewModel.cancelBooking(
                        model.id, id.toString().toInt(), true, Constants.API.CANCEL_BOOKING, this
                    )
                }
            }




        }
    }

    /**
     * Function for setting up image status
     * */
    private fun setImagesStatus() {
        binding.tvPreTripStatus.text = when (model.preTripImagesStatus) {
            Constants.HostBookingImagesStatus.NOT_ADDED -> {
                resources.getString(R.string.pending)
            }
            else -> {
                resources.getString(R.string.added)
            }
        }
        binding.tvPostTripStatus.text = when (model.postTripImagesStatus) {
            Constants.HostBookingImagesStatus.NOT_ADDED -> {
                resources.getString(R.string.pending)
            }
            else -> {
                resources.getString(R.string.added)
            }
        }
    }


}