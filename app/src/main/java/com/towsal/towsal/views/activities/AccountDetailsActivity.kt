package com.towsal.towsal.views.activities

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityAccountBinding
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.helper.SetActionBar
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.profile.GetAccountResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.MainScreenViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for account details
 * */
class AccountDetailsActivity : BaseActivity(), OnNetworkResponse {
    lateinit var binding: ActivityAccountBinding
    val homeViewModel: MainScreenViewModel by viewModel()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_account)
        binding.clMainRoot.isVisible = false
        binding.activity = this
        setData()
        homeViewModel.getAccountInfo(true, Constants.API.ACCOUNT_INFO, this)

    }

    /**
     * Function for setting action bar settings
     * */

    private fun setData() {
        binding.toolBar.setAsGuestToolBar(
            R.string.account,
            supportActionBar
        )
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {

            R.id.submit -> {
                if (binding.firstNameEdt.text.toString().isEmpty()) {
                    binding.firstNameEdt.setBackgroundResource(R.drawable.red_editext_bg)
                    binding.lastNameEdt.setBackgroundResource(R.drawable.button_primary_bg)
                    uiHelper.showLongToastInCenter(this, getString(R.string.first_name_err_msg))
                } else if (binding.lastNameEdt.text.toString().isEmpty()) {
                    binding.firstNameEdt.setBackgroundResource(R.drawable.button_primary_bg)
                    binding.lastNameEdt.setBackgroundResource(R.drawable.red_editext_bg)
                    uiHelper.showLongToastInCenter(this, getString(R.string.last_name_err_msg))
                } else {
                    binding.firstNameEdt.setBackgroundResource(R.drawable.button_primary_bg)
                    binding.lastNameEdt.setBackgroundResource(R.drawable.button_primary_bg)


                    homeViewModel.updateAccount(
                        binding.firstNameEdt.text.toString(),
                        binding.lastNameEdt.text.toString(),
                        true,
                        Constants.API.UPDATE_ACCOUNT_INFO,
                        this
                    )
                }

            }
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.ACCOUNT_INFO -> {
                val model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    GetAccountResponseModel::class.java
                )
                binding.clMainRoot.isVisible = true
                binding.emailEdt.setText(model.account.email)
                binding.phoneEdt.setText(model.account.phone)
                binding.firstNameEdt.setText(model.account.first_name)
                binding.lastNameEdt.setText(model.account.last_name)

                binding.mainLayout.visibility = View.VISIBLE
            }
            Constants.API.UPDATE_ACCOUNT_INFO -> {
                uiHelper.showLongToastInCenter(this, response?.message)
                finish()
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)

    }


}