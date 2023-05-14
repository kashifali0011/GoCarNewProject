package com.towsal.towsal.views.activities
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityListingStatusBinding
import com.towsal.towsal.extensions.showListingHoldPopup
import com.towsal.towsal.interfaces.PickerCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.settings.StatusListingResponseModel
import com.towsal.towsal.network.serializer.settings.VehicleInfoSettingModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for changing listing status
 * */
class ListingStatusActivity : BaseActivity(), OnNetworkResponse, PickerCallback {

    var callApi = true
    lateinit var binding: ActivityListingStatusBinding
    var model = VehicleInfoSettingModel()
    val settingsViewModel: SettingsViewModel by viewModel()
    val sourceString =
        "Note: You listing status is on snoozed, If you want to listed your car you have to add payment method first"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_listing_status)
        binding.activity = this
        uiHelper.hideActionBar(supportActionBar)
        binding.layoutToolBar.toolbarTitle.text = getString(R.string.listing_status)
        binding.layoutToolBar.ivArrowBack.setOnClickListener {
            onBackPressed()
        }
        setData()
    }

    /**
     * Function for setting up data in views
     * */
    @SuppressLint("SetTextI18n")
    private fun setData() {
        if (intent.extras != null) {
            model =
                intent.extras!!.getSerializable(Constants.DataParsing.MODEL) as VehicleInfoSettingModel
            if (model.status == Constants.CarStatus.LISTED) {
                binding.listed.isChecked = true
            } else if (model.status == Constants.CarStatus.SNOOZED) {
                binding.snoozed.isChecked = true
                if (model.snooze_end_to.isNotEmpty()) {
                    val value = uiHelper.spanString(sourceString, "Note:", R.font.inter_bold, this)
                    binding.date.text = value
                }
            }
            setListingCheckChange()
            binding.snoozed.setOnClickListener {
                uiHelper.showDatePickerWithLimit(this, this, false, true)
            }
        }
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.deleteListing -> {
                val bundle = Bundle()
                bundle.putSerializable(Constants.DataParsing.MODEL, model)
                uiHelper.openActivityForResult(
                    this,
                    DeleteCarListingActivity::class.java,
                    true,
                    Constants.RequestCodes.ACTIVTIY_DELETE_LISTING, bundle
                )
            }
        }

    }

    /**
     * Function for setting up listed radio button checked change listener
     * */
    private fun setListingCheckChange() {
        binding.listed.setOnCheckedChangeListener { compoundButton, b ->
            if (callApi) {
                if (b) {
//                    settingsViewModel.updateCarStatus(
//                        Constants.CarStatus.LISTED,
//                        "",
//                        "",
//                        true,
//                        Constants.API.UPDATE_CAR_STATUS,
//                        this
//                    )

//                    showListingHoldPopup {
//                        uiHelper.openActivity(activity, PaymentsActivity::class.java)
//                    }
                }
            } else {
                callApi = true
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.UPDATE_CAR_STATUS -> {
                val modelResponse = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    StatusListingResponseModel::class.java
                )
                model.status = modelResponse.status

                /*...trying to set the crash for listed status .....*/
                model.snooze_start_from = modelResponse.snooze_start_from
                model.snooze_end_to = modelResponse.snooze_end_to

                if (modelResponse.status == Constants.CarStatus.LISTED) {
                    binding.date.text = ""
                } else {
                    val value = uiHelper.spanString(sourceString, "Note:", R.font.inter_bold, this)
                    binding.date.text = value
                }
                val intent = Intent()
                val bundle = Bundle()
                bundle.putSerializable(Constants.DataParsing.MODEL, model)
                intent.putExtra(Constants.DataParsing.MODEL, bundle)
                setResult(RESULT_OK, intent)
                uiHelper.showLongToastInCenter(this, response?.message)
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        if (model.status == Constants.CarStatus.LISTED) {
            callApi = false
            binding.listed.isChecked = true
        } else if (model.status == Constants.CarStatus.SNOOZED) {
            binding.snoozed.isChecked = true
        }
        if (response?.code == 424) {
            showListingHoldPopup {
                uiHelper.openActivity(activity, PaymentsActivity::class.java)
            }
        } else
            uiHelper.showLongToastInCenter(this, response?.message)
    }

    @SuppressLint("SetTextI18n")
    override fun onSelected(date: Any?) {
        if (date.toString().isEmpty()) {
            callApi = false
            binding.listed.isChecked = true
        } else {
//            settingsViewModel.updateCarStatus(
//                Constants.CarStatus.SNOOZED,
//                uiHelper.currentDate,
//                date.toString(),
//                true,
//                Constants.API.UPDATE_CAR_STATUS,
//                this
//            )
        }
    }
}