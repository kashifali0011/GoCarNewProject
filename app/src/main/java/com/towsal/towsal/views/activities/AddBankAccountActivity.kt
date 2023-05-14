package com.towsal.towsal.views.activities

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityAddBankAccountBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.payment.PaymentMethodRequestModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.PaymentsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddBankAccountActivity : BaseActivity(), View.OnClickListener, OnNetworkResponse {

    lateinit var binding: ActivityAddBankAccountBinding
    private val paymentsViewModel: PaymentsViewModel by viewModel()

    private var isAccountTitle: Boolean = false
    private var isAccountNumber: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBankAccountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.activity = this
        val isHost = intent?.extras?.getInt(Constants.DataParsing.IS_HOST, 0) ?: 0
        if (isHost == 1)
            binding.layoutToolBar.setAsHostToolBar(R.string.payment_method, supportActionBar)
        else binding.layoutToolBar.setAsGuestToolBar(R.string.payment_method, supportActionBar)

        addTextChangedListeners()
    }

    private fun addTextChangedListeners() {
        binding.edtAccountTitle.addTextChangedListener {
            isAccountTitle = it.toString().isNotEmpty()
            if (isAccountTitle)
                binding.edtAccountTitle.error = null
        }

        binding.edtAccountNumber.addTextChangedListener {
            isAccountNumber = it.toString().length in 17..35
            if (isAccountNumber)
                binding.edtAccountNumber.error = null
        }
    }

    override fun onClick(view: View?) {
        view?.preventDoubleClick()
        when (view) {
            binding.btnSave -> {
                if (isAccountTitle && isAccountNumber) {
                    val paymentMethod = PaymentMethodRequestModel(
                        paymentMethodType = 3,
                        accountTitle = binding.edtAccountTitle.text.toString(),
                        accountNumber = binding.edtAccountNumber.text.toString()
                    )
                    paymentsViewModel.addPaymentMethod(
                        paymentMethod,
                        Constants.API.ADD_PAYMENT_METHOD,
                        this,
                        true
                    )
                } else if (!isAccountTitle) {
                    binding.edtAccountTitle.error = "Account title required"
                } else if (!isAccountNumber || binding.edtAccountNumber.text.toString()
                        .isNotEmpty()
                ) {
                    binding.edtAccountNumber.error = "Account number invalid"
                }
            }

            binding.layoutToolBar.ivArrowBack -> {
                onBackPressed()
            }
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        finish()
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }
}