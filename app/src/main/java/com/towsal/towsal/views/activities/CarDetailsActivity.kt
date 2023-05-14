package com.towsal.towsal.views.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devs.readmoreoption.ReadMoreOption
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.app.MainApplication
import com.towsal.towsal.databinding.ActivityCarDetailsBinding
import com.towsal.towsal.extensions.*
import com.towsal.towsal.interfaces.CalendarCallback
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.interfaces.PopupReasonDetaisCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.UserModel
import com.towsal.towsal.network.serializer.cardetails.*
import com.towsal.towsal.network.serializer.carlist.CarInformationModel
import com.towsal.towsal.network.serializer.carlist.CustomPriceModel
import com.towsal.towsal.network.serializer.checkoutcarbooking.GetCarBookingModel
import com.towsal.towsal.network.serializer.cities.CitiesListModel
import com.towsal.towsal.network.serializer.home.CarHomeModel
import com.towsal.towsal.network.serializer.settings.DeleteReasonListModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil
import com.towsal.towsal.utils.Days
import com.towsal.towsal.viewmodel.CarDetailViewModel
import com.towsal.towsal.viewmodel.CheckoutCarBookingViewModel
import com.towsal.towsal.views.adapters.CarFeatureAdapterVerticalDetail
import com.towsal.towsal.views.adapters.InfiniteViewPagerAdapter
import com.towsal.towsal.views.adapters.RatingsAdapter
import com.towsal.towsal.views.bottomsheets.PriceDetailsBottomSheet
import com.towsal.towsal.views.bottomsheets.PriceNewDetailsBottomSheet
import kotlinx.android.synthetic.main.activity_basic_car_details.*
import kotlinx.android.synthetic.main.activity_car_features.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringUtils
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.Serializable
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * Activity class for car details
 * */
class CarDetailsActivity : BaseActivity(), OnNetworkResponse, CalendarCallback, PopupCallback{
    private lateinit var binding: ActivityCarDetailsBinding
    private val carDetailViewModel: CarDetailViewModel by viewModel()
    private var model = CarFullDetailModel()
    var reasonResponseModel = ReasonrResponse()
    private var calendarDateTimeModel = CalendarDateTimeModel()
    private var carInfoModel = CarInformationModel()
    private val checkOutInfoModel = GetCarBookingModel()
    private var snackBar: Snackbar? = null
    private var lat = 0.0
    private var lng = 0.0
    private var isForResult = false
    private var isCitySelected = false
    private var cityModel = CitiesListModel()
    var customPriceList: ArrayList<CustomPriceModel> = ArrayList()

    /// new change

    private val sdfServerDateFormat =
        SimpleDateFormat(Constants.ServerDateFormat, Locale.getDefault())
    private val sdfServerDateTimeFormat =
        SimpleDateFormat(Constants.ServerDateTimeFormat, Locale.getDefault())
    var noOfDaysDiscount = 0
    var threeDayDiscountAmount = 0.0
    var sevenDayDiscountAmount = 0.0
    var thirtyDayDiscountAmount = 0.0
    var isDiscountApplied = false
    var daysCount = 0
    var isFavourite = false
    var imageResource: Int = 0
    var carId = 0
    var totalAmount = ""


    var defaultDayCount = 0
    var mondayCount = 0
    var tuesdayCount = 0
    var wednesdayCount = 0
    var thursdayCount = 0
    var fridayCount = 0
    var saturdayCount = 0
    var sundayCount = 0

    var defaultDayPrice = 0
    var mondayPrice = 0
    var tuesdayPrice = 0
    var wednesdayPrice = 0
    var thursdayPrice = 0
    var fridayPrice = 0
    var saturdayPrice = 0
    var sundayPrice = 0

    var totalDiscount = 0

    var customPriceDays = 0
    var popupCustomPrice = ""


    companion object {
        const val CAR_DETAIL_ACTIVITY = 1
        const val PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
    }

