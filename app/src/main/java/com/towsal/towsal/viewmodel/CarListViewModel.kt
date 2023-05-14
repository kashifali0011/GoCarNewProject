package com.towsal.towsal.viewmodel

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.carlist.CarInformationModel
import com.towsal.towsal.network.serializer.carlist.step2.CarAvailabilityPostModel
import com.towsal.towsal.network.serializer.carlist.step3.CarPhotoListModel
import com.towsal.towsal.network.serializer.carlist.step4.VehicleProtectionPostModel
import com.towsal.towsal.network.serializer.carlist.step5.CarSafetyInfoModel
import com.towsal.towsal.utils.Constants
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * View model for car listing process
 * */
class CarListViewModel : BaseViewModel() {

    var carId = -1

    /**
     * Function for getting car details of car listing process
     * */
    fun getCarList(
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse,
        step: Int = 0
    ) {
        dataRepository.callApi(
            if (carId != -1)
                dataRepository.network.apis()!!.getCarsList(
                    carId,
                    step
                )
            else dataRepository.network.apis()!!.getCarsList(
                step
            ),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for getting creating request body
     * */
    private fun requestBody(value: String): RequestBody {
        return value
            .toRequestBody("text/plain".toMediaTypeOrNull())
    }

    /**
     * Function for posting car information to the server
     * */
    fun sendCarInfo(
        carInfoModel: CarInformationModel,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        val hasMap: HashMap<String?, Any?> = HashMap()
        hasMap[DataKeyValues.USER_ID] = carInfoModel.user_id.toString()
        hasMap[DataKeyValues.CarInfo.ODOMETER_ID] = carInfoModel.odometer.toString()
        hasMap[DataKeyValues.CarInfo.MODEL_ID] = carInfoModel.model_id.toString()
        hasMap[DataKeyValues.CarInfo.MAKE_ID] = carInfoModel.make_id.toString()
        hasMap[DataKeyValues.CarInfo.CAR_FEATURES_SEND] =
            carInfoModel.car_features
        hasMap[DataKeyValues.CarInfo.CAR_REGISTRATION_CITY_ID] =
            carInfoModel.car_registration_city_id.toString()
        hasMap[DataKeyValues.CarInfo.COUNTRY_NAME] =
            carInfoModel.country_name
        hasMap[DataKeyValues.CarInfo.DESCRIPTION] = carInfoModel.description
        hasMap[DataKeyValues.CarInfo.LICENCE_PLATE_NUM] =
            carInfoModel.licence_plate_num
        hasMap[DataKeyValues.CarInfo.LIST_PERSONAL_CAR] =
            carInfoModel.list_personal_car.toString()
        hasMap[DataKeyValues.CarInfo.CITY_NAME] = carInfoModel.city_name
        hasMap[DataKeyValues.CarInfo.ZIP_CODE] = carInfoModel.zip_code
        carInfoModel.year_id.toString().also {
            hasMap[DataKeyValues.CarInfo.YEAR_ID] = it
        }
        hasMap[DataKeyValues.CarInfo.VIN_CHASIS_NUM] = carInfoModel.vin_chasis_num
        hasMap[DataKeyValues.CarInfo.TRANSMISSION] =
            carInfoModel.transmission
        hasMap[DataKeyValues.CarInfo.NEVER_MAJOR_ACCIDENT] =
            carInfoModel.never_major_accident.toString()
        hasMap[DataKeyValues.CarInfo.STEP] = carInfoModel.step.toString()
        hasMap[DataKeyValues.CarInfo.PIN_ADDRESS] = carInfoModel.pin_address
        hasMap[DataKeyValues.CarInfo.STATE_NAME] = carInfoModel.state_name
        hasMap[DataKeyValues.CarInfo.PIN_LAT] = carInfoModel.pin_lat.toString()
        hasMap[DataKeyValues.CarInfo.PIN_LNG] = carInfoModel.pin_long.toString()
        hasMap[DataKeyValues.CarInfo.STREET_ADDRESS] =
            carInfoModel.street_address
        hasMap[DataKeyValues.CarInfo.LATEST_TECHNICAL_REPORT] =
            carInfoModel.technicalReportPath
        hasMap[DataKeyValues.CarInfo.SNAP_OF_ISTAMARA] =
            carInfoModel.istamarImagePath
        hasMap[DataKeyValues.CarInfo.CAR_ID] = carId
        hasMap[DataKeyValues.HOST_TYPE] = carInfoModel.hostType
        if (carInfoModel.hostType == Constants.HostType.COMPANY && carInfoModel.isSelectionMade) {
            hasMap[DataKeyValues.VAT_REGISTRATION_SNAP] = carInfoModel.vat_registration_snap
            hasMap[DataKeyValues.COMPANY_REGISTRATION_SNAP] = carInfoModel.nationalIDImagePath
            hasMap[DataKeyValues.User.CR_NUMBER] = carInfoModel.crNumber
        } else if (carInfoModel.isSelectionMade) {
            hasMap[DataKeyValues.CarInfo.SNAP_OF_NATIONAL_ID] =
                carInfoModel.nationalIDImagePath

            hasMap[DataKeyValues.CarInfo.NATIONAL_ID] =
                carInfoModel.national_id
        }

        dataRepository.callApi(
            dataRepository.network.apis()!!.carInformationSave(
                map = hasMap
            ),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for posting car availability details
     * */
    fun sendCarInfo(
        data: CarAvailabilityPostModel,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.carInformationSave(
                data
            ),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for saving car photos
     * */
    fun sendCarPhotos(
        listPhotos: ArrayList<HashMap<String, Any>>,
        step: Int,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        val map = HashMap<String, Any>()
        map[DataKeyValues.CarInfo.CAR_PHOTOS] = listPhotos
        map[DataKeyValues.CarInfo.CAR_ID] = carId
        dataRepository.callApi(
            dataRepository.network.apis()!!.carInformationSave(
                step, map
            ),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for saving car vehicle protection
     * */
    fun sendVehicleProtection(
        model: VehicleProtectionPostModel,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        val hasMap: HashMap<String?, Any?> = HashMap()
        var haveInsurance = 0
        if (model.have_insurance) {
            haveInsurance = 1
        }
        hasMap[DataKeyValues.CarInfo.STEP] =
            model.step.toString()
        hasMap[DataKeyValues.CarInfo.HAVE_INSURANCE] = haveInsurance.toString()
        if (model.have_insurance) {
            hasMap[DataKeyValues.CarInfo.INSURANCE_NUMBER] =
                model.insurance_number
            hasMap[DataKeyValues.CarInfo.DATE_OF_EXP] =
                model.date_of_exp
            hasMap[DataKeyValues.CarInfo.LIABILITY_COVERED] =
                model.liability_covered
            var costCovered = 0
            if (model.insurance_covered_cost) {
                costCovered = 1
            }
            hasMap[DataKeyValues.CarInfo.INSURANCE_COST_COVERED] =
                costCovered.toString()
            hasMap[DataKeyValues.CarInfo.SNAP_INSURANCE] =
                model.snap_insurance

        }
        hasMap[DataKeyValues.CarInfo.CAR_ID] = carId
        dataRepository.callApi(
            dataRepository.network.apis()!!.carInformationSave(
                hasMap
            ),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for saving car safety standard check
     * */
    fun sendSafetyStandardCheck(
        model: CarSafetyInfoModel,
        showLoader: Boolean,
        tag: Int,
        callback: OnNetworkResponse
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.carInformationSave(
                model.step,
                model.userAgreedToTermsAndConditions,
                carId
            ),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for showing nationalIdInfo popup
     * */
    @SuppressLint("SetTextI18n")
    fun showNationIdInfoPopup(activity: Activity) {
        val nationalIdInfo: Dialog? = Dialog(activity, R.style.full_screen_dialog)
        nationalIdInfo?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        nationalIdInfo?.setCancelable(false)
        nationalIdInfo?.setContentView(R.layout.popup_national_id_info)
        nationalIdInfo?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        nationalIdInfo?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val crossBtn: ImageButton? = nationalIdInfo?.findViewById(R.id.cross)
        val text: TextView? = nationalIdInfo?.findViewById(R.id.textBoldID)
        text?.text = uiHelper.spanString(
            activity.getString(R.string.take_snap_of_front_side_of_your_national_id),
            activity.getString(R.string.national_id),
            R.font.bold,
            activity
        )
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

    /**
     * Function for showing driving license popup
     * */
    @SuppressLint("SetTextI18n")
    fun showIstamaraIdInfoPopup(activity: Activity) {
        val nationalIdInfo: Dialog? = Dialog(activity, R.style.full_screen_dialog)
        nationalIdInfo?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        nationalIdInfo?.setCancelable(false)
        nationalIdInfo?.setContentView(R.layout.popup_istamara_info)
        nationalIdInfo?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        nationalIdInfo?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val crossBtn: ImageButton? = nationalIdInfo?.findViewById(R.id.cross)
        val text: TextView? = nationalIdInfo?.findViewById(R.id.textBoldID)
        text?.text = uiHelper.spanString(
            activity.getString(R.string.take_snap_of_your_istamara_id),
            activity.getString(R.string.istamara_Id),
            R.font.bold,
            activity
        )
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

    /**
     * Function for showing car insurance popup
     * */
    @SuppressLint("SetTextI18n")
    fun showInsuranceInfoPopup(activity: Activity) {
        val nationalIdInfo: Dialog? = Dialog(activity, R.style.full_screen_dialog)
        nationalIdInfo?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        nationalIdInfo?.setCancelable(false)
        nationalIdInfo?.setContentView(R.layout.popup_insurance_info)
        nationalIdInfo?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        nationalIdInfo?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        val crossBtn: ImageButton? = nationalIdInfo?.findViewById(R.id.cross)
        val text: TextView? = nationalIdInfo?.findViewById(R.id.textBoldID)
        text?.text = uiHelper.spanString(
            activity.getString(R.string.take_a_snap_of_your_insurance),
            activity.getString(R.string.insurance_doc),
            R.font.bold,
            activity
        )
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

    /**
     * Function for getting makes of cars
     * */
    fun getMakes(
        yearId: Int,
        callback: OnNetworkResponse,
        tag: Int
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.makeSelection(yearId),
            tag,
            callback,
            false
        )
    }

    /**
     * Function for getting models of cars
     * */
    fun getModels(
        yearId: Int,
        makeId: Int,
        callback: OnNetworkResponse,
        tag: Int
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.modelSelection(yearId, makeId),
            tag,
            callback,
            false
        )
    }

    /**
     * Function for getting car documents
     * */
    fun getCarDocuments(carId: Int, callback: OnNetworkResponse, tag: Int) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getCarDocs(
                carId
            ),
            tag,
            callback,
            true
        )
    }

    fun updateCarDocs(
        carId: Int,
        list: List<CarPhotoListModel>,
        callback: OnNetworkResponse,
        tag: Int
    ) {
        val map = HashMap<String, String>()
        for (item in list) {
            if (item.status == -1) {
                map[item.type] = item.url
            }
        }
        dataRepository.callApi(
            dataRepository.network.apis()!!.updateCarDocs(carId, map),
            tag,
            callback,
            true
        )
    }

    fun getFeaturesList(
        callback: OnNetworkResponse,
        tag: Int
    ) {
        dataRepository
            .callApi(
                dataRepository.network.apis()!!.getCarFeaturesList(),
                tag,
                callback,
                true
            )
    }

    fun getCarAttributes(
        tag: Int,
        callback: OnNetworkResponse,
        showLoader: Boolean
    ) {
        dataRepository.callApi(
            dataRepository.network.apis()!!.getCarAttributes(),
            tag,
            callback,
            showLoader
        )
    }

    /**
     * Function for showing report listing error
     * */
    @SuppressLint("SimpleDateFormat")
    fun showFullImage(
        activity: Activity,
        imageUrl: String
    ) {
        val calendarPopup = Dialog(activity, R.style.full_screen_dialog_2)
        calendarPopup.requestWindowFeature(Window.FEATURE_NO_TITLE)
        calendarPopup.setCancelable(false)
        calendarPopup.setContentView(R.layout.full_image_show_layout)
        calendarPopup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        calendarPopup.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        val ivBack: ImageView? = calendarPopup.findViewById(R.id.ivBack)
        val ivFullPicShow: ImageView = calendarPopup.findViewById(R.id.ivFullPicShow)
        uiHelper.glideLoadImage(
            activity,
            imageUrl,
            ivFullPicShow
        )
        ivBack?.setOnClickListener {
            try {
                calendarPopup.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (calendarPopup != null) {
            try {
                calendarPopup.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Function for showing report listing error
     * */
    @SuppressLint("SimpleDateFormat")
    fun showFullWebView(
        activity: Activity,
        pdfUrl: String
    ) {
        val calendarPopup = Dialog(activity, R.style.full_screen_dialog_2)
        calendarPopup.requestWindowFeature(Window.FEATURE_NO_TITLE)
        calendarPopup.setCancelable(false)
        calendarPopup.setContentView(R.layout.full_web_view_show_layout)
        calendarPopup.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        calendarPopup.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT
        )
        val ivBack: ImageView? = calendarPopup.findViewById(R.id.ivBack)
        val wvPdfLoader: WebView = calendarPopup.findViewById(R.id.wvPdfLoader)
        Handler(Looper.getMainLooper()).postDelayed({
            wvPdfLoader.webViewClient = WebViewClient()
            wvPdfLoader.settings.setSupportZoom(false)
            wvPdfLoader.settings.javaScriptEnabled = true
            wvPdfLoader.settings.builtInZoomControls = false
            //showLoaderDialog()
            Handler(Looper.getMainLooper()).postDelayed({
                wvPdfLoader.loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=$pdfUrl")
            }, 500)
          //  wvPdfLoader.loadUrl(pdfUrl)
            wvPdfLoader.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    //   hideLoaderDialog()
                }
            }
        }, 500)

        ivBack?.setOnClickListener {
            try {
                calendarPopup.dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        if (calendarPopup != null) {
            try {
                calendarPopup.show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

}