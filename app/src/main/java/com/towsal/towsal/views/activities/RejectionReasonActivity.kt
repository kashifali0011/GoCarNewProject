package com.towsal.towsal.views.activities

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityRejectionReasonBinding
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.trips.CancellationPolicyModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.TripsViewModel
import com.towsal.towsal.views.adapters.CancelReasonAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for rejection reasons
 * */
class RejectionReasonActivity : BaseActivity(), OnNetworkResponse {

    lateinit var binding: ActivityRejectionReasonBinding
    val tripsViewModel: TripsViewModel by viewModel()
    lateinit var adapter: CancelReasonAdapter
    var id = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rejection_reason)
        binding.activity = this
        actionBarSetting()
        setData()
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        if (intent.extras != null) {
            id = intent.extras!!.getInt(Constants.DataParsing.ID, 0)
            tripsViewModel.rejectBookingReason(true, Constants.API.REJECT_BOOKING_REASON, this)
        }
    }

    /**
     * Function for setting up action bar
     * */
    private fun actionBarSetting() {
        binding.layoutToolBar.setAsGuestToolBar(
            R.string.rejection_reason,
            supportActionBar
        )
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view) {
            binding.btnSubmit -> {
                var selectedReason = false
                var reasonId = 0
                if (adapter.getList().isNotEmpty())
                    for (i in adapter.getList().indices) {
                        if (adapter.getList()[i].selected) {
                            selectedReason = true
                            reasonId = adapter.getList()[i].id
                            break
                        }
                    }
                if (selectedReason) {
                    tripsViewModel.rejectBooking(
                        id,
                        reasonId,
                        true,
                        Constants.API.REJECT_BOOKING,
                        this
                    )
                } else {
                    uiHelper.showLongToastInCenter(
                        this,
                        getString(R.string.select_reasons_for_booking_rejection)
                    )
                }
            }
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.REJECT_BOOKING_REASON -> {
                val responseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    CancellationPolicyModel::class.java
                )
                adapter = CancelReasonAdapter(this, uiHelper, responseModel.rejectReason)
                binding.recyclerView.adapter = adapter
                binding.mainLayout.visibility = View.VISIBLE
            }
            Constants.API.REJECT_BOOKING -> {
                uiHelper.showLongToastInCenter(this, response?.message)
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }
}