    private val locationLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult(),
    ) {
        if (it.resultCode == RESULT_OK) {
            if (MainApplication.lastKnownLocation != null) {
                gpsTracker?.setLocation(MainApplication.lastKnownLocation)
            } else gpsTracker?.setLocation(MainApplication.getLastDeviceLocation())
        }
        lifecycleScope.launch {
            delay(5000)
            setData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_car_details)
        binding.activity = this
        uiHelper.hideActionBar(supportActionBar)
        addClickListeners()
        setData()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        lifecycleScope.launch(Dispatchers.Main) {
            this@CarDetailsActivity.intent = intent
            setData()
        }
    }

    private fun addClickListeners() {
        binding.ivArrowBack.setOnClickListener {
            if (isForResult) {
                val intent = intent
                intent.putExtra(Constants.DataParsing.FAVORITE_FLAG, model.fav_flag)
                setResult(Activity.RESULT_OK, intent)
            }
            finish()
        }
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        val cityId = preferenceHelper.getIntOther(Constants.DataParsing.CITY_SELECTED_ID, 0)
        lat = 0.0
        lng = 0.0
        try {
            if (uiHelper.isPermissionAllowed(
                    this, PERMISSION
                ) && !isCitySelected
            ) {

                if (gpsTracker!!.checkGPsEnabled()) {
                    if (gpsTracker?.getLatitude() != null && gpsTracker?.getLongitude() != null) {
                        lat = gpsTracker?.getLatitude() ?: 0.0
                        lng = gpsTracker?.getLongitude() ?: 0.0
                        callCarDetailsApi(0)
                    } else {
                        checkCityIdAndCallApi(cityId)
                    }
                } else {
                    showEnableLocationSetting(locationLauncher)
                }
            } else {
                checkCityIdAndCallApi(cityId)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("setData: ", e.message ?: "")
        }


        carDetailViewModel.getReportReason(
            true, Constants.API.GET_REPORT_REASON, this
        )

    }

    private fun checkCityIdAndCallApi(cityId: Int) {
        if (cityId == 0) carDetailViewModel.getCitiesList(
            true, Constants.API.CITIES_LIST, this
        )
        else callCarDetailsApi(cityId)
    }

    private fun callCarDetailsApi(cityId: Int) {
        if (intent.extras != null) {
            if (intent.hasExtra(Constants.DataParsing.ACTIVITY_NAME)) {
                isForResult = true
            }
            val carId = if (intent.data != null && intent.data?.isHierarchical == true) {
                ((intent.data ?: Uri.EMPTY).getQueryParameter("id") ?: "0").toInt()
            } else if (intent.extras?.containsKey(Constants.CAR_ID) == true) {
                intent.extras?.getInt(Constants.CAR_ID, 0) ?: 0
            } else {
                0
            }

            if (intent.extras!!.getSerializable(Constants.DataParsing.DATE_TIME_MODEL) != null) {
                calendarDateTimeModel =
                    intent.extras!!.getSerializable(Constants.DataParsing.DATE_TIME_MODEL) as CalendarDateTimeModel
            }
            setGradientTextColors()
            addScrollListeners()
            carDetailViewModel.getCarDetail(
                lat, lng, cityId, carId, true, Constants.API.CAR_DETAILS, this
            )
        }
    }

    private fun addScrollListeners() {
        binding.appBar.addOnOffsetChangedListener { appBar, offset ->
            if (abs(offset) >= appBar.totalScrollRange) {
                Log.e("addScrollListeners:", "visible")
                binding.tvCarName.isVisible = true
                binding.share.setColorFilter(
                    ContextCompat.getColor(this, R.color.dot_dark_screen1),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.ivArrowBack.setColorFilter(
                    ContextCompat.getColor(
                        this, R.color.dot_dark_screen1
                    ), android.graphics.PorterDuff.Mode.SRC_IN
                )
                imageResource = R.drawable.ic_favorite_black
                if (!isFavourite) {
                    binding.ivFavourite.setImageResource(imageResource)
                }

            } else {
                binding.share.setColorFilter(
                    ContextCompat.getColor(this, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.ivArrowBack.setColorFilter(
                    ContextCompat.getColor(this, R.color.white),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
                binding.tvCarName.isVisible = false

                imageResource = R.drawable.ic_new_un_favorite
            }


            if (!isFavourite) binding.ivFavourite.setImageResource(imageResource)
        }
    }

    private fun setGradientTextColors() {

        binding.tvDiscount3Title.setGradientTextColor(
            intArrayOf(
                Color.parseColor("#0F0F7D"), Color.parseColor("#1420E4")
            )
        )
        binding.tvDiscount3Amount.setGradientTextColor(
            intArrayOf(
                Color.parseColor("#0F0F7D"), Color.parseColor("#1420E4")
            )
        )

        binding.tvDiscount7Title.setGradientTextColor(
            intArrayOf(
                Color.parseColor("#0F0F7D"), Color.parseColor("#1420E4")
            )
        )

        binding.tvDiscount7Amount.setGradientTextColor(
            intArrayOf(
                Color.parseColor("#0F0F7D"), Color.parseColor("#1420E4")
            )
        )


        binding.tvDiscount30Title.setGradientTextColor(
            intArrayOf(
                Color.parseColor("#0F0F7D"), Color.parseColor("#1420E4")
            )
        )

        binding.tvDiscount30Amount.setGradientTextColor(
            intArrayOf(
                Color.parseColor("#0F0F7D"), Color.parseColor("#1420E4")
            )
        )

        binding.tvCarRating.setGradientTextColorNew(
            intArrayOf(
                Color.parseColor("#FF7DC3"), Color.parseColor("#1442E4")
            )
        )
    }

    /**
     * Function for click listeners
     * */
    @SuppressLint("SimpleDateFormat")
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view) {

            binding.share -> {
                openSharingIntent(
                    model.carSharing
                )
            }
            binding.clTripAmountDetails -> {
                val priceDetailsBottomSheet = PriceNewDetailsBottomSheet(

                    defaultDayCount ,
                  mondayCount ,
                  tuesdayCount ,
                  wednesdayCount,
                  thursdayCount ,
                  fridayCount ,
                  saturdayCount ,
                  sundayCount ,

                  defaultDayPrice,
                  mondayPrice,
                  tuesdayPrice ,
                  wednesdayPrice ,
                  thursdayPrice,
                  fridayPrice ,
                  saturdayPrice ,
                  sundayPrice ,
                  totalDiscount,
                  popupCustomPrice
                )
                priceDetailsBottomSheet.show(supportFragmentManager, "tag")
            }
            binding.tvChangeDate -> {

                Log.e(
                    "dateTime",
                    "${calendarDateTimeModel.pickUpDateServer}:${calendarDateTimeModel.dropOffDateServer}"
                )
                val maxDateCalendar =
                    getMaxCalendar(model.carInsuranceExpiry, Constants.ServerDateFormat).invoke()
                if (model.daysCustomAvailability.filter { it.isUnavailable == 1 }.size != 7) {
                    carDetailViewModel.showDateTimePickerWithTime(
                        this,
                        calendarDateTimeModel,
                        this,
                        model.un_available_date,
                        maxDateCalendar,
                        model.drop_off_date_and_time,
                        model.car_availability
                    )
                } else {
                    uiHelper.showLongToastInCenter(this, "You can't book")
                }

            }
            binding.clGuidelines -> {
                if (model.car_guideline_by_host.isEmpty()) {
                    uiHelper.showLongToastInCenter(
                        this, getString(R.string.ca_guidlines_not_available_err_msg)
                    )
                } else {
                    carDetailViewModel.showGuideLinesByHost(this, model.car_guideline_by_host)
                }
            }
            binding.continueBtn -> {
                if (uiHelper.userLoggedIn(preferenceHelper)) {
                    val userModel = preferenceHelper.getObject(
                        Constants.USER_MODEL, UserModel::class.java
                    ) as? UserModel
                    when {
                        binding.tvStartTime.text.toString().isEmpty() -> {
                            uiHelper.showLongToastInCenter(
                                this, getString(R.string.pickup_date_err_msg)
                            )
                        }
                        binding.tvFinishTime.text.toString().isEmpty() -> {
                            uiHelper.showLongToastInCenter(
                                this, getString(R.string.dropOff_date_err_msg)
                            )
                        }
                        else -> {
                            if (model.userStep in listOf(
                                    Constants.CarStep.STEP_3_COMPLETED,
                                    Constants.CarStep.STEP_4_COMPLETED
                                )
                            ) {
                                gotoCheckOutScreen()
                            } else {
                                userModel?.let {
                                    carDetailViewModel.getUserInfo(
                                        it.id, true, Constants.API.GET_USER_INFO, this
                                    )
                                }
                            }


                        }


                    }
                } else {
                    val bundle = Bundle()
                    bundle.putInt(Constants.DataParsing.LOGIN_RESULT, CAR_DETAIL_ACTIVITY)
                    uiHelper.openActivity(
                        this, LoginActivity::class.java, bundle
                    )
                }
            }

            binding.tvChangeLocation -> {

                    if (model.car_location_delivery.pin_lat != 0.0) {
                        carInfoModel.isHideChnageLocation = false
                        val bundle = Bundle()
                        bundle.putSerializable(
                            Constants.DataParsing.MODEL, model.car_location_delivery
                        )
                        bundle.putSerializable(Constants.DataParsing.CARINFODATAMODEL, carInfoModel)
                        uiHelper.openActivityForResult(
                            this,
                            LocationSelectionActivity::class.java,
                            true,
                            Constants.RequestCodes.ACTIVTIY_LOCATION_SELECTION,
                            bundle
                        )
                    }

            }
            binding.tvPickUpAndReturn ->{
                if (model.car_detail_info_model.carDetail.delivery_status != Constants.CarDeliveryStatus.AVAILABLE) {
                    if (model.car_location_delivery.pin_lat != 0.0) {
                        openMap(
                            model.car_location_delivery.pin_lat,
                            model.car_location_delivery.pin_long
                        )
                    }
                }
            }
            binding.tvReportListing -> {
                carDetailViewModel.showReportListingError(
                    this,
                    this@CarDetailsActivity,
                    reasonResponseModel.reportReasonDetails)
            }
            binding.tvCancellationPolicy -> {
                carDetailViewModel.showCancellationPolicy(this)
            }
            binding.tvViewMore -> {
                val bundle = Bundle()
                bundle.putSerializable(Constants.DataParsing.MODEL, model.car_feature)
                bundle.putString(
                    Constants.DataParsing.NAME, "${
                        model.car_detail_info_model.carDetail.name
                    } ${model.car_detail_info_model.carDetail.model}"
                )
                uiHelper.openActivityForResult(
                    this,
                    ViewAllCarFeaturesActivity::class.java,
                    true,
                    Constants.RequestCodes.ACTIVITY_CAR_FEATURE,
                    bundle
                )
            }
            binding.tvViewAllRatings -> {
                val bundle = Bundle()
                bundle.putSerializable(
                    Constants.DataParsing.REVIEWS_LIST, model.carReviewsList as Serializable
                )
                bundle.putString(
                    Constants.DataParsing.HEADING, resources.getString(R.string.reviews_from_guests)
                )
                bundle.putFloat(
                    Constants.DataParsing.RATING,
                    model.car_detail_info_model.carDetail.rating.toFloat()
                )
                bundle.putString(
                    Constants.DataParsing.REVIEW, binding.tvHostRatings.text.toString()
                )
                uiHelper.openActivityForResult(
                    this,
                    RatingAndReviewsActivity::class.java,
                    true,
                    Constants.RequestCodes.ACTIVITY_TRIPS,
                    bundle
                )

            }

            binding.ivFavourite -> {
                if (uiHelper.userLoggedIn(preferenceHelper)) {
                    if (model.fav_flag == 1) {
                        carDetailViewModel.deleteFavorite(
                            model.car_detail_info_model.id, true, Constants.API.DELETE_FAV_CAR, this
                        )
                    } else {
                        carDetailViewModel.addFavorite(
                            model.car_detail_info_model.id, true, Constants.API.ADD_FAV_CAR, this
                        )
                    }
                } else {
                    uiHelper.showLongToastInCenter(
                        this, getString(R.string.log_in_to_add_fav_car_err_msg)
                    )
                }
            }
            binding.ivHostProfileImage -> {
                val bundle = Bundle()
                bundle.putInt(Constants.DataParsing.USER_ID, model.host_by.id)
                uiHelper.openActivity(this, UserProfileActivity::class.java, bundle)
            }
        }
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.CAR_DETAILS -> {
                model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    CarFullDetailModel::class.java
                )
                prepareUnavailableDaysIntegerList()
                setUiRestrictionForUser()
                calendarDateTimeModel.carId = model.car_detail_info_model.id

                binding.carName.text = getString(
                    R.string.car_place_holder,
                    model.car_detail_info_model.carDetail.name,
                    model.car_detail_info_model.carDetail.model,
                    model.car_detail_info_model.carDetail.year
                )
                binding.tvHostedBy.text = getString(R.string.owner_name, model.host_by.name)
                binding.tvCarName.text =
                    "" + model.car_detail_info_model.carDetail.name + " " + model.car_detail_info_model.carDetail.model

                binding.tvChangeLocation.isVisible =
                    model.car_detail_info_model.carDetail.delivery_status == Constants.CarDeliveryStatus.AVAILABLE
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                val currentDate =
                    SimpleDateFormat(Constants.ServerDateFormat).format(calendar.time.time)
                model.un_available_date = ArrayList(model.un_available_date.filter {
                    (SimpleDateFormat(Constants.ServerDateFormat, Locale.getDefault()).parse(
                        currentDate
                    ) ?: Date()).before(SimpleDateFormat(Constants.ServerDateFormat).parse(it))
                }.sorted())

                if (model.host_by.averageRating == "0.0" || model.host_by.averageRating == "0") {
                    binding.tvCarRating.text = getString(R.string.no_ratings)
                    binding.tvRating.isVisible = false
                    binding.tvCarTotalTrips.isVisible = false
                    binding.carRating.isVisible = false
                    binding.civProfileStar.isVisible = false
                    binding.tvNoRating.isVisible = true
                } else {
                    binding.tvNoRating.isVisible = false
                  //  binding.tvRating.text = model.userAverageRating
                    binding.tvRating.text = model.host_by.averageRating.toFloat().toString()
                    binding.tvCarRating.text =
                        model.car_detail_info_model.carDetail.rating.toString()
                }

                binding.rbHostRatings.rating =
                    model.car_detail_info_model.carDetail.rating.toFloat()


                binding.tvTotalTrips.text = uiHelper.spanString(
                    getString(R.string.total_trips) + "\n" + model.host_by.total_Trips.toString(),
                    getString(R.string.total_trips),
                    R.font.inter_bold,
                    this
                )
                binding.tvCarTotalTrips.text =
                    "(" + model.host_by.total_Trips.toString() + " " + getString(R.string.trips) + ")"

                binding.viewpager.apply {
                    adapter = InfiniteViewPagerAdapter(
                        model.car_detail_info_model.images,
                        true,
                        this@CarDetailsActivity,
                        uiHelper,
                        false
                    )
                }

                binding.viewpager.clipToPadding = false
                binding.viewpager.clipChildren = false
                binding.viewpager.offscreenPageLimit = 3
                binding.viewpager.getChildAt(0).overScrollMode = RecyclerView.SCROLL_STATE_DRAGGING

                val adapterCarFeature = CarFeatureAdapterVerticalDetail(
                    this, uiHelper
                )
                val adapterCarBasicFeature = CarFeatureAdapterVerticalDetail(
                    this, uiHelper, true
                )
                val adapterRatings = RatingsAdapter(
                    uiHelper
                )
                binding.rvCarFeatures.apply {
                    layoutManager = LinearLayoutManager(
                        this@CarDetailsActivity, LinearLayoutManager.VERTICAL, false
                    )
                    adapter = adapterCarFeature
                }
                binding.rvCarBasicFeatures.apply {
                    layoutManager = LinearLayoutManager(
                        this@CarDetailsActivity, LinearLayoutManager.VERTICAL, false
                    )
                    adapter = adapterCarBasicFeature
                }
                binding.rvHostRatings.apply {
                    layoutManager = LinearLayoutManager(
                        this@CarDetailsActivity, LinearLayoutManager.HORIZONTAL, false
                    )
                    adapter = adapterRatings
                }
                if (model.car_feature.isNotEmpty()) {
                    if (model.car_feature.size > 4) binding.tvViewMore.visibility = View.VISIBLE
                    else {
                        binding.tvViewMore.visibility = View.GONE
                    }
                }
                adapterCarFeature.setList(model.car_feature)
                adapterCarBasicFeature.setList(model.car_basic_feature)


                binding.clHostRatings.isVisible = model.carReviewsList.isNotEmpty()
                binding.viewDescription.isVisible = model.carReviewsList.isNotEmpty()

                if (model.carReviewsList.isNotEmpty()) {
                    adapterRatings.setList(model.carReviewsList)
                    binding.tvHostRatings.text =
                        model.carReviewsList.size.toString() + if (model.carReviewsList.size > 1) " ratings" else " rating"
                } else {
                    binding.clHostRatings.isVisible = false
                }


                if (model.trip_discount.threeDayDiscountPrice == 0 || model.trip_discount.sevenDayDiscountPrice == 0 || model.trip_discount.thirtyDayDiscountPrice == 0) {
                    binding.clTripDiscount.visibility = View.GONE
                } else {
                    if (model.trip_discount.threeDayDiscountPrice != 0) {
                        count = 1
                        threeDayDiscountAmount =
                            (model.car_detail_info_model.carDetail.price.replace(",", "")
                                .toFloat() * (model.trip_discount.threeDayDiscountPrice.toFloat()
                                .div(100))).toDouble()
                        threeDayDiscountAmount = DecimalFormat("#.##").format(
                            threeDayDiscountAmount
                        ).toDouble()
                        val textDiscount = "$threeDayDiscountAmount SAR/day"
                        binding.tvDiscount3Amount.text = textDiscount
                        popupCustomPrice = textDiscount
                    }

                    if (model.trip_discount.sevenDayDiscountPrice != 0) {
                        sevenDayDiscountAmount =
                            (model.car_detail_info_model.carDetail.price.replace(",", "")
                                .toFloat() * (model.trip_discount.sevenDayDiscountPrice.toFloat()
                                .div(100))).toDouble()
                        sevenDayDiscountAmount = DecimalFormat("#.##").format(
                            sevenDayDiscountAmount
                        ).toDouble()
                        val textDiscount = "$sevenDayDiscountAmount SAR/day"
                        binding.tvDiscount7Amount.text = "-SAR $textDiscount"
                        popupCustomPrice = sevenDayDiscountAmount.toString()

                    }
                    if (model.trip_discount.thirtyDayDiscountPrice != 0) {

                        thirtyDayDiscountAmount =
                            (model.car_detail_info_model.carDetail.price.replace(",", "")
                                .toFloat() * (model.trip_discount.thirtyDayDiscountPrice.toFloat()
                                .div(100))).toDouble()
                        thirtyDayDiscountAmount = DecimalFormat("#.##").format(
                            thirtyDayDiscountAmount
                        ).toDouble()
                        val textDiscount = "$thirtyDayDiscountAmount"
                        binding.tvDiscount30Amount.text = "-SAR $textDiscount"
                        popupCustomPrice = thirtyDayDiscountAmount.toString()
                    }
                    binding.clTripDiscount.visibility = View.GONE
//                    binding.discount.text = textDiscount
                }
                binding.tvLocation.text = model.car_location_delivery.street_address

                if (model.distanceIncludedModel.unlimited_distance == 1) {
                    //  binding.clDistanceIncluded.visibility = View.GONE
                    binding.tvAllowedKMS.text = getString(R.string.kms_are_unlimited)
                } else {
                    binding.tvAllowedKMS.text = getString(
                        R.string.km_day_, model.distanceIncludedModel.distanceincluded.value
                    )
                    binding.tvPerKilometerCharge.text = getString(
                        R.string.charge_extra_msg,
                        model.distanceIncludedModel.charge,
                        getString(R.string.sar)
                    )
                }
                //-------------------------Calculate Number Of Lines And Add Read More or Read Less-----------------------------

                if (model.car_detail_info_model.carDetail.description.isEmpty()) {
                    binding.clDescription.isVisible = false
                } else {
                    binding.clDescription.isVisible = true
                    // binding.tvDescription.text = model.car_detail_info_model.carDetail.description
                    binding.tvDescription.text = model.car_detail_info_model.carDetail.description
                }
                val readMoreOption =
                    ReadMoreOption.Builder(this).textLength(3, ReadMoreOption.TYPE_LINE) // OR
                        .moreLabel(getString(R.string.read_more))
                        .lessLabel(getString(R.string.read_less))
                        .moreLabelColor(getColor(R.color.send_msg_bg))
                        .lessLabelColor(getColor(R.color.send_msg_bg)).labelUnderLine(false)
                        .expandAnimation(false).build()
                val countN: Int = StringUtils.countMatches(
                    model.car_detail_info_model.carDetail.description, "\n"
                )
                var count = uiHelper.countLines(binding.tvDescription)
                count += countN
                if (count > 3) {
                    readMoreOption.addReadMoreTo(
                        binding.tvDescription, model.car_detail_info_model.carDetail.description
                    )
                }
                //----------------------------------------------host data ----------------------------------------------------------
                binding.tvHostName.text = model.host_by.name
                binding.tvJoined.text = uiHelper.spanString(
                    getString(R.string.joined_) + "\n" + model.host_by.joined,
                    getString(R.string.joined_),
                    R.font.inter_bold,
                    this
                )
                binding.tvResponseTime.text = uiHelper.spanString(
                    getString(R.string.avg_response_timeees) + "\n" + model.host_by.average_response_time,
                    getString(R.string.avg_response_timeees),
                    R.font.inter_bold,
                    this
                )

                uiHelper.glideLoadImage(
                    this,
                    BuildConfig.IMAGE_BASE_URL + model.host_by.profile_img,
                    binding.ivHostProfileImage
                )
                binding.tvDiscountedAmount.text =
                    model.car_detail_info_model.carDetail.price + " " + getString(R.string.sar) + "/" + getString(
                        R.string.day
                    )
//                binding.depositAmount.text =
//                    getString(R.string.securityAmount) + " " + getString(R.string.sar) + " " + model.deposit_amount


                //Calculate Date Time With Advance Notice And minimum Duration limit
                val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:00:ss")
                val dateOnly = SimpleDateFormat("yyyy-MM-dd")
                var currentDateTimePickup = if (calendarDateTimeModel.pickUpDateServer.isEmpty()) {
                    Calendar.getInstance()
                } else {
                    val newInstance = Calendar.getInstance()
                    newInstance.time = timeFormat.parse(calendarDateTimeModel.pickUpDateServer)
                        ?: Calendar.getInstance().time
                    newInstance
                }

                var currentDateTimeDropOff =
                    if (calendarDateTimeModel.dropOffDateServer.isEmpty()) {
                        Calendar.getInstance()
                    } else {
                        val newInstance = Calendar.getInstance()
                        newInstance.time = timeFormat.parse(calendarDateTimeModel.dropOffDateServer)
                            ?: Calendar.getInstance().time
                        newInstance
                    }
                val pair = setNearestAvailability(
                    currentDateTimePickup,
                    currentDateTimeDropOff,
                    (NumberFormat.getInstance()
                        .parse(model.car_availability.adv_notice_trip_start) as Number).toInt(),
                    (NumberFormat.getInstance()
                        .parse(model.car_availability.min_trip_dur) as Number).toInt()
                )

                currentDateTimePickup = pair.first
                calendarDateTimeModel.minimumDateTime =
                    timeFormat.format(Calendar.getInstance().time)
                calendarDateTimeModel.minimumDateTimeDefault =
                    timeFormat.format(Calendar.getInstance().time)
                calendarDateTimeModel.minimumDateOnly = dateOnly.format(Calendar.getInstance().time)
                calendarDateTimeModel.minimumDateOnlyDefault =
                    dateOnly.format(Calendar.getInstance().time)
                calendarDateTimeModel.pickUpDateServer = timeFormat.format(currentDateTimePickup.time)
                calendarDateTimeModel.pickUpDateComplete = dateOnly.format(currentDateTimePickup.time)
                currentDateTimeDropOff = pair.second
                calendarDateTimeModel.dropOffDateServer = timeFormat.format(currentDateTimeDropOff.time)
                calendarDateTimeModel.dropOffDateComplete = dateOnly.format(currentDateTimeDropOff.time)
                daysCount =
                    (currentDateTimeDropOff[Calendar.DAY_OF_YEAR] - currentDateTimePickup[Calendar.DAY_OF_YEAR]) + 1
                noOfDaysDiscount = checkDiscountDays(daysCount)

                preferenceHelper.setString(
                    Constants.DataParsing.CUSTOM_DAYS,
                    noOfDaysDiscount.toString()
                )

                setLayoutAccordingToDiscount(daysCount)

            /*    preferenceHelper.setString(
                    Constants.DataParsing.CUSTOM_DAYS,
                    "0000"
                )*/



                calendarDateTimeModel.minimumDurationDateTime = timeFormat.format(currentDateTimeDropOff.time)
                calendarDateTimeModel.minimumDurationDateTimeDefault = timeFormat.format(currentDateTimeDropOff.time)

                loadDates(1)
                binding.mainLayout.visibility = View.VISIBLE

                if (model.fav_flag == 1) {
                    binding.ivFavourite.setImageResource(R.drawable.ic_new_favourite)
                } else {
                    binding.ivFavourite.setImageResource(imageResource)
                }

                isFavourite = model.fav_flag == 1
                carId = model.car_detail_info_model.id
            }

            Constants.API.GET_REPORT_REASON ->{
                 reasonResponseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    ReasonrResponse::class.java
                )

            }

            Constants.API.GET_USER_INFO -> {
                val modelUserInfo = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    UserInfoResponseModel::class.java
                )

                if (modelUserInfo.current_step in listOf(
                        Constants.CarStep.STEP_3_COMPLETED, Constants.CarStep.STEP_4_COMPLETED
                    )
                ) {
                    gotoCheckOutScreen()
                } else {
                    gotoUserInformationActivity(modelUserInfo)
                }
            }
            Constants.API.DELETE_FAV_CAR -> {
                uiHelper.showLongToastInCenter(this, response?.message)
                model.fav_flag = 0
                binding.ivFavourite.setImageResource(imageResource)
                isFavourite = false
            }
            Constants.API.ADD_FAV_CAR -> {
                model.fav_flag = 1
                uiHelper.showLongToastInCenter(this, response?.message)
                binding.ivFavourite.setImageResource(R.drawable.ic_new_favourite)
                isFavourite = true
            }
            Constants.API.CITIES_LIST -> {
                cityModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    CitiesListModel::class.java
                )
                if (cityModel.cityList.isNotEmpty()) {
                    carDetailViewModel.showCitySelectionPopup(this, object : PopupCallback {
                        override fun popupButtonClick(value: Int) {
                            super.popupButtonClick(value)
                            if (cityModel.cityList.isNotEmpty()) {
                                isCitySelected = true
                                preferenceHelper.setIntOther(
                                    Constants.DataParsing.CITY_SELECTED_ID,
                                    cityModel.cityList[value].id
                                )
                                preferenceHelper.setString(
                                    Constants.DataParsing.CITY_SELECTED,
                                    cityModel.cityList[value].city
                                )
                                preferenceHelper.setLong(
                                    Constants.DataParsing.CITY_LAT,
                                    cityModel.cityList[value].lat.toLong()
                                )
                                preferenceHelper.setLong(
                                    Constants.DataParsing.CITY_LNG,
                                    cityModel.cityList[value].lng.toLong()
                                )
                                lat = cityModel.cityList[value].lat
                                lng = cityModel.cityList[value].lng
                                callCarDetailsApi(cityModel.cityList[value].id)
                            }
                            carDetailViewModel.dismissCitySelectionDialog()
                        }
                    }, ArrayList(cityModel.cityList.map {
                        it.city
                    })
                    )
                }
            }
        }


    }

    private fun setUiRestrictionForUser() {
        /*binding.share.isInvisible = model.isOwnCar && model.car_detail_info_model.carDetail.status == Constants.CarStatus.SNOOZED
        binding.ivFavourite.isInvisible = binding.share.isInvisible
        binding.tvChangeDate.isVisible = !binding.share.isInvisible
        binding.tvChangeLocation.isVisible = !binding.share.isInvisible
        binding.clTripAmountDetails.isVisible = !binding.share.isInvisible
        binding.share.isEnabled = !binding.share.isInvisible
        binding.ivFavourite.isEnabled = !binding.share.isInvisible*/
        if (model.isOwnCar) {
            binding.clTripAmountDetails.isVisible = false
            binding.tvChangeDate.isVisible = false
            binding.tvChangeLocation.isVisible = false
        }

      /*  if (model.car_detail_info_model.carDetail.status == Constants.CarStatus.SNOOZED) {
            if (model.isOwnCar) {
                showToast(getString(R.string.the_car_has_been_snoozed_by_host))
            } else {
                showToast(getString(R.string.the_car_has_been_snoozed_by_host))
            }
        }*/

    }

    private fun loadDates(reachecd: Int) {
        Log.d("reachecd" , reachecd.toString())
        binding.tvStartDate.text = DateUtil.changeDateFormat(
            Constants.ServerDateTimeFormat,
            Constants.UIDateFormat,
            calendarDateTimeModel.pickUpDateServer,
        )
        Log.d("calendarDateTimeModel" , calendarDateTimeModel.dropOffDateServer)

        binding.tvStartTime.text = DateUtil.changeDateFormat(
            Constants.ServerDateTimeFormat,
            Constants.UITimeFormat,
            calendarDateTimeModel.pickUpDateServer,
        )
        binding.tvFinishDate.text = DateUtil.changeDateFormat(
            Constants.ServerDateTimeFormat,
            Constants.UIDateFormat,
            calendarDateTimeModel.dropOffDateServer,
        )
        binding.tvFinishTime.text = DateUtil.changeDateFormat(
            Constants.ServerDateTimeFormat,
            Constants.UITimeFormat,
            calendarDateTimeModel.dropOffDateServer,
        )
    }

    private fun setLayoutAccordingToDiscount(daysCount: Int) {

        customPriceList = model.carCustomPrice

        var monday = ""
        var tuesday = ""
        var wednesday = ""
        var thursday = ""
        var friday = ""
        var saturday = ""
        var sunday = ""



        for (i in customPriceList.indices) {
            Log.d("customPriceList", customPriceList[i].day)
            when (customPriceList[i].day) {

                "mon" -> {
                    monday = customPriceList[i].price
                    Log.d("", "")
                }
                "tue" -> {
                    tuesday = customPriceList[i].price
                    Log.d("", "")
                }
                "wed" -> {
                    wednesday = customPriceList[i].price
                    Log.d("", "")
                }
                "thu" -> {
                    thursday = customPriceList[i].price
                    Log.d("", "")
                }
                "fri" -> {
                    friday = customPriceList[i].price
                    Log.d("", "")
                }
                "sat" -> {
                    saturday = customPriceList[i].price
                    Log.d("", "")
                }
                "sun" -> {
                    sunday = customPriceList[i].price
                    Log.d("", "")
                }

            }

        }




        isDiscountApplied = noOfDaysDiscount != 0

        var newDiscouintDays = preferenceHelper.getString(Constants.DataParsing.CUSTOM_DAYS, "") ?: ""
        Log.d("newDiscouintDayss" , newDiscouintDays)

        if (noOfDaysDiscount == 0) {

            Log.d("newDiscouintDays" , newDiscouintDays)

            defaultDayCount = 0
            mondayCount = 0
            tuesdayCount = 0
            wednesdayCount = 0
            thursdayCount = 0
            fridayCount = 0
            saturdayCount = 0
            sundayCount = 0


            defaultDayPrice = 0
            mondayPrice = 0
            tuesdayPrice = 0
            wednesdayPrice = 0
            thursdayPrice = 0
            fridayPrice = 0
            saturdayPrice = 0
            sundayPrice = 0



            val dates: MutableList<Date> = ArrayList()
            val str_date = calendarDateTimeModel.pickUpDateServer
            val end_date = calendarDateTimeModel.dropOffDateServer
            val formatter: DateFormat
            formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val interval = (24 * 1000 * 60 * 60).toLong()
            val endTime = formatter.parse(end_date).time

            var curTime = formatter.parse(str_date).time
            while (curTime <= endTime) {
                dates.add(Date(curTime))
                curTime += interval
            }


            var selectDatePrice = 0

            var pickUpTime = calendarDateTimeModel.pickUpDateServer.subSequence(11 , 19)
            var dropOfTime = calendarDateTimeModel.dropOffDateServer.subSequence(11 , 19)

            if (pickUpTime == dropOfTime){
                dates.removeAt(dates.size -1)
            }else{}


            var numberOfDay = 0

            for (i in dates.indices) {
                numberOfDay ++
                val ds: String = formatter.format(dates[i])
                var currentDays = DateUtil.changeDateFormat(
                    Constants.ServerDateTimeFormat,
                    Constants.UIDateFormat,
                    ds,
                )

                if (currentDays.substring(0, 3) == "Mon") {
                    if (monday == null) {
                        selectDatePrice += model.car_detail_info_model.carDetail.price.toInt()
                        defaultDayPrice = model.car_detail_info_model.carDetail.price.toInt()
                        defaultDayCount++
                        Log.d(
                            "Date is ...",
                            "monday  ${model.car_detail_info_model.carDetail.price.toInt()}"
                        )
                    } else {
                        selectDatePrice += monday.toInt()
                        mondayPrice = monday.toInt()
                        mondayCount++
                        Log.d("Date is ...", "monday $monday")
                    }
                }
                if (currentDays.substring(0, 3) == "Tue") {
                    if (tuesday == null) {
                        defaultDayCount++
                        selectDatePrice += model.car_detail_info_model.carDetail.price.toInt()
                        defaultDayPrice = model.car_detail_info_model.carDetail.price.toInt()

                        Log.d(
                            "Date is ...",
                            "tuesday  ${model.car_detail_info_model.carDetail.price.toInt()}"
                        )
                    } else {
                        selectDatePrice += tuesday.toInt()
                        tuesdayPrice = tuesday.toInt()
                        tuesdayCount++
                        Log.d("Date is ...", "tuesday $tuesday")
                    }
                }
                if (currentDays.substring(0, 3) == "Wed") {
                    if (wednesday == null) {
                        defaultDayCount++
                        selectDatePrice += model.car_detail_info_model.carDetail.price.toInt()
                        defaultDayPrice = model.car_detail_info_model.carDetail.price.toInt()

                        Log.d(
                            "Date is ...",
                            "wednesday  ${model.car_detail_info_model.carDetail.price.toInt()}"
                        )

                    } else {
                        selectDatePrice += wednesday.toInt()
                        wednesdayPrice = wednesday.toInt()
                        wednesdayCount++
                        Log.d("Date is ...", "wednesday $wednesday")

                    }
                }
                if (currentDays.substring(0, 3) == "Thu") {
                    if (thursday == null) {
                        defaultDayCount++
                        selectDatePrice += model.car_detail_info_model.carDetail.price.toInt()
                        defaultDayPrice = model.car_detail_info_model.carDetail.price.toInt()

                        Log.d(
                            "Date is ...",
                            "thursday  ${model.car_detail_info_model.carDetail.price.toInt()}"
                        )
                    } else {
                        selectDatePrice += thursday.toInt()
                        thursdayPrice = thursday.toInt()
                        thursdayCount++
                        Log.d("Date is ...", "thursday $thursday")
                    }
                }
                if (currentDays.substring(0, 3) == "Fri") {
                    if (friday == null) {
                        defaultDayCount++
                        selectDatePrice += model.car_detail_info_model.carDetail.price.toInt()
                        defaultDayPrice = model.car_detail_info_model.carDetail.price.toInt()

                        Log.d(
                            "Date is ...",
                            "friday  ${model.car_detail_info_model.carDetail.price.toInt()}"
                        )

                    } else {
                        selectDatePrice += friday.toInt()
                        fridayPrice = friday.toInt()
                        fridayCount++
                        Log.d("Date is ...", "friday $friday")

                    }
                }
                if (currentDays.substring(0, 3) == "Sat") {
                    if (saturday == null) {
                        defaultDayCount++
                        selectDatePrice += model.car_detail_info_model.carDetail.price.toInt()
                        defaultDayPrice = model.car_detail_info_model.carDetail.price.toInt()

                        Log.d(
                            "Date is ...",
                            "saturday  ${model.car_detail_info_model.carDetail.price.toInt()}"
                        )

                    } else {
                        selectDatePrice += saturday.toInt()
                        saturdayPrice = saturday.toInt()
                        saturdayCount++
                        Log.d("Date is ...", "saturday $saturday")

                    }
                }
                if (currentDays.substring(0, 3) == "Sun") {
                    if (sunday == null) {
                        defaultDayCount++
                        selectDatePrice += model.car_detail_info_model.carDetail.price.toInt()
                        defaultDayPrice = model.car_detail_info_model.carDetail.price.toInt()

                        Log.d(
                            "Date is ...",
                            "sunday ${model.car_detail_info_model.carDetail.price.toInt()}"
                        )

                    } else {
                        selectDatePrice += sunday.toInt()
                        sundayPrice = sunday.toInt()
                        sundayCount++
                        Log.d("Date is ...", "sunday $sunday")

                    }
                }

                Log.d("Date is ...", "-> $selectDatePrice ->   ${currentDays.substring(0, 3)}")

            }

            var discountPrices = selectDatePrice / daysCount
            binding.tvDiscountedAmount.text = "$discountPrices SAR/DAY"
            binding.tvEstimatedTotal.text = "$selectDatePrice est. total"

            Log.d("COUNT is ...", "defaultDayCount -> $defaultDayCount  -> $defaultDayPrice")
            Log.d("COUNT is ...", "mondayCount -> $mondayCount -> $mondayPrice")
            Log.d("COUNT is ...", "tuesdayCount -> $tuesdayCount -> $tuesdayPrice")
            Log.d("COUNT is ...", "wednesdayCount -> $wednesdayCount -> $wednesdayPrice")
            Log.d("COUNT is ...", "thursdayCount -> $thursdayCount -> $thursdayPrice")
            Log.d("COUNT is ...", "fridayCount -> $fridayCount -> $fridayPrice")
            Log.d("COUNT is ...", "saturdayCount -> $saturdayCount -> $saturdayPrice")
            Log.d("COUNT is ...", "sundayCount -> $sundayCount -> $sundayPrice")

        }
        else {
            Log.d("newDiscouintDaysss" , newDiscouintDays)
            defaultDayCount = 0
            mondayCount = 0
            tuesdayCount = 0
            wednesdayCount = 0
            thursdayCount = 0
            fridayCount = 0
            saturdayCount = 0
            sundayCount = 0


            defaultDayPrice = 0
            mondayPrice = 0
            tuesdayPrice = 0
            wednesdayPrice = 0
            thursdayPrice = 0
            fridayPrice = 0
            saturdayPrice = 0
            sundayPrice = 0



            val dates: MutableList<Date> = ArrayList()
            val str_date = calendarDateTimeModel.pickUpDateServer
            val end_date = calendarDateTimeModel.dropOffDateServer
            val formatter: DateFormat
            formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            val interval = (24 * 1000 * 60 * 60).toLong() // 1 hour in millis
            val endTime =
                formatter.parse(end_date).time // create your endtime here, possibly using Calendar or Date
            var curTime = formatter.parse(str_date).time
            while (curTime <= endTime) {
                dates.add(Date(curTime))
                curTime += interval
            }
            var selectDatePrice = 0

            var pickUpTime = calendarDateTimeModel.pickUpDateServer.subSequence(11 , 19)
            var dropOfTime = calendarDateTimeModel.dropOffDateServer.subSequence(11 , 19)

            if (pickUpTime == dropOfTime){
                dates.removeAt(dates.size -1)
            }else{}

          /*  discountAppliedAmount = when (dates.size) {
                3 -> threeDayDiscountAmount
                7 -> sevenDayDiscountAmount
                else -> thirtyDayDiscountAmount
            }*/


            var numberOfDay = 0

            for (i in dates.indices) {
                numberOfDay ++
                val ds: String = formatter.format(dates[i])
                var currentDays = DateUtil.changeDateFormat(
                    Constants.ServerDateTimeFormat,
                    Constants.UIDateFormat,
                    ds,
                )

                if (currentDays.substring(0, 3) == "Mon") {
                    if (monday == null) {
                        selectDatePrice += model.car_detail_info_model.carDetail.price.toInt()
                        defaultDayPrice = model.car_detail_info_model.carDetail.price.toInt()
                        defaultDayCount++
                        Log.d(
                            "Date is ...",
                            "monday  ${model.car_detail_info_model.carDetail.price.toInt()}"
                        )
                    } else {
                        selectDatePrice += monday.toInt()
                        mondayPrice = monday.toInt()
                        mondayCount++
                        Log.d("Date is ...", "monday $monday")
                    }
                }
                if (currentDays.substring(0, 3) == "Tue") {
                    if (tuesday == null) {
                        defaultDayCount++
                        selectDatePrice += model.car_detail_info_model.carDetail.price.toInt()
                        defaultDayPrice = model.car_detail_info_model.carDetail.price.toInt()

                        Log.d(
                            "Date is ...",
                            "tuesday  ${model.car_detail_info_model.carDetail.price.toInt()}"
                        )
                    } else {
                        selectDatePrice += tuesday.toInt()
                        tuesdayPrice = tuesday.toInt()
                        tuesdayCount++
                        Log.d("Date is ...", "tuesday $tuesday")
                    }
                }
                if (currentDays.substring(0, 3) == "Wed") {
                    if (wednesday == null) {
                        defaultDayCount++
                        selectDatePrice += model.car_detail_info_model.carDetail.price.toInt()
                        defaultDayPrice = model.car_detail_info_model.carDetail.price.toInt()

                        Log.d(
                            "Date is ...",
                            "wednesday  ${model.car_detail_info_model.carDetail.price.toInt()}"
                        )

                    } else {
                        selectDatePrice += wednesday.toInt()
                        wednesdayPrice = wednesday.toInt()
                        wednesdayCount++
                        Log.d("Date is ...", "wednesday $wednesday")

                    }
                }
                if (currentDays.substring(0, 3) == "Thu") {
                    if (thursday == null) {
                        defaultDayCount++
                        selectDatePrice += model.car_detail_info_model.carDetail.price.toInt()
                        defaultDayPrice = model.car_detail_info_model.carDetail.price.toInt()

                        Log.d(
                            "Date is ...",
                            "thursday  ${model.car_detail_info_model.carDetail.price.toInt()}"
                        )
                    } else {
                        selectDatePrice += thursday.toInt()
                        thursdayPrice = thursday.toInt()
                        thursdayCount++
                        Log.d("Date is ...", "thursday $thursday")
                    }
                }
                if (currentDays.substring(0, 3) == "Fri") {
                    if (friday == null) {
                        defaultDayCount++
                        selectDatePrice += model.car_detail_info_model.carDetail.price.toInt()
                        defaultDayPrice = model.car_detail_info_model.carDetail.price.toInt()

                        Log.d(
                            "Date is ...",
                            "friday  ${model.car_detail_info_model.carDetail.price.toInt()}"
                        )

                    } else {
                        selectDatePrice += friday.toInt()
                        fridayPrice = friday.toInt()
                        fridayCount++
                        Log.d("Date is ...", "friday $friday")

                    }
                }
                if (currentDays.substring(0, 3) == "Sat") {
                    if (saturday == null) {
                        defaultDayCount++
                        selectDatePrice += model.car_detail_info_model.carDetail.price.toInt()
                        defaultDayPrice = model.car_detail_info_model.carDetail.price.toInt()

                        Log.d(
                            "Date is ...",
                            "saturday  ${model.car_detail_info_model.carDetail.price.toInt()}"
                        )

                    } else {
                        selectDatePrice += saturday.toInt()
                        saturdayPrice = saturday.toInt()
                        saturdayCount++
                        Log.d("Date is ...", "saturday $saturday")

                    }
                }
                if (currentDays.substring(0, 3) == "Sun") {
                    if (sunday == null) {
                        defaultDayCount++
                        selectDatePrice += model.car_detail_info_model.carDetail.price.toInt()
                        defaultDayPrice = model.car_detail_info_model.carDetail.price.toInt()

                        Log.d(
                            "Date is ...",
                            "sunday ${model.car_detail_info_model.carDetail.price.toInt()}"
                        )

                    } else {
                        selectDatePrice += sunday.toInt()
                        sundayPrice = sunday.toInt()
                        sundayCount++
                        Log.d("Date is ...", "sunday $sunday")

                    }
                }

                   Log.d("Date is ...", "-> $selectDatePrice ->   ${currentDays.substring(0, 3)}")

            }

            customPriceDays =   mondayCount + tuesdayCount + wednesdayCount + thursdayCount + fridayCount + saturdayCount + sundayCount

            if (dates.size in 0 .. 2 ){

                binding.clTripDiscount.isVisible = false
                binding.clDiscount3Details.isVisible = false
                binding.clDiscount7Details.isVisible = false
                binding.clDiscount30Details.isVisible = false

                binding.tvOriginalAmount.visibility = View.GONE
                if (dates.size > 1){
                    var dayPrice = selectDatePrice / numberOfDay
                    binding.tvDiscountedAmount.text = "$dayPrice SAR/DAY"
                    binding.tvEstimatedTotal.text = "$selectDatePrice est. total"
                }else {
                    binding.tvDiscountedAmount.text = "$selectDatePrice SAR/DAY"
                    binding.tvEstimatedTotal.text = "$selectDatePrice est. total"
                }
            }else
            if (dates.size in 3..6){
                totalDiscount = model.trip_discount.threeDayDiscountPrice

                if (model.trip_discount.threeDayDiscountPrice > 0){

                    var originalPrice = selectDatePrice / numberOfDay
                    binding.tvOriginalAmount.text = "$originalPrice  SAR"
                    binding.tvOriginalAmount.visibility = View.VISIBLE

                    var parDiscount =  model.trip_discount.threeDayDiscountPrice / 100
                    var discountPrice = originalPrice * parDiscount
                    var discountedPrice = originalPrice - discountPrice
                    binding.tvDiscountedAmount.text = "$discountedPrice SAR/DAY"

                    var total = discountedPrice * numberOfDay

                    binding.tvEstimatedTotal.text = "$total est. total"




                    binding.clTripDiscount.isVisible = true
                    binding.clDiscount3Details.isVisible = true
                    var amountWithPercentage = model.trip_discount.threeDayDiscountPrice * selectDatePrice
                    var finalPertage = amountWithPercentage / 100
                    binding.tvDiscount3Amount.text = "-SAR $finalPertage"
                    popupCustomPrice = finalPertage.toString()


                    binding.tvDiscount3Title.text = "3+ days discount"

                    binding.clDiscount7Details.isVisible = false
                    binding.clDiscount30Details.isVisible = false


                }else{
                    var discountPrices = selectDatePrice / numberOfDay
                    binding.tvDiscountedAmount.text = "$discountPrices SAR/DAY"
                    binding.tvEstimatedTotal.text = "$selectDatePrice est. total"
                }


              /*  binding.tvOriginalAmount.visibility = View.VISIBLE
                var originalPrice =  selectDatePrice / numberOfDay
                binding.tvOriginalAmount.text = "$originalPrice SAR"
                var discountPrice = model.trip_discount.threeDayDiscountPrice.toInt()
                val discountedAmount = selectDatePrice - discountPrice
                var discountPrices = discountedAmount / numberOfDay
                binding.tvDiscountedAmount.text = "$discountPrices SAR"
                var price = selectDatePrice - discountPrice
                price * dates.size
               var finalPrice =  price + 1
                binding.tvEstimatedTotal.text = "$finalPrice est. total"*/

            }else
            if (dates.size in 7..30){
                totalDiscount = model.trip_discount.sevenDayDiscountPrice
                if (selectDatePrice *model.trip_discount.sevenDayDiscountPrice > 0) {
                    var originalPrice = selectDatePrice / numberOfDay
                    binding.tvOriginalAmount.text = "$originalPrice  SAR"

                    binding.tvOriginalAmount.visibility = View.VISIBLE
                    var newDiscountPrice =
                        selectDatePrice * model.trip_discount.sevenDayDiscountPrice / 100
                    var prices = selectDatePrice - newDiscountPrice
                    var discountPrices = prices / numberOfDay
                    binding.tvDiscountedAmount.text = "$discountPrices SAR/DAY"
                    var finalPrice = prices
                    binding.tvEstimatedTotal.text = "$finalPrice est. total"
                }else{
                    var discountPrices = selectDatePrice / numberOfDay
                    binding.tvDiscountedAmount.text = "$discountPrices SAR/DAY"
                    binding.tvEstimatedTotal.text = "$selectDatePrice est. total"
                }


                binding.clTripDiscount.isVisible = true
                binding.clDiscount3Details.isVisible = false
                var amountWithPercentage = model.trip_discount.sevenDayDiscountPrice * selectDatePrice
                var finalPertage = amountWithPercentage / 100
                binding.tvDiscount7Amount.text = "-SAR $finalPertage"
                binding.tvDiscount7Title.text = "7+ days discount"
                popupCustomPrice = finalPertage.toString()

                binding.clDiscount7Details.isVisible = true
                binding.clDiscount30Details.isVisible = false

            }else
            if (dates.size > 30){
                if (model.trip_discount.threeDayDiscountPrice > 0){
                    totalDiscount = model.trip_discount.threeDayDiscountPrice
                    var originalPrice = selectDatePrice / numberOfDay
                    binding.tvOriginalAmount.text = "$originalPrice  SAR"

                    binding.tvOriginalAmount.visibility = View.VISIBLE
                    var newDiscountPrice =
                        selectDatePrice * model.trip_discount.threeDayDiscountPrice / 100
                    var prices = selectDatePrice - newDiscountPrice
                    var discountPrices = prices / numberOfDay
                    binding.tvDiscountedAmount.text = "$discountPrices SAR/DAY"
                    var finalPrice = prices
                    binding.tvEstimatedTotal.text = "$finalPrice est. total"
                }else{
                    var discountPrices = selectDatePrice / numberOfDay
                    binding.tvDiscountedAmount.text = "$discountPrices SAR/DAY"
                    binding.tvEstimatedTotal.text = "$selectDatePrice est. total"
                }


                binding.clTripDiscount.isVisible = true
                binding.clDiscount3Details.isVisible = false
                var amountWithPercentage = model.trip_discount.threeDayDiscountPrice * selectDatePrice
                var finalPertage = amountWithPercentage / 100
                binding.tvDiscount30Amount.text = "-SAR $finalPertage"
                binding.tvDiscount30Title.text = "30+ days discount"
                popupCustomPrice = finalPertage.toString()

                binding.clDiscount7Details.isVisible = false
                binding.clDiscount30Details.isVisible = true
            }

            Log.d("COUNT is ...", "defaultDayCount -> $defaultDayCount  -> $defaultDayPrice")
            Log.d("COUNT is ...", "mondayCount -> $mondayCount -> $mondayPrice")
            Log.d("COUNT is ...", "tuesdayCount -> $tuesdayCount -> $tuesdayPrice")
            Log.d("COUNT is ...", "wednesdayCount -> $wednesdayCount -> $wednesdayPrice")
            Log.d("COUNT is ...", "thursdayCount -> $thursdayCount -> $thursdayPrice")
            Log.d("COUNT is ...", "fridayCount -> $fridayCount -> $fridayPrice")
            Log.d("COUNT is ...", "saturdayCount -> $saturdayCount -> $saturdayPrice")
            Log.d("COUNT is ...", "sundayCount -> $sundayCount -> $sundayPrice")

            Log.d("daysCount", "$daysCount //// $selectDatePrice")


        }
    }

    private fun checkDiscountDays(daysCount: Int): Int {
        return if (model.trip_discount.threeDayDiscountPrice != 0 || model.trip_discount.sevenDayDiscountPrice != 0 || model.trip_discount.thirtyDayDiscountPrice != 0) {
            if (daysCount in 3..6) 3
            else if (daysCount in 7..29) 7
            else if (daysCount >= 30) 30
            else 0
        } else 0
    }

    private fun setNearestAvailability(
        pickUpCalendarInstance: Calendar,
        dropOffCalendarInstance: Calendar,
        noticeTime: Int,
        minTripDuration: Int
    ): Pair<Calendar, Calendar> {

        pickUpCalendarInstance.time = getAvailableDate(
            pickUpCalendarInstance, noticeTime + 1, 0
        ).first.time

        dropOffCalendarInstance.time = pickUpCalendarInstance.time

        val dropOffPair = getAvailableDate(
            dropOffCalendarInstance, minTripDuration, 0
        )
        dropOffCalendarInstance.time = dropOffPair.first.time

        if (dropOffPair.second != 0) {
            pickUpCalendarInstance.add(Calendar.DAY_OF_YEAR, 1)
            setNearestAvailability(
                dropOffCalendarInstance, dropOffCalendarInstance, 0, minTripDuration
            )
        }

        val foundedPickUpCustomAvailability: CarCustomAvailability? =
            getCustomAvailability(pickUpCalendarInstance)

        return if (foundedPickUpCustomAvailability != null) {
            if (DateUtil.isToday(sdfServerDateTimeFormat.format(pickUpCalendarInstance.time))) {
                val foundedAvailability = getHostAvailability(
                    pickUpCalendarInstance,
                    foundedPickUpCustomAvailability,
                )

                val startTime = Calendar.getInstance()
                startTime.time = foundedAvailability.first.time
                if (startTime.time < Calendar.getInstance().time) {
                    foundedAvailability.first.add(Calendar.DAY_OF_WEEK, 1)
                    setNearestAvailability(
                        foundedAvailability.first,
                        dropOffCalendarInstance,
                        noticeTime,
                        minTripDuration
                    )
                } else {
                    if (pickUpCalendarInstance.time >= startTime.time && pickUpCalendarInstance.time <= foundedAvailability.second.time) {
                        startTime.time = pickUpCalendarInstance.time
                    }
                    pickUpCalendarInstance.time = startTime.time
                    startTime.add(Calendar.HOUR, minTripDuration)
                    dropOffCalendarInstance.time = startTime.time
                    checkDropOf(
                        pickUpCalendarInstance, dropOffCalendarInstance, minTripDuration
                    )
                }
            } else {
                val foundedAvailability =
                    getHostAvailability(pickUpCalendarInstance, foundedPickUpCustomAvailability)
                val startTime = Calendar.getInstance()
                startTime.time = foundedAvailability.first.time
                if (pickUpCalendarInstance.time >= startTime.time && pickUpCalendarInstance.time <= foundedAvailability.second.time) {
                    startTime.time = pickUpCalendarInstance.time
                }
                pickUpCalendarInstance.time = startTime.time
                startTime.add(Calendar.HOUR, minTripDuration)
                dropOffCalendarInstance.time = startTime.time
                checkDropOf(
                    pickUpCalendarInstance, dropOffCalendarInstance, minTripDuration
                )
            }
        } else run {

            //  Simple case when on pickup host has not applied custom availability
            if (!DateUtil.isToday(sdfServerDateTimeFormat.format(pickUpCalendarInstance.time))) {
                val calendar = pickUpCalendarInstance.clone() as Calendar
                calendar.add(Calendar.HOUR, minTripDuration)
                dropOffCalendarInstance.time = calendar.time
            }


            checkDropOf(
                pickUpCalendarInstance, dropOffCalendarInstance, minTripDuration
            )
        }
    }

    /**
     *  Function for checking and setting drop off time
     * */
    private fun checkDropOf(
        pickUpCalendarInstance: Calendar, dropOffCalendarInstance: Calendar, minTripDuration: Int
    ): Pair<Calendar, Calendar> {
        return if (dropOffCalendarInstance[Calendar.DAY_OF_YEAR] != pickUpCalendarInstance[Calendar.DAY_OF_YEAR]) {
            val pairCalendarAndInt = getAvailableDate(
                dropOffCalendarInstance, 0, 0
            )
            if (pairCalendarAndInt.second != 0) {
                pickUpCalendarInstance.time = pairCalendarAndInt.first.time
                setNearestAvailability(
                    pickUpCalendarInstance = pickUpCalendarInstance,
                    dropOffCalendarInstance = dropOffCalendarInstance,
                    noticeTime = 0,
                    minTripDuration = minTripDuration
                )
            } else {
                dropOffCalendarInstance.time = pairCalendarAndInt.first.time
                val foundedCarCustomAvailability = getCustomAvailability(dropOffCalendarInstance)
                if (foundedCarCustomAvailability != null) {
                    val foundedDropOffAvailability = getHostAvailability(
                        dropOffCalendarInstance, foundedCarCustomAvailability
                    )
                    if (dropOffCalendarInstance.time != foundedDropOffAvailability.first) {
                        compareDropOffWithCustomAvailability(
                            pickUpCalendarInstance,
                            dropOffCalendarInstance,
                            minTripDuration,
                            foundedDropOffAvailability
                        )
                    } else {
                        Pair(pickUpCalendarInstance, dropOffCalendarInstance)
                    }
                } else {
                    Pair(pickUpCalendarInstance, dropOffCalendarInstance)
                }
            }
        } else {
            val foundedCarCustomAvailability = getCustomAvailability(dropOffCalendarInstance)
            if (foundedCarCustomAvailability != null) {
                val foundedDropOffAvailability = getHostAvailability(
                    dropOffCalendarInstance, foundedCarCustomAvailability
                )
                compareDropOffWithCustomAvailability(
                    pickUpCalendarInstance,
                    dropOffCalendarInstance,
                    minTripDuration,
                    foundedDropOffAvailability
                )
            } else {
                Pair(pickUpCalendarInstance, dropOffCalendarInstance)
            }
        }
    }

    /**
     * Function for comparing drop off with custom availability
     * */
    private fun compareDropOffWithCustomAvailability(
        pickUpCalendarInstance: Calendar,
        dropOffCalendarInstance: Calendar,
        minTripDuration: Int,
        foundedDropOffAvailability: Pair<Calendar, Calendar>
    ): Pair<Calendar, Calendar> {
        val calendar = Calendar.getInstance()
        calendar.time = pickUpCalendarInstance.time
        calendar.add(Calendar.HOUR, minTripDuration)
        return if (dropOffCalendarInstance.time > foundedDropOffAvailability.second.time) {
            if (calendar.time >= foundedDropOffAvailability.first.time && calendar.time <= foundedDropOffAvailability.second.time) {
                Pair(pickUpCalendarInstance, calendar)
            } else {
                dropOffCalendarInstance.add(Calendar.DAY_OF_YEAR, 1)
                checkDropOf(
                    pickUpCalendarInstance, dropOffCalendarInstance, minTripDuration
                )
            }
        } else {
            if (pickUpCalendarInstance[Calendar.HOUR] - dropOffCalendarInstance[Calendar.HOUR] >= minTripDuration && dropOffCalendarInstance.time < foundedDropOffAvailability.second.time) {
                Pair(pickUpCalendarInstance, dropOffCalendarInstance)
            } else {
                if (calendar.time >= foundedDropOffAvailability.first.time && calendar.time <= foundedDropOffAvailability.second.time) {
                    Pair(pickUpCalendarInstance, dropOffCalendarInstance)
                } else {
                    dropOffCalendarInstance.add(Calendar.DAY_OF_YEAR, 1)
                    checkDropOf(
                        pickUpCalendarInstance, dropOffCalendarInstance, minTripDuration
                    )
                }
            }
        }
    }

    /**
     * Function for getting day custom availability
     * */
    private fun getCustomAvailability(calendarInstance: Calendar): CarCustomAvailability? {
        return model.car_availability.daysCustomAvailability.find {
            calendarInstance[Calendar.DAY_OF_WEEK] == it.dayIndex && it.isUnavailable == Constants.Availability.CUSTOM_AVAILABILITY
        }
    }

    /**
     * Function for getting available date
     * */
    private fun getAvailableDate(
        calendarInstance: Calendar, offSet: Int, count: Int
    ): Pair<Calendar, Int> {

        val isUnAvailableDate = calendarInstance.isDateAvailable(
            model.un_available_date, model.daysCustomAvailability, sdfServerDateFormat
        )

        return if (isUnAvailableDate) {
            calendarInstance.add(Calendar.DAY_OF_WEEK, 1)
            calendarInstance[Calendar.HOUR] = 0
            getAvailableDate(
                calendarInstance, offSet, count + 1
            )
        } else {
            calendarInstance.add(Calendar.HOUR, offSet)
            Pair(calendarInstance, count)
        }
    }

    /**
     * Function for getting availability range
     * */
    private fun getHostAvailability(
        startTime: Calendar,
        foundedCarCustomAvailability: CarCustomAvailability,
    ): Pair<Calendar, Calendar> {
        val isPickUpAvailable = DateUtil.checkTimeIsBetweenCustomHours(
            startTime.time, foundedCarCustomAvailability, sdfServerDateTimeFormat.format(
                startTime.time
            )
        )

        if (!isPickUpAvailable) {
            var dateTimeString = sdfServerDateFormat.format(startTime.time)
            dateTimeString += " " + foundedCarCustomAvailability.from + ":00"
            startTime.time = sdfServerDateTimeFormat.parse(dateTimeString) ?: Date()
        }

        val endTime = Calendar.getInstance()
        endTime.time = startTime.time
        var dateTimeString = sdfServerDateFormat.format(endTime.time)
        dateTimeString += " " + foundedCarCustomAvailability.to + ":00"
        endTime.time = sdfServerDateTimeFormat.parse(dateTimeString) ?: Date()

        return Pair(startTime, endTime)
    }

    /**
     * Function for preparing unavailable days list
     * */
    private fun prepareUnavailableDaysIntegerList() {
        if (model.daysCustomAvailability.isNotEmpty()) {
            for (i in model.daysCustomAvailability) {
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
            model.car_availability.daysCustomAvailability = model.daysCustomAvailability
        }
    }

    /**
     * Function for opening user information activity
     * */
    private fun gotoUserInformationActivity(modelUserInfo: UserInfoResponseModel) {
        val bundle = Bundle()
        if (lat == 0.0 && lng == 0.0) {
            if (uiHelper.isPermissionAllowed(this, PERMISSION) && gpsTracker != null) {
                checkOutInfoModel.lat = gpsTracker?.getLatitude()!!
                checkOutInfoModel.lng = gpsTracker?.getLongitude()!!
            }
        } else {
            checkOutInfoModel.lat = lat
            checkOutInfoModel.lng = lng
        }
        checkOutInfoModel.city_id =
            preferenceHelper.getIntOther(Constants.DataParsing.CITY_SELECTED_ID, 0)
        checkOutInfoModel.pick_up = calendarDateTimeModel.pickUpDateServer
        checkOutInfoModel.drop_off = calendarDateTimeModel.dropOffDateServer
        checkOutInfoModel.car_id = model.car_detail_info_model.id
        bundle.putSerializable(Constants.DataParsing.CARINFODATAMODEL, carInfoModel)
        bundle.putSerializable(Constants.DataParsing.MODEL_OTHER, checkOutInfoModel)
        bundle.putSerializable(Constants.DataParsing.DATE_TIME_MODEL, calendarDateTimeModel)
        bundle.putSerializable(Constants.DataParsing.MODEL, modelUserInfo)
        uiHelper.openActivity(
            this, UserInformationActivity::class.java, bundle
        )
    }

    /**
     * Function for opening checkout activity
     * */
    private fun gotoCheckOutScreen() {
        val bundle = Bundle()
        if (lat == 0.0 && lng == 0.0) {
            if (uiHelper.isPermissionAllowed(this, PERMISSION) && gpsTracker != null) {
                checkOutInfoModel.lat = gpsTracker?.getLatitude()!!
                checkOutInfoModel.lng = gpsTracker?.getLongitude()!!
            }
        } else {
            checkOutInfoModel.lat = lat
            checkOutInfoModel.lng = lng
        }
        checkOutInfoModel.city_id =
            preferenceHelper.getIntOther(Constants.DataParsing.CITY_SELECTED_ID, 0)
        checkOutInfoModel.pick_up = calendarDateTimeModel.pickUpDateServer
        checkOutInfoModel.drop_off = calendarDateTimeModel.dropOffDateServer
        Log.e("DateTime", checkOutInfoModel.pick_up)
        Log.e("DateTime", checkOutInfoModel.drop_off)
        checkOutInfoModel.car_id = model.car_detail_info_model.id
        bundle.putSerializable(Constants.DataParsing.HOST_NAME, model.host_by)
        bundle.putSerializable(Constants.DataParsing.CARINFODATAMODEL, carInfoModel)
        bundle.putSerializable(Constants.DataParsing.MODEL, checkOutInfoModel)
        bundle.putSerializable(Constants.DataParsing.DATE_TIME_MODEL, calendarDateTimeModel)
        bundle.putInt("flow", 1)
        uiHelper.openActivity(
            this, CheckoutCarBookingActivity::class.java, bundle
        )
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }

    @SuppressLint("SetTextI18n")
    override fun onCalendarDateTimeSelected(model: CalendarDateTimeModel) {
        calendarDateTimeModel = model
        if (calendarDateTimeModel.pickupDate.isNotEmpty()) {
            hideSnackBar()
            Log.d("calendarDateTimeModelpp...." ,calendarDateTimeModel.dropOffDateServer.toString())
            Log.d("calendarDateTimeModelpp" ,calendarDateTimeModel.dropOffDate.toString())
            val calendarPickup = Calendar.getInstance()
            val calendarDropOff = Calendar.getInstance()
            calendarPickup.time = SimpleDateFormat(Constants.UIDateTimeFormat).parse(calendarDateTimeModel.pickupDate) as Date
            calendarDropOff.time = SimpleDateFormat(Constants.UIDateTimeFormat).parse(calendarDateTimeModel.dropOffDate) as Date
       /*     calendarPickup.time = SimpleDateFormat(Constants.UIDateTimeFormat).parse(calendarDateTimeModel.pickUpDateServer) as Date
            calendarDropOff.time = SimpleDateFormat(Constants.UIDateTimeFormat).parse(calendarDateTimeModel.dropOffDateServer) as Date*/
            daysCount = (calendarDropOff[Calendar.DAY_OF_YEAR] - calendarPickup[Calendar.DAY_OF_YEAR]) + 1
            noOfDaysDiscount = checkDiscountDays(daysCount)

            preferenceHelper.setString(
                Constants.DataParsing.CUSTOM_DAYS,
                noOfDaysDiscount.toString()
            )

            setLayoutAccordingToDiscount(daysCount)
            loadDates(2)


            /* var models = GetCarBookingModel()
             models.car_id = carId
             models.city_id = 0
             models.pick_up = calendarDateTimeModel.pickUpDateServer
             models.drop_off = calendarDateTimeModel.dropOffDateServer

             if (lat == 0.0 && lng == 0.0) {
                 if (uiHelper.isPermissionAllowed(this, PERMISSION) && gpsTracker != null) {
                     models.lat = gpsTracker?.getLatitude()!!
                     models.lng = gpsTracker?.getLongitude()!!
                 }
             } else {
                 models.lat = lat
                 models.lng = lng
             }

             carCheckOutViewModel.getCheckoutDetail(
                 models,
                 true,
                 Constants.API.GET_CHECKOUT_DETAIL,
                 this
             )*/


        }else{
            Log.d("","")
        }

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
                    checkOutInfoModel.apply_delivery_fee = carInfoModel.locationSelected
                    binding.tvLocation.text = carInfoModel.street_address
                }

            }

        }
    }


    /**
     * Function for hiding snack bar
     * */
    private fun hideSnackBar() {
        if (snackBar != null) {
            if (snackBar?.isShown == true) snackBar?.dismiss()
        }
    }

    override fun popupButtonClick(value: Int) {
        if (value == 1) {
            if (!uiHelper.userLoggedIn(preferenceHelper)) {
                val bundle = Bundle()
                bundle.putInt(Constants.DataParsing.LOGIN_RESULT, CAR_DETAIL_ACTIVITY)
                uiHelper.openActivity(
                    this, LoginActivity::class.java, bundle
                )
            }
        }
    }

    override fun popupButtonClick(reportId: Int, reasons: Any) {

        carDetailViewModel.saveReportReason(
            reportId,
            model.car_detail_info_model.id,
            reasons.toString(),
            true,
            Constants.API.CANCEL_BOOKING,
            this
        )

    }


    override fun onBackPressed() {
        if (isForResult) {
            val intent = intent
            intent.putExtra(Constants.DataParsing.FAVORITE_FLAG, model.fav_flag)
            setResult(Activity.RESULT_OK, intent)
        }
        super.onBackPressed()
    }



}


