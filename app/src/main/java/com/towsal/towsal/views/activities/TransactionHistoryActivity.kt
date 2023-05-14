package com.towsal.towsal.views.activities

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityTransactionHistoryBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.profile.TransactionResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.MainScreenViewModel
import com.towsal.towsal.views.fragments.TripsFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for transaction history
 * */
class TransactionHistoryActivity : BaseActivity(), OnNetworkResponse {

    var responseModel = TransactionResponseModel()

    lateinit var binding: ActivityTransactionHistoryBinding
    lateinit var navController: NavController
    val homeViewModel: MainScreenViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_transaction_history)
        binding.clMainRoot.isVisible = false
        binding.activity = this
        binding.layoutToolBar.setAsGuestToolBar(
            titleId = R.string.transaction_history,
            actionBar = supportActionBar,
        )
    }

    override fun onResume() {
        super.onResume()
        callApi()
    }


    /**
     * Function for calling api
     * */
    private fun callApi() {
        homeViewModel.getTransactionList(true, Constants.API.GET_TRANSACTION_HISTORY, this)

    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_TRANSACTION_HISTORY -> {
                responseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    TransactionResponseModel::class.java
                )
                binding.clMainRoot.isVisible = true
                homeViewModel.setTransactionModel(
                    responseModel.transactionhistory
                )
                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
                navController = navHostFragment.navController
                binding.clHost.isVisible = responseModel.transactionhistory.hostList.isNotEmpty()
                addDestinationChangeListener()
                setClickListeners()
                binding.mainLayout.visibility = View.VISIBLE

            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }

    private fun addDestinationChangeListener() {
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            if (destination.id == R.id.transactionHistoryGuestFragment)
                binding.layoutToolBar.setAsGuestToolBar(
                    titleId = R.string.transaction_history,
                    actionBar = supportActionBar,
                )
            else
                binding.layoutToolBar.setAsHostToolBar(
                    titleId = R.string.transaction_history,
                    actionBar = supportActionBar,
                )

            setVisibility(
                destination.id
            )
        }
    }

    private fun setVisibility(destination: Int) {
        binding.viewGuest.isInvisible = destination != R.id.transactionHistoryGuestFragment
        binding.viewHost.isInvisible = destination != R.id.transactionHistoryFragment
    }

    private fun setClickListeners() {
        binding.clHost.setOnClickListener {
            it.preventDoubleClick()
            if (binding.viewHost.isInvisible) {
                navController.navigate(
                    R.id.transactionHistoryFragment
                )
            }
        }
        binding.clGuest.setOnClickListener {
            it.preventDoubleClick()
            if (binding.viewGuest.isInvisible) {
                navController.navigate(
                    R.id.transactionHistoryGuestFragment
                )
            }
        }
    }


}