package com.towsal.towsal.views.activities

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityInvoiceBinding
import com.towsal.towsal.extensions.*
import com.towsal.towsal.helper.SetActionBar
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.cardetails.CarFullDetailModel
import com.towsal.towsal.network.serializer.carlist.CarInformationModel
import com.towsal.towsal.network.serializer.checkoutcarbooking.ReceiptInvoiceModel
import com.towsal.towsal.network.serializer.settings.CarLocationDeliveryModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil
import com.towsal.towsal.viewmodel.CheckoutCarBookingViewModel
import com.towsal.towsal.views.activities.UserInformationActivity.Companion.carInfoModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

/**
 * Activity class for booking invoice
 * */
class InvoiceActivity : BaseActivity(), OnNetworkResponse, View.OnClickListener, PopupCallback {

    lateinit var binding: ActivityInvoiceBinding
    private val carCheckOutViewModel: CheckoutCarBookingViewModel by viewModel()
    private var checkOutId: Int = -1
    lateinit var responseModel: ReceiptInvoiceModel
    private var carInfoModel = CarInformationModel()

    private var model = CarFullDetailModel()

    var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_invoice)
        binding.activity = this
        binding.layoutToolBar.setAsGuestToolBar(
            titleId = R.string.invoice,
            actionBar = supportActionBar,
            toolBarBg = R.drawable.bg_sky_blue_gradient,
            arrowColor = R.color.white
        )
        if (intent.extras != null) {
            checkOutId = intent.getIntExtra(Constants.DataParsing.ID, 0)
        }

        setGradientColors()

        carCheckOutViewModel.getReceiptInvoiceDetails(
            checkOutId,
            1,
            true,
            Constants.API.RECEIPT_INVOICE_DETAILS,
            this
        )
      /*  val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)*/
