package com.towsal.towsal.views.activities

import android.graphics.Color
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityUserInformationBinding
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.extensions.setGradientTextColor
import com.towsal.towsal.interfaces.StepInfoCallback
import com.towsal.towsal.network.serializer.cardetails.CalendarDateTimeModel
import com.towsal.towsal.network.serializer.cardetails.UserInfoResponseModel
import com.towsal.towsal.network.serializer.carlist.CarInformationModel
import com.towsal.towsal.network.serializer.carlist.StepViewModel
import com.towsal.towsal.network.serializer.checkoutcarbooking.GetCarBookingModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.UserInformationViewModel
import com.towsal.towsal.views.adapters.StepViewAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for user information screen
 * */
class UserInformationActivity : BaseActivity(), StepInfoCallback {

    val list = ArrayList<StepViewModel>()

    companion object {
        var stepInfoCallback: StepInfoCallback? = null
        var carInfoModel = CarInformationModel()
        var checkoutInfoModel = GetCarBookingModel()
        var calendarDateTimeModel = CalendarDateTimeModel()
    }

    lateinit var binding: ActivityUserInformationBinding
    lateinit var adapter: StepViewAdapter
    var navController: NavController? = null
    var model = UserInfoResponseModel()
    private val userInformationViewModel: UserInformationViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_information)
        binding.activity = this
        stepInfoCallback = this
        binding.layoutToolBar.setAsGuestToolBar(R.string.profile, supportActionBar)
        if (intent.extras != null) {
            model =
                intent!!.extras?.getSerializable(Constants.DataParsing.MODEL) as UserInfoResponseModel
            userInformationViewModel.setUserResponseModelValue(model)
            if (model.current_step == Constants.CarStep.STEP_1_COMPLETED) {
                setList(Constants.CarStep.STEP_2_COMPLETED)
            }
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.navGraphUserInformation) as NavHostFragment
            val navController = navHostFragment.navController
            checkoutInfoModel =
                intent.extras!!.getSerializable(Constants.DataParsing.MODEL_OTHER) as GetCarBookingModel
            carInfoModel =
                intent.extras!!.getSerializable(Constants.DataParsing.CARINFODATAMODEL) as CarInformationModel
            if (intent.extras!!.getSerializable(Constants.DataParsing.DATE_TIME_MODEL) != null) {
                calendarDateTimeModel =
                    intent.extras!!.getSerializable(Constants.DataParsing.DATE_TIME_MODEL) as CalendarDateTimeModel
            }
            val bundle = Bundle()
            bundle.putSerializable(Constants.DataParsing.MODEL, model)
            navController.setGraph(navController.graph, bundle)

            binding.tvSubTitle.setGradientTextColor(
                intArrayOf(
                    Color.parseColor("#0F0F7D"),
                    Color.parseColor("#1420E4")
                )
            )
            when (model.current_step) {
                Constants.CarStep.STEP_1_COMPLETED -> {
                    setList(Constants.CarStep.STEP_2_COMPLETED)
                    navController.navigate(
                        R.id.action_phoneInformationFragment_to_profilePhotFragment,
                        bundle
                    )
                }
                in Constants.CarStep.STEP_2_COMPLETED..Constants.CarStep.STEP_4_COMPLETED -> {
                    setList(Constants.CarStep.STEP_3_COMPLETED)
                    navController.navigate(
                        R.id.action_phoneInformationFragment_to_profilePhotFragment,
                        bundle
                    )
                    navController.navigate(
                        R.id.action_profilePhotFragment_to_userLicenseInformationFragment,
                        bundle
                    )
                }
            }
            this.navController = navController

            navController.addOnDestinationChangedListener { controller, destination, arguments ->
                when (destination.id) {
                    R.id.phoneInformationFragment -> {
                        binding.layoutToolBar.toolbarTitle.text = getString(R.string.phone)
                    }
                    R.id.profilePhotFragment -> {
                        binding.layoutToolBar.toolbarTitle.text = getString(R.string.profile_photo)

                    }
                    R.id.userLicenseInformationFragment -> {
                        binding.layoutToolBar.toolbarTitle.text = getString(R.string.driver_license)
                    }
                }
            }
        }

    }

    /**
     * Function for setting up steps view
     * */
    fun setList(num: Int) {
        list.clear()
        for (i in 1..3) {
            val model = StepViewModel()
            if (i == num) {
                model.selected = true
            }
            model.number = i
            list.add(model)
        }
        binding.stepViewRecyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )
        adapter = StepViewAdapter(this, uiHelper, true)
        binding.stepViewRecyclerView.adapter = adapter
        adapter.setList(list)
    }

    override fun stepNumber(step: Int) {
        setList(step)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (navController != null) {
            if (navController?.currentDestination?.id == R.id.phoneInformationFragment) {
                setList(Constants.CarStep.STEP_1_COMPLETED)
            }
            if (navController?.currentDestination?.id == R.id.profilePhotFragment) {
                setList(Constants.CarStep.STEP_2_COMPLETED)
            }
        }
    }

}