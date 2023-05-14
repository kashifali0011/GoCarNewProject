package com.towsal.towsal.views.activities

import android.app.Activity
import android.app.Notification
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.get
import androidx.core.view.isVisible
import androidx.core.view.iterator
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.internal.BaselineLayout
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityMainBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setGradientTextColor
import com.towsal.towsal.helper.DelegateStatic
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.checkoutcarbooking.BookingConfirmationResponseModel
import com.towsal.towsal.network.serializer.messages.MessagesResponseModel
import com.towsal.towsal.network.serializer.trips.NotificationUnReadData
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.MainScreenViewModel
import com.towsal.towsal.viewmodel.SettingsViewModel
import com.towsal.towsal.viewmodel.TripsViewModel
import com.towsal.towsal.views.adapters.NotificationAdapter
import com.towsal.towsal.views.fragments.TripsFragment
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for main screen activity
 * */
class MainActivity : BaseActivity(), OnNetworkResponse, PopupCallback {

    companion object {
        var GOTO_FRAGMENT = 0
        const val GOTO_HOST_FRAGMENT = 1
        const val GOTO_TRIPS_FRAGMENT = 2
        const val GOTO_MESSAGES_FRAGMENT = 3
        const val GOTO_PROFILE_FRAGMENT = 4
        const val LOCATION_SETTING_REQUEST = 999
        var firstTimeCalled = false
        var notificationTitle: String? = ""
        var comingFirstTimeAfterRegistration = false
    }

    lateinit var navController: NavController
    lateinit var binding: ActivityMainBinding
    val homeViewModel: MainScreenViewModel by viewModel()
    val settingsViewModel: SettingsViewModel by viewModel()
    val tripsViewModel: TripsViewModel by viewModel()


