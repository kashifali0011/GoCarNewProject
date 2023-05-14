package com.towsal.towsal.views.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import com.anggrayudi.storage.callback.FileCallback
import com.anggrayudi.storage.file.copyFileTo
import com.baoyz.actionsheet.ActionSheet
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityVehicleProtectionBinding
import com.towsal.towsal.extensions.*
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.ImagesResponseModel
import com.towsal.towsal.network.serializer.carlist.step4.Step4Model
import com.towsal.towsal.network.serializer.carlist.step4.VehicleProtectionPostModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil
import com.towsal.towsal.viewmodel.CarListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class VehicleProtectionActivity : BaseActivity(), View.OnClickListener, OnNetworkResponse,
    ActionSheet.ActionSheetListener, DatePicker.OnDateChangedListener {

    lateinit var binding: ActivityVehicleProtectionBinding
    val carListViewModel: CarListViewModel by viewModel()
    var step4Model: Step4Model? = null
    private var insurancePath = ""
    var imagesToUpload = ArrayList<MultipartBody.Part>()
    private var vehicleProtection = VehicleProtectionPostModel()
    private var expiryDate = ""
    var retryCount = 0

    var insurancePolicyUrl = ""
    var pdfTechnicalUrl = ""


    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        if (it.resultCode == RESULT_OK) {
            val files = intentToDocumentFiles(it.data)

            if (files.isNotEmpty()) {
                val destFile = cacheDir

                lifecycleScope.launch(Dispatchers.IO) {
                    (files.first()).copyFileTo(
                        this@VehicleProtectionActivity,
                        destFile.path.toString(),
                        null,
                        callback = createFileCallback()
                    )
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vehicle_protection)
        binding.activity = this
        actionBarSetting()
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.yesInsurance) {
                binding.insuranceLayout.visibility = View.VISIBLE
            } else {
                binding.insuranceLayout.visibility = View.GONE
            }
        }

        carListViewModel.carId = intent?.extras?.getInt(Constants.DataParsing.CAR_ID, -1) ?: -1

        binding.datePicker.minDate = Calendar.getInstance().time.time
        binding.datePicker.formatDate("dmy")
        carListViewModel.getCarList(
            true,
            Constants.API.GET_CAR_LIST,
            this,
            step = Constants.CarStep.STEP_4_COMPLETED
        )
    }

    override fun onClick(v: View?) {
        v?.preventDoubleClick()
        when (v) {
            binding.btnInsuranceImg -> {
                ActionSheet.createBuilder(this, supportFragmentManager)
                    .setCancelButtonTitle(getString(R.string.cancel_))
                    .setOtherButtonTitles(
                        getString(R.string.camera),
                        getString(R.string.gallery),
                        getString(R.string.file)
                    )
                    .setCancelableOnTouchOutside(true).setListener(this).show()
            }

            binding.save -> {
                vehicleProtection.step = Constants.CarStep.STEP_4_COMPLETED
                if (binding.noInsurance.isChecked) {
                    vehicleProtection.have_insurance = false
                    callApi()
                } else {
                    checkValidation()
                }
            }
            binding.ivInsurancePolicy ->{
                carListViewModel.showFullImage(
                    this,
                    insurancePolicyUrl
                )
            }
            binding.viewPdf ->{
                carListViewModel.showFullWebView(
                    this,
                    pdfTechnicalUrl
                )
            }
        }
    }

    /**
     * Function for checking validation
     * */
    private fun checkValidation() {
        when {
            binding.insurancePolicyEdt.text.toString().isEmpty() -> {
                uiHelper.showLongToastInCenter(
                    this,
                    getString(R.string.enter_insurance_policy_number)
                )
            }
            insurancePath.isEmpty() && vehicleProtection.snap_insurance.isEmpty() -> {
                uiHelper.showLongToastInCenter(
                    this,
                    getString(R.string.enter_copy_of_insurance_policy)
                )
            }
            binding.insuranceCoverageEdt.text.toString().isEmpty() -> {
                uiHelper.showLongToastInCenter(
                    this,
                    getString(R.string.enter_total_liablity_covered)
                )
            }
            else -> {
                vehicleProtection.have_insurance = true
                vehicleProtection.insurance_number =
                    binding.insurancePolicyEdt.text.toString()
                binding.datePicker.run {
                    vehicleProtection.date_of_exp =
                        "$year-${if ((month + 1).toString().length == 1) 0.toString() + (month + 1).toString() else month + 1}-${if ((month).toString().length == 1) "".toString() + (dayOfMonth).toString() else dayOfMonth}"
                }

                vehicleProtection.liability_covered =
                    binding.insuranceCoverageEdt.text.toString()
                vehicleProtection.insurance_covered_cost =
                    binding.yesInsuranceRC.isChecked
                callApi()
            }
        }
    }

    /**
     * Function for calling api
     * */
    private fun callApi() {

        carListViewModel.sendVehicleProtection(
            vehicleProtection,
            true,
            Constants.API.SEND_CAR_DATA,
            this
        )
    }


    /**
     * Function for setting action bar
     * */
    fun actionBarSetting() {
        binding.layoutToolBar.setAsHostToolBar(
            R.string.vehicle_protection,
            supportActionBar
        )
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_CAR_LIST -> {
                step4Model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as LinkedTreeMap<*, *>),
                    Step4Model::class.java
                )
                showProtectionData()
            }
            Constants.API.UPLOAD_PHOTO -> {
                val imagesResponseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    ImagesResponseModel::class.java
                )
                vehicleProtection.snap_insurance = imagesResponseModel.imagesList[0]
              /*  binding.btnInsuranceImg.text =
                    imagesResponseModel.imagesList[0].toUri().lastPathSegment*/

                // insurancePolicyUrl =


                if (vehicleProtection.snap_insurance.contains(".pdf")){
                    pdfTechnicalUrl = BuildConfig.IMAGE_BASE_URL + vehicleProtection.snap_insurance
                    binding.ivInsurancePolicy.isVisible = false
                    binding.wvPdfLoader.isVisible = true
                    binding.viewPdf.isVisible = true
                    Handler(Looper.getMainLooper()).postDelayed({
                    binding.wvPdfLoader.loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=" + BuildConfig.IMAGE_BASE_URL + vehicleProtection.snap_insurance)
                    }, 500)

                }else{
                    insurancePolicyUrl = BuildConfig.IMAGE_BASE_URL + vehicleProtection.snap_insurance
                    binding.ivInsurancePolicy.isVisible = true
                    binding.wvPdfLoader.isVisible = false
                    binding.viewPdf.isVisible = false
                    uiHelper.glideLoadImage(
                        this,
                        BuildConfig.IMAGE_BASE_URL + vehicleProtection.snap_insurance,
                        binding.ivInsurancePolicy
                    )
                }

              /*  uiHelper.glideLoadImage(
                    this,
                    BuildConfig.IMAGE_BASE_URL + vehicleProtection.snap_insurance,
                    binding.ivInsurancePolicy
                )*/

            }
            Constants.API.SEND_CAR_DATA -> {
                finish()
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {

        when (tag) {
            Constants.API.UPLOAD_PHOTO -> {
                if (retryCount < 3) {
                    callApiToUploadImage(
                        imagesToUpload
                    )
                    retryCount++
                }
            }
            else -> {
                uiHelper.showLongToastInCenter(this, response?.message)
            }
        }
    }

    private fun showProtectionData() {
        step4Model?.let {
            binding.yesInsurance.isChecked = it.vehicleProtection.have_insurance
            binding.noInsurance.isChecked = !it.vehicleProtection.have_insurance

            binding.insurancePolicyEdt.setText(it.vehicleProtection.insurance_number)
            val calendar = Calendar.getInstance()

            if (it.vehicleProtection.date_of_exp.isNotEmpty()) {
                val expiryDate = DateUtil.stringToDateWithoutTime(
                    it.vehicleProtection.date_of_exp
                )
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.time =
                    if ((expiryDate ?: calendar.time).time > calendar.time.time) expiryDate
                        ?: calendar.time else calendar.time

                binding.datePicker.init(
                    calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH],
                    calendar[Calendar.DAY_OF_MONTH],
                    this
                )
            } else {
                calendar.add(Calendar.DAY_OF_YEAR, 1)

                binding.datePicker.init(
                    calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH],
                    calendar[Calendar.DAY_OF_MONTH],
                    this
                )
                expiryDate = DateUtil.changeDateFormat(
                    Constants.ServerDateTimeFormatT,
                    Constants.ServerDateFormat,
                    calendar.time.toString()
                )
            }
            binding.insuranceCoverageEdt.setText(it.vehicleProtection.liability_covered)
            if (it.vehicleProtection.insurance_covered_cost) {
                binding.yesInsuranceRC.isChecked = true
            } else {
                binding.noInsuranceRC.isChecked = true
            }

            if (it.vehicleProtection.snap_insurance.isNotEmpty()) {
                if (it.vehicleProtection.snap_insurance.contains(".pdf")){
                    pdfTechnicalUrl = BuildConfig.IMAGE_BASE_URL + it.vehicleProtection.snap_insurance
                    binding.ivInsurancePolicy.isVisible = false
                    binding.wvPdfLoader.isVisible = true
                    binding.viewPdf.isVisible = true
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.wvPdfLoader.loadUrl("https://drive.google.com/viewerng/viewer?embedded=true&url=" + it.vehicleProtection.snap_insurance)
                    }, 500)

                }else{
                    insurancePolicyUrl = BuildConfig.IMAGE_BASE_URL + it.vehicleProtection.snap_insurance
                    binding.ivInsurancePolicy.isVisible = true
                    binding.wvPdfLoader.isVisible = false
                    binding.viewPdf.isVisible = false
                    uiHelper.glideLoadImage(
                        this,
                        BuildConfig.IMAGE_BASE_URL + it.vehicleProtection.snap_insurance,
                        binding.ivInsurancePolicy
                    )
                }
            }

        }

    }


    override fun onDismiss(actionSheet: ActionSheet?, isCancel: Boolean) {

    }

    override fun onOtherButtonClick(actionSheet: ActionSheet?, index: Int) {
        when (index) {
            0 -> {
                ImagePicker.with(this)
                    .crop()
                    .compress(1024).cameraOnly() //Final image size will be less than 1 MB(Optional)
                    .maxResultSize(
                        1080,
                        1080
                    )  //Final image resolution will be less than 1080 x 1080(Optional)
                    .start { resultCode, data ->
                        when (resultCode) {
                            Activity.RESULT_OK -> {
                                data?.let {
                                    setImageSelectionData(data)
                                }
                            }
                            ImagePicker.RESULT_ERROR -> {
                                Toast.makeText(
                                    this,
                                    ImagePicker.getError(data),
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                    }
            }
            1 -> {
                ImagePicker.with(this)
                    .crop()
                    .compress(1024)
                    .galleryOnly() //Final image size will be less than 1 MB(Optional)
                    .maxResultSize(
                        1080,
                        1080
                    )  //Final image resolution will be less than 1080 x 1080(Optional)
                    .start { resultCode, data ->
                        when (resultCode) {
                            Activity.RESULT_OK -> {
                                data?.let {
                                    setImageSelectionData(data)
                                }

                            }
                            ImagePicker.RESULT_ERROR -> {
                                Toast.makeText(
                                    this,
                                    ImagePicker.getError(data),
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }
                        }
                    }
            }
            else -> {
                browseDocuments(launcher)
            }
        }
        actionSheet?.dismiss()
    }

    private fun setImageSelectionData(data: Intent?) {
        val fileUri = data?.data
        if (fileUri != null) {
            insurancePath = fileUri.path.toString()
           // binding.btnInsuranceImg.text = fileUri.lastPathSegment
            retryCount = 0
            callApiToUploadImage(
                uiHelper.multiPart(
                    fileUri,
                    "files"
                )
            )
        }
    }

    private fun callApiToUploadImage(list: ArrayList<MultipartBody.Part>) {
        imagesToUpload = list
        carListViewModel.uploadImage(
            arrayList = imagesToUpload,
            showLoader = true,
            tag = Constants.API.UPLOAD_PHOTO,
            callback = this
        )
    }

    override fun onDateChanged(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {

    }


    private fun createFileCallback() = object : FileCallback(lifecycleScope) {


        override fun onConflict(destinationFile: DocumentFile, action: FileConflictAction) {
            val resolution = ConflictResolution.values()[0]
            action.confirmResolution(resolution)
        }

        // only show dialog if file size greater than 10Mb
        // 0.5 second
        override fun onStart(file: Any, workerThread: Thread) = 500L

        override fun onReport(report: Report) {}

        override fun onFailed(errorCode: ErrorCode) =
            showToast("Failed copying file: $errorCode")


        override fun onCompleted(result: Any) {
            val path = (result as DocumentFile).uri.path
            if (path.toString().isNotEmpty()) {
                imagesToUpload =
                    uiHelper.multiPartFile(
                        path.toString().toUri(),
                        "files"
                    )
               // binding.btnInsuranceImg.text = path.toString().toUri().lastPathSegment
                count = 0
                callApiToUploadImage(
                    imagesToUpload
                )
            }
        }
    }

}