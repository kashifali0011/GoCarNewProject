package com.towsal.towsal.views.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentHostBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.settings.MyCarsResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.SettingsViewModel
import com.towsal.towsal.views.activities.CarListActivity
import com.towsal.towsal.views.activities.LoginActivity
import com.towsal.towsal.views.activities.MainActivity

class HostFragment : BaseFragment(), OnNetworkResponse, PopupCallback {

    private lateinit var binding: FragmentHostBinding
    private lateinit var myCarsResponseModel: MyCarsResponseModel
    private val settingsViewModel: SettingsViewModel by activityViewModels()
    lateinit var navController: NavController

    companion object {
        var dateTimeServerFormat = "yyyy-MM-dd HH:mm:ss"
        var dateTimeConversionFormat = "MMM dd, yyyy - hh:mm a"
        var timeFormat = "hh:mm a"
        var timeFormatNew = "hh:mm"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_host, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
        binding.layoutToolBar.ivArrowBack.isVisible = false

        binding.layoutToolBar.setAsHostToolBar(
            titleId = R.string.your_car,
            bool = false
        )
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {

        if (uiHelper.userLoggedIn(preferenceHelper)) {
            settingsViewModel.getMyVehicles(true, Constants.API.GET_MY_VEHICLES, this)
        }

        binding.clNoCar.isVisible = !uiHelper.userLoggedIn(preferenceHelper)

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        navController.setGraph(R.navigation.nav_graph_host)
        addDestinationChangeListener()
        setOnClickListeners()
    }

    private fun setOnClickListeners() {
        binding.clVehicle.setOnClickListener {
            if (binding.viewVehicle.isInvisible) {
                navController.navigate(
                    R.id.myVehicleListingFragment
                )
            }
        }
        binding.clPerformance.setOnClickListener {
            if (binding.viewPerformance.isInvisible) {
                navController.navigate(
                    R.id.performanceSettingFragment
                )
            }
        }
        binding.clEarnings.setOnClickListener {
            if (binding.viewEarnings.isInvisible) {
                navController.navigate(
                    R.id.earningSettingFragment
                )
            }
        }
        binding.clReviews.setOnClickListener {
            if (binding.viewReviews.isInvisible) {
                navController.navigate(
                    R.id.reviewSettingFragment
                )
            }
        }
    }


    /*...trying to fetch the car list when the user enlists the car .....*/

    override fun onResume() {
        super.onResume()
        (activity as MainActivity?)?.setBadgeIcon()
        setData()
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view.id) {
            R.id.gotoCarList -> {
                if (uiHelper.userLoggedIn(preferenceHelper)) {
                    uiHelper.openActivity(requireActivity(), CarListActivity::class.java, true)
                } else {
                    uiHelper.showLongToastInCenter(
                        requireActivity(),
                        getString(R.string.login_list_car_err_msg)
                    )
                    uiHelper.openActivityForResult(
                        requireActivity(),
                        LoginActivity::class.java,
                        Constants.RequestCodes.ACTIVITY_LOGIN
                    )
                }
            }

        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_MY_VEHICLES -> {
                val myCarsResponseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    MyCarsResponseModel::class.java
                )
                this.myCarsResponseModel = myCarsResponseModel

                binding.clNoCar.isVisible = myCarsResponseModel.myCarsList.isEmpty()
                binding.clMainLayout.isVisible = myCarsResponseModel.myCarsList.isNotEmpty()
                settingsViewModel.setMyCars(
                    myCarsResponseModel.myCarsList
                )
            }
        }

    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_MY_VEHICLES -> {
                binding.clNoCar.visibility = View.VISIBLE
                binding.clMainLayout.visibility = View.GONE
            }
        }

        uiHelper.showLongToastInCenter(requireContext(), response?.message)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            childFragmentManager.fragments.forEach { fragment ->
                fragment.onActivityResult(requestCode, resultCode, data)
            }
        }
    }

    override fun popupButtonClick(value: Int) {
        if (value == 1) {
            uiHelper.openActivityForResult(
                requireActivity(),
                LoginActivity::class.java,
                Constants.RequestCodes.ACTIVITY_LOGIN
            )
        }
    }

    private fun addDestinationChangeListener() {
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            setVisibility(
                destination.id
            )

            binding.layoutToolBar.toolbarTitle.text = getString(
                when (destination.id) {
                    R.id.myVehicleListingFragment -> R.string.vehicle
                    R.id.earningSettingFragment -> R.string.earnings
                    R.id.performanceSettingFragment -> R.string.performance
                    R.id.reviewSettingFragment -> R.string.review
                    else -> R.string.vehicle
                }
            )

        }


    }

    private fun setVisibility(destination: Int) {
        binding.viewVehicle.isInvisible = destination != R.id.myVehicleListingFragment
        binding.viewPerformance.isInvisible = destination != R.id.performanceSettingFragment
        binding.viewEarnings.isInvisible = destination != R.id.earningSettingFragment
        binding.viewReviews.isInvisible = destination != R.id.reviewSettingFragment
    }

}