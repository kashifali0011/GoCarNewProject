package com.towsal.towsal.views.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityCheckoutCarBookingBinding
import com.towsal.towsal.extensions.*
import com.towsal.towsal.interfaces.CalendarCallback
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.cardetails.CalendarDateTimeModel
import com.towsal.towsal.network.serializer.cardetails.HostByModel
import com.towsal.towsal.network.serializer.carlist.CarInformationModel
import com.towsal.towsal.network.serializer.checkoutcarbooking.BookingConfirmationResponseModel
import com.towsal.towsal.network.serializer.checkoutcarbooking.BookingPostModel
import com.towsal.towsal.network.serializer.checkoutcarbooking.CarCheckOutResponseModel
import com.towsal.towsal.network.serializer.checkoutcarbooking.GetCarBookingModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil
import com.towsal.towsal.utils.Days
import com.towsal.towsal.utils.ExtensionEnum
import com.towsal.towsal.viewmodel.CarDetailViewModel
import com.towsal.towsal.viewmodel.CheckoutCarBookingViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity class for Checkout process
 * */
class CheckoutCarBookingActivity : BaseActivity(), OnNetworkResponse, PopupCallback,
        CalendarCallback {

    lateinit var binding: ActivityCheckoutCarBookingBinding
    private val carCheckOutViewModel: CheckoutCarBookingViewModel by viewModel()
    private var model = GetCarBookingModel()
    private var responseModel = CarCheckOutResponseModel()
    private var acceptTerm = 0
    private var carInfoModel = CarInformationModel()
    private var bookingPostModel = BookingPostModel()
    private val carDetailViewModel: CarDetailViewModel by viewModel()
    private var calendarDateTimeModel = CalendarDateTimeModel()
    private var firstTime = true
    private var flow = 1
    private var checkoutId: Int = -1
    private var status = 0
    private var hostBy = HostByModel()
    private var isInProgress = false

    private var numberFormatter = DecimalFormat("##.##")
    var maxDateCalendar: Calendar? = null

    val launcher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_checkout_car_booking)
        binding.clMainRoot.isVisible = false
        binding.activity = this
        binding.layoutToolBar.setAsGuestToolBar(
                titleId = R.string.chekout,
                actionBar = supportActionBar,
                toolBarBg = R.drawable.bg_sky_blue_gradient,
                arrowColor = R.color.colorWhite

        )

        setData()
        setGradientTextColors()
    }

    private fun setGradientTextColors() {
        binding.tvDiscountTitle.setGradientTextColor(
                intArrayOf(
                        Color.parseColor("#4CA9FE"),
                        Color.parseColor("#14E3E4")
                )
        )

        binding.tvDiscountAmount.setGradientTextColor(
                intArrayOf(
                        Color.parseColor("#4CA9FE"),
                        Color.parseColor("#14E3E4")
                )
        )
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {

        if (intent.extras != null) {
            flow = intent.extras!!.getInt("flow", 0)
            model =
                    intent.extras!!.getSerializable(Constants.DataParsing.MODEL) as GetCarBookingModel
            carInfoModel =
                    intent.extras!!.getSerializable(Constants.DataParsing.CARINFODATAMODEL) as CarInformationModel
            if (intent.extras!!.getSerializable(Constants.DataParsing.DATE_TIME_MODEL) != null) {
                calendarDateTimeModel =
                        intent.extras!!.getSerializable(Constants.DataParsing.DATE_TIME_MODEL) as CalendarDateTimeModel
            }
            if (intent.extras!!.getSerializable(Constants.DataParsing.HOST_NAME) != null && flow == 1) {
                hostBy =
                        intent.extras!!.getSerializable(Constants.DataParsing.HOST_NAME) as HostByModel
            }
            status = intent.extras!!.getInt("modelStatus", 0)
            checkoutId = intent.extras!!.getInt(Constants.DataParsing.ID, -1)

            model.checkout_id = checkoutId

            callApi()
        }
    }

    /**
     * Function for calling api
     * */
    private fun callApi() {
        if (model.apply_delivery_fee == Constants.DeliveryFeeStatus.APPLIED){
            model.lat = carInfoModel.pin_lat
            model.lng = carInfoModel.pin_long
            carCheckOutViewModel.getCheckoutDetail(
                model,
                true,
                Constants.API.GET_CHECKOUT_DETAIL,
                this
            )
        }else{
            carCheckOutViewModel.getCheckoutDetail(
                model,
                true,
                Constants.API.GET_CHECKOUT_DETAIL,
                this
            )
        }


    }

    @SuppressLint("SetTextI18n")
    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_CHECKOUT_DETAIL -> {
                responseModel = Gson().fromJson(
                        uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                        CarCheckOutResponseModel::class.java
                )
                binding.clMainRoot.isVisible = true
                var ed = responseModel.charges.is_extended
                Log.d("testingtesting" , ed.toString())

                prepareUnavailableDaysIntegerList()
                binding.carName.text =
                        responseModel.carDetail.carDetailInfo.name + " " + responseModel.carDetail.carDetailInfo.model

                binding.carName.text = getString(
                        R.string.car_place_holder,
                        responseModel.carDetail.carDetailInfo.name,
                        responseModel.carDetail.carDetailInfo.model,
                        responseModel.carDetail.carDetailInfo.year
                )
                if (responseModel.carDetail.carDetailInfo.rating.toFloat() ==0f || responseModel.carDetail.carDetailInfo.rating.toFloat() ==0.0f){
                    binding.rating.isVisible = false
                }
                binding.rating.rating = responseModel.carDetail.carDetailInfo.rating.toFloat()
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_MONTH, -1)

                uiHelper.glideLoadImage(
                        this,
                        BuildConfig.IMAGE_BASE_URL + responseModel.carDetail.url,
                        binding.ivCarImage
                )
                binding.tvStartDate.text = DateUtil.changeDateFormat(
                        Constants.ServerDateTimeFormat,
                        Constants.UIDateFormat,
                        responseModel.trip_date.pickUp
                )
                binding.tvStartTime.text = DateUtil.changeDateFormat(
                        Constants.ServerDateTimeFormat,
                        Constants.UITimeFormat,
                        responseModel.trip_date.pickUp
                )
                binding.tvFinishDate.text = DateUtil.changeDateFormat(
                        Constants.ServerDateTimeFormat,
                        Constants.UIDateFormat,
                        responseModel.trip_date.dropOff
                )
                binding.tvFinishTime.text = DateUtil.changeDateFormat(
                        Constants.ServerDateTimeFormat,
                        Constants.UITimeFormat,
                        responseModel.trip_date.dropOff
                )

                binding.clDeliveryFee.isVisible =
                        responseModel.carDetail.carDetailInfo.delivery_status == Constants.DeliveryFeeStatus.APPLIED && model.apply_delivery_fee == Constants.DeliveryFeeStatus.APPLIED
                binding.tvChangeLocation.isVisible = binding.clDeliveryFee.isVisible
                binding.tvDeliveryFeesAmount.text = getString(
                        R.string.sar_,
                        numberFormatter.format(
                                responseModel.car_location_delivery.delivery_price.toString().toDouble()
                        )
                )

                binding.tvTotalDaysTitle.text = resources.getQuantityString(
                        R.plurals.day_rent,
                        responseModel.total_number_of_days,
                        responseModel.total_number_of_days
                )
                if (carInfoModel.street_address.isNotEmpty()) {
                    binding.tvLocation.text =
                            carInfoModel.street_address
                } else {
                    binding.tvLocation.text =
                            responseModel.car_location_delivery.street_address
                }

                binding.clDiscount.isVisible = responseModel.discountPercentage != 0
                binding.tvDiscountTitle.text =
                        responseModel.discount_day_value.toString() + "+ " + resources.getString(R.string.days)
                                .lowercase(
                                        Locale.getDefault()
                                ) + " " + resources.getString(R.string.discount).lowercase(
                                Locale.getDefault()
                        )
                binding.tvDiscountAmount.text =
                        resources.getString(R.string.sar_, responseModel.discountAmount)
                resources.getString(
                        R.string.sar_,
                        numberFormatter.format(
                                responseModel.discountAmount.toDouble()
                        )
                )


                binding.pickupOnly.text =
                        if (responseModel.carDetail.carDetailInfo.delivery_status == Constants.DeliveryFeeStatus.APPLIED)
                            "Available for delivery"
                        else
                            "Pick up only"

                binding.tvTotalDaysAmount.text =
                        resources.getString(R.string.sar_, responseModel.final_rent)
                binding.tvTotalDaysAmount.text = resources.getString(
                        R.string.sar_,
                        numberFormatter.format(
                                responseModel.day_wise_rent.toDouble()
                                        .times(responseModel.total_number_of_days)
                        )
                )

                binding.price.text = responseModel.day_wise_rent.split(".")[0]

                binding.tvSubTotalAmount.text =
                        resources.getString(R.string.sar_, responseModel.sub_total)
                binding.tvSubTotalAmount.text =
                        resources.getString(
                                R.string.sar_,
                                numberFormatter.format(responseModel.sub_total.toDouble())
                        )

                binding.distance.text =
                        getString(R.string.km_, responseModel.carDetail.carDetailInfo.distance)

                binding.tvTripFeeAmount.text = getString(R.string.sar_, responseModel.toalTripFee)
                binding.distance.text =
                        getString(R.string.km_, responseModel.carDetail.carDetailInfo.distance)

                binding.tvGrandTotalAmount.text = getString(R.string.sar_, responseModel.total)
                binding.tvVatAmount.text = getString(R.string.sar_, responseModel.vatAmount)
                binding.tvGrandTotalAmount.text = resources.getString(
                        R.string.sar_,
                        numberFormatter.format(responseModel.total.toDouble())
                )
                binding.tvVatAmount.text =
                        resources.getString(
                                R.string.sar_,
                                numberFormatter.format(responseModel.vatAmount.toDouble())
                        )
                binding.tvVatTitle.text = resources.getString(R.string.vat) + " " + responseModel.vatPercentage + "%"
                binding.clDepositAmount.isVisible = responseModel.deposit_amount.toDouble() > 0.0
                binding.tvDepositAmount.text = getString(
                        R.string.sar_,
                        responseModel.deposit_amount
                )
                binding.tvDepositAmount.text =
                        getString(
                                R.string.sar_,
                                numberFormatter.format(responseModel.deposit_amount.toDouble())
                        )

                calendarDateTimeModel.pickUpDateServer = responseModel.trip_date.pickUp
                calendarDateTimeModel.dropOffDateServer = responseModel.trip_date.dropOff
                calendarDateTimeModel.carId = responseModel.carDetail.id
                binding.mainLayout.visibility = View.VISIBLE
                if (firstTime) {
                    carCheckOutViewModel.showTermConditionPrivacyPolicyPopup(this, this)
                    firstTime = false
                }

                binding.btnBookTrip.isVisible = flow == 1
                binding.btnEditTrip.isVisible = !binding.btnBookTrip.isVisible

                binding.clAdditionalCharges.isVisible =
                        responseModel.charges.is_extended != ExtensionEnum.NO_EXTENSION.value

                maxDateCalendar =
                        getMaxCalendar(
                                responseModel.carInsuranceExpiry,
                                Constants.ServerDateFormat
                        ).invoke()
                if (maxDateCalendar != null) {
                    binding.tvChangeDate.isVisible =
                            Calendar.getInstance().time < maxDateCalendar?.time
                }
            }
            Constants.API.SAVE_CHECKOUT_DETAIL -> {
                val thankYouModel = Gson().fromJson(
                        uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                        BookingConfirmationResponseModel::class.java
                )
                thankYouModel.hostProfileImage = hostBy.profile_img
                val bundle = Bundle()
                bundle.putSerializable(Constants.DataParsing.MODEL, thankYouModel)
                bundle.putInt(
                        Constants.DataParsing.FLOW_COMING_FROM,
                        PaymentsActivity.FLOW_FROM_CHECKOUT
                )
                uiHelper.openActivity(this, PaymentsActivity::class.java, bundle)
            }
            Constants.API.EDIT_TRIP_BOOKING -> {
                setResult(RESULT_OK, intent)
                finish()
            }
            Constants.API.DELETE_FAV_CAR -> {
                uiHelper.showLongToastInCenter(this, response?.message)
                responseModel.carDetail.carDetailInfo.isFavourite = 0
            }
            Constants.API.ADD_FAV_CAR -> {
                responseModel.carDetail.carDetailInfo.isFavourite = 1
                uiHelper.showLongToastInCenter(this, response?.message)
            }
        }

    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        if (response?.code == 205) {
            model = intent.extras!!.getSerializable(Constants.DataParsing.MODEL) as GetCarBookingModel
            callApi()
        } else {
            uiHelper.showLongToastInCenter(this, response?.message)
        }

    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view) {
            binding.tvTermsAndConditions -> {
                carCheckOutViewModel.showTermConditionPrivacyPolicyPopup(this, this)
            }
            binding.tvFuelPolicy -> {
                carCheckOutViewModel.showTermConditionPrivacyPolicyPopup(this, this)
            }
            binding.tvChangeDate -> {
                Log.d("calendarDateTimeModel", Gson().toJson(calendarDateTimeModel))
                openCalendarPopUp()
            }
            binding.btnBookTrip -> {
                if (acceptTerm == 0) {
                    uiHelper.showLongToastInCenter(this, getString(R.string.term_condition_err_msg))
                } else {
                    bookingPostModel.car_id = responseModel.carDetail.id
                    bookingPostModel.rent = responseModel.day_wise_rent
                    bookingPostModel.dayWiseTripFee = responseModel.dayWiseTripFee
                    if (carInfoModel.locationSelected == 1) {
                        bookingPostModel.delivery_fee = responseModel.car_location_delivery.delivery_price!!.toInt()
                        bookingPostModel.pin_address = carInfoModel.pin_address
                        bookingPostModel.pin_lat = carInfoModel.pin_lat
                        bookingPostModel.pin_long = carInfoModel.pin_long
                        bookingPostModel.country_name = carInfoModel.country_name
                        bookingPostModel.state_name = carInfoModel.state_name
                        bookingPostModel.city_name = carInfoModel.city_name
                        bookingPostModel.street_address = carInfoModel.street_address
                        bookingPostModel.zip_code = carInfoModel.zip_code
                    } else {
                        bookingPostModel.pin_address =
                                responseModel.car_location_delivery.pin_address
                        bookingPostModel.pin_lat = responseModel.car_location_delivery.pin_lat
                        bookingPostModel.pin_long = responseModel.car_location_delivery.pin_long
                        bookingPostModel.country_name =
                                responseModel.car_location_delivery.country_name
                        bookingPostModel.state_name = responseModel.car_location_delivery.state_name
                        bookingPostModel.city_name = responseModel.car_location_delivery.city_name
                        bookingPostModel.street_address =
                                responseModel.car_location_delivery.street_address
                        bookingPostModel.zip_code = responseModel.car_location_delivery.zip_code
                    }
                    bookingPostModel.apply_delivery_fee = model.apply_delivery_fee
                    bookingPostModel.delivery_status =
                            responseModel.carDetail.carDetailInfo.delivery_status
                    bookingPostModel.deposit = responseModel.deposit_amount
                    bookingPostModel.discountAmount = responseModel.discountAmount
                    bookingPostModel.is_accept = 1
                    bookingPostModel.is_booked = 1
                    bookingPostModel.sub_total = responseModel.sub_total
                    bookingPostModel.total = responseModel.total
                    bookingPostModel.vat_amount = responseModel.vatAmount
                    bookingPostModel.number_of_days = responseModel.total_number_of_days
                    if (!isInProgress) {
                        bookingPostModel.pick_up = calendarDateTimeModel.pickUpDateServer
                    }
                    bookingPostModel.drop_off = calendarDateTimeModel.dropOffDateServer
                    bookingPostModel.is_book_immediately =
                            responseModel.carDetail.carDetailInfo.is_book_immediately
                    bookingPostModel.discountPercentage = responseModel.discountPercentage
                    bookingPostModel.perDayDistance = responseModel.perDayDistance
                    bookingPostModel.totalAllowedDistance = responseModel.totalAllowedDistance
                    bookingPostModel.totalTripFee = responseModel.toalTripFee
                    bookingPostModel.vatPercentage = responseModel.vatPercentage
                    bookingPostModel.hostVatAmount = responseModel.hostVatAmount
                    bookingPostModel.hostEarned = responseModel.hostEarned
                    bookingPostModel.systemFee = responseModel.systemFee
                    bookingPostModel.protectionFee = responseModel.protectionFee


                    carCheckOutViewModel.saveCarBooking(
                            bookingPostModel,
                            true,
                            Constants.API.SAVE_CHECKOUT_DETAIL,
                            this
                    )
                }
            }
            binding.btnEditTrip -> {
                if (acceptTerm == 0) {
                    uiHelper.showLongToastInCenter(this, getString(R.string.term_condition_err_msg))
                } else {
                    bookingPostModel.car_id = responseModel.carDetail.id
                    bookingPostModel.rent = responseModel.day_wise_rent
                    bookingPostModel.dayWiseTripFee = responseModel.dayWiseTripFee
                    if (carInfoModel.locationSelected == Constants.DeliveryFeeStatus.APPLIED) {
                        bookingPostModel.delivery_fee = responseModel.car_location_delivery.delivery_price!!.toInt()
                        bookingPostModel.pin_address = carInfoModel.pin_address
                        bookingPostModel.pin_lat = carInfoModel.pin_lat
                        bookingPostModel.pin_long = carInfoModel.pin_long
                        bookingPostModel.country_name = carInfoModel.country_name
                        bookingPostModel.state_name = carInfoModel.state_name
                        bookingPostModel.city_name = carInfoModel.city_name
                        bookingPostModel.street_address = carInfoModel.street_address
                        bookingPostModel.zip_code = carInfoModel.zip_code
                    } else {
                        bookingPostModel.pin_address =
                                responseModel.car_location_delivery.pin_address
                        bookingPostModel.pin_lat = responseModel.car_location_delivery.pin_lat
                        bookingPostModel.pin_long = responseModel.car_location_delivery.pin_long
                        bookingPostModel.country_name =
                                responseModel.car_location_delivery.country_name
                        bookingPostModel.state_name = responseModel.car_location_delivery.state_name
                        bookingPostModel.city_name = responseModel.car_location_delivery.city_name
                        bookingPostModel.street_address =
                                responseModel.car_location_delivery.street_address
                        bookingPostModel.zip_code = responseModel.car_location_delivery.zip_code
                    }
                    bookingPostModel.apply_delivery_fee = model.apply_delivery_fee
                    bookingPostModel.delivery_status = responseModel.carDetail.carDetailInfo.delivery_status
                    bookingPostModel.isExtended = responseModel.charges.is_extended
                    bookingPostModel.updateDays = responseModel.charges.updatedDays!!
                    bookingPostModel.remainingAmount = responseModel.charges.remainingAmount
                    bookingPostModel.deposit = responseModel.deposit_amount
                    bookingPostModel.discountAmount = responseModel.discountAmount
                    bookingPostModel.is_accept = 1
                    bookingPostModel.is_booked = 1
                    bookingPostModel.sub_total = responseModel.sub_total
                    bookingPostModel.total = responseModel.total
                    bookingPostModel.vat_amount = responseModel.vatAmount
                    bookingPostModel.number_of_days = responseModel.total_number_of_days
                    bookingPostModel.pick_up = calendarDateTimeModel.pickUpDateServer
                    bookingPostModel.drop_off = calendarDateTimeModel.dropOffDateServer
                    bookingPostModel.is_book_immediately =
                    responseModel.carDetail.carDetailInfo.is_book_immediately
                    bookingPostModel.checkout_id = checkoutId
                    bookingPostModel.discountPercentage = responseModel.discountPercentage
                    bookingPostModel.perDayDistance = responseModel.perDayDistance
                    bookingPostModel.totalTripFee = responseModel.toalTripFee
                    bookingPostModel.hostVatAmount = responseModel.hostVatAmount
                    bookingPostModel.hostEarned = responseModel.hostEarned
                    bookingPostModel.systemFee = responseModel.systemFee
                    bookingPostModel.protectionFee = responseModel.protectionFee

                    carCheckOutViewModel.editTripBooking(
                            bookingPostModel,
                            true,
                            Constants.API.EDIT_TRIP_BOOKING,
                            this
                    )
                }
            }
            binding.tvChangeLocation -> {
                if (responseModel.car_location_delivery.pin_lat != 0.0) {
                    carInfoModel.isHideChnageLocation = false
                    val bundle = Bundle()
                    bundle.putSerializable(
                            Constants.DataParsing.MODEL,
                            responseModel.car_location_delivery
                    )
                    bundle.putSerializable(Constants.DataParsing.CARINFODATAMODEL, carInfoModel)
                    uiHelper.openActivityForResult(
                            this,
                            LocationSelectionActivity::class.java,
                            true,
                            Constants.RequestCodes.ACTIVTIY_LOCATION_SELECTION, bundle
                    )
                }
            }

            binding.tvPickUpAndReturn ->{
                if (responseModel.carDetail.carDetailInfo.delivery_status != Constants.CarDeliveryStatus.AVAILABLE) {
                    if (responseModel.car_location_delivery.pin_lat != 0.0) {
                        openMap(
                            responseModel.car_location_delivery.pin_lat,
                            responseModel.car_location_delivery.pin_long
                        )
                       /* carInfoModel.isHideChnageLocation = true
                        val bundle = Bundle()
                        bundle.putSerializable(
                            Constants.DataParsing.MODEL, responseModel.car_location_delivery
                        )
                        bundle.putSerializable(Constants.DataParsing.CARINFODATAMODEL, carInfoModel)
                        uiHelper.openActivityForResult(
                            this,
                            LocationSelectionActivity::class.java,
                            true,
                            Constants.RequestCodes.ACTIVTIY_LOCATION_SELECTION,
                            bundle
                        )*/
                    }
                }
            }
            binding.tvAdditionalCharges -> {
                openChargesPopUp(
                        responseModel.charges, true, responseModel.charges.remainingAmount
                )
            }
        }
    }

    private fun openCalendarPopUp() {
        if (responseModel.isFullyChangeable == 0) {
            var pickUp = DateUtil.changeDateFormat(
                    Constants.ServerDateTimeFormat,
                    Constants.ServerDateFormat,
                    responseModel.trip_date.pickUp
            )
            pickUp = if (SimpleDateFormat(Constants.ServerDateFormat).parse(
                            pickUp
                    ).before(
                            Calendar.getInstance().time
                    )
            ) {
                SimpleDateFormat(Constants.ServerDateFormat).format(Calendar.getInstance().time)
            } else {
                pickUp
            }
            if (responseModel.un_available_date.isNotEmpty()) {

                val previousDate =
                        DateUtil.getDateForPastFromDays(
                                -1,
                                responseModel.un_available_date[0] + " 00:00:00"
                        ) // previous date before the first booked date after this booking


                if (pickUp == previousDate)
                    activity.resources.getString(R.string.cannot_book_consecutive)
                else
                    carDetailViewModel.showDateTimePicker(
                            pickUp,
                            this,
                            calendarDateTimeModel,
                            this,
                            previousDate,
                            responseModel.trip_date.pickUp,
                            responseModel.trip_date.dropOff,
                            responseModel.car_availability,

                    )
            } else {
                carDetailViewModel.showDateTimePicker(
                        pickUp,
                        this,
                        calendarDateTimeModel,
                        this,
                        "2024-03-15 13:00:57",
                      /*  responseModel.carInsuranceExpiry,*/
                        responseModel.trip_date.pickUp,
                        responseModel.trip_date.dropOff,
                        responseModel.car_availability
                )
            }
        } else {
            carDetailViewModel.showDateTimePicker(
                    this,
                    calendarDateTimeModel,
                    this,
                    maxDateCalendar,
                    responseModel.un_available_date,
                    responseModel.car_availability,
                    false

            )
        }
    }

    override fun popupButtonClick(value: Int) {
        acceptTerm = value
    }

    override fun popupButtonClick(value: Int, id: Any) {
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                Constants.RequestCodes.ACTIVTIY_LOCATION_SELECTION -> {
                    val bundle = data?.getBundleExtra(Constants.DataParsing.MODEL)
                    carInfoModel =
                            bundle?.getSerializable(Constants.DataParsing.MODEL) as CarInformationModel
                    binding.tvLocation.text = carInfoModel.street_address
                    model.apply_delivery_fee = carInfoModel.locationSelected
                    callApi()
                }

            }

        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCalendarDateTimeSelected(model: CalendarDateTimeModel) {
        calendarDateTimeModel = model
        if (responseModel.isFullyChangeable == 0) {
            this.model.pick_up = responseModel.trip_date.pickUp
            this.model.drop_off = model.dropOffDateServer
            callApi()
        } else {
            this.model.pick_up = model.pickUpDateServer
            this.model.drop_off = model.dropOffDateServer
            callApi()
        }
    }

    private fun prepareUnavailableDaysIntegerList() {
        if (responseModel.daysCustomAvailability.isNotEmpty()) {
            for (i in responseModel.daysCustomAvailability) {
                when (i.day.lowercase(Locale.getDefault())) {
                    Days.SUNDAY.value -> {
                        i.dayIndex = 1
                    }
                    Days.MONDAY.value -> {
                        i.dayIndex = 2
                    }
                    Days.TUESDAY.value -> {
                        i.dayIndex = 3
                    }
                    Days.WEDNESDAY.value -> {
                        i.dayIndex = 4
                    }
                    Days.THURSDAY.value -> {
                        i.dayIndex = 5
                    }
                    Days.FRIDAY.value -> {
                        i.dayIndex = 6
                    }
                    Days.SATURDAY.value -> {
                        i.dayIndex = 7
                    }
                }
            }
            responseModel.car_availability.daysCustomAvailability =
                    responseModel.daysCustomAvailability
        }
    }


}