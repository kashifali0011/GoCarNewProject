package com.towsal.towsal.viewmodel

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.Spinner
import android.widget.TextView
import com.towsal.towsal.R
import com.towsal.towsal.databinding.PopupLocationPermissionBinding
import com.towsal.towsal.extensions.setGradientTextColor
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.respository.DataRepository
import com.towsal.towsal.utils.Constants

/**
 * View model for language selection process
 * */
class LanguageSelectionViewModel :
    BaseViewModel() {
    private var locationPermission: Dialog? = null

    /**
     * Function for showing location permission popup
     * */
    @SuppressLint("SetTextI18n")
    fun showLocationPermissionPopup(activity: Activity, callback: PopupCallback) {
        locationPermission = Dialog(activity, R.style.full_screen_dialog)
        val binding = PopupLocationPermissionBinding.bind(
            LayoutInflater.from(activity).inflate(R.layout.popup_location_permission, null)
        )
        locationPermission?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        locationPermission?.setCancelable(false)
        locationPermission?.setContentView(binding.root)
        locationPermission?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        locationPermission?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        val colors = intArrayOf(Color.parseColor("#1420E4"), Color.parseColor("#0F0F7D"))
        binding.tvAllow.setGradientTextColor(colors)
        val allowBtn: TextView? = locationPermission?.findViewById(R.id.tvAllow)
        val dontAllowBtn: TextView? = locationPermission?.findViewById(R.id.tvDontAllow)

        dontAllowBtn?.setOnClickListener {
            try {
                callback.popupButtonClick(Constants.PopupCallbackConstants.PermissionDontAllow)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        allowBtn?.setOnClickListener {
            try {
                callback.popupButtonClick(Constants.PopupCallbackConstants.PermissionAllow)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        try {
            locationPermission?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Function for dismiss location permission popup
     * */
    fun dismissLocationPermissionDialog() {
        if (locationPermission != null) {
            locationPermission?.dismiss()
        }
    }


}
