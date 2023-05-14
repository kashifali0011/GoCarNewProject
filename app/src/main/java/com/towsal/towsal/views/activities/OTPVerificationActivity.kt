package com.towsal.towsal.views.activities

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityOTPVerificationBinding
import com.towsal.towsal.extensions.setGradientTextColor
import com.towsal.towsal.extensions.setValidationMessage
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.UserModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.OTPViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for Otp Verification
 * */
class OTPVerificationActivity : BaseActivity(), OnNetworkResponse {

    lateinit var binding: ActivityOTPVerificationBinding
    var phone = ""
    var bundle = Bundle()
    var userId = 0
    private val otpViewModel: OTPViewModel by viewModel()
    private var gotoLogin = 0
    private var resetPassToken = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_o_t_p_verification)
        binding.activity = this
        uiHelper.hideActionBar(supportActionBar)
        val colors = intArrayOf(
            getColor(R.color.send_msg_bg),
            getColor(R.color.text_receive_msg)
        )
        binding.btnVerify.setGradientTextColor(colors)
        setData()
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        if (intent.extras != null) {
            bundle = intent.extras!!
            phone = bundle.getString(Constants.DataParsing.PHONE_NUMBER, "")
            userId = bundle.getInt(Constants.DataParsing.USER_ID, 0)
            gotoLogin = bundle.getInt(Constants.DataParsing.GOTO_LOGIN, 0)
            binding.tvPhoneNumber.text = phone
            resetPassToken = bundle.getString(Constants.DataParsing.RESET_PASS_TOKEN, "")
        }
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.tvChangePhoneNumber -> {
                finish()
            }
            R.id.btnVerify -> {
                if (binding.otpView.otp.toString().isEmpty()) {
                    setValidationMessage(
                        "Otp",
                        R.string.otp_validation_err_msg
                    )
                } else {
                    val type: String = if (resetPassToken.isEmpty()) {
                        "registration"
                    } else {
                        "forget"
                    }

                    otpViewModel.verifyOTP(
                        userId,
                        binding.otpView.otp.toString(),
                        type,
                        Constants.API.OTP_VERIFY,
                        this,
                        true
                    )
                }
            }
            R.id.tvResend -> {
                otpViewModel.resendOTP(
                    userId,
                    Constants.API.RESEND_OTP,
                    this,
                    true
                )
            }
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.OTP_VERIFY -> {
                uiHelper.showLongToastInCenter(this, response?.message)
                if (gotoLogin == 1) {
                    val model = Gson().fromJson(
                        uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                        UserModel::class.java
                    )
                    preferenceHelper.saveObject(model, Constants.USER_MODEL)
                    uiHelper.showLongToastInCenter(this, response?.message)
                    uiHelper.openAndClearActivity(this, MainActivity::class.java)
                } else {
                    if (resetPassToken.isEmpty()) {
                        uiHelper.openAndClearActivity(this, LoginActivity::class.java)
                    } else {
                        val bundle = Bundle()
                        bundle.putString(Constants.DataParsing.PHONE_NUMBER, phone)
                        bundle.putString(
                            Constants.DataParsing.RESET_PASS_TOKEN,
                            resetPassToken
                        )
                        uiHelper.openActivity(this, ResetPasswordActivity::class.java, bundle)
                    }
                }
            }
            Constants.API.RESEND_OTP -> {
                uiHelper.showLongToastInCenter(this, response?.message)
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }
}