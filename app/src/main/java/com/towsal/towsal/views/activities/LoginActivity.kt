package com.towsal.towsal.views.activities


import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.PopupWindow
import android.widget.RadioButton
import androidx.cardview.widget.CardView
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.app.MainApplication
import com.towsal.towsal.databinding.ActivityLoginBinding
import com.towsal.towsal.extensions.setGradientTextColor
import com.towsal.towsal.extensions.setValidationMessage
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.UserModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.LoginViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

/**
 * Activity class for login
 * */
class LoginActivity : BaseActivity(), OnNetworkResponse {

    private lateinit var binding: ActivityLoginBinding
    private var popupWindow = PopupWindow()
    private lateinit var popupView: View
    private val loginViewModel: LoginViewModel by viewModel()
    private var emailCheck = false
    private var loginFrom = 0
    private var fcmToken = ""
    private var popupShown = false
    private var isPopUpVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.activity = this
        uiHelper.hideActionBar(supportActionBar)
        setGradientColors()
        setData()
        setKeyListeners()
        //aa
    }

    private fun setKeyListeners() {
        binding.edtEmail.setOnEditorActionListener { textView, actionId, keyEvent ->
            var returnValue = false
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                returnValue = if (
                    binding.edtEmail.text.toString().isNotEmpty()
                ) {
                    binding.edtPassword.requestFocus()
                    true
                } else {
                    binding.edtEmail.requestFocus()
                    setValidationMessage(
                        "Email",
                        "Email required!"
                    )
                    false
                }
            }
            returnValue
        }

        binding.edtPhone.setOnEditorActionListener { textView, actionId, keyEvent ->
            var returnValue = false
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                returnValue = if (
                    binding.edtPhone.text.toString().isNotEmpty()
                ) {
                    binding.edtPassword.requestFocus()
                    true
                } else {
                    binding.edtPhone.requestFocus()
                    setValidationMessage(
                        "Phone number",
                        "Phone number required!"
                    )
                    false
                }
            }
            returnValue
        }
        binding.edtPassword.setOnEditorActionListener { textView, actionId, keyEvent ->
            var returnValue = false
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                returnValue = if (
                    binding.edtPassword.text.toString().isNotEmpty()
                ) {
                    performLoginAction()
                    true
                } else {
                    binding.edtPassword.requestFocus()
                    setValidationMessage(
                        messageId = "Password required!"
                    )
                    false
                }
            }
            returnValue
        }
    }

    private fun setGradientColors() {
        val colors = intArrayOf(Color.parseColor("#1420E4"), Color.parseColor("#0F0F7D"))
        binding.login.setGradientTextColor(colors)
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        if (intent.extras != null) {
            loginFrom = intent.extras!!.getInt(Constants.DataParsing.LOGIN_RESULT, 0)
        }
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            // Get new FCM registration token
            fcmToken = task.result
        })

    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.clMethodSelection -> {
                if (!popupShown) {
                    showPopup(binding.clMethodSelection)
                }
            }
            R.id.tvOption -> {
                if (!popupShown ) {
                    showPopup(binding.clMethodSelection)
                }
            }
            R.id.dropDown -> {
                if (!isPopUpVisible) {
                    showPopup(binding.clMethodSelection)
                }
                else {
                    isPopUpVisible = false
                }
            }
            R.id.skip -> {
                MainActivity.firstTimeCalled = false
                MainActivity.GOTO_FRAGMENT = 0
                //redirecting straight to the main activity not on car search
                uiHelper.openAndClearActivity(this, MainActivity::class.java)
            }
            R.id.login -> {
                performLoginAction()
            }
            R.id.register -> {
                uiHelper.openActivity(this, RegistrationActivity::class.java)
            }
            R.id.forgotPassword -> {
                val bundle = Bundle()
                bundle.putInt(Constants.DataParsing.ID, ForgotPasswordActivity.GOTO_LOGIN)
                uiHelper.openActivity(this, ForgotPasswordActivity::class.java, bundle)
            }
            R.id.email -> {
                popupShown = true
                emailCheck = true
                binding.edtEmail.isVisible = true
                binding.clMethodSelection.isVisible = false
                binding.clPhone.isVisible = false
                popupWindow.dismiss()
            }
            R.id.phone -> {
                popupShown = true
                emailCheck = false
                binding.clPhone.isVisible = true
                binding.clMethodSelection.isVisible = false
                binding.edtEmail.isVisible = false
                popupWindow.dismiss()
            }
        }
    }

    private fun performLoginAction() {
        if (binding.clMethodSelection.isVisible) {
            setValidationMessage(
                "Authentication type",
                R.string.select_email_or_phone
            )
        } else if (checkValidation()) {
            //changing button transition
            val userModel = UserModel()
            if (emailCheck) {
                userModel.email = binding.edtEmail.text.toString()
            } else {
                userModel.phone =
                    binding.ccp.selectedCountryCodeWithPlus + binding.edtPhone.text.toString()
            }
            userModel.password = binding.edtPassword.text.toString()

            userModel.timeZone = Calendar.getInstance().timeZone.id
            loginViewModel.getLoginData(
                userModel, fcmToken, true,
                Constants.API.LOGIN,
                this
            )
        }
    }

    /**
     * Function for checking validations
     * */
    private fun checkValidation(): Boolean {
        if (binding.edtPassword.text.toString().isEmpty()) {
            binding.edtPassword.requestFocus()
            setValidationMessage(
                messageId = R.string.password_err_msg
            )
            return false
        } else if (emailCheck) {
            return if (binding.edtEmail.text.toString().isEmpty()) {
                binding.edtEmail.requestFocus()
                setValidationMessage(
                    title = "Email",
                    messageId = R.string.email_pass_err_msg
                )
                false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.edtEmail.text.toString().trim())
                    .matches()
            ) {
                binding.edtEmail.requestFocus()
                setValidationMessage(
                    title = "Email",
                    messageId = R.string.inalid_email_err_msg
                )
                false
            } else {
                true
            }
        } else {
            return if (binding.edtPhone.text.toString().isEmpty()) {
                binding.edtPhone.requestFocus()
                setValidationMessage(
                    title = "Phone number",
                    messageId = R.string.enter_phone_err_msg
                )
                false
            } else {
                true
            }
        }
    }

    /**
     * Function for showing popup
     * */
    private fun showPopup(v: View?) {
        isPopUpVisible = true
        popupView = View.inflate(this, R.layout.popup_filter_layout, null)
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
            Constants.API.LOGIN -> {
                val model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    UserModel::class.java
                )
                binding.model = model
                if (model.is_email_verified) {
                    preferenceHelper.saveObject(binding.model!!, Constants.USER_MODEL)
                 //   uiHelper.showLongToastInCenter(this, response?.message)
                    if (loginFrom == CarDetailsActivity.CAR_DETAIL_ACTIVITY) {
                        //coming from car details screen
                        finish()
                    } else {
                        if (MainActivity.GOTO_FRAGMENT == 0) {
                            MainActivity.firstTimeCalled = false
                            //redirecting straight to the main activity not on car search
                        } else {
                            MainActivity.GOTO_FRAGMENT = 0
                        }
                        uiHelper.openAndClearActivity(this, MainActivity::class.java)

                    }
                    MainApplication.isLoggedIn = true
                } else {
                    Log.d("loggin", "comesHere")
                 //   uiHelper.showLongToastInCenter(this, response?.message)
                    val bundle = Bundle()
                    bundle.putInt(Constants.DataParsing.USER_ID, model.id)
                    bundle.putString(Constants.DataParsing.PHONE_NUMBER, model.phone)
                    bundle.putInt(Constants.DataParsing.GOTO_LOGIN, 1)
                    if (loginFrom == Constants.LoginFrom.SearchHomeFragment) {
                        preferenceHelper.saveObject(model, Constants.USER_MODEL)
                        MainApplication.isLoggedIn = true
                        finish()
                    } else
                        uiHelper.openActivity(this, EmailVerificationActivity::class.java, bundle)
                }
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }
}