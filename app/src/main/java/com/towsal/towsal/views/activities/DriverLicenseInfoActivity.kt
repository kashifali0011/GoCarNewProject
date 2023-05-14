package com.towsal.towsal.views.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.ParseException
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.anggrayudi.storage.callback.FileCallback
import com.anggrayudi.storage.file.copyFileTo
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityDriverLicenseInfoBinding
import com.towsal.towsal.extensions.intentToDocumentFiles
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.extensions.showChooseAppDialog
import com.towsal.towsal.interfaces.PickerCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.ImagesResponseModel
import com.towsal.towsal.network.serializer.UserModel
import com.towsal.towsal.network.serializer.cardetails.UserInfoResponseModel
import com.towsal.towsal.network.serializer.cardetails.UserInfoStepThreeModel
import com.towsal.towsal.network.serializer.carlist.CarInformationModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.MoveFrom
import com.towsal.towsal.viewmodel.CarDetailViewModel
import com.towsal.towsal.viewmodel.TripsViewModel
import com.towsal.towsal.viewmodel.UserInformationViewModel
import com.towsal.towsal.views.adapters.CarFeatureAdapterVertical
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


/**
 * Activity class for driver's driving license
 * */
class DriverLicenseInfoActivity : BaseActivity(), OnNetworkResponse, PickerCallback {

    lateinit var binding: ActivityDriverLicenseInfoBinding
    val carDetailViewModel: CarDetailViewModel by viewModel()
    var driverLicenseImage = ""
    private val userInformationViewModel: UserInformationViewModel by viewModel()
    val tripsViewModel: TripsViewModel by viewModel()
    var model = UserInfoResponseModel()
    var pickDob = false
    var userId = 0
    var countRetry = 0
    private var imagesToUpload = ArrayList(emptyList<MultipartBody.Part>())


    private var pickerSwitch = 1
    private var natIDPath = ""

    var driverLicenseUrl = ""


    private val launcherTakePhoto = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        result.data?.extras?.let {
            when (pickerSwitch) {
                1 -> {
                    val file = File(it.getString("file_uri").toString())
                    natIDPath = file.path
                   /* binding.driverLicenseImg.text =
                        file.path.toUri().lastPathSegment.toString()*/
                    imagesToUpload =
                        uiHelper.multiPart(
                            file.path.toUri(),
                            "files"
                        )

                    carDetailViewModel.uploadImage(
                        true,
                        Constants.API.UPLOAD_PHOTO,
                        this,
                        imagesToUpload
                    )

                }

            }

        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_driver_license_info)
        binding.clMainRoot.isVisible = false
        binding.activity = this
        uiHelper.hideActionBar(supportActionBar)
        setData()
        val userModel = preferenceHelper
            .getObject(
                Constants.USER_MODEL,
                UserModel::class.java
            ) as? UserModel
        if (userModel != null)
            userId = userModel.id

