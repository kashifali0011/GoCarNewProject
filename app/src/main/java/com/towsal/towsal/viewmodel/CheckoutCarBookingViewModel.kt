package com.towsal.towsal.viewmodel

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.method.LinkMovementMethod
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.towsal.towsal.R
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.checkoutcarbooking.BookingPostModel
import com.towsal.towsal.network.serializer.checkoutcarbooking.GetCarBookingModel

/**
 * View model for checkout screen
 * */
class CheckoutCarBookingViewModel :
    BaseViewModel() {

    /**
     * Function for getting checkout details
     * */
    fun getCheckoutDetail(
        model: GetCarBookingModel,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {

        dataRepository.callApi(
            dataRepository.network.apis()!!.getCheckoutDetail(model),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for booking the trip
     * */
    fun saveCarBooking(
        model: BookingPostModel,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.saveCheckOutDetail(model),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for editing trip
     * */
    fun editTripBooking(
        model: BookingPostModel,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.editCheckoutDetail(model),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for receipt and invoice details
     * */
    fun getReceiptInvoiceDetails(
        checkOutId: Int,
        type: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getReceiptInvoiceDetails(checkOutId, type),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for showing privacy policy
     * */
    @SuppressLint("SetTextI18n")
    fun showTermConditionPrivacyPolicyPopup(activity: Activity, callBack: PopupCallback) {
        val termConditionPopup: Dialog? = Dialog(activity, R.style.full_screen_dialog)
        termConditionPopup?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        termConditionPopup?.setCancelable(false)
        termConditionPopup?.setContentView(R.layout.popup_term_condition_privacy_policy)
        termConditionPopup?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        termConditionPopup?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        var textView47: TextView = termConditionPopup?.findViewById(R.id.textView47)!!

        //textView47.autoLinkMask = Linkify.ALL
        textView47.movementMethod = LinkMovementMethod.getInstance()
        val agree: Button? = termConditionPopup.findViewById(R.id.agree)

        agree?.setOnClickListener {
            try {
                callBack.popupButtonClick(9)
                termConditionPopup.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (termConditionPopup != null) {
            try {
                termConditionPopup.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}