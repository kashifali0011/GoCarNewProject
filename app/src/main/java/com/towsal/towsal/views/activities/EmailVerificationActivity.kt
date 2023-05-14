package com.towsal.towsal.views.activities

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.app.MainApplication
import com.towsal.towsal.databinding.ActivityEmailVerificationBinding
import com.towsal.towsal.extensions.setGradientTextColor
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.UserModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.MainScreenViewModel
import com.towsal.towsal.viewmodel.RegistrationViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for email verification
 * */
class EmailVerificationActivity : BaseActivity(), OnNetworkResponse, PopupCallback {

    lateinit var binding: ActivityEmailVerificationBinding
    private val registrationViewModel: RegistrationViewModel by viewModel()
    val homeViewModel: MainScreenViewModel by viewModel()
    var email = ""
    var userId = 0
    private var fcmToken = ""
    private var token = ""
    private var model = UserModel()

    companion object {
        var isAlreadyVerified: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_email_verification)
        binding.activity = this
        uiHelper.hideActionBar(supportActionBar)
        val colors = intArrayOf(Color.parseColor("#1420E4"), Color.parseColor("#0F0F7D"))
        binding.tvResendEmail.setGradientTextColor(colors)

        if (intent!!.extras != null) {
            email = intent.extras!!.getString(Constants.DataParsing.Email, "")
            userId = intent.extras!!.getInt(Constants.DataParsing.USER_ID, 0)
        }

        binding.tvEmail.text = email
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    /**
     * Function for handling new intent
     * */
    private fun handleIntent(intent: Intent?) {
        if (intent != null) {
            val data = intent.data
            if (data != null && data.isHierarchical) {
                val uri = intent.dataString
                token = Uri.parse(uri).getQueryParameter("token").toString()
                email = Uri.parse(uri).getQueryParameter("email").toString()
                binding.tvEmail.text = email
                if (uiHelper.userLoggedIn(preferenceHelper)) {
                    val userModel = preferenceHelper.getObject(
                        Constants.USER_MODEL,
                        UserModel::class.java
                    ) as UserModel
                    if (userModel.email == email) {
                        uiHelper.openActivity(this, MainActivity::class.java)
                    } else {
                        Handler(Looper.getMainLooper()).postDelayed({
                            uiHelper.showPopup(
                                this,
                                this,
                                R.string.continue_,
                                R.string.cancel_,
                                R.string.login_alert,
                                R.string.new_account_confirmation_msg,
                                false
                            )
                        }, 300)
                    }
                } else {
                    verifyEmail()
                }
            }
        }
    }

    /**
     * Function for verification of email
     * */
    private fun verifyEmail() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            // Get new FCM registration token
            Log.d("fcmmmToken", "fcm token = " + task.result)
            fcmToken = task.result
            registrationViewModel.verifyEmail(
                fcmToken,
                token,
                true,
                Constants.API.VERIFY_EMAIL,
                this
            )
        })

    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.tvOpenEmailBox -> {

                sendEmail()

            }
            R.id.tvResendEmail -> {
                registrationViewModel.resendEmail(userId, true, Constants.API.RESEND_EMAIL, this)
            }
        }
    }

    /**
     * Function for sending email
     * */
    private fun sendEmail() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_APP_EMAIL)
        try {
            //start email intent
            startActivity(Intent.createChooser(intent, "Email"))
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.RESEND_EMAIL -> {
                uiHelper.showLongToastInCenter(this, response?.message)
            }
            Constants.API.VERIFY_EMAIL -> {
                uiHelper.showLongToastInCenter(this, response?.message)
                preferenceHelper.clearKey(Constants.USER_MODEL)
                uiHelper.openAndClearActivity(this, LoginActivity::class.java)

            }
            Constants.API.LOGOUT -> {
                verifyEmail()
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        if (response?.code ==  Constants.ResponseCodes.ALREADY_VERIFY){
            preferenceHelper.clearKey(Constants.USER_MODEL)
            uiHelper.openAndClearActivity(this, LoginActivity::class.java)
        }
        uiHelper.showLongToastInCenter(this, response?.message)

    }

    override fun popupButtonClick(value: Int) {
        if (value == 1) {
            homeViewModel.logout(true, Constants.API.LOGOUT, this)
        } else {
            uiHelper.openActivity(this, MainActivity::class.java)
        }
    }

    override fun popupButtonClick(value: Int, text_id_model: Any) {

    }
}