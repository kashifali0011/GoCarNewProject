package com.towsal.towsal.views.activities

import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.RadioButton
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityForgotPasswordBinding
import com.towsal.towsal.extensions.setGradientTextColor
import com.towsal.towsal.extensions.setValidationMessage
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.UserModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.ForgotPasswordViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for forgot password
 * */
class ForgotPasswordActivity : BaseActivity(), OnNetworkResponse {

    private var popupShown: Boolean = false
    lateinit var binding: ActivityForgotPasswordBinding
    private val forgotPasswordViewModel: ForgotPasswordViewModel by viewModel()
    private var popupWindow = PopupWindow()
    private var emailCheck = false

    lateinit var popupView: View

    companion object {
        var GOTO_ACTIVITY = 0
        var GOTO_LOGIN = 1
        var GOTO_MAIN = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forgot_password)
        binding.activity = this
        uiHelper.hideActionBar(supportActionBar)
        if (intent.extras != null) {
            GOTO_ACTIVITY = intent.extras!!.getInt(Constants.DataParsing.ID, 0)
        }

        val colors = intArrayOf(Color.parseColor("#1420E4"), Color.parseColor("#0F0F7D"))
        binding.verifyBtn.setGradientTextColor(colors)
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.clMethodSelection -> {
                showPopup(binding.clMethodSelection)
            }
            R.id.tvOption -> {
                showPopup(binding.clMethodSelection)
            }
            R.id.dropDown -> {
                showPopup(binding.clMethodSelection)
            }
            R.id.verifyBtn -> {
                if (binding.clMethodSelection.isVisible)
                    setValidationMessage(
                        "Authentication type",
                        messageId = R.string.select_email_or_phone
                    )
                else if (checkValidation()) {
                    val text: String = if (emailCheck) {
                        binding.edtEmail.text.toString()
                    } else {
                        binding.ccp.selectedCountryCodeWithPlus + binding.edtPhone.text.toString()
                    }

                    forgotPasswordViewModel.resetPasswordRequest(
                        text,
                        emailCheck,
                        true,
                        Constants.API.REST_PASS_REQUEST,
                        this
                    )
                }
            }
            R.id.email -> {
                emailCheck = true
                popupShown = true
                binding.clMethodSelection.isVisible = false
                binding.clPhone.isVisible = false
                binding.edtEmail.isVisible = true
                binding.edtEmail.isFocusableInTouchMode = true
                binding.edtEmail.requestFocus()
                binding.edtEmail.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                binding.edtEmail.hint = getString(R.string.enter_email)
                popupWindow.dismiss()
            }
            R.id.phone -> {
                emailCheck = false
                popupShown = true
                binding.clMethodSelection.isVisible = false
                binding.clPhone.isVisible = true
                binding.edtPhone.requestFocus()
                binding.edtEmail.isVisible = false
                binding.edtPhone.hint = getString(R.string.enter_phone)
                popupWindow.dismiss()
            }
        }
    }

    /**
     * Function for showing popup
     * */
    fun showPopup(v: View?) {
        popupView =
            View.inflate(this, R.layout.popup_filter_layout, null)
        popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        popupWindow.isOutsideTouchable = true
        popupWindow.showAsDropDown(v)
        val email = popupView.findViewById(R.id.email) as RadioButton
        val phone = popupView.findViewById(R.id.phone) as RadioButton
        email.isChecked = emailCheck || !popupShown
        phone.isChecked = !emailCheck && popupShown
        email.setOnClickListener(this::onClick)
        phone.setOnClickListener(this::onClick)
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.REST_PASS_REQUEST -> {
                uiHelper.showLongToastInCenter(this, response?.message)
                if (emailCheck) {
                    uiHelper.openAndClearActivity(this, LoginActivity::class.java)
                } else {
                    binding.model = Gson().fromJson(
                        uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                        UserModel::class.java
                    )
                    val bundle = Bundle()
                    bundle.putInt(Constants.DataParsing.USER_ID, binding.model?.id!!)
                    bundle.putString(Constants.DataParsing.PHONE_NUMBER, binding.model?.phone)
                    bundle.putString(
                        Constants.DataParsing.RESET_PASS_TOKEN,
                        binding.model?.token_rest_password
                    )
                    uiHelper.openActivity(this, OTPVerificationActivity::class.java, bundle)
                }
            }
        }
    }

    /**
     * Function for checking validation
     * */
    private fun checkValidation(): Boolean {
        if (emailCheck) {
            return if (binding.edtEmail.text.toString().isEmpty()) {
                binding.edtEmail.requestFocus()
                setValidationMessage(
                    "Invalid email",
                    messageId = R.string.enter_email_err_msg
                )
                false
            } else {
                true
            }
        } else {
            return if (binding.edtPhone.text.toString().isEmpty()) {
                binding.edtPhone.requestFocus()
                setValidationMessage(
                    "Invalid phone number",
                    messageId = R.string.enter_phone_err_msg
                )
                false
            } else {
                true
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }

}