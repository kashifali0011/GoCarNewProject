package com.towsal.towsal.views.activities

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.oppwa.mobile.connect.exception.PaymentError
import com.oppwa.mobile.connect.exception.PaymentException
import com.oppwa.mobile.connect.payment.CheckoutInfo
import com.oppwa.mobile.connect.payment.card.CardPaymentParams
import com.oppwa.mobile.connect.provider.Connect
import com.oppwa.mobile.connect.provider.ITransactionListener
import com.oppwa.mobile.connect.provider.Transaction
import com.oppwa.mobile.connect.provider.TransactionType
import com.oppwa.mobile.connect.service.ConnectService
import com.oppwa.mobile.connect.service.IProviderBinder
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityPaymentsBinding
import com.towsal.towsal.extensions.*
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.checkoutcarbooking.BookingConfirmationResponseModel
import com.towsal.towsal.network.serializer.payment.PaymentCheckOutIdResponseModel
import com.towsal.towsal.network.serializer.payment.PaymentMethodsResponseModel
import com.towsal.towsal.network.serializer.payment.PaymentStatusResponse
import com.towsal.towsal.network.serializer.payment.PaymentsResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.PaymentsViewModel
import com.towsal.towsal.views.adapters.PaymentMethodsAdapter
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for payments screen
 * */
