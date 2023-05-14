package com.towsal.towsal.views.activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityReceiptBinding
import com.towsal.towsal.extensions.*
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.cardetails.CarFullDetailModel
import com.towsal.towsal.network.serializer.carlist.CarInformationModel
import com.towsal.towsal.network.serializer.checkoutcarbooking.ReceiptInvoiceModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil
import com.towsal.towsal.viewmodel.CheckoutCarBookingViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

/**
 * Activity class for booking receipt
 * */
class ReceiptActivity : BaseActivity(), OnNetworkResponse, View.OnClickListener, PopupCallback {

    lateinit var binding: ActivityReceiptBinding
    private val carCheckOutViewModel: CheckoutCarBookingViewModel by viewModel()
    private var checkOutId: Int = -1
    lateinit var responseModel: ReceiptInvoiceModel
    private var carInfoModel = CarInformationModel()
    private var model = CarFullDetailModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_receipt)
        binding.activity = this
        actionBarSetting()
        setGradientColors()
        if (intent.extras != null) {
            checkOutId = intent.getIntExtra(Constants.DataParsing.ID, 0)
        }

        carCheckOutViewModel.getReceiptInvoiceDetails(
            checkOutId,
            0,
            true,
            Constants.API.RECEIPT_INVOICE_DETAILS,
            this
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
    override fun onClick(v: View?) {
        v!!.preventDoubleClick()
        when (v) {
            binding.tvGuestName -> {
                val bundle = Bundle()
                bundle.putInt(Constants.DataParsing.USER_ID, binding.model!!.guestId!!)
                uiHelper.openActivity(this, UserProfileActivity::class.java, bundle)
            }

            binding.tvAdditionalCharges -> {
                openChargesPopUp(
                    responseModel.charges, false, responseModel.charges.remainingAmount
                )
            }
            binding.tvPickUpAndReturn ->{

                if (responseModel.pinLat != 0.0){
                    openMap(
                        responseModel.pinLat,
                        responseModel.pinLong
                    )
                }
               /* carInfoModel.isHideChnageLocation = true
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
            }
            binding.tvAccruedFines -> {
                openAccruedFinesReceipt(
                    responseModel.failureToReport,
                    responseModel.hostNoShow,

                    responseModel.cancellationFine,
                    responseModel.totalFine

                )

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
        binding.tvGuestName.text = responseModel.hostedTo
        binding.tvCarOwnerName.text =
            getString(R.string.owner_name, responseModel.hostedBy.toString().split(" ")[0])
        binding.tvTripId.text = getString(R.string.trip_id, responseModel.checkoutId)
        binding.tvCarName.text = "${responseModel.make} ${responseModel.model}"
        binding.tvYear.text =  "(${responseModel.year})"

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


        if (responseModel.hostNoShow != "0.00" || responseModel.failureToReport != "0.00"  || responseModel.cancellationFine != "0.00"){
            binding.tvAccruedFines.isVisible = true
            binding.viewAccruedFines.isVisible = true
        }else{
            binding.tvAccruedFines.isVisible = false
            binding.viewAccruedFines.isVisible = false
        }

        binding.clDistanceIncluded.isVisible = responseModel.distance < 999999
        binding.clTotalKilometers.isVisible = binding.clDistanceIncluded.isVisible

        binding.tvTotalKilometersTitle.text =
            resources.getString(R.string.total_kilometers_, responseModel.distance.toString())
        binding.tvDistanceIncluded.text =
            responseModel.distance.toString() + " " + getString(R.string.km_small)

        if (responseModel.discount != "0.00") {
            binding.clDiscount.visibility = View.VISIBLE
            binding.tvDiscountTitle.text =
                responseModel.discountDayValue.toString() + "+ " + resources.getString(R.string.days)
                    .lowercase(
                        Locale.getDefault()
                    ) + " " + resources.getString(R.string.discount).lowercase(
                    Locale.getDefault()
                )
            binding.tvDiscountAmount.text = responseModel.discount + " SAR"
               /* resources.getString(
                    R.string.sar_day_,
                    responseModel.discount
                )*/


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

        binding.tvTotalPerDayAmount.text =
            resources.getString(
                R.string.sar_day_,
                responseModel.totalPerDay
            )


        binding.tvTripTotal.text =
            resources.getString(
                R.string.sar_day_,
                responseModel.newTripTotal
            )


        binding.tvTotalDaysRentTitle.text =
            resources.getQuantityString(R.plurals.day_trip, responseModel.numOfDays, responseModel.numOfDays)
        binding.tvDistanceIncluded.text =
            responseModel.distance.toString() + " " + getString(R.string.km_small)
        binding.tvTripId.text = getString(R.string.trip_id, responseModel.checkoutId.toString())
        binding.clAdditionalCharges.isVisible =
            responseModel.charges.is_extended != 0 && responseModel.charges.is_extended != -1
        binding.tvTotalAmount.text = resources.getString(R.string.sar_, responseModel.total)
        binding.tvGoCarFeesAmount.text = resources.getString(R.string.sar_, responseModel.gocarFee)
        binding.tvEarnedAmount.text = resources.getString(R.string.sar_, responseModel.earned)
        binding.tvGoCarFees.text =
            getString(R.string.gocar_fee_20_, "${responseModel.goCarFeePercentage}%")
        binding.clAdditionalCharges.isVisible =
            responseModel.charges.is_extended != 0 && responseModel.charges.is_extended != -1

        binding.ivCarImage.loadImage(
            BuildConfig.IMAGE_BASE_URL + responseModel.carImage
        )
        if (responseModel.isCompany){
            binding.clTotalBeforeTax.isVisible = true
            binding.clVat.isVisible = true
            var vatPercentage = responseModel.vatPercentage
            binding.tvVat.text = "VAT ($vatPercentage%)"
            binding.tvVatAmount.text = responseModel.hostVatAmount.toString() + " SAR"
            var totalBeforeTex = responseModel.earned?.toDouble()!! - responseModel.hostVatAmount?.toDouble()!!
            binding.tvTotalBeforeTaxAmount.text = "$totalBeforeTex SAR"
        }else{
           binding.clTotalBeforeTax.isVisible = false
           binding.clVat.isVisible = false
        }
    }

    /**
     * Function for setting up action bar
     * */
    private fun actionBarSetting() {
        binding.layoutToolBar.setAsGuestToolBar(
            titleId = R.string.receipt,
            actionBar = supportActionBar,
            toolBarBg = R.drawable.bg_gradient_toolbar_host,
            arrowColor = R.color.white
        )
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

        binding.tvEarningsTitle.setGradientTextColor(
            colors = intArrayOf(
                Color.parseColor("#FF3131E2"),
                Color.parseColor("#FF42AEEB")
            ),
            x0 = -4.13f,
            y0 = -4f,
            x1 = 241.48f,
            y1 = 253.25f,
            floatArrayOf(
                0f,
                1f
            )
        )

    }
}