package com.towsal.towsal.viewmodel

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import com.towsal.towsal.R
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.cardetails.UserInfoResponseModel
import com.towsal.towsal.network.serializer.cardetails.UserInfoStepFourModel
import com.towsal.towsal.network.serializer.cardetails.UserInfoStepThreeModel
import com.towsal.towsal.respository.DataRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * View model class fro user information module
 * */
class UserInformationViewModel :
    BaseViewModel() {

    private var userInfoResponseModel = UserInfoResponseModel()

    /**
     * Function for creating request body
     * */
    fun requestBody(value: String): RequestBody? {
        return value
            .toRequestBody("text/plain".toMediaTypeOrNull())
    }

    /**
     * Function for saving profile photo to server
     * */
    fun saveProfilePhoto(
        profilePhoto: String?,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        val hasMap: HashMap<String?, String?> = HashMap()
        hasMap[DataKeyValues.Profile.PROFILE_IMAGE] = profilePhoto
        hasMap[DataKeyValues.CarDetails.STEP] = "2"
        dataRepository.callApi(
            dataRepository.network.apis()!!.saveUserProfileImage(
                hasMap
            ),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Save user license information
     * */
    fun saveLicenseInformation(
        stepThreeModel: UserInfoStepThreeModel,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.saveLicenseInformation(
                stepThreeModel
            ),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Save user card information
     * */
    fun saveCardInfo(
        paymentInfoModel: UserInfoStepFourModel,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        val hasMap: HashMap<String?, RequestBody?> = HashMap()
        hasMap[DataKeyValues.CarDetails.STEP] = requestBody("2")
        dataRepository.callApi(
            dataRepository.network.apis()!!.savePaymentInfo(
                paymentInfoModel
            ),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for showing popup dialog for driving license guidelines
     * */
    @SuppressLint("SetTextI18n")
    fun showIstamaraIdInfoPopup(activity: Activity, string: String) {
        val nationalIdInfo: Dialog? = Dialog(activity, R.style.full_screen_dialog)
        nationalIdInfo?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        nationalIdInfo?.setCancelable(false)
        nationalIdInfo?.setContentView(R.layout.popup_identification_status)
        nationalIdInfo?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        nationalIdInfo?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val crossBtn: ImageButton? = nationalIdInfo?.findViewById(R.id.cross)
        val text: TextView? = nationalIdInfo?.findViewById(R.id.textBoldID)
        if (string.isNullOrEmpty())
            text?.text = uiHelper.spanString(
                activity.getString(R.string.take_snap_of_your_driving_licence),
                activity.getString(R.string.driving_licence),
                R.font.bold,
                activity
            )
        else {
            text?.text = activity.getString(R.string.take_copy_of) + " " + string
        }
        crossBtn?.setOnClickListener {
            try {
                nationalIdInfo.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (nationalIdInfo != null) {
            try {
                nationalIdInfo.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setUserResponseModelValue(
        userInfoResponseModel: UserInfoResponseModel
    ) {
        this.userInfoResponseModel = userInfoResponseModel
    }

    fun getUserInfoResponseModel(): UserInfoResponseModel {
        return userInfoResponseModel
    }
}