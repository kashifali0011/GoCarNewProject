package com.towsal.towsal.views.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityTransactionDetailBinding
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.profile.TransactionDetailResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.MainScreenViewModel
import com.towsal.towsal.views.adapters.TripsMultiViewAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for transaction details
 * */
class TransactionDetailActivity : BaseActivity(), OnNetworkResponse {

    lateinit var binding: ActivityTransactionDetailBinding

    val homeViewModel: MainScreenViewModel by viewModel()
    var viewType = 0
    var name = ""
    var image = ""
    var id = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_transaction_detail)
        binding.clMainRoot.isVisible = false
        binding.tvPaidOrEarned.setOnClickListener {
            val bundle = Bundle()
            bundle.putInt(Constants.DataParsing.ID, id)
            uiHelper.openActivity(
                this,
                if (viewType == TripsMultiViewAdapter.HOST_TYPE) ReceiptActivity::class.java else InvoiceActivity::class.java,
                bundle
            )
        }
        setUpView()
    }

    /**
     * Function for setting up view
     * */
    private fun setUpView() {
        binding.activity = this
        binding.toolBar.setAsGuestToolBar(
            R.string.trips,
            supportActionBar
        )

        intent?.extras?.let {
            id =
                it.getInt(Constants.DataParsing.ID, 0)
            viewType =
                it.getInt(Constants.DataParsing.VIEW_TYPE, 0)
            name =
                it.getString(Constants.DataParsing.NAME, "")
            image =
                it.getString(Constants.DataParsing.IMAGE, "")
            val isHost = it.getBoolean(Constants.DataParsing.IS_HOST, false)

            binding.toolBar.clToolBar.setBackgroundResource(
                if (isHost) R.drawable.bg_gradient_toolbar_host else R.drawable.bg_blue
            )
            binding.toolBar.ivArrowBack.setColorFilter(
                if (isHost) ContextCompat.getColor(
                    this,
                    R.color.line_bg_1
                ) else ContextCompat.getColor(
                    this,
                    R.color.sky_blue_variation_1
                ),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
            homeViewModel.getTransactionDetail(
                id,
                viewType,
                true,
                Constants.API.GET_TRANSACTION_DETAIL,
                this
            )
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_TRANSACTION_DETAIL -> {
                val responseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    TransactionDetailResponseModel::class.java
                )
                binding.clMainRoot.isVisible = true
                binding.model = responseModel.model
                binding.executePendingBindings()
                binding.bookingId.text =
                    getString(R.string.booking_id) + ": " + responseModel.model.id.toString()
                if (viewType == TripsMultiViewAdapter.HOST_TYPE) {
                    binding.guestName.text = getString(R.string.guest_name)
                    binding.tvPaidOrEarned.text = getString(R.string.earned)
                    binding.rentAmount.text = getString(
                        R.string.sar
                    ) + " " + responseModel.model.hostEaring

                } else {
                    binding.guestName.text = getString(R.string.host_name)
                    binding.tvPaidOrEarned.text = getString(R.string.paid)
                    binding.rentAmount.text = getString(
                        R.string.sar
                    ) + " " + responseModel.model.total_rent
                }
                binding.tvName.text = name

                Glide.with(this)
                    .load(BuildConfig.IMAGE_BASE_URL + image)
                    .into(binding.profileImage)
                binding.pickUpReturnAddress.text = responseModel.model.car_street_address
                binding.tvRating.text = responseModel.model.ratings.toString()
                //here to set pickup and dropoff date
                binding.mainLayout.visibility = View.VISIBLE
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }


}