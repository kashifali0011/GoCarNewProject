package com.towsal.towsal.views.activities

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityResetPasswordBinding
import com.towsal.towsal.extensions.setGradientTextColor
import com.towsal.towsal.extensions.setValidationMessage
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.register.ResetPasswordModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.ForgotPasswordViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.regex.Pattern

/**
 * Activity class for reset password
 * */
class ResetPasswordActivity : BaseActivity(), OnNetworkResponse {
    lateinit var binding: ActivityResetPasswordBinding
    var bundle = Bundle()
    var phone = ""
    var resetPassToken = ""
    private val forgotPasswordViewModel: ForgotPasswordViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_reset_password)
        binding.activity = this
        setData()
        uiHelper.hideActionBar(supportActionBar)
        val colors = intArrayOf(
            Color.parseColor("#1420E4"),
            Color.parseColor("#0F0F7D")
        )
        binding.resetPasswordBtn.setGradientTextColor(
            colors = colors,
            x0 = -4.263f,
            y0 = -4.3f,
            x1 = 49.835f,
            y1 = 61.278f,
            positions = floatArrayOf(
                0f,
                1f
            )
        )
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        if (intent.extras != null) {
            bundle = intent.extras!!
            phone = bundle.getString(Constants.DataParsing.PHONE_NUMBER, "")
            resetPassToken = bundle.getString(Constants.DataParsing.RESET_PASS_TOKEN, "")
        }
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view) {
            binding.resetPasswordBtn -> {
                if (validatePassword()) {
                    val model = ResetPasswordModel()
                    model.phone = phone
                    model.password = binding.edtPassword.text.toString()
                    model.confirm_password = binding.edtPassword.text.toString()
                    model.token_rest_password = resetPassToken
                    forgotPasswordViewModel.resetPassword(
                        model,
                        true,
                        Constants.API.REST_PASSWORD,
                        this
                    )
                }
            }
        }
    }

    /**
     * Function for validate password
     * */
    private fun validatePassword(): Boolean {

        if (binding.edtPassword.text.toString().isEmpty()) {
            binding.edtPassword.requestFocus()
            setValidationMessage(
                messageId = R.string.enter_password_err_msg
            )
            return false
        } else if (binding.edtConfirmPassword.text.toString().isEmpty()) {
            binding.edtPassword.requestFocus()
            setValidationMessage(
                messageId = R.string.enter_confirm_password_err_msg
            )
            return false
        } else if (!Constants.PASSWORD_PATTERN.matcher(binding.edtPassword.text.toString()).matches()) {
            setValidationMessage(
                messageId = R.string.pass_err_msg
            )
            return false
        } else if (binding.edtPassword.text.toString() != binding.edtConfirmPassword.text.toString()) {
            binding.edtPassword.requestFocus()
            setValidationMessage(
                messageId = R.string.pass_not_match_err_msg
            )
            return false
        } else {
            return true
        }
    }


    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.REST_PASSWORD -> {
                uiHelper.showLongToastInCenter(this, response?.message)
                if (ForgotPasswordActivity.GOTO_ACTIVITY == ForgotPasswordActivity.GOTO_LOGIN) {
                    uiHelper.openAndClearActivity(this, LoginActivity::class.java)
                } else if (ForgotPasswordActivity.GOTO_ACTIVITY == ForgotPasswordActivity.GOTO_MAIN) {
                    MainActivity.GOTO_FRAGMENT = MainActivity.GOTO_PROFILE_FRAGMENT
                    uiHelper.openAndClearActivity(this, MainActivity::class.java)
                }
                ForgotPasswordActivity.GOTO_ACTIVITY = 0
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }
}