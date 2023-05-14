package com.towsal.towsal.views.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.ParseException
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentUserLicenseInformationBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.showChooseAppDialog
import com.towsal.towsal.interfaces.PickerCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.ImagesResponseModel
import com.towsal.towsal.network.serializer.cardetails.UserInfoResponseModel
import com.towsal.towsal.network.serializer.cardetails.UserInfoStepThreeModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.MoveFrom
import com.towsal.towsal.viewmodel.TripsViewModel
import com.towsal.towsal.viewmodel.UserInformationViewModel
import com.towsal.towsal.views.activities.CheckoutCarBookingActivity
import com.towsal.towsal.views.activities.MainActivity
import com.towsal.towsal.views.activities.TakePhotoActivity
import com.towsal.towsal.views.activities.UserInformationActivity
import okhttp3.MultipartBody
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment class for user license information
 * */
class UserLicenseInformationFragment : BaseFragment(), OnNetworkResponse, PickerCallback {

    lateinit var binding: FragmentUserLicenseInformationBinding
    private var driverLicenseImage = ""
    private val args: UserLicenseInformationFragmentArgs by navArgs()
    var model = UserInfoResponseModel()
    private val userInformationViewModel: UserInformationViewModel by activityViewModels()
    val tripsViewModel: TripsViewModel by activityViewModels()
    private var pickDob = false
    var count = 0
    var imagesToUpload = ArrayList(emptyList<MultipartBody.Part>())
    var isRequestSent = false

    var driverLicenseUrl = ""



    private var pickerSwitch = 1
    private var natIDPath = ""


    private val launcherTakePhoto = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        result.data?.extras?.let {
            when (pickerSwitch) {
                1 -> {
                    val file = File(it.getString("file_uri").toString())
                    natIDPath = file.path
                  /*  binding.driverLicenseImg.text =
                        file.path.toUri().lastPathSegment.toString()*/
                    imagesToUpload =
                        uiHelper.multiPart(
                            file.path.toUri(),
                            "files"
                        )

                    userInformationViewModel.uploadImage(
                        true,
                        Constants.API.UPLOAD_PHOTO,
                        this,
                        imagesToUpload
                    )

                }

            }

        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_user_license_information,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
        if (args.model != null) {
            model = userInformationViewModel.getUserInfoResponseModel()
            setData()
        }
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        if (model.stepThree.snap_of_driving_licence.isNotEmpty()) {
            binding.firstNameEdt.setText(model.stepThree.first_name)
            binding.lastNameEdt.setText(model.stepThree.last_name)
            binding.licenseExpirationEdt.setText(model.stepThree.licence_exp_date)
            binding.dobEdt.setText(model.stepThree.dob)
            binding.cvUserStatusRelatedImage.isVisible = true
            driverLicenseUrl = BuildConfig.IMAGE_BASE_URL + model.stepThree.snap_of_driving_licence
            uiHelper.glideLoadImage(
                requireActivity(),
                BuildConfig.IMAGE_BASE_URL + model.stepThree.snap_of_driving_licence,
                binding.ivDriverLicense
            )
      /*      binding.driverLicenseImg.text =
                model.stepThree.snap_of_driving_licence.toUri().lastPathSegment*/
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

             /*   ImagePicker.with(requireActivity())
                    .crop()
                    .compress(1024) //Final image size will be less than 1 MB(Optional)
                    .maxResultSize(
                        1080,
                        1080
                    )  //Final image resolution will be less than 1080 x 1080(Optional)
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
                                    count = 0
                                    isRequestSent = true
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
                                    context,
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
                    uiHelper.showLongToastInCenter(
                        requireContext(),
                        requireActivity().getString(R.string.upload_copy_of_your_driving_licence)
                    )
                } else if (binding.firstNameEdt.text.toString().isEmpty()) {
                    uiHelper.showLongToastInCenter(
                        requireContext(),
                        requireActivity().getString(R.string.first_name_err_msg)
                    )
                }

                else if (binding.edtDrivingLicenseNumber.text.toString().isEmpty()) {
                    uiHelper.showLongToastInCenter(
                        requireContext(),
                        requireActivity().getString(R.string.enter_driving_license_number)
                    )
                }

                else if (binding.lastNameEdt.text.toString().isEmpty()) {
                    uiHelper.showLongToastInCenter(
                        requireContext(),
                        requireActivity().getString(R.string.last_name_err_msg)
                    )
                } else if (binding.licenseExpirationEdt.text.toString().isEmpty()) {
                    uiHelper.showLongToastInCenter(
                        requireContext(),
                        requireActivity().getString(R.string.licence_s_expiration_date_err_msg)
                    )
                } else if (binding.dobEdt.text.toString().isEmpty()) {
                    uiHelper.showLongToastInCenter(
                        requireContext(),
                        requireActivity().getString(R.string.enter_dob_err_msg)
                    )
                } else if (getAge(binding.dobEdt.text.toString()) < 18) {
                    uiHelper.showLongToastInCenter(
                        requireContext(),
                        requireActivity().getString(R.string.enter_dob_under_18_err_msg)
                    )
                } else {

                    val stepThreeModel = UserInfoStepThreeModel()
                    stepThreeModel.dob = binding.dobEdt.text.toString()
                    stepThreeModel.snap_of_driving_licence =
                        driverLicenseImage.ifEmpty { model.stepThree.snap_of_driving_licence }
                    stepThreeModel.licence_exp_date =
                        binding.licenseExpirationEdt.text.toString()
                    stepThreeModel.first_name = binding.firstNameEdt.text.toString()
                    stepThreeModel.last_name = binding.lastNameEdt.text.toString()
                    stepThreeModel.licenseNumber = binding.edtDrivingLicenseNumber.text.toString()
                    //it was real
                    if (!isRequestSent)
                        userInformationViewModel.saveLicenseInformation(
                            stepThreeModel = stepThreeModel,
                            true,
                            Constants.API.SAVE_USER_INFO,
                            this
                        )
                    else
                        uiHelper.showLongToastInCenter(requireActivity(), "wait server is busy")
                }
            }
            R.id.licenseExpirationEdt -> {
                pickDob = false
                uiHelper.showDatePickerWithLimit(
                    requireActivity(), this,
                    maxLimit = false,
                    minLimit = true
                )
            }
            R.id.dobEdt -> {
                pickDob = true
                uiHelper.showDatePickerWithLimit(
                    requireActivity(), this,
                    maxLimit = true,
                    minLimit = false
                )
            }
            R.id.ivDriverLicense ->{
                tripsViewModel.showFullImage(
                    requireActivity(),
                    driverLicenseUrl
                )
            }
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.SAVE_USER_INFO -> {
                uiHelper.showLongToastInCenter(requireContext(), response?.message)
                val bundle = Bundle()
                bundle.putSerializable(
                    Constants.DataParsing.CARINFODATAMODEL,
                    UserInformationActivity.carInfoModel
                )
                bundle.putSerializable(
                    Constants.DataParsing.MODEL,
                    UserInformationActivity.checkoutInfoModel
                )
                bundle.putSerializable(
                    Constants.DataParsing.DATE_TIME_MODEL,
                    UserInformationActivity.calendarDateTimeModel
                )
                bundle.putInt("flow", 1)

                uiHelper.openAndClearActivity(
                    requireActivity(),
                    CheckoutCarBookingActivity::class.java, bundle
                )
                requireActivity().finish()
            }

            Constants.API.UPLOAD_PHOTO -> {
                val imagesResponseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    ImagesResponseModel::class.java
                )
                count = 0
                isRequestSent = false
                driverLicenseImage = imagesResponseModel.imagesList[0]
                binding.cvUserStatusRelatedImage.isVisible = true
                driverLicenseUrl = BuildConfig.IMAGE_BASE_URL + imagesResponseModel.imagesList[0]
                uiHelper.glideLoadImage(
                    requireActivity(),
                    BuildConfig.IMAGE_BASE_URL + imagesResponseModel.imagesList[0],
                    binding.ivDriverLicense
                )
                /*binding.driverLicenseImg.text = driverLicenseImage.toUri().lastPathSegment*/
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.UPLOAD_PHOTO -> {
                if (count < 3) {
                    userInformationViewModel.uploadImage(
                        true,
                        Constants.API.UPLOAD_PHOTO,
                        this,
                        imagesToUpload
                    )
                    count++
                } else {
                    uiHelper.showLongToastInCenter(
                        requireActivity(),
                        "Server not responding or may be your internet is down"
                    )
                }
            }
            else -> {
                uiHelper.showLongToastInCenter(requireContext(), response?.message)
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
     * Function for getting user age
     * */
    @SuppressLint("SimpleDateFormat")
    fun getAge(date: String): Int {
        var age = 0
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val date1: Date = dateFormat.parse(date) ?: Date()
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

    private fun showPickerDialog() {
        requireActivity().showChooseAppDialog {
            when (it) {
                com.towsal.towsal.utils.ImagePicker.CAMERA -> {
                    val intent =
                        Intent(requireActivity(), TakePhotoActivity::class.java)
                    intent.putExtra(Constants.DataParsing.MOVE_TO_DRIVER_INFO, MoveFrom.OTHER_ACTIVITY.value)
                    launcherTakePhoto.launch(intent)
                }
                com.towsal.towsal.utils.ImagePicker.GALLERY -> {
                    ImagePicker.with(
                        requireActivity()
                    )
                        .galleryOnly()
                        .crop()
                        .compress(1024)
                        .maxResultSize(
                            1080,
                            1080
                        )
                        .start { resultCode, data ->
                            if (resultCode == AppCompatActivity.RESULT_OK) {
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
     /*   binding.driverLicenseImg.text =
            url.lastPathSegment.toString()*/
        userInformationViewModel.uploadImage(
            true,
            Constants.API.UPLOAD_PHOTO,
            this,
            imagesToUpload
        )
    }

}