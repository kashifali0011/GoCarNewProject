package com.towsal.towsal.views.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityLanguageSelectionBinding
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.cities.CitiesListModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.LanguageSelectionViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for language selection
 * */
class LanguageSelectionActivity : BaseActivity(), OnNetworkResponse, PopupCallback,
    ActivityCompat.OnRequestPermissionsResultCallback {

    lateinit var binding: ActivityLanguageSelectionBinding
    var value = 0
    private var fromWithinAPP = false
    private val languageSelectionViewModel: LanguageSelectionViewModel by viewModel()

    var languageType = Constants.ENGLISH
    var cityModel = CitiesListModel()

    var permissionBox: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_language_selection)
        binding.activity = this
        uiHelper.hideActionBar(supportActionBar)
        if (intent.extras != null) {
            value = intent.extras?.getInt(Constants.BUNDLE_VALUE, 0)!!
            fromWithinAPP = intent.extras?.getBoolean(Constants.IS_FROM_WITHIN_APP)!!
            if (value == Constants.CHANGE_LANGUAGE) {
                binding.ivCancel.visibility =
                    View.VISIBLE //visibility hiding when comes through the app
            }

            if (fromWithinAPP) {
                binding.ibBack.visibility =
                    View.VISIBLE  //visibility hiding when comes through the app
            }
        }

    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.english -> {
                languageType = Constants.ENGLISH
                handleReDirections()
            }
            R.id.arabic -> {
                languageType = Constants.ARABIC
                handleReDirections()
            }
            R.id.ib_back -> {
                finish()
            }
            R.id.ivCancel -> {
                finish()
            }
        }
    }

    /**
     * Function for changing language
     * */
    private fun handleReDirections() {
        if (value == Constants.CHANGE_LANGUAGE) {
            //For Api purpose
            setLanguageVal(languageType)
            uiHelper.openAndClearActivity(this, MainActivity::class.java)
        } else {
            //trying to set box
            if (!fromWithinAPP && languageType == languageSelected) {
                languageSelectionViewModel.showLocationPermissionPopup(this, this)
            } else {
                languageSelected = languageType
                permissionBox = setLanguageVal(languageType)
            }
        }
    }

    /**
     * Function for sending broad cast to the activity
     * */
    private fun setLanguageVal(language: String): Boolean {
        preferenceHelper.saveLanguage(Constants.LANGUAGE, languageType)
        val intent = Intent(Constants.BROCAST_RECIEVER)
        intent.putExtra(Constants.TYPE, Constants.CHANGE_LANGUAGE)
        intent.putExtra(Constants.LANGUAGE, language)
        sendBroadcast(intent)
        return true
    }

    /**
     * Function for writing data in preferences
     * */
    private fun setSavedValues() {
        preferenceHelper.setBolOther(Constants.DataParsing.IS_FIRST_RUN, false)
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.CITIES_LIST -> {
                cityModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    CitiesListModel::class.java
                )
                if (cityModel.cityList.isNotEmpty()) {
                    languageSelectionViewModel.showCitySelectionPopup(
                        this,
                        this,
                        ArrayList(cityModel.cityList.map {
                            it.city
                        })
                    )
                }
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }

    override fun popupButtonClick(value: Int) {
        Log.d("valueee", "value = " + value)
        languageSelectionViewModel.dismissLocationPermissionDialog()
        when (value) {
            Constants.PopupCallbackConstants.PermissionAllow -> {
                if (cityModel.cityList.isNotEmpty()) {
                    preferenceHelper.setIntOther(
                        Constants.DataParsing.CITY_SELECTED_ID,
                        cityModel.cityList[value].id
                    )
                    setSavedValues()
                    uiHelper.openAndClearActivity(this, IntroActivity::class.java)
                } else {
                    uiHelper.requestPermission(PERMISSION, this)            //it was initially set
                }
            }
            Constants.PopupCallbackConstants.PermissionDontAllow -> {
                if (cityModel.cityList.isNotEmpty()) {
                    preferenceHelper.setIntOther(
                        Constants.DataParsing.CITY_SELECTED_ID,
                        cityModel.cityList[value].id
                    )
                    preferenceHelper.setString(
                        Constants.DataParsing.CITY_SELECTED,
                        cityModel.cityList[value].city
                    )
                    preferenceHelper.setLong(
                        Constants.DataParsing.CITY_LAT,
                        cityModel.cityList[value].lat.toLong()
                    )
                    preferenceHelper.setLong(
                        Constants.DataParsing.CITY_LNG,
                        cityModel.cityList[value].lng.toLong()
                    )
                    setSavedValues()
                    uiHelper.openAndClearActivity(this, IntroActivity::class.java)
                } else {
                    languageSelectionViewModel.getCitiesList(true, Constants.API.CITIES_LIST, this)
                }
            }
            else -> {
                if (cityModel.cityList.isNotEmpty()) {
                    preferenceHelper.setIntOther(
                        Constants.DataParsing.CITY_SELECTED_ID,
                        cityModel.cityList[value].id
                    )
                    preferenceHelper.setString(
                        Constants.DataParsing.CITY_SELECTED,
                        cityModel.cityList[value].city
                    )
                    setSavedValues()
                    languageSelectionViewModel.dismissCitySelectionDialog()
                    uiHelper.openAndClearActivity(this, IntroActivity::class.java)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String?>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            languageSelectionViewModel.dismissLocationPermissionDialog()
            setSavedValues()
            uiHelper.openAndClearActivity(this, IntroActivity::class.java)
        } else {
            languageSelectionViewModel.getCitiesList(true, Constants.API.CITIES_LIST, this)
        }
    }

    override fun onAfterLocaleChanged() {
        super.onAfterLocaleChanged()
        Handler(Looper.getMainLooper()).postDelayed({
            languageSelectionViewModel.showLocationPermissionPopup(this, this)
        }, 500)
    }
}