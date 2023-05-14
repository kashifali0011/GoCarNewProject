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
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
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
import com.towsal.towsal.databinding.ActivityAddCreditCardBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.extensions.showPaymentSuccessPopup
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.checkoutcarbooking.BookingConfirmationResponseModel
import com.towsal.towsal.network.serializer.payment.PaymentMethodRequestModel
import com.towsal.towsal.network.serializer.payment.PaymentStatusResponse
import com.towsal.towsal.utils.CardBrand
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.Constants.AMEX_PATTERN
import com.towsal.towsal.utils.Constants.MADA_PATTERN
import com.towsal.towsal.utils.Constants.MASTER_PATTERN
import com.towsal.towsal.utils.Constants.VISA_PATTERN
import com.towsal.towsal.viewmodel.PaymentsViewModel
import org.json.JSONObject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import java.util.regex.Pattern

/**
 * Activity class for adding credit card
 * */
class AddCreditCardActivity : BaseActivity(), View.OnClickListener, OnNetworkResponse,
    ITransactionListener, OnEditorActionListener {

    lateinit var binding: ActivityAddCreditCardBinding
    val list: ArrayList<HashMap<String, Pattern>> = ArrayList()
    private var cardType: String = ""
    private val paymentsViewModel: PaymentsViewModel by viewModel()
    private lateinit var checkoutId: String
    private var resourcePath: String? = null
    var cardNumber = 0
    var isFromKey = false
    var registrationId: String = ""
    var citId = ""
    companion object {
        val visaHashMap = HashMap<String, Pattern>()
        val masterHashMap = HashMap<String, Pattern>()
        val amexHashMap = HashMap<String, Pattern>()
        val madaHashMap = HashMap<String, Pattern>()
    }

    private var providerBinder: IProviderBinder? = null

    private var thankYouModel: BookingConfirmationResponseModel? =
        BookingConfirmationResponseModel()
    private var flowComingFrom = 0


    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            /* we have a connection to the service */
            providerBinder = service as IProviderBinder
            providerBinder!!.addTransactionListener(this@AddCreditCardActivity)

            try {
                providerBinder!!.initializeProvider(Connect.ProviderMode.TEST)
            } catch (ee: PaymentException) {
                showErrorDialog(ee.message!!)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            providerBinder = null
        }
    }

    override fun onResume() {
        super.onResume()
        val intent = Intent(this, ConnectService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onPause() {
        super.onPause()
        val intent = Intent(this, ConnectService::class.java)
        stopService(intent)
        unbindService(serviceConnection)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_credit_card)
        binding.activity = this
        uiHelper.hideActionBar(supportActionBar)
        val isHost = intent?.extras?.getInt(Constants.DataParsing.IS_HOST, 0) ?: 0
        if (isHost == 1)
            binding.layoutToolBar.setAsHostToolBar(R.string.payment_method, supportActionBar)
        else binding.layoutToolBar.setAsGuestToolBar(R.string.payment_method, supportActionBar)
        addKeyListeners()
        prepareCardsPatternsList()
        addTextChangedListeners()
        if (intent.extras != null) {
            flowComingFrom = intent.extras?.getInt(Constants.DataParsing.FLOW_COMING_FROM, 0) ?: 0
            if (flowComingFrom == 1)
                thankYouModel =
                    intent.extras?.getSerializable(Constants.DataParsing.MODEL) as BookingConfirmationResponseModel
            binding.btnSave.text = if (flowComingFrom == 1)
                "Verify & pay"
            else
                binding.btnSave.text
        }
    }

    private fun addKeyListeners() {
        binding.edtCardNumber.setOnEditorActionListener(this)
        binding.edtCardHolderName.setOnEditorActionListener(this)
        binding.edtExpiryDate.setOnEditorActionListener(this)
        binding.edtCVV.setOnEditorActionListener(this)
    }

    private fun prepareCardsPatternsList() {
        visaHashMap["visa"] = VISA_PATTERN
        masterHashMap["master"] = MASTER_PATTERN
//        list.add(madaHashMap)
        list.add(visaHashMap)
        list.add(masterHashMap)
//        list.add(amexHashMap)
    }

    private fun addTextChangedListeners() {
        binding.edtCardNumber.addTextChangedListener {
            checkCardType(it.toString().replace(" ", ""))

            if (it.toString().isNotEmpty()) {
                if (it.toString().length % 5 == 0 && cardNumber == it.toString().length + 1) {
                    binding.edtCardNumber.setText(
                        it.toString().substring(0, it.toString().length - 1)
                    )

                } else if (
                    it.toString().length % 5 == 0
                ) {
                    binding.edtCardNumber.setText(
                        "${it.toString().substring(0, it.toString().length - 1)} ${
                            it.toString().toCharArray()[it.toString().length - 1]
                        }"
                    )
                }

                binding.edtCardNumber.setSelection(
                    binding.edtCardNumber.text.toString().length
                )
                cardNumber = binding.edtCardNumber.text.toString().length
            }
        }

        binding.edtExpiryDate.addTextChangedListener {
            if (it.toString().isNotEmpty()) {
                if (it.toString().length >= 3 && !it.toString().contains("/")) {
                    val stringBuilder = StringBuilder()
                    stringBuilder.append(it.toString().substring(0, 2))
                    stringBuilder.append("/" + it.toString().substring(2, it.toString().length))
                    binding.edtExpiryDate.setText(stringBuilder)
                    binding.edtExpiryDate.setSelection(stringBuilder.length)
                } else if (it.toString().length <= 3 && it.toString().contains("/")) {
                    binding.edtExpiryDate.setText(it.toString().replace("/", ""))
                    binding.edtExpiryDate.setSelection(it.toString().replace("/", "").length)
                }
            }
        }
    }

    private fun checkCardType(value: String) {
        for (key in list) {
            if (key.values.first().matcher(value).matches()) {
                Log.e("checkCardType: ", key.keys.first())
                setUiAccordingToCardType(key.keys.first())
                break
            }
        }

    }

    private fun setUiAccordingToCardType(cardType: String) {
        this.cardType = cardType
        when (cardType) {
            CardBrand.AMEX.value -> {
                binding.ivCardBrand.setImageResource(R.drawable.ic_amex)
            }
            CardBrand.MADA.value -> {
                binding.ivCardBrand.setImageResource(R.drawable.ic_mada)
            }
            CardBrand.MASTER.value -> {
                binding.ivCardBrand.setImageResource(R.drawable.ic_master)
            }
            CardBrand.VISA.value -> {
                binding.ivCardBrand.setImageResource(R.drawable.ic_visa)
            }
            else -> {
                binding.edtCardNumber.error = getString(R.string.invalid_card_number)
                return
            }
        }
        binding.edtCardNumber.error = null
    }


    override fun onClick(view: View) {
        view.preventDoubleClick()
        when (view) {
            binding.btnSave -> {
                isFromKey = false
                callApi()
            }

            binding.layoutToolBar.ivArrowBack -> {
                finish()
            }
        }
    }

    private fun callApi() {
        if (checkValidations()) {
            paymentsViewModel.prepareCheckout(
                binding.edtCardNumber.text.toString().replace(" ", "").takeLast(4),
                this,
                Constants.API.PREPARE_CHECKOUT,
                true
            )
        }
    }

    private fun checkValidations() = if (binding.edtCardNumber.text.toString().isEmpty()) {
        if (!isFromKey)
            uiHelper.showLongToastInCenter(this, "Card number is required")
        binding.edtCardNumber.requestFocus()
        false
    } else if (
        cardType == ""
    ) {
        if (!isFromKey) {
            uiHelper.showLongToastInCenter(this, "Invalid card number")
        }
        binding.edtCardNumber.requestFocus()
        false
    } else if (
        binding.edtCardHolderName.text.toString().isEmpty()
    ) {
        if (!isFromKey) {
            uiHelper.showLongToastInCenter(this, "Card holder name is required")
        }
        binding.edtCardHolderName.requestFocus()
        false
    } else if (binding.edtExpiryDate.text.toString().isEmpty()) {
        if (!isFromKey) {
            uiHelper.showLongToastInCenter(this, "Card expiry is required")
        }
        binding.edtExpiryDate.requestFocus()
        false
    } else if (
        binding.edtExpiryDate.text.toString()
            .split("/")[0].toInt() > 12 || !binding.edtExpiryDate.text.toString().contains("/")
    ) {
        if (!isFromKey) {
            uiHelper.showLongToastInCenter(this, "Card expiry is not correct")
        }
        binding.edtExpiryDate.requestFocus()
        false
    } else if (!checkCardExpiry()) {
        if (!isFromKey) {
            uiHelper.showLongToastInCenter(this, "Your card is expired")
        }
        binding.edtExpiryDate.requestFocus()
        false
    } else if (binding.edtCVV.text.toString().isEmpty()) {
        if (!isFromKey) {
            uiHelper.showLongToastInCenter(this, "Card cvv is required")
        }
        binding.edtCVV.requestFocus()
        false
    } else true


    private fun checkCardExpiry(): Boolean {
        val currentDate = Calendar.getInstance().time
        val expiryDate = Calendar.getInstance()
        expiryDate.set(Calendar.MONTH, binding.edtExpiryDate.text.toString().split("/")[0].toInt())
        expiryDate.set(
            Calendar.YEAR,
            ("20" + binding.edtExpiryDate.text.toString().split("/")[1]).toInt()
        )
        return expiryDate.time > currentDate
    }


    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.PREPARE_CHECKOUT -> {
                val model = Gson().toJson(response?.dataObject)
                val jsonObject = JSONObject(model)
                this.checkoutId = jsonObject.getString("checkoutId")
                binding.btnSave.isClickable = false
                requestCheckoutInfo(checkoutId)
            }
            Constants.API.CARD_REGISTRATION_STATUS -> {
                val model = Gson().toJson(response?.dataObject)
                val jsonObject = JSONObject(model)
                registrationId = jsonObject.getString("registration_id")
                if (
                    flowComingFrom == 1
                ) {
                    paymentsViewModel.holdPayment(
                        "addcreditcard://result",
                        checkOutId = thankYouModel?.checkout_id,
                        this,
                        Constants.API.HOLD_PAYMENT,
                        true
                    )
                } else {
                    finish()
                }
            }

            Constants.API.HOLD_PAYMENT -> {
                val model = Gson().toJson(response?.dataObject)
                val jsonObject = JSONObject(model)
                val redirectJsonObject = jsonObject.getJSONObject("redirect")
                citId = "${jsonObject.getString("id")}"
                Log.e("onSuccess: ", citId)

                startActivity(Intent(Intent.ACTION_VIEW, redirectJsonObject.getString("url").toUri()))
            }

            Constants.API.PAYMENT_STATUS -> {
                val model = Gson().fromJson(
                    uiHelper.jsonConverterObject(
                        response?.dataObject as? LinkedTreeMap<*, *>
                    ),
                    PaymentStatusResponse::class.java
                )
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
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        binding.btnSave.isClickable = true
        uiHelper.showLongToastInCenter(this, response?.message)
    }

    override fun transactionCompleted(transaction: Transaction) {
        if (transaction.transactionType == TransactionType.SYNC) {
            /* check the status of synchronous transaction */
            prepareRegistrationStatusRequest()
        } else {
            /* wait fot the callback in the onNewIntent() */
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(transaction.redirectUrl)))
        }
    }

    override fun transactionFailed(p0: Transaction, paymentError: PaymentError) {
        hideLoaderDialog()
        Log.e("transactionFailed:", Gson().toJson(paymentError))
        showErrorDialog(paymentError)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.scheme.equals("addcreditcard")) {
            prepareRegistrationStatusRequest()
        }else if (intent.scheme.equals("payment")) {
            preparePaymentStatusRequest()
        }
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
    private fun prepareRegistrationStatusRequest() {
        paymentsViewModel.getCarRegistrationStatus(
            resourcePath ?: "",
            this,
            Constants.API.CARD_REGISTRATION_STATUS,
            true
        )
    }

    /**
     * Function for requesting payment information
     * */
    private fun requestCheckoutInfo(checkoutId: String) {
        try {
            providerBinder!!.requestCheckoutInfo(checkoutId)
        } catch (e: PaymentException) {
            e.message?.let { showAlertDialog(it) }
        }
    }

    override fun paymentConfigRequestSucceeded(checkoutInfo: CheckoutInfo) {
        super.paymentConfigRequestSucceeded(checkoutInfo)
        resourcePath = checkoutInfo.resourcePath

        runOnUiThread {
            pay(checkoutId)
        }
    }

    override fun paymentConfigRequestFailed(paymentError: PaymentError) {
        super.paymentConfigRequestFailed(paymentError)
        hideLoaderDialog()
        Log.e("paymentConfigRequestFailed:", paymentError.errorMessage)
        showErrorDialog(paymentError)
    }

    /**
     * Function for processing payment
     * */
    private fun pay(checkoutId: String) {
        try {
            val paymentParams = createPaymentParams(checkoutId)
            paymentParams.shopperResultUrl = "addcreditcard"
            val transaction = Transaction(paymentParams)
            showLoaderDialog()
            providerBinder!!.registerTransaction(transaction)
        } catch (e: PaymentException) {
            showErrorDialog(e.error)
        }
    }

    /**
     * Function for creating payment parameters
     * */
    private fun createPaymentParams(checkoutId: String): CardPaymentParams {
        val cardHolder = binding.edtCardHolderName.text.toString()
        val cardNumber = binding.edtCardNumber.text.toString()
        val cardExpiryMonth = binding.edtExpiryDate.text.toString().split("/")[0]
        val cardExpiryYear = "20" + binding.edtExpiryDate.text.toString().split("/")[1]
        val cardCVV = binding.edtCVV.text.toString()

        return CardPaymentParams(
            checkoutId,
            cardType.uppercase(),
            cardNumber,
            cardHolder,
            cardExpiryMonth,
            cardExpiryYear,
            cardCVV
        )
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        isFromKey = true
        callApi()
        return false
    }


}