package com.towsal.towsal.views.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityLocationInfoBinding
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.helper.SetActionBar
import com.towsal.towsal.network.serializer.carlist.CarInformationModel
import com.towsal.towsal.utils.Constants

/**
 * Activity class for selected location information
 * */
class LocationInfoActivity : BaseActivity() {

    lateinit var binding: ActivityLocationInfoBinding
    var carInfoModel = CarInformationModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_location_info)
        binding.activity = this
        //actionBarSetting()

        binding.layoutToolBar.setAsHostToolBar(
            R.string.list_your_car,
            supportActionBar
        )
        setData()
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        if (intent.extras != null) {
            carInfoModel =
                intent?.extras?.getSerializable(Constants.DataParsing.MODEL) as CarInformationModel
            binding.countryEdt.setText(carInfoModel.country_name)
            binding.cityEdt.setText(carInfoModel.city_name)
            binding.stateEdt.setText(carInfoModel.state_name)
            binding.zipCodeEdt.setText(carInfoModel.zip_code)
            binding.streetEdt.setText(carInfoModel.street_address)

        }
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.next -> {
                when {
                    binding.streetEdt.text.toString().isNullOrEmpty() -> {
                        uiHelper.showLongToastInCenter(this, getString(R.string.address_err_msg))
                    }
                    binding.zipCodeEdt.text.toString().isNullOrEmpty() -> {
                        uiHelper.showLongToastInCenter(this, getString(R.string.zip_code_err_msg))
                    }
                    else -> {
                        carInfoModel.zip_code = binding.zipCodeEdt.text.toString()
                        carInfoModel.street_address = binding.streetEdt.text.toString()
                        carInfoModel.pin_address = binding.streetEdt.text.toString()
                        val intent = Intent()
                        val bundle = Bundle()
                        bundle.putSerializable(Constants.DataParsing.MODEL, carInfoModel)
                        intent.putExtra(Constants.DataParsing.MODEL, bundle)
                        setResult(RESULT_OK, intent)
                        finish()
                    }
                }
            }
        }
    }
}