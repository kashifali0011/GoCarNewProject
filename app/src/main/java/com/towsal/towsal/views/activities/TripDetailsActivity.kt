package com.towsal.towsal.views.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.app.MainApplication
import com.towsal.towsal.databinding.ActivityTripDetailsBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.messages.MessagesDetailModel
import com.towsal.towsal.network.serializer.profile.SecondMessage
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.MainScreenViewModel
import com.towsal.towsal.viewmodel.TripsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class TripDetailsActivity : BaseActivity(), PopupCallback , OnNetworkResponse {

    lateinit var binding: ActivityTripDetailsBinding

    val tripsViewModel: TripsViewModel by viewModel()
    val homeViewModel: MainScreenViewModel by viewModel()
    lateinit var navController: NavController
    var isInProgress = false
    var isLateReturnNeeded = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_trip_details)

        tripsViewModel.tripId =
            intent?.extras?.getInt(Constants.DataParsing.ID, tripsViewModel.tripId)
                ?: tripsViewModel.tripId
        tripsViewModel.viewType =
            intent?.extras?.getInt(Constants.DataParsing.VIEW_TYPE, tripsViewModel.viewType)
                ?: tripsViewModel.viewType

        binding.layoutToolBar.setAsGuestToolBar(
            titleId = R.string.trips,
            actionBar = supportActionBar,
            toolBarBg = if (tripsViewModel.viewType == Constants.TripUserType.HOST_TYPE) R.drawable.bg_gradient_toolbar_host else R.drawable.bg_sky_blue_gradient,
            arrowColor = R.color.white
        )
        setNavController()
        setObserver()
        setClickListeners()
        setDestinationListener()
    }

    private fun setDestinationListener() {
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            setVisibility(destination.id)
        }
    }

    private fun setVisibility(destination: Int) {
        binding.viewTrips.isInvisible = destination !in listOf(
            R.id.hostTripDetailFragment,
            R.id.guestTripDetailFragment
        )
        binding.viewMessages.isInvisible = destination !in listOf(
            R.id.action_global_messagesDetailsFragment,
            R.id.messagesDetailsFragment
        )
        binding.viewSupport.isInvisible = true
    }

    private fun setClickListeners() {
        binding.clTrips.setOnClickListener {
            it.preventDoubleClick()
            if (binding.viewTrips.isInvisible) {
                navController.navigate(
                    if (tripsViewModel.viewType == Constants.TripUserType.HOST_TYPE) R.id.hostTripDetailFragment else R.id.guestTripDetailFragment
                )
            }
        }
        binding.clMessages.setOnClickListener {
            it.preventDoubleClick()
            if (binding.viewMessages.isInvisible) {
                navController.navigate(
                    R.id.action_global_messagesDetailsFragment
                )
            }
        }
        binding.clSupport.setOnClickListener {
            binding.viewSupport.isInvisible = false
            binding.viewTrips.isVisible = binding.viewSupport.isInvisible
            binding.viewMessages.isVisible = binding.viewSupport.isInvisible
            tripsViewModel.popupEmergencyContact(this, this, isInProgress, isLateReturnNeeded) {
                setVisibility(navController.currentDestination?.id ?: R.id.guestTripDetailFragment)
            }
        }
        binding.ivClosed.setOnClickListener {
            binding.clMessageLayout.isVisible = false
        }
        binding.btnSecondMessage.setOnClickListener {

            var message = binding.edtMessage.text
            if (message.isNotEmpty()){
                homeViewModel.sendMessageWithId(
                    "$message",
                    tripsViewModel.tripId.toString(),
                    true,
                    Constants.API.SEND_MESSAGE,
                    this
                )
            }else{
                uiHelper.showLongToastInCenter(applicationContext, "Enter message")
            }
        }
    }

    private fun setObserver() {
        tripsViewModel.bookingStatus.observe(this) {
            binding.clTabLayout.isVisible = it != Constants.BookingStatus.PENDING
            binding.view.isVisible = it != Constants.BookingStatus.PENDING
            isInProgress = it == Constants.BookingStatus.IN_PROGRESS
        }

        tripsViewModel.isLateReturnNeeded.observe(this) {
            isLateReturnNeeded = it
        }
    }

    private fun setNavController() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        navController.setGraph(R.navigation.nav_graph_booking_details)
        val navGraph = navController.graph
        navGraph.startDestination =
            if (tripsViewModel.viewType == Constants.TripUserType.HOST_TYPE) R.id.hostTripDetailFragment else R.id.guestTripDetailFragment
        navController.graph = navGraph
    }


    override fun popupButtonClick(value: Int) {
        super.popupButtonClick(value)

        if (value == 1) {
            if (ActivityCompat.checkSelfPermission(
                    MainApplication.applicationContext(),
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                uiHelper.requestPermission(Manifest.permission.CALL_PHONE, this)
            } else {
                val intent = Intent(
                    Intent.ACTION_CALL,
                    Uri.parse("tel:" + tripsViewModel.adminContactNo)
                )
                startActivity(intent)
            }
        }
        else if (value == 2) {
            val bundle = Bundle()
            bundle.putInt(
                Constants.DataParsing.GOTO_PHOTOS,
                Constants.CarPhotos.GUEST_ACCIDENT_PHOTOS
            )
            bundle.putInt(
                Constants.DataParsing.ID,
                tripsViewModel.tripId
            )
            bundle.putInt(
                Constants.DataParsing.CAR_ID,
                tripsViewModel.carId.toInt()
            )
            bundle.putString(Constants.DataParsing.HEADING, getString(R.string.report_accident))
            uiHelper.openActivityForResult(
                this,
                AddTripDetailImages::class.java,
                true,
                Constants.RequestCodes.GUEST_TRIP_DETAIL, bundle
            )
        }
        else if (value == 3){
         //   Late Return Scenario
            //lateReturn

            tripsViewModel.lateReturn(
                tripsViewModel.tripId,
                true,
                Constants.API.LATE_RETURN,
                this
            )

        }
        else if (value == 4){
            binding.clMessageLayout.isVisible = true

        }
    }

    override fun message(threadId: String, messageId: String, textMessage: String) {
        if (navController.currentDestination?.id == R.id.messagesDetailsFragment) {
            tripsViewModel.setMessage(MessagesDetailModel().apply {
                id = messageId.toInt()
                message = textMessage
                this.threadId = threadId.toInt()
                sender_id = tripsViewModel.receiverId
            })
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceHelper.clearKey(Constants.DataParsing.USER_THREAD_ID)
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.LATE_RETURN -> {

            }
            Constants.API.SEND_MESSAGE -> {
                val model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    SecondMessage::class.java
                )
                binding.edtMessage.setText("")
                binding.clMessageLayout.isVisible = false
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        binding.clMessageLayout.isVisible = false
        uiHelper.showLongToastInCenter(this, response?.message)
    }


}