class PaymentsActivity : BaseActivity(), View.OnClickListener,
    OnNetworkResponse {

    lateinit var binding: ActivityPaymentsBinding
    private val paymentsViewModel: PaymentsViewModel by viewModel()
    private var cardsList: ArrayList<PaymentsResponseModel> = arrayListOf()
    private var thankYouModel: BookingConfirmationResponseModel? =
        BookingConfirmationResponseModel()
    private var flowComingFrom = 0
    lateinit var model: PaymentMethodsResponseModel
    var bankAccountMethod: PaymentsResponseModel? = null
    var selectedPaymentMethod: PaymentsResponseModel? = null
    var alreadyDefaultCardPosition = 0
    var selectedPosition = 0
    var isCard = false
    lateinit var adapter: PaymentMethodsAdapter
    var citId = ""
    companion object {
        const val NORMAL_FLOW = 0
        const val FLOW_FROM_CHECKOUT = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_payments)
        binding.clRoot.isVisible = false
        uiHelper.hideActionBar(supportActionBar)
        binding.activity = this
        setContentView(binding.root)

        if (intent.extras != null) {
            thankYouModel =
                intent.extras?.getSerializable(Constants.DataParsing.MODEL) as BookingConfirmationResponseModel

            preferenceHelper.saveObject(
                thankYouModel!!,
                Constants.DataParsing.THANK_YOU_MODEL
            )
            flowComingFrom = intent.extras?.getInt(Constants.DataParsing.FLOW_COMING_FROM, 0) ?: 0
        }
        adapter = PaymentMethodsAdapter(
            cardsList,
            object : (Int) -> Unit {
                override fun invoke(position: Int) {
                    selectedPosition = position
                    isCard = true
                    sendRequestForDeletePaymentMethod(cardsList[position] , 2)
                }
            }
        ) { position, alreadyDefaultCardPosition ->
            selectedPosition = position
            this@PaymentsActivity.alreadyDefaultCardPosition =
                alreadyDefaultCardPosition
            sendDefaultCardRequest(true)
        }
        addClickListeners()
    }

    private fun sendDefaultCardRequest(showLoader: Boolean) {
        paymentsViewModel.setDefaultCard(
            cardsList[selectedPosition].id,
            Constants.API.SET_DEFAULT_PAYMENT_METHOD,
            this@PaymentsActivity,
            showLoader
        )
    }


    private fun addClickListeners() {
        binding.layoutBankAccount.ivDelete.setOnClickListener(this)
    }

    /**
     * Function for click listeners
     * */
    override fun onClick(view: View?) {
        view?.preventDoubleClick()
        when (view) {
            binding.btnPayNow -> {
                paymentsViewModel.holdPayment(
                    shopperResultUrl = "payment://result",
                    checkOutId = thankYouModel?.checkout_id,
                    onNetworkResponse = this,
                    tag = Constants.API.HOLD_PAYMENT,
                    showLoader = true
                )
            }
            binding.btnAddAccount -> {
                val bundle = Bundle()
                bundle.putInt(Constants.DataParsing.IS_HOST, model.isHost)
                uiHelper.openActivity(activity, AddBankAccountActivity::class.java, bundle)
            }
            binding.btnAddCreditDebitCard -> {
                val bundle = Bundle()
                if (flowComingFrom != NORMAL_FLOW) {
                    bundle.putSerializable(Constants.DataParsing.MODEL, thankYouModel)
                    bundle.putInt(Constants.DataParsing.FLOW_COMING_FROM, 1)
                }
                bundle.putInt(Constants.DataParsing.IS_HOST, model.isHost)

                uiHelper.openActivity(activity, AddCreditCardActivity::class.java, bundle)

            }
            binding.layoutBankAccount.ivDelete -> {
                isCard = false
                sendRequestForDeletePaymentMethod(bankAccountMethod , 3)
            }
        }
    }

    private fun sendRequestForDeletePaymentMethod(model: PaymentsResponseModel? , paymentMethodType: Int) {

        var cardDeleteMsg = if (paymentMethodType == 2){
            getString(R.string.delete_confirmation_card)
        }else{
            getString(R.string.delete_confirmation_bank)
        }
        showPopupWithIndication(
            object : PopupCallback {
                override fun popupButtonClick(value: Int) {
                    super.popupButtonClick(value)
                    if (value == 1) {
                        model?.let {
                            paymentsViewModel.deletePaymentMethod(
                                id = it.id,
                                paymentMethodType,
                                onNetworkResponse = this@PaymentsActivity,
                                tag = Constants.API.REMOVE_PAYMENT_METHOD,
                                showLoader = true
                            )
                        }

                    }
                }
            }, R.string.payment_method, R.string.delete_confirmation_message , true , cardDeleteMsg
        )
    }


    private fun preparePaymentStatusRequest() {
        paymentsViewModel.getPaymentStatus(
            citId,
            thankYouModel?.checkout_id.toString(),
            this,
            Constants.API.PAYMENT_STATUS,
            true
        )
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.scheme.equals("payment")) {
            preparePaymentStatusRequest()
        }
    }


    /**
     * Function for creating payment parameters
     * */
    private fun createPaymentParams(checkoutId: String): CardPaymentParams {

        val cardHolder: String =
            selectedPaymentMethod?.details?.cardHolderName ?: Constants.CARD_HOLDER_NAME
        val cardNumber: String = selectedPaymentMethod?.details?.cardNumber ?: Constants.CARD_NUMBER
        val cardExpiryMonth: String = selectedPaymentMethod?.details?.expiryDate?.split("/")?.get(0)
            ?: Constants.CARD_EXPIRY_MONTH
        val cardExpiryYear: String = selectedPaymentMethod?.details?.expiryDate?.split("/")?.get(1)
            ?: Constants.CARD_EXPIRY_YEAR
        val cardCVV: String = selectedPaymentMethod?.details?.cvv ?: Constants.CARD_CVV


        return CardPaymentParams(
            checkoutId,
            Constants.CARD_BRAND,
            cardNumber,
            cardHolder,
            cardExpiryMonth,
            cardExpiryYear,
            cardCVV
        )
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.PAYMENT_STATUS -> {
                val model = Gson().fromJson(
                    uiHelper.jsonConverterObject(
                        response?.dataObject as? LinkedTreeMap<*, *>
                    ),
                    PaymentStatusResponse::class.java
                )

                binding.clRoot.isVisible = true
                if (
                    Constants.RESULT_CODES.contains(
                        model.result.code
                    )
                ) {
                    showPaymentSuccessPopup(
                        thankYouModel?.total.toString(),
                        model.transactionNo
                    ) { paymentSuccessDialog ->
                        Handler(Looper.getMainLooper()).postDelayed({
                            paymentSuccessDialog.dismiss()
                            val bundle = Bundle()
                            bundle.putSerializable(Constants.DataParsing.MODEL, thankYouModel)
                            bundle.putInt(Constants.DataParsing.FLOW_COMING_FROM, 1)
                            uiHelper.openAndClearActivity(
                                this,
                                BookingConfirmationActivity::class.java,
                                bundle
                            )
                        }, 3000)
                    }
                } else {
                    showErrorDialog("something went wrong")
                }
            }

            Constants.API.GET_PAYMENT_METHODS -> {
                model = Gson().fromJson(
                    uiHelper.jsonConverterObject(
                        response?.dataObject as? LinkedTreeMap<*, *>
                    ),
                    PaymentMethodsResponseModel::class.java
                )
                binding.clRoot.isVisible = true

                if (model.isHost == 1)
                    binding.layoutToolBar.setAsHostToolBar(
                        R.string.payment_method,
                        supportActionBar,
                    )
                else
                    binding.layoutToolBar.setAsGuestToolBar(
                        R.string.payment_method,
                        supportActionBar,
                    )
                if (
                    model.list.filter {
                        it.paymentMethodType in
                                Constants.PaymentMethodTypes.CARD..Constants.PaymentMethodTypes.BANK_ACCOUNT
                    }.isNotEmpty()
                ) {
                    bankAccountMethod = model.list.find {
                        it.paymentMethodType == Constants.PaymentMethodTypes.BANK_ACCOUNT
                    }

                    cardsList.clear()
                    cardsList.addAll(model.list.filter { it.paymentMethodType == Constants.PaymentMethodTypes.CARD })

                    selectedPaymentMethod = model.list.find { it.isDefault == 1 }
                    if (selectedPaymentMethod == null && cardsList.isNotEmpty()) {
                        selectedPosition = 0
                        alreadyDefaultCardPosition = 0
                        sendDefaultCardRequest(false)
                    }
                    binding.rvPaymentMethods.apply {
                        isVisible = cardsList.isNotEmpty()
                        adapter = this@PaymentsActivity.adapter
                        layoutManager = LinearLayoutManager(
                            this@PaymentsActivity,
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                    }
                    adapter.notifyDataSetChanged()
                    binding.tvTitle.isVisible = cardsList.isNotEmpty()
                    binding.btnPayNow.isVisible =
                        cardsList.isNotEmpty() && flowComingFrom == FLOW_FROM_CHECKOUT
                    binding.rvPaymentMethods.isVisible = true

                    bankAccountMethod?.let {
                        binding.layoutBankAccount.tvTypeName.text =
                            bankAccountMethod?.details?.accountTitle
                        binding.layoutBankAccount.tvCardNumber.text = "IBAN (${
                            bankAccountMethod?.details?.accountNumber?.substring(
                                bankAccountMethod?.details?.accountNumber?.length?.minus(4) ?: 0
                            ) ?: ""
                        })"
                        binding.layoutBankAccount.ivCardType.setImageResource(R.drawable.ic_bank)
                        binding.layoutBankAccount.rbDefaultCard.visibility = View.INVISIBLE
                    }
                }
                handleVisibilities()
            }

            Constants.API.REMOVE_PAYMENT_METHOD -> {
                binding.clRoot.isVisible = true
                if (isCard) {
                    if (cardsList[selectedPosition].isDefault == 1) {
                        cardsList.remove(cardsList[selectedPosition])
                        if (cardsList.isNotEmpty()) {
                            selectedPosition = 0
                            alreadyDefaultCardPosition = 0
                            cardsList[0].isDefault = 1
                            sendDefaultCardRequest(false)
                        }
                    } else {
                        cardsList.remove(cardsList[selectedPosition])
                    }

                    adapter.notifyDataSetChanged()
                } else {
                    bankAccountMethod = null
                }

                handleVisibilities()
            }
            Constants.API.SET_DEFAULT_PAYMENT_METHOD -> {
                binding.clRoot.isVisible = true
                cardsList[alreadyDefaultCardPosition].isDefault = 0
                cardsList[selectedPosition].isDefault = 1
                selectedPaymentMethod = cardsList[selectedPosition]
                adapter.notifyItemChanged(alreadyDefaultCardPosition)
                adapter.notifyItemChanged(selectedPosition)
            }
            Constants.API.HOLD_PAYMENT -> {
                binding.clRoot.isVisible = true
                val model = Gson().toJson(response?.dataObject)
                val jsonObject = JSONObject(model)
                val redirectJsonObject = jsonObject.getJSONObject("redirect")
                citId = "${jsonObject.getString("id")}"
                Log.e("onSuccess: ", citId)

                startActivity(Intent(Intent.ACTION_VIEW, redirectJsonObject.getString("url").toUri()))
            }
        }
    }

    private fun handleVisibilities() {
        binding.llBankAccountInfo.isVisible = bankAccountMethod != null  && flowComingFrom == NORMAL_FLOW
        binding.btnAddAccount.isVisible = bankAccountMethod == null && flowComingFrom == NORMAL_FLOW
        binding.nsvPaymentMethods.isVisible =
            cardsList.isNotEmpty() || binding.llBankAccountInfo.isVisible
        binding.tvTitle.isVisible = cardsList.isNotEmpty()
        binding.clNoPaymentMethod.isVisible =
            !binding.nsvPaymentMethods.isVisible

    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)

        when (tag) {
            Constants.API.SET_DEFAULT_PAYMENT_METHOD -> {
                cardsList[alreadyDefaultCardPosition].isDefault = 1
                cardsList[selectedPosition].isDefault = 0
                selectedPaymentMethod = cardsList[selectedPosition]
                adapter.notifyItemChanged(alreadyDefaultCardPosition)
                adapter.notifyItemChanged(selectedPosition)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (intent.scheme == null) {
            paymentsViewModel.getPaymentMethods(
                this,
                Constants.API.GET_PAYMENT_METHODS,
                true
            )
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        if (flowComingFrom == 1)
            uiHelper.openAndClearActivity(
                this,
                MainActivity::class.java
            )
        else
            super.onBackPressed()
    }

}