//        actionBarSetting()
    }

    private fun setGradientColors() {
        binding.tvDiscountTitle.setGradientTextColor(
            intArrayOf(
                Color.parseColor(
                    "#0F0F7D"
                ),
                Color.parseColor(
                    "#1420E4"
                )
            )
        )

        binding.tvDiscountAmount.setGradientTextColor(
            intArrayOf(
                Color.parseColor(
                    "#0F0F7D"
                ),
                Color.parseColor(
                    "#1420E4"
                )
            )
        )
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.RECEIPT_INVOICE_DETAILS -> {
                setData(response)
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response!!.message)
    }

    /**
     * Function for click listeners
     * */
    override fun onClick(view: View) {
        when (view) {
            binding.tvHostName -> {
                val bundle = Bundle()
                bundle.putInt(Constants.DataParsing.USER_ID, binding.model!!.hostedId!!)
                uiHelper.openActivity(this, UserProfileActivity::class.java, bundle)
            }
            binding.tvAdditionalCharges -> {
                openChargesPopUp(
                    binding.model?.charges!!, true, responseModel.charges.remainingAmount
                )
            }

            binding.tvAccruedFines -> {
                openAccruedFines(
                    responseModel.improperReturnFine,
                    responseModel.lateReturnFine,
                    responseModel.totalFine

                )
            }
            binding.tvPickUpAndReturn ->{

                if (responseModel.pinLat != 0.0) {
                    openMap(
                        responseModel.pinLat,
                        responseModel.pinLong
                    )
                }

                  /*  carInfoModel.isHideChnageLocation = true
                    carInfoModel.fromActivity = true
                    carInfoModel.pin_lat = responseModel.pinLat
                    carInfoModel.pin_long = responseModel.pinLong



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
                    )*/
              //  }
            }
        }
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData(response: BaseResponse?) {
        responseModel = Gson().fromJson(
            uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
            ReceiptInvoiceModel::class.java
        )
        binding.model = responseModel

        binding.tvHostName.text = (responseModel.hostedBy?.trim() ?: "")
        binding.tvCarOwnerName.text =
            getString(R.string.owner_name, (responseModel.hostedBy ?: "").split(" ")[0])
        binding.tvCarName.text = "${responseModel.make} ${responseModel.model}"
        binding.tvYear.text =  "(${responseModel.year})"
        binding.ivCarImage.loadImage(
            BuildConfig.IMAGE_BASE_URL + responseModel.carImage
        )
        binding.tvBookedAt.text = "${getString(R.string.booked)} ${
            DateUtil.changeDateFormat(
                Constants.ServerDateTimeFormat,
                Constants.CUSTOM_DATE_TIME_FORMAT,
                responseModel.bookedAt
            )
        }"
        binding.tvStartDate.text = DateUtil.changeDateFormat(
            Constants.ServerDateTimeFormat,
            Constants.UIDateFormat,
            responseModel.pickUp
        )
        binding.tvStartTime.text = DateUtil.changeDateFormat(
            Constants.ServerDateTimeFormat,
            Constants.UITimeFormat,
            responseModel.pickUp
        )
        binding.tvFinishDate.text = DateUtil.changeDateFormat(
            Constants.ServerDateTimeFormat,
            Constants.UIDateFormat,
            responseModel.dropOff
        )
        binding.tvFinishTime.text = DateUtil.changeDateFormat(
            Constants.ServerDateTimeFormat,
            Constants.UITimeFormat,
            responseModel.dropOff
        )


        binding.tvLocation.text = responseModel.pickUpLocation

        binding.tvTotalDaysRentTitle.text =
            resources.getQuantityString(R.plurals.day_trip, responseModel.numOfDays, responseModel.numOfDays)


        if (responseModel.discount != "0.00") {
            binding.clDiscount.visibility = View.VISIBLE
            binding.tvDiscountTitle.text =
                responseModel.discountDayValue.toString() + "+ " + resources.getString(R.string.days)
                    .lowercase(
                        Locale.getDefault()
                    ) + " " + resources.getString(R.string.discount).lowercase(
                    Locale.getDefault()
                )
            binding.tvDiscountAmount.text =
                responseModel.discount + " SAR"

        } else {
            binding.clDiscount.visibility = View.GONE
        }
        binding.tvPerDayAmount.text =
            resources.getString(
                R.string.sar_day_,
                responseModel.perDay
            )
        binding.tvTripFeeAmount.text =
            resources.getString(
                R.string.sar_day_,
                responseModel.tripFee
            )

     //   binding.tvTripFeeAmount.text = responseModel.tripFee +" "+ getString(R.string.sar)

        binding.tvProtectionFeeAmount.text = responseModel.protectionFee +" "+ getString(R.string.sar)

        if (responseModel.improperReturnFine != "0.00" || responseModel.lateReturnFine != "0.00"){
            binding.tvAccruedFines.isVisible = true
            binding.viewAccruedFines.isVisible = true
        }else{
            binding.tvAccruedFines.isVisible = false
            binding.viewAccruedFines.isVisible = false
        }

        binding.tvTotalPerDayAmount.text =
            resources.getString(
                R.string.sar_day_,
                responseModel.totalPerDay
            )

        binding.tvTripTotal.text = responseModel.subTotal +" "+ getString(R.string.sar)

        Log.d("responseModel.deliveryFee" , responseModel.deliveryFee.toString())

        if (responseModel.deliveryFee == "0" || responseModel.deliveryFee == "0.00"){
            binding.clDelivery.isVisible = false

        }else{
            binding.tvDeliveryTotal.text = responseModel.deliveryFee +" "+ getString(R.string.sar)
            binding.clDelivery.isVisible = true
        }

        binding.tvVatTitle.text = "VAT (" + responseModel.vatPercentage + "%)"
        binding.tvVatTotal.text = responseModel.vatAmount +" "+ getString(R.string.sar)




        binding.clDistanceIncluded.isVisible = responseModel.distance < 999999
        binding.clTotalKilometers.isVisible = binding.clDistanceIncluded.isVisible
        binding.tvTotalDaysRentTitle.text =
            resources.getQuantityString(R.plurals.day_trip, responseModel.numOfDays, responseModel.numOfDays)
        binding.tvDistanceIncluded.text =
            responseModel.distance.toString() + " " + getString(R.string.km_small)
        binding.tvTripId.text = getString(R.string.trip_id, responseModel.checkoutId.toString())

        binding.clAdditionalCharges.isVisible =
            responseModel.charges.is_extended != 0 && responseModel.charges.is_extended != -1
        binding.tvTotalAmount.text = resources.getString(R.string.sar_, responseModel.total)

        binding.clAdditionalCharges.isVisible =
            responseModel.charges.is_extended != 0 && responseModel.charges.is_extended != -1

        binding.ivCarImage.loadImage(
            BuildConfig.IMAGE_BASE_URL + responseModel.carImage
        )
    }






}