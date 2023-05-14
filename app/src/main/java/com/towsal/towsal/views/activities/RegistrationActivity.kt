package com.towsal.towsal.views.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityRegistrationBinding
import com.towsal.towsal.extensions.hideKeyboard
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.extensions.setGradientTextColor
import com.towsal.towsal.extensions.setValidationMessage
import com.towsal.towsal.interfaces.TermConditionCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.UserModel
import com.towsal.towsal.network.serializer.register.RegisterModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.RegistrationViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import java.util.regex.Pattern

/**
 * Activity class for registration
 * */
class RegistrationActivity : BaseActivity(), OnNetworkResponse, TermConditionCallback {
    lateinit var binding: ActivityRegistrationBinding
    private val registrationViewModel: RegistrationViewModel by viewModel()
    var userModel = RegisterModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setUpView()
    }

    /**
     * Function for setting up views
     * */
    private fun setUpView() {
        binding = DataBindingUtil.setContentView(this, R.layout.activity_registration)
        binding.activity = this

       setTabBar(getString(R.string.register))
        applyShaderColors()
        addClickListeners()
    }

    private fun applyShaderColors() {
        val colors = intArrayOf(Color.parseColor("#1420E4"), Color.parseColor("#0F0F7D"))
        val colors2 = intArrayOf(
            Color.parseColor("#14E3E4"),
            Color.parseColor("#4CA9FE")
        )
        binding.submit.setGradientTextColor(colors)
       /* binding.accept.setGradientTextColor(colors)
        binding.decline.setGradientTextColor(colors2)*/
    }

    private fun addClickListeners() {
        binding.accept.setOnClickListener(this::onClick)
        binding.decline.setOnClickListener(this::onClick)
        binding.acceptTerms.setOnClickListener(this::onClick)
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            binding.termCondition.id -> {
                hideKeyboard(activity)
                setTabBar(getString(R.string.terms_of_services))
                binding.clPopUp.isVisible = true
                binding.scrollView.isVisible = false

            }
            binding.submit.id -> {

                if (binding.edtPhone.text.toString().isEmpty()) {
                    binding.edtPhone.requestFocus()
                    setValidationMessage(
                        "Phone number",
                        R.string.enter_phone_err_msg
                    )
                } else if (binding.edtEmail.text.toString().isEmpty()) {
                    binding.edtEmail.requestFocus()
                    setValidationMessage(
                        "Email",
                        R.string.enter_email_err_msg
                    )
                } else if (!uiHelper.isValidEmail(binding.edtEmail.text.toString())) {
                    binding.edtEmail.requestFocus()
                    setValidationMessage(
                        "Email",
                        R.string.inalid_email_err_msg
                    )
                } else if (binding.edtFirstName.text.toString().isEmpty()) {
                    binding.edtFirstName.requestFocus()
                    setValidationMessage(
                        "First name",
                        R.string.first_name_err_msg
                    )
                } else if (binding.edtLastName.text.toString().isEmpty()) {
                    binding.edtLastName.requestFocus()
                    setValidationMessage(
                        "Last name",
                        R.string.last_name_err_msg
                    )
                } else if (binding.edtPassword.text.toString().isEmpty()) {
                    setValidationMessage(
                        "Password",
                        R.string.enter_password_err_msg
                    )
                } else if (!Constants.PASSWORD_PATTERN.matcher(binding.edtPassword.text.toString())
                        .matches()
                ) {
                    binding.edtPassword.requestFocus()
                    setValidationMessage(
                        "Password",
                        R.string.pass_err_msg
                    )
                } else if (!binding.checkTermCondition.isChecked) {
                    setValidationMessage(
                        "Terms of service",
                        R.string.term_condition_err_msg
                    )
                } else {
                    registration()
                }

            }

            binding.accept.id -> {
                setTabBar(getString(R.string.register))
                binding.checkTermCondition.isChecked = true
                binding.clPopUp.isVisible = false
                binding.scrollView.isVisible = true
            }

            binding.decline.id -> {
                setTabBar(getString(R.string.register))
                binding.checkTermCondition.isChecked = false
                binding.clPopUp.isVisible = false
                binding.scrollView.isVisible = true
            }
            binding.acceptTerms.id ->{
                var builder = CustomTabsIntent.Builder()
                val url = "https://gocar.sa/help"
                val customTabsIntent: CustomTabsIntent = builder.build()
                customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                customTabsIntent.intent.`package` = "com.android.chrome"
                customTabsIntent.launchUrl(applicationContext, url.toUri())
            }
        }
    }

    override fun onBackPressed() {

        if (binding.clPopUp.isVisible) {
            setTabBar(getString(R.string.register))
            binding.clPopUp.isVisible = false
            binding.scrollView.isVisible = true
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Function for registering user
     * */
    private fun registration() {
        userModel = RegisterModel()
        userModel.firstName = binding.edtFirstName.text.toString()
        userModel.lastName = binding.edtLastName.text.toString()
        userModel.phone = binding.ccp.selectedCountryCodeWithPlus + binding.edtPhone.text.toString()
        userModel.email = binding.edtEmail.text.toString()
        userModel.password = binding.edtPassword.text.toString()
        userModel.user_type = Constants.USER_TYPE_GUEST
        userModel.is_accept_tc = 1
        userModel.timeZone = Calendar.getInstance().timeZone.id
        registrationViewModel.registerUser(userModel, true, Constants.API.REGISTER, this)

    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.REGISTER -> {
                binding.model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    UserModel::class.java
                )
                val bundle = Bundle()
                bundle.putInt(Constants.DataParsing.USER_ID, binding.model?.id!!)
                bundle.putString(Constants.DataParsing.PHONE_NUMBER, binding.model?.phone)
                bundle.putString(Constants.DataParsing.Email, binding.model?.email)
                uiHelper.openActivity(this, EmailVerificationActivity::class.java, bundle)
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }

    override fun onTermAccept(check: Boolean) {
        binding.checkTermCondition.isChecked = check
    }
    private fun setTabBar(stata: String){
        binding.layoutToolBar.setAsGuestToolBar(
            stata,
            supportActionBar
        )
     }
}