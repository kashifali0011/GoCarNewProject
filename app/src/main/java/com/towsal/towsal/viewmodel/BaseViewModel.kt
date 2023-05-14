package com.towsal.towsal.viewmodel

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.view.WindowManager
import android.widget.Spinner
import android.widget.TextView
import androidx.lifecycle.ViewModel
import com.towsal.towsal.R
import com.towsal.towsal.extensions.setGradientTextColor
import com.towsal.towsal.helper.PreferenceHelper
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.respository.DataRepository
import okhttp3.MultipartBody
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Base class for view model that every view model class will extend from this class
 * */
open class BaseViewModel : ViewModel(), KoinComponent {
    val preferenceHelper: PreferenceHelper by inject()
    val uiHelper: UIHelper by inject()
    val dataRepository: DataRepository by inject()

    private var citySelection: Dialog? = null


    /**
     * Function for getting cities
     * */
    fun getCitiesList(
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse,
        type: Int = 1
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.cityList(type),
            tag,
            callback,
            showLoader
        )
    }

    fun getFuelLevels(
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse,
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getFuelLevels(),
            tag,
            callback,
            showLoader
        )
    }

    fun uploadImage(
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse,
        arrayList: ArrayList<MultipartBody.Part>
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.uploadImage(
                arrayList
            ),
            tag,
            callback,
            showLoader
        )

    }

    /**
     * Function for showing city's popup for selecting city
     * */
    fun showCitySelectionPopup(
        activity: Activity,
        callback: PopupCallback,
        list: ArrayList<String>
    ) {
        citySelection = Dialog(activity, R.style.full_screen_dialog)
        citySelection?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        citySelection?.setCancelable(false)
        citySelection?.setContentView(R.layout.popup_city_selection)
        citySelection?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        citySelection?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        val continueBtn: TextView? = citySelection?.findViewById(R.id.tvContinue)
        val spinner: Spinner? = citySelection?.findViewById(R.id.citiesList)

        uiHelper.setSpinnerWithNewLayout(activity, list, spinner!!)

        val colors = intArrayOf(Color.parseColor("#1420E4"), Color.parseColor("#0F0F7D"))
        continueBtn?.setGradientTextColor(colors)
        continueBtn?.setOnClickListener {
            try {
                callback.popupButtonClick(spinner.selectedItemPosition)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        try {
            citySelection?.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Function for dismiss city selection popup
     * */
    fun dismissCitySelectionDialog() {
        if (citySelection != null) {
            citySelection?.dismiss()
        }
    }


}