        carDetailViewModel.getUserInfo(
            userId,
            true,
            Constants.API.GET_USER_INFO,
            this
        )

    }

    /**
     * Function for setting up action bar
     * */

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_USER_INFO -> {
                model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    UserInfoResponseModel::class.java
                )
                binding.clMainRoot.isVisible = true
                setData()
            }
            Constants.API.SAVE_USER_INFO -> {
                uiHelper.showLongToastInCenter(this, response?.message)
                finish()
            }

            Constants.API.UPLOAD_PHOTO -> {
                val imagesResponseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    ImagesResponseModel::class.java
                )
                countRetry = 0
                driverLicenseImage = imagesResponseModel.imagesList[0]
                binding.cvUserStatusRelatedImage.isVisible = true
                driverLicenseUrl = BuildConfig.IMAGE_BASE_URL +imagesResponseModel.imagesList[0]
                uiHelper.glideLoadImage(
                    this,
                    BuildConfig.IMAGE_BASE_URL +imagesResponseModel.imagesList[0],
                    binding.ivDriverLicense
                )
                binding.clMainRoot.isVisible = true

              //  binding.driverLicenseImg.text = driverLicenseImage.toUri().lastPathSegment
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
        when (tag) {
            Constants.API.UPLOAD_PHOTO -> {
                if (countRetry < 3) {
                    ++countRetry
                    userInformationViewModel.uploadImage(
                        true,
                        Constants.API.UPLOAD_PHOTO,
                        this,
                        imagesToUpload
                    )
                } else {
                    uiHelper.showLongToastInCenter(
                        this,
                        "Server not responding or may be your internet is down"
                    )
                }
            }
        }

    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        binding.toolBar.setAsGuestToolBar(
            R.string.driver_license,
            supportActionBar
        )
        if (model.stepThree.snap_of_driving_licence.isNotEmpty()) {
            binding.firstNameEdt.setText(model.stepThree.first_name)
            binding.lastNameEdt.setText(model.stepThree.last_name)
            binding.licenseExpirationEdt.setText(model.stepThree.licence_exp_date)
            binding.edtDrivingLicenseNumber.setText(model.stepThree.licenseNumber)
            binding.dobEdt.setText(model.stepThree.dob)
           /* binding.driverLicenseImg.text =
                model.stepThree.snap_of_driving_licence.toUri().lastPathSegment*/
            binding.cvUserStatusRelatedImage.isVisible = true
            driverLicenseUrl = BuildConfig.IMAGE_BASE_URL +model.stepThree.snap_of_driving_licence
            uiHelper.glideLoadImage(
                this,
                BuildConfig.IMAGE_BASE_URL +model.stepThree.snap_of_driving_licence,
                binding.ivDriverLicense
            )

            binding.nextBtn.text = getString(R.string.update_)
        //    binding.clMainRoot.isVisible = true

        }else{
            binding.nextBtn.text = getString(R.string.save)
         //   binding.clMainRoot.isVisible = true
        }
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view.id) {
            R.id.driverLicenseImg -> {
                showPickerDialog()

                /*   val intent =
                       Intent(this@DriverLicenseInfoActivity, TakePhotoActivity::class.java)
                   launcherTakePhoto.launch(intent)*/
                /*   ImagePicker.with(this)
                       .crop()
                       .compress(1024) //Final image size will be less than 1 MB(Optional)
                       .maxResultSize(
                           1080,
                           1080
                       ) //Final image resolution will be less than 1080 x 1080(Optional)
                       .start { resultCode, data ->
                           when (resultCode) {
                               Activity.RESULT_OK -> {

                                   val fileUri = data?.data
                                   if (fileUri != null) {
                                       val fileName = fileUri.pathSegments.last()
                                       binding.driverLicenseImg.text = fileName.toString()
                                       imagesToUpload =
                                           uiHelper.multiPart(
                                               fileUri,
                                               "files"
                                           )
                                       isRequestSent = true
                                       countRetry = 0
                                       userInformationViewModel.uploadImage(
                                           showLoader = true,
                                           tag = Constants.API.UPLOAD_PHOTO,
                                           callback = this,
                                           arrayList = imagesToUpload
                                       )
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
                               else -> {

                               }
                           }
                       }*/
            }
            R.id.nextBtn -> {

                if (driverLicenseImage.isEmpty() && model.stepThree.snap_of_driving_licence.isEmpty()) {
                    Log.d("immageUri", "uriEmpty  " + driverLicenseImage)
                    uiHelper.showLongToastInCenter(
                        this,
                        this.getString(R.string.upload_copy_of_your_driving_licence)
                    )
                } else if (binding.firstNameEdt.text.toString().isEmpty()) {
                    binding.firstNameEdt.setBackgroundResource(R.drawable.red_editext_bg)
                    uiHelper.showLongToastInCenter(
                        this,
                        this.getString(R.string.first_name_err_msg)
                    )
                } else if (binding.lastNameEdt.text.toString().isEmpty()) {
                    binding.lastNameEdt.setBackgroundResource(R.drawable.red_editext_bg)
                    binding.firstNameEdt.setBackgroundResource(R.drawable.editext_bg)
                    uiHelper.showLongToastInCenter(
                        this,
                        this.getString(R.string.last_name_err_msg)
                    )
                } else if (binding.licenseExpirationEdt.text.toString().isEmpty()) {
                    binding.licenseExpirationEdt.setBackgroundResource(R.drawable.red_editext_bg)
                    binding.lastNameEdt.setBackgroundResource(R.drawable.editext_bg)
                    binding.firstNameEdt.setBackgroundResource(R.drawable.editext_bg)
                    uiHelper.showLongToastInCenter(
                        this,
                        this.getString(R.string.licence_s_expiration_date_err_msg)
                    )
                } else if (binding.dobEdt.text.toString().isEmpty()) {
                    binding.dobEdt.setBackgroundResource(R.drawable.red_editext_bg)
                    binding.licenseExpirationEdt.setBackgroundResource(R.drawable.editext_bg)
                    binding.lastNameEdt.setBackgroundResource(R.drawable.editext_bg)
                    binding.firstNameEdt.setBackgroundResource(R.drawable.editext_bg)
                    uiHelper.showLongToastInCenter(
                        this,
                        this.getString(R.string.enter_dob_err_msg)
                    )
                }

                else if (binding.edtDrivingLicenseNumber.text.toString().isEmpty()) {
                    binding.edtDrivingLicenseNumber.setBackgroundResource(R.drawable.red_editext_bg)
                    binding.dobEdt.setBackgroundResource(R.drawable.editext_bg)
                    binding.licenseExpirationEdt.setBackgroundResource(R.drawable.editext_bg)
                    binding.lastNameEdt.setBackgroundResource(R.drawable.editext_bg)
                    binding.firstNameEdt.setBackgroundResource(R.drawable.editext_bg)
                    uiHelper.showLongToastInCenter(
                        this,
                        this.getString(R.string.enter_license_number)
                    )
                }

                else if (getAge(binding.dobEdt.text.toString()) < 18) {
                    binding.dobEdt.setBackgroundResource(R.drawable.red_editext_bg)
                    binding.licenseExpirationEdt.setBackgroundResource(R.drawable.editext_bg)
                    binding.lastNameEdt.setBackgroundResource(R.drawable.editext_bg)
                    binding.firstNameEdt.setBackgroundResource(R.drawable.editext_bg)
                    uiHelper.showLongToastInCenter(
                        this,
                        this.getString(R.string.enter_dob_under_18_err_msg)
                    )

                } else {
                    val stepThreeModel = UserInfoStepThreeModel()
                    stepThreeModel.dob = binding.dobEdt.text.toString()
                    stepThreeModel.snap_of_driving_licence =
                        driverLicenseImage.ifEmpty { model.stepThree.snap_of_driving_licence }
                    stepThreeModel.licence_exp_date = binding.licenseExpirationEdt.text.toString()
                    stepThreeModel.first_name = binding.firstNameEdt.text.toString()
                    stepThreeModel.last_name = binding.lastNameEdt.text.toString()
                    stepThreeModel.licenseNumber = binding.edtDrivingLicenseNumber.text.toString()
                    userInformationViewModel.saveLicenseInformation(
                        stepThreeModel,
                        true,
                        Constants.API.SAVE_USER_INFO,
                        this
                    )

                }
            }
            R.id.licenseExpirationEdt -> {
                pickDob = false
                uiHelper.showDatePickerWithLimit(this, this, false, true)
            }
            R.id.dobEdt -> {
                pickDob = true
                uiHelper.showDatePickerWithLimit(this, this, true, false)
            }
            R.id.ivDriverLicense ->{
                tripsViewModel.showFullImage(
                    this,
                    driverLicenseUrl
                )
            }
        }
    }

    override fun onSelected(date: Any?) {
        if (pickDob) {
            binding.dobEdt.setText(date.toString())
        } else {
            binding.licenseExpirationEdt.setText(date.toString())
        }
    }

    /**
     * Function for checking date
     * */
    @SuppressLint("SimpleDateFormat")
    fun getAge(date: String): Int {
        var age = 0
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val date1: Date = dateFormat.parse(date)
            val now: Calendar = Calendar.getInstance()
            val dob: Calendar = Calendar.getInstance()
            dob.time = date1
            require(!dob.after(now)) { "Can't be born in the future" }
            val year1: Int = now.get(Calendar.YEAR)
            val year2: Int = dob.get(Calendar.YEAR)
            age = year1 - year2
            val month1: Int = now.get(Calendar.MONTH)
            val month2: Int = dob.get(Calendar.MONTH)
            if (month2 > month1) {
                age--
            } else if (month1 == month2) {
                val day1: Int = now.get(Calendar.DAY_OF_MONTH)
                val day2: Int = dob.get(Calendar.DAY_OF_MONTH)
                if (day2 > day1) {
                    age--
                }
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return age
    }

    private fun createFileCallback() = object : FileCallback(lifecycleScope) {


        override fun onConflict(destinationFile: DocumentFile, action: FileConflictAction) {
            val resolution = ConflictResolution.values()[0]
            action.confirmResolution(resolution)
        }

        override fun onStart(file: Any, workerThread: Thread): Long {
            // only show dialog if file size greater than 10Mb
            return 500 // 0.5 second
        }

        override fun onReport(report: Report) {
        }

        override fun onFailed(errorCode: ErrorCode) {

            Toast.makeText(
                applicationContext,
                "Failed copying file: $errorCode",
                Toast.LENGTH_SHORT
            )
                .show()
        }

        override fun onCompleted(result: Any) {
            val path = (result as DocumentFile).uri.path
            if (path.toString().isNotEmpty()) {
                imagesToUpload =
                    uiHelper.multiPartFile(
                        path.toString().toUri(),
                        "files"
                    )
              //  binding.driverLicenseImg.text = path.toString().toUri().lastPathSegment
                count = 0
                carDetailViewModel.uploadImage(
                    true,
                    Constants.API.TECHNICAL_REPORT,
                    this@DriverLicenseInfoActivity,
                    imagesToUpload
                )
            }

        }
    }


    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val file: File? = ImagePicker.getFile(data)
        if (file != null) {
            natIDPath = file.path
            /*binding.driverLicenseImg.text =
                file.path.toUri().lastPathSegment.toString()*/
            imagesToUpload =
                uiHelper.multiPart(
                    file.path.toUri(),
                    "files"
                )
            carDetailViewModel.uploadImage(
                true,
                Constants.API.SNAP_OF_NATIONAL_ID,
                this,
                imagesToUpload
            )
        }
    }

    private fun showPickerDialog() {
        this@DriverLicenseInfoActivity.showChooseAppDialog {
            when (it) {
                com.towsal.towsal.utils.ImagePicker.CAMERA -> {
                    val intent =
                        Intent(this@DriverLicenseInfoActivity, TakePhotoActivity::class.java)
                    intent.putExtra(Constants.DataParsing.MOVE_TO_DRIVER_INFO,MoveFrom.DriverLicenseInfoActivity.value)
                    launcherTakePhoto.launch(intent)
                }
                com.towsal.towsal.utils.ImagePicker.GALLERY -> {
                    ImagePicker.with(
                        this@DriverLicenseInfoActivity
                    )
                        .galleryOnly()
                        .crop()
                        .compress(1024)
                        .maxResultSize(
                            1080,
                            1080
                        )
                        .start { resultCode, data ->
                            if (resultCode == RESULT_OK) {
                                data?.let {
                                    it.data?.let { uri ->
                                        uploadImage(uri)
                                    }
                                }
                            }
                        }
                }
            }
        }
    }

    private fun uploadImage(url: Uri) {
        imagesToUpload =
            uiHelper.multiPart(
                url,
                "files"
            )
        natIDPath = url.path.toString()
        /*binding.driverLicenseImg.text =
            url.lastPathSegment.toString()*/
        carDetailViewModel.uploadImage(
            true,
            Constants.API.UPLOAD_PHOTO,
            this,
            imagesToUpload
        )
    }
}