    /**********************************************************************************************************************************
     *                                                     override methods
     * ********************************************************************************************************************************/
    //have to refresh messages list on receiving msg
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        uiHelper.hideActionBar(supportActionBar)
        DelegateStatic.tmainActivity = this
        firstTimeCalled = true
        changeMenuItemTextColor()
        binding.bottomNavView.dispatchSetSelected(true)

    }

    private fun addDestinationChangeListener() {
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            binding.bottomNavView.isVisible = destination.id != R.id.vehicleSettingFragment
        }
    }


    private fun changeMenuItemTextColor() {
        for (bottomNavigationItem in binding.bottomNavView[0] as BottomNavigationMenuView) {
            (((bottomNavigationItem as BottomNavigationItemView)
                .getChildAt(1) as BaselineLayout)
                .getChildAt(1) as AppCompatTextView)
                .setGradientTextColor(
                    intArrayOf(
                        Color.parseColor("#3131E2"),
                        Color.parseColor("#42AEEB")
                    )
                )
        }
    }


    override fun onResume() {
        super.onResume()
        Log.d("notttiii", "comingHereinresume")
        setData()

        setBadgeIcon()
        activity = this
        //for alert icon
        if (uiHelper.userLoggedIn(preferenceHelper)) {
            callMessagesApi()
            callReadNotificationApi()
        }


    }


    override fun onBackPressed() {
        when (navController.currentDestination?.id) {
            R.id.bnv_search -> {
                finish()
            }
            else -> {
                super.onBackPressed()
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.fragments?.forEach { fragment ->
                fragment.onActivityResult(requestCode, resultCode, data)
            }
            if (requestCode == Constants.RequestCodes.ACTIVITY_LOGIN) {
                setUpUi()
            }
        }
    }

    private fun setUpUi() {
        when (GOTO_FRAGMENT) {
            GOTO_HOST_FRAGMENT -> {
                navController.navigate(R.id.bnv_host)
            }
            GOTO_TRIPS_FRAGMENT -> {
                navController.navigate(R.id.bnv_trips)
            }
            GOTO_MESSAGES_FRAGMENT -> {
                navController.navigate(R.id.bnv_message)
            }
            GOTO_PROFILE_FRAGMENT -> {
                navController.navigate(R.id.bnv_profile)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String?>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.fragments?.forEach { fragment ->
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun popupButtonClick(value: Int) {
        if (value == 1) {
            uiHelper.openActivityForResult(
                this,
                LoginActivity::class.java,
                Constants.RequestCodes.ACTIVITY_LOGIN
            )
        } else {
            binding.bottomNavView.menu[0].isChecked = true
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.READ_NOTIFICATION -> {
                val modelNotification = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    NotificationUnReadData::class.java
                )
                if (modelNotification.unReadCount != 0) {
                    DelegateStatic.notificationTitle = "Some Notification"
                }
                setBadgeIcon()
            }
            Constants.API.GET_MESSAGES -> {
                val responseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    MessagesResponseModel::class.java
                )
                if (responseModel.messagesList.isNotEmpty()) {
                    for (row in responseModel.messagesList) {
                        if (row.is_seen == 0) {
                            DelegateStatic.notificationTitle = "New message received"
                            if (DelegateStatic.tmainActivity != null) {
                                setBadgeIcon()
                            }
                            break
                        }
                    }
                }
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message.toString())
    }

    /**
     * Function for calling notifications count api
     * */
    private fun callReadNotificationApi() {
        homeViewModel.unReadNotification(false, Constants.API.READ_NOTIFICATION, this)
    }

    /**
     * Function for calling messages count api
     * */
    private fun callMessagesApi() {
        homeViewModel.getMessages(false, Constants.API.GET_MESSAGES, this)
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController
        addDestinationChangeListener()
        setBadgeIcon()
        //navigation to profile screen when comes first time
        if (comingFirstTimeAfterRegistration) {
            comingFirstTimeAfterRegistration = false
            navController.navigate(R.id.bnv_profile)
        }

        binding.bottomNavView.setupWithNavController(navController)
        if (GOTO_FRAGMENT == GOTO_HOST_FRAGMENT) {
            navController.navigate(R.id.bnv_host)
            GOTO_FRAGMENT = 0
        }
        val extras = intent.extras
        val trigger = extras?.get(Constants.TRIGGER_NOTIFICATION)
        val trigger2 = extras?.get("google.delivered_priority") as? String?
        if (GOTO_FRAGMENT == GOTO_TRIPS_FRAGMENT || (trigger != null && trigger.toString()
                .toInt() != 0) || !trigger2.isNullOrEmpty()
        ) {
            intent.extras?.putString(Constants.TRIGGER_NOTIFICATION, null)
            intent.extras?.putString("google.delivered_priority", null)
            navController.navigate(R.id.bnv_trips)
            GOTO_FRAGMENT = 0
        }
        if (GOTO_FRAGMENT == GOTO_MESSAGES_FRAGMENT) {
            navController.navigate(R.id.bnv_message)
            GOTO_FRAGMENT = 0
        }
        if (GOTO_FRAGMENT == GOTO_PROFILE_FRAGMENT) {
            navController.navigate(R.id.bnv_profile)
            GOTO_FRAGMENT = 0
        }
        binding.bottomNavView.setOnItemSelectedListener { item: MenuItem ->

            if (item.actionView != null)
                item.actionView?.preventDoubleClick()
            when (item.itemId) {
                R.id.bnv_search -> {
                    navController.navigate(R.id.bnv_search)
                }
                R.id.bnv_host -> {

                    navController.navigate(R.id.bnv_host)
                }
                R.id.bnv_trips -> {
                    checkUserLogin(
                        navController,
                        GOTO_TRIPS_FRAGMENT
                    )
                }
                R.id.bnv_message -> {
                    checkUserLogin(
                        navController,
                        GOTO_MESSAGES_FRAGMENT
                    )
                }
                R.id.bnv_profile -> {
                    checkUserLogin(
                        navController,
                        GOTO_PROFILE_FRAGMENT
                    )
                }
            }
            true
        }
    }


    /**
     * Function for checking that user is logged in
     * */
    private fun checkUserLogin(
        navController: NavController,
        gotoFragment: Int
    ) {
        if (uiHelper.userLoggedIn(preferenceHelper)) {
            when (gotoFragment) {
                GOTO_HOST_FRAGMENT -> {
                    navController.navigate(R.id.bnv_host)
                }
                GOTO_TRIPS_FRAGMENT -> {
                    navController.navigate(R.id.bnv_trips)
                }
                GOTO_MESSAGES_FRAGMENT -> {
                    navController.navigate(R.id.bnv_message)
                }
                GOTO_PROFILE_FRAGMENT -> {
                    navController.navigate(R.id.bnv_profile)
                }
            }
            changeMenuItemTextColor()
        } else {
            GOTO_FRAGMENT = gotoFragment
            uiHelper.openActivityForResult(
                this,
                LoginActivity::class.java,
                Constants.RequestCodes.ACTIVITY_LOGIN
            )

        }

    }

    /**
     * Function for setting up badge on bottom navigation icons
     * */
    fun setBadgeIcon() {
        if (DelegateStatic.notificationType != -1) {
            if(DelegateStatic.notificationType == NotificationAdapter.SEND_MESSAGE)  {
                bottomNavView.getOrCreateBadge(R.id.bnv_message)
                if (bottomNavView.selectedItemId == R.id.bnv_message) {
                    bottomNavView.removeBadge(R.id.bnv_message)
                    DelegateStatic.notificationType = -1
                    GOTO_FRAGMENT = 0
                }
            } else {
                bottomNavView.getOrCreateBadge(R.id.bnv_trips)
                if (bottomNavView.selectedItemId == R.id.bnv_trips) {
                    if (TripsFragment.navController.currentDestination?.id == R.id.notificationsFragment) {
                        bottomNavView.removeBadge(R.id.bnv_trips)
                        DelegateStatic.notificationType = -1
                    }
                }
            }
        } else {
            when(
                navController.currentDestination?.id
            ){
                R.id.bnv_message -> bottomNavView.removeBadge(R.id.bnv_message)
                R.id.bnv_trips -> bottomNavView.removeBadge(R.id.bnv_trips)
            }
        }
    }

}