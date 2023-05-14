package com.towsal.towsal.views.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityBookingConfirmationBinding
import com.towsal.towsal.network.serializer.checkoutcarbooking.BookingConfirmationResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil

/**
 * Activity class for booking confirmation
 * */
class BookingConfirmationActivity : BaseActivity() {

    lateinit var binding: ActivityBookingConfirmationBinding
    lateinit var thankYouModel: BookingConfirmationResponseModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_booking_confirmation)
        binding.activity = this
        uiHelper.hideActionBar(supportActionBar)
        setData()
    }

    /**
     * Function for setting up data in views
     * */
    @SuppressLint("SetTextI18n")
    private fun setData() {
        if (intent.extras != null) {
            thankYouModel =
                intent.extras!!.getSerializable(Constants.DataParsing.MODEL) as BookingConfirmationResponseModel
            binding.tvCarName.text = getString(
                R.string.car_place_holder,
                thankYouModel.make,
                thankYouModel.model,
                thankYouModel.year
            )
            binding.tvHostName.text = thankYouModel.hosted_by
            uiHelper.glideLoadImage(
                this,
                BuildConfig.IMAGE_BASE_URL + thankYouModel.hostProfileImage,
                binding.ivUserProfileImage
            )
            try {
                binding.tvStartDate.text = DateUtil.changeDateFormat(
                    Constants.ServerDateTimeFormat,
                    Constants.UIDateTimeFormat,
                    thankYouModel.pickUp,
                )
                binding.tvFinishDate.text = DateUtil.changeDateFormat(
                    Constants.ServerDateTimeFormat,
                    Constants.UIDateTimeFormat,
                    thankYouModel.dropOff,

                    )

            } catch (e: Exception) {
                e.printStackTrace()

            }
        }
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.home -> {
                uiHelper.openAndClearActivity(this, MainActivity::class.java)
            }
        }
    }
}