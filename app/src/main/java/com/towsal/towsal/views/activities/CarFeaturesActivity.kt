package com.towsal.towsal.views.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityCarFeaturesBinding
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.carlist.CarInformationModel
import com.towsal.towsal.network.serializer.carlist.CarListDataModel
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel
import com.towsal.towsal.network.serializer.settings.BasicCarResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.CarListViewModel
import com.towsal.towsal.views.adapters.CarFeatureAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for car features
 * */
class CarFeaturesActivity : BaseActivity(), OnNetworkResponse {
    lateinit var binding: ActivityCarFeaturesBinding
    var carInfoModel = CarInformationModel()
    var carListDataModel = CarListDataModel()
    var carFeaturesList = ArrayList<CarMakeInfoModel>()
    lateinit var adapter: CarFeatureAdapter
    var carBasicResponseModel = BasicCarResponseModel()
    private val carListViewModel: CarListViewModel by viewModel()
    var carName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_car_features)
        binding.activity = this
        actionBarSetting()
        setData()
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        if (intent.extras != null) {
            if (intent?.extras?.getSerializable(Constants.DataParsing.CARINFODATAMODEL) != null) {
                carListDataModel =
                    intent?.extras?.getSerializable(Constants.DataParsing.CARINFODATAMODEL) as CarListDataModel
            }
            if (intent?.extras?.getSerializable(Constants.DataParsing.MODEL) != null) {
                carInfoModel =
                    intent?.extras?.getSerializable(Constants.DataParsing.MODEL) as CarInformationModel
            }
            if (intent?.extras?.getString(Constants.DataParsing.NAME) != null) {
                carName = intent?.extras?.getString(Constants.DataParsing.NAME) ?: ""
            }

            carListViewModel.getFeaturesList(
                this,
                Constants.API.CARS_FEATURES_LIST
            )
        }
    }

    /**
     * Function for setting up action bar
     * */
    private fun actionBarSetting() {
        binding.layoutToolBar.setAsHostToolBar(
            R.string.car_features,
            supportActionBar
        )
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.next -> {
                val checked = adapter.getList().any { it.checked }
                if (!checked) {
                    uiHelper.showLongToastInCenter(this, getString(R.string.car_feature_err_msg))
                } else {
                    carInfoModel.car_features.clear()
                    for (i in adapter.getList().indices) {
                        if (adapter.getList()[i].checked) {
                            carInfoModel.car_features.add(adapter.getList()[i].id)
                            carInfoModel.car_features_list.add(adapter.getList()[i])
                        }
                    }
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

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        carFeaturesList = Gson().fromJson(
            uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
            CarListDataModel::class.java
        ).car_feature
        if (carListDataModel.current_step == 0) {
            if (intent?.extras?.getSerializable(Constants.DataParsing.DATECOMPLETEMODEL) != null) {
                carBasicResponseModel =
                    intent?.extras?.getSerializable(Constants.DataParsing.DATECOMPLETEMODEL) as BasicCarResponseModel
                carListDataModel = CarListDataModel()
                carInfoModel = CarInformationModel()

                carInfoModel.car_features = carBasicResponseModel.car_featuresID
            }
        }
        adapter = CarFeatureAdapter(this, uiHelper)
        binding.tvCarName.text = carName
        if (carInfoModel.car_features.isNotEmpty()) {
            carFeaturesList.forEach { parent ->
                parent.checked = carInfoModel.car_features.indexOfFirst { parent.id == it } != -1
            }
        }

        binding.featureListRecyclerView.adapter = adapter
        adapter.setList(carFeaturesList)
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message ?: "")
    }
}