package com.towsal.towsal.views.activities

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityGuidelinesByHostBinding
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.settings.GuidelinesHostResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.SettingsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for host guidelines
 * */
class GuidelinesByHostActivity : BaseActivity(), OnNetworkResponse {

    lateinit var binding: ActivityGuidelinesByHostBinding
    val settingsViewModel: SettingsViewModel by viewModel()
    var model = GuidelinesHostResponseModel()
    var carId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_guidelines_by_host)
        binding.activity = this
        carId = intent?.extras?.getInt(Constants.DataParsing.CAR_ID, 0) ?: 0
        actionBarSetting()
        settingsViewModel.getGuidelines(carId, true, Constants.API.GET_GUIDELINES_BY_HOST, this)
    }

    /**
     * Function for setting up action bar
     * */
    private fun actionBarSetting() {
        binding.layoutToolBar.setAsHostToolBar(
            R.string.guidelines,
            supportActionBar
        )
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_GUIDELINES_BY_HOST -> {
                model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    GuidelinesHostResponseModel::class.java
                )
                binding.guidelinesEdt.setText(model.model.text)
                binding.mainLayout.visibility = View.VISIBLE
            }
            Constants.API.SAVE_GUIDELINES_BY_HOST -> {
                uiHelper.showLongToastInCenter(this, response?.message)
                finish()
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            binding.save.id -> {
                if (binding.guidelinesEdt.text.toString().isEmpty()) {
                    uiHelper.showLongToastInCenter(
                        this,
                        getString(R.string.add_instructions_for_the_guest)
                    )
                } else {
                    settingsViewModel.saveGuidelines(
                        carId,
                        binding.guidelinesEdt.text.toString(),
                        true,
                        Constants.API.SAVE_GUIDELINES_BY_HOST,
                        this
                    )
                }
            }
        }
    }
}