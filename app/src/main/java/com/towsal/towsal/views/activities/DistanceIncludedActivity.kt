package com.towsal.towsal.views.activities

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityDistanceIncludedBinding
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.settings.CarDistanceIncludedResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.SettingsViewModel
import com.towsal.towsal.views.adapters.DeleteReasonListingAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for adding allowed per day distance
 * */
class DistanceIncludedActivity : BaseActivity(), OnNetworkResponse {

    lateinit var binding: ActivityDistanceIncludedBinding
    val settingsViewModel: SettingsViewModel by viewModel()
    var model = CarDistanceIncludedResponseModel()
    lateinit var adapter: DeleteReasonListingAdapter
    var carId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_distance_included)
        binding.activity = this
        actionBarSetting()
        carId = intent?.extras?.getInt(Constants.DataParsing.CAR_ID, 0) ?: 0
        settingsViewModel.getDistanceIncluded(
            true,
            Constants.API.GET_DISTANCE_INCLUDED,
            carId,
            this
        )
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        if (model.car_distance_included.unlimited_distance == 1) {
            binding.unlimitedDistance.isChecked = false
            binding.unlimitedDistance.isChecked = true
            binding.distanceList.isEnabled = false
        } else {
            binding.unlimitedDistance.isChecked = true
            binding.unlimitedDistance.isChecked = false
            binding.distanceList.isEnabled = true
        }
        val list = ArrayList<String>()
        var selectedIndex = -1
        list.add(getString(R.string.select))
        if (model.distance_included.isNotEmpty()) {
            for (i in model.distance_included.indices) {
                list.add(model.distance_included[i].value + " " + getString(R.string.km_day))
                if (model.car_distance_included.distance_included != null)
                    if (model.distance_included[i].id == model.car_distance_included.distance_included?.id) {
                        selectedIndex = i
                    }
            }
            uiHelper.setSpinner(this, list, binding.distanceList)
            if (selectedIndex != -1)
                binding.distanceList.setSelection(selectedIndex + 1)
        }
        binding.mainLayout.visibility = View.VISIBLE

        binding.unlimitedDistance.setOnCheckedChangeListener { compoundButton, b ->
            binding.distanceList.isEnabled = !b
            binding.distanceList.setSelection(0)
            binding.textView43.setTextColor(
                getColor(
                    if (!b) R.color.black else R.color.grey_text_color_new
                )
            )

            binding.ivDistanceDropDown.setColorFilter(
                getColor(
                    if (!b) R.color.send_msg_bg else R.color.grey_text_color_new
                )
            )
        }

    }

    private fun actionBarSetting() {
        binding.layoutToolBar.setAsHostToolBar(
            R.string.distance_included_,
            supportActionBar
        )
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.save -> {
                var distanceincluded_id = 0
                var unlimited_distance = 0

                if (binding.unlimitedDistance.isChecked) {
                    unlimited_distance = 1
                    distanceincluded_id = 0
                } else {
                    if (binding.distanceList.selectedItemPosition == 0) {
                        uiHelper.showLongToastInCenter(
                            this,
                            getString(R.string.disctance_include_err_msg)
                        )
                        return
                    } else {
                        distanceincluded_id =
                            model.distance_included[binding.distanceList.selectedItemPosition - 1].id

                    }
                }


                settingsViewModel.saveDistanceIncludedInfo(
                    carId,
                    distanceincluded_id,
                    unlimited_distance,
                    true,
                    Constants.API.SAVE_DISTANCE_INCLUDED,
                    this
                )
            }
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_DISTANCE_INCLUDED -> {
                model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    CarDistanceIncludedResponseModel::class.java
                )
                setData()
            }
            Constants.API.SAVE_DISTANCE_INCLUDED -> {
                uiHelper.showLongToastInCenter(this, response?.message)
                finish()
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }

}