package com.towsal.towsal.views.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityCarListBinding
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.carlist.CarListDataModel
import com.towsal.towsal.network.serializer.carlist.StepViewModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.CarListViewModel
import com.towsal.towsal.views.adapters.StepViewAdapter
import com.towsal.towsal.views.fragments.VehicleSettingFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for car listing process
 * */
open class CarListActivity : BaseActivity(), OnNetworkResponse {

    lateinit var binding: ActivityCarListBinding
    lateinit var adapter: StepViewAdapter
    val carListViewModel: CarListViewModel by viewModel()
    var model = CarListDataModel()
    val list = ArrayList<StepViewModel>()
    var gotPhotos = 0
    var navController: NavController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_car_list)
        binding.activity = this
        actionBarSetting()
        setData()
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        if (intent.extras != null) {
            gotPhotos = intent.extras!!.getInt(Constants.DataParsing.GOTO_PHOTOS, 0)
        }
        setActionBar?.setActionBarHeaderText("")
        val carId = intent?.extras?.getInt(Constants.DataParsing.CAR_ID, -1) ?: -1
        carListViewModel.carId = carId
        carListViewModel.getCarList(true, Constants.API.GET_CAR_LIST, this)
    }

    private fun addDestinationListener() {
        navController?.addOnDestinationChangedListener { controller, destination, arguments ->
            when (
                destination.id
            ) {
                R.id.carInformationFragment -> {
                    binding.pbCarProcessProgress.progress = 8
                    binding.layoutToolBar.toolbarTitle.text = getString(R.string.list_your_car)
                }
                R.id.carAvailabilityFragment -> {
                    binding.pbCarProcessProgress.progress = 32
                    binding.layoutToolBar.toolbarTitle.text = getString(R.string.car_availability)
                }
                R.id.carPhotosFragment -> {
                    binding.pbCarProcessProgress.progress = 47
                    binding.layoutToolBar.toolbarTitle.text = getString(R.string.car_photos)
                }
                R.id.vehicleProtectionFragment -> {
                    binding.pbCarProcessProgress.progress = 77
                    binding.layoutToolBar.toolbarTitle.text = getString(R.string.vehicle_protection)
                }
                R.id.safteyQualityStandardFragment -> {
                    binding.pbCarProcessProgress.progress = 100
                    binding.layoutToolBar.toolbarTitle.text =
                        getString(R.string.saftey_quality_standard)
                }
            }
        }
    }


    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_CAR_LIST -> {
                model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    CarListDataModel::class.java
                )
                if (intent.extras != null) {
                    val disableViews =
                        intent.extras!!.getBoolean(Constants.DataParsing.DISABLE_VIEWS, false)
                    model.disableViews = disableViews
                }
                navController = findNavController(R.id.navHostFragmentCarList)
                addDestinationListener()
                model.isStepOneApiRequired = model.current_step > 0
                model.isStepThreeApiRequired = model.current_step > 2
                model.isStepFourApiRequired = model.current_step > 3

                navController?.setGraph(R.navigation.nav_graph_step_car_list)


                if (gotPhotos == VehicleSettingFragment.GOTO_PHOTOS) {
                    model.current_step = Constants.CarStep.STEP_2_COMPLETED
                    model.finishActivity = true
                } else if (gotPhotos == VehicleSettingFragment.GOTO_VEHICLE_PROTECTION) {
                    model.current_step = Constants.CarStep.STEP_3_COMPLETED
                    model.finishActivity = true
                }
                preferenceHelper.saveObject(model, Constants.DataParsing.CAR_LIST_DATA_MODEL)
                when (model.current_step) {
                    Constants.CarStep.STEP_1_COMPLETED -> {
                        navController?.navigate(
                            R.id.action_carInformationFragment_to_carAvailabilityFragment
                        )
                    }
                    Constants.CarStep.STEP_2_COMPLETED -> {
                        navController?.navigate(
                            R.id.action_carInformationFragment_to_carAvailabilityFragment
                        )
                        navController?.navigate(
                            R.id.action_carAvailabilityFragment_to_carPhotosFragment
                        )
                    }
                    Constants.CarStep.STEP_3_COMPLETED -> {
                        navController?.navigate(
                            R.id.action_carInformationFragment_to_carAvailabilityFragment
                        )
                        navController?.navigate(
                            R.id.action_carAvailabilityFragment_to_carPhotosFragment
                        )
                        navController?.navigate(
                            R.id.action_carPhotosFragment_to_vehicleProtectionFragment
                        )
                    }
                    Constants.CarStep.STEP_4_COMPLETED -> {
                        navController?.navigate(
                            R.id.action_carInformationFragment_to_carAvailabilityFragment
                        )
                        navController?.navigate(
                            R.id.action_carAvailabilityFragment_to_carPhotosFragment
                        )
                        navController?.navigate(
                            R.id.action_carPhotosFragment_to_vehicleProtectionFragment
                        )
                        navController?.navigate(
                            R.id.action_vehicleProtectionFragment_to_safteyQualityStandardFragment
                        )
                    }
                }

                Handler(Looper.getMainLooper()).postDelayed({
                    binding.navHostFragmentCarList.visibility = View.VISIBLE
                }, 100)

//                addDestinationChangeListener()
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.fragments?.forEach { fragment ->
                fragment.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    /**
     * Function for setting up action bar
     * */
    open fun actionBarSetting() {
        binding.layoutToolBar.setAsGuestToolBar(
            titleId = R.string.list_your_car,
            actionBar = supportActionBar,
            toolBarBg = R.drawable.bg_gradient_toolbar_host,
            arrowColor = R.color.white
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceHelper.clearKey(Constants.DataParsing.CAR_LIST_DATA_MODEL)
    }

    override fun onResume() {
        super.onResume()
        activity = this
    }
}
