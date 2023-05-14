package com.towsal.towsal.views.activities


import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityChangePasswordBinding
import com.towsal.towsal.extensions.setGradientTextColor
import com.towsal.towsal.extensions.setValidationMessage
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.profile.GetAccountResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.ChangePasswordViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.regex.Pattern

/**
 * Activity class for changing password
 * */
class ChangePasswordActivity : BaseActivity(), OnNetworkResponse {

    lateinit var binding: ActivityChangePasswordBinding
    private val changePassViewModel: ChangePasswordViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_change_password)
        binding.activity = this
        supportActionBar?.hide()
        setData()
    }


    private fun setData() {
        val colors = intArrayOf(Color.parseColor("#1420E4"), Color.parseColor("#0F0F7D"))
        binding.submit.setGradientTextColor(colors)
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.submit -> {
                if (validatePassword()) {
                    changePassViewModel.changePassword(
                        binding.oldPassEdt.text.toString(),
                        binding.newPassEdt.text.toString(),
                        true,
                        Constants.API.CHANGE_PASSWORD,
                        this
                    )
                }
            }

        }
    }

    /**
     * Function for validating password
     * */
    private fun validatePassword(): Boolean {
        if (binding.oldPassEdt.text.toString().isEmpty()) {
            setValidationMessage(
                messageId = R.string.old_pass_err_msg
            )
            return false
        } else if (binding.newPassEdt.text.toString().isEmpty()) {
            setValidationMessage(
                messageId = R.string.new_pass_err_msg
            )
            return false
        } else if (binding.confirmPasswordEdt.text.toString().isEmpty()) {
            setValidationMessage(
                messageId = R.string.confirm_pass_err_msg
            )
            return false
        } else if (!Constants.PASSWORD_PATTERN.matcher(binding.newPassEdt.text.toString()).matches()) {
            setValidationMessage(
                messageId = R.string.pass_err_msg
            )
            return false
        } else if (binding.newPassEdt.text.toString() != binding.confirmPasswordEdt.text.toString()
        ) {
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
            Constants.API.CHANGE_PASSWORD -> {
                val model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    GetAccountResponseModel::class.java
                )
                if (model.account.password) {
                    uiHelper.showLongToastInCenter(this, response?.message)
                    finish()
                } else if (model.account.error_type == 1) {
                    //   binding.oldPassEdt.setBackgroundResource(R.drawable.red_editext_bg)
                    uiHelper.showLongToastInCenter(this, response?.message)
                } else if (model.account.error_type == 2) {
                    /*    binding.oldPassEdt.setBackgroundResource(R.drawable.red_editext_bg)
                        binding.newPassEdt.setBackgroundResource(R.drawable.red_editext_bg)*/
                    uiHelper.showLongToastInCenter(this, response?.message)
                }

            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
/*        binding.confirmPasswordEdt.setBackgroundResource(R.drawable.red_editext_bg)
        binding.newPassEdt.setBackgroundResource(R.drawable.red_editext_bg)
        binding.oldPassEdt.setBackgroundResource(R.drawable.red_editext_bg)*/
        uiHelper.showLongToastInCenter(this, response?.message)
    }
}