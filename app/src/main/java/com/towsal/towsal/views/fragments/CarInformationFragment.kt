package com.towsal.towsal.views.fragments

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.anggrayudi.storage.callback.FileCallback
import com.anggrayudi.storage.file.copyFileTo
import com.baoyz.actionsheet.ActionSheet
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.DropDownViewCompanySelectionBinding
import com.towsal.towsal.databinding.FragmentCarInformationBinding
import com.towsal.towsal.extensions.*
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.ImagesResponseModel
import com.towsal.towsal.network.serializer.UserModel
import com.towsal.towsal.network.serializer.carlist.CarInformationModel
import com.towsal.towsal.network.serializer.carlist.CarListDataModel
import com.towsal.towsal.network.serializer.carlist.CarMakeInfoModel
import com.towsal.towsal.network.serializer.carlist.StepOneDataModel
import com.towsal.towsal.network.serializer.cities.CitiesListModel
import com.towsal.towsal.network.serializer.cities.CityModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.Constants.DataParsing.CARINFODATAMODEL
import com.towsal.towsal.utils.Constants.DataParsing.MODEL
import com.towsal.towsal.utils.Constants.RequestCodes.ACTIVITY_CAR_FEATURE
import com.towsal.towsal.utils.Constants.RequestCodes.ACTIVITY_CAR_INFO
import com.towsal.towsal.utils.Constants.RequestCodes.ACTIVTIY_LOCATION
import com.towsal.towsal.utils.MoveFrom
import com.towsal.towsal.viewmodel.CarListViewModel
import com.towsal.towsal.viewmodel.TripsViewModel
import com.towsal.towsal.views.activities.*
import com.towsal.towsal.views.adapters.CarFeatureAdapterVertical
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.File
import com.towsal.towsal.utils.ImagePicker as MyImagePickerEnum

/**
 * Fragment class for car information
 * */
class CarInformationFragment : BaseFragment(), OnNetworkResponse, ActionSheet.ActionSheetListener {

    private lateinit var binding: FragmentCarInformationBinding
    private lateinit var dropDownViewCompanySelectionBinding: DropDownViewCompanySelectionBinding
    private var popupWindow = PopupWindow()
    private var pickerSwitch = 0
    private var natIDPath = ""
    private var drivingLicensePath = ""
    private var techReportPath = ""
    private var carInfoModel = CarInformationModel()
    private var carListDataModel = CarListDataModel()
    private val carRegistrationCityList = ArrayList<String>()
    private var citiesList = ArrayList<CityModel>()
    private var stepOneDataModel: StepOneDataModel? = null
    private val carListViewModel: CarListViewModel by activityViewModels()
    val tripsViewModel: TripsViewModel by activityViewModels()
    var selectedIndex = 0
    var imagesToUpload = ArrayList<MultipartBody.Part>()
    var count = 0
    var isApiNeeded = false
    var isRequestSent = false
    private var isCompany = false
    private var needToShowError = true
    var carFeature: ArrayList<CarMakeInfoModel> = ArrayList()

    var commercialRegisterUrl = ""
    var vatCertificateUrl = ""
    var vehicleRegistrationCardUrl = ""
    var technicalReportUrl = ""
    var pdfTechnicalRepot = ""


    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        if (it.resultCode == AppCompatActivity.RESULT_OK) {
            val files = requireActivity().intentToDocumentFiles(it.data)

            if (files.isNotEmpty()) {
                val destFile = requireActivity().cacheDir

                lifecycleScope.launch(Dispatchers.IO) {
                    (files.first()).copyFileTo(
                        requireActivity(),
                        destFile.path.toString(),
                        null,
                        callback = createFileCallback()
                    )
                }

            }
        }
    }

    private val launcherTakePhoto = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        result.data?.extras?.let {
            when (pickerSwitch) {
                1 -> {
                    val file = File(it.getString("file_uri").toString())
                    natIDPath = file.path
                    binding.btnUserStatusRelatedImage.text = getString(R.string.Update_image)
                    imagesToUpload =
                        uiHelper.multiPart(
                            file.path.toUri(),
                            "files"
                        )
                    count = 0
                    if (!isRequestSent) {
                        isRequestSent = true
                        carListViewModel.uploadImage(
                            true,
                            Constants.API.SNAP_OF_NATIONAL_ID,
                            this,
                            imagesToUpload
                        )
                    }
                }
                2 -> {
                    val file = File(it.getString("file_uri").toString())
                    drivingLicensePath = file.path
                  //  binding.btnUploadVehicleRegistrationCard.text = drivingLicensePath.toUri().lastPathSegment
                    imagesToUpload =
                        uiHelper.multiPart(
                            file.path.toUri(),
                            "files"
                        )
                    count = 0
                    if (!isRequestSent) {
                        isRequestSent = true
                        carListViewModel.uploadImage(
                            true,
                            Constants.API.SNAP_OF_DRIVING_LICENSE,
                            this,
                            imagesToUpload
                        )
                    }
                }
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dropDownViewCompanySelectionBinding =
            DropDownViewCompanySelectionBinding.bind(
                LayoutInflater.from(requireActivity())
                    .inflate(R.layout.drop_down_view_company_selection, null)
            )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_car_information,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
        setTextGradientColors()

        binding.carFeatureList.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
        val adapter = CarFeatureAdapterVertical(
            requireContext(),
            uiHelper
        )
        binding.carFeatureList.adapter = adapter
        setData()

    }

    private fun setTextGradientColors() {
        binding.addCarInfo.setGradientTextColor(
            colors = intArrayOf(
                requireActivity().getColor(R.color.send_msg_bg),
                requireActivity().getColor(R.color.text_receive_msg),
            )
        )
        binding.editInfoLocation.setGradientTextColor(
            colors = intArrayOf(
                requireActivity().getColor(R.color.send_msg_bg),
                requireActivity().getColor(R.color.text_receive_msg),
            )
        )
        binding.editInfoCar.setGradientTextColor(
            colors = intArrayOf(
                requireActivity().getColor(R.color.send_msg_bg),
                requireActivity().getColor(R.color.text_receive_msg),
            )
        )
    }

    /**
     * Function for setting up views data
     * */
    @SuppressLint("SetTextI18n")
    fun setData() {
        carListDataModel = preferenceHelper.getObject(
            Constants.DataParsing.CAR_LIST_DATA_MODEL,
            CarListDataModel::class.java
        ) as CarListDataModel

        carInfoModel.hostType = carListDataModel.hostType
        setUserStatus()
        isApiNeeded = carListDataModel.isStepOneApiRequired
        if (citiesList.isEmpty())
            callCitiesListApi()
        if (
            isApiNeeded && stepOneDataModel == null
        ) {
            carListViewModel.getCarList(
                showLoader = true,
                tag = Constants.API.GET_CAR_LIST,
                callback = this,
                step = Constants.CarStep.STEP_1_COMPLETED
            )
        } else if (isApiNeeded && stepOneDataModel != null) {
            populateStepOneData()
        }
        if (carListDataModel.disableViews) {
            binding.editInfoCar.visibility = View.INVISIBLE
            binding.editInfoLocation.visibility = View.INVISIBLE
            binding.addCarInfo.visibility = View.INVISIBLE
            binding.automatic.isEnabled = false
            binding.manual.isEnabled = false
            binding.citiesList.isEnabled = false
            for (i in 0 until binding.mainLayout.childCount - 1) {
                val child: View = binding.mainLayout.getChildAt(i)
                if (child is ConstraintLayout) {

                    for (j in 0 until child.childCount) {
                        val nestedChild: View = binding.mainLayout.getChildAt(i)
                        nestedChild.isEnabled = false
                    }
                }
                child.isEnabled = false
            }

        }

        binding.edtUserStatusRelatedNumber.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (needToShowError) {
                    if (!s.toString().startsWith("1")) {
                        binding.edtUserStatusRelatedNumber.setText("1")
                        Selection.setSelection(
                            binding.edtUserStatusRelatedNumber.text,
                            binding.edtUserStatusRelatedNumber.text!!.length
                        )
                    }
                    if (s.isNotEmpty()) {
                        if (s[0].toString() != "1" || s.length < 10) {
                            if (isCompany) getString(
                                R.string.user_status_related_number_err_msg,
                                R.string.the_company_registration_number
                            ) else getString(
                                R.string.user_status_related_number_err_msg,
                                R.string.the_national_id
                            )
                        }
                    } else {
                        if (isCompany) getString(
                            R.string.user_status_related_number_err_msg,
                            R.string.the_company_registration_number
                        ) else getString(
                            R.string.user_status_related_number_err_msg,
                            R.string.the_national_id
                        )
                    }
                } else {
                    needToShowError = true
                }
            }
        })

    }

    private fun callCitiesListApi() {
        if (citiesList.isEmpty()) {
            carListViewModel.getCitiesList(
                false,
                Constants.API.CITIES_LIST,
                this,
                2
            )
        } else {
            setSpinnerData()
        }

    }


    /**
     * Function for showing sheet for camera, gallery and file selection
     * */
    private fun imgOrFile() {
        pickerSwitch = 0
        ActionSheet.createBuilder(requireActivity(), childFragmentManager)
            .setCancelButtonTitle(getString(R.string.cancel_))
            .setOtherButtonTitles(
                getString(R.string.camera),
                getString(R.string.gallery),
                getString(R.string.file)
            )
            .setCancelableOnTouchOutside(true).setListener(this).show()
    }

    /**
     * Function for making request to OS for image pick
     * */
    private fun imagePick() {
        ImagePicker.with(this)
            .crop()
            .compress(1024) //Final image size will be less than 1 MB(Optional)
            .maxResultSize(
                1080,
                1080
            )    //Final image resolution will be less than 1080 x 1080(Optional)
            .start()
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val snapOfNationalId = carInfoModel.nationalIDImagePath
        val snapOfIstamara = carInfoModel.istamarImagePath
        val technicalReportPath = carInfoModel.technicalReportPath
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                ACTIVTIY_LOCATION -> {
                    val bundle = data?.getBundleExtra(MODEL)
                    carInfoModel = bundle?.getSerializable(MODEL) as CarInformationModel
                    binding.locationBtn.setText(carInfoModel.street_address)
                    binding.editInfoLocation.text = getText(R.string.edit_)
                }
                ACTIVITY_CAR_INFO -> {
                    val bundle = data?.getBundleExtra(MODEL)
                    carInfoModel = bundle?.getSerializable(MODEL) as CarInformationModel
                    binding.carInfoBtn.setText(
                        "" + carInfoModel.makeName + " " + carInfoModel.modelName + " " + carInfoModel.year
                    )
                    binding.editInfoCar.text = getString(R.string.edit_)
                }
                ACTIVITY_CAR_FEATURE -> {
                    val bundle = data?.getBundleExtra(MODEL)
                    carInfoModel = bundle?.getSerializable(MODEL) as CarInformationModel
                    carFeature = ArrayList(carInfoModel.car_features_list.distinctBy {
                        it.id
                    })
                    binding.viewCarFeatures.isVisible = false
                    (binding.carFeatureList.adapter as CarFeatureAdapterVertical).setList(
                        ArrayList(
                            carFeature.take(4)
                        )
                    )
                    binding.tvSeeMore.isVisible = carFeature.size > 4
                    binding.tvViewLess.isVisible = false
                    binding.addCarInfo.text = getString(R.string.edit_)
                }
                else -> {
                    when (pickerSwitch) {
                        1 -> {
                            val file: File? = ImagePicker.getFile(data)
                            if (file != null) {
                                natIDPath = file.path
                                binding.btnUserStatusRelatedImage.text = getString(R.string.Update_image)
                                imagesToUpload =
                                    uiHelper.multiPart(
                                        file.path.toUri(),
                                        "files"
                                    )
                                count = 0
                                if (!isRequestSent) {
                                    isRequestSent = true
                                    carListViewModel.uploadImage(
                                        true,
                                        Constants.API.SNAP_OF_NATIONAL_ID,
                                        this,
                                        imagesToUpload
                                    )
                                }
                            }
                        }
                        3 -> {
                            val file: File = ImagePicker.getFile(data)!!
                          /*  binding.btnVatRegistration.text =
                                file.path.toUri().lastPathSegment.toString()*/
                            imagesToUpload =
                                uiHelper.multiPart(
                                    file.path.toUri(),
                                    "files"
                                )
                            count = 0
                            if (!isRequestSent) {
                                isRequestSent = true
                                carListViewModel.uploadImage(
                                    true,
                                    Constants.API.VAT_REGISTRATION,
                                    this,
                                    imagesToUpload
                                )
                            }
                        }
                    }
                }
            }
            carInfoModel.nationalIDImagePath = snapOfNationalId
            carInfoModel.istamarImagePath = snapOfIstamara
            carInfoModel.technicalReportPath = technicalReportPath
        }
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view.id) {
            R.id.editInfoLocation -> {
                val bundle = Bundle()
                bundle.putSerializable(MODEL, carInfoModel)
                uiHelper.openActivityForResult(
                    requireActivity(),
                    MapsActivity::class.java,
                    true,
                    ACTIVTIY_LOCATION, bundle
                )
            }
            R.id.locationBtn -> {
                val bundle = Bundle()
                bundle.putSerializable(MODEL, carInfoModel)
                uiHelper.openActivityForResult(
                    requireActivity(),
                    MapsActivity::class.java,
                    true,
                    ACTIVTIY_LOCATION, bundle
                )
            }
            R.id.editInfoCar -> {
                val bundle = Bundle()
                bundle.putSerializable(MODEL, carInfoModel)
                bundle.putSerializable(CARINFODATAMODEL, carListDataModel)
                uiHelper.openActivityForResult(
                    requireActivity(),
                    CarInformationActivity::class.java,
                    true,
                    ACTIVITY_CAR_INFO, bundle
                )
            }
            R.id.carInfoBtn -> {
                val bundle = Bundle()
                bundle.putSerializable(MODEL, carInfoModel)
                bundle.putSerializable(CARINFODATAMODEL, carListDataModel)
                uiHelper.openActivityForResult(
                    requireActivity(),
                    CarInformationActivity::class.java,
                    true,
                    ACTIVITY_CAR_INFO, bundle
                )
            }
            R.id.ivCommercialRegister ->{

                tripsViewModel.showFullImage(
                    requireActivity(),
                    commercialRegisterUrl
                )
            }

            R.id.ivVatCertificate ->{
                tripsViewModel.showFullImage(
                    requireActivity(),
                    vatCertificateUrl
                )
            }
            R.id.ivVehicleRegistrationCard ->{
                tripsViewModel.showFullImage(
                    requireActivity(),
                    vehicleRegistrationCardUrl
                )
            }

            R.id.ivTechnicalReport ->{

                tripsViewModel.showFullImage(
                    requireActivity(),
                    technicalReportUrl
                )
            }
            R.id.viewPdf ->{
                tripsViewModel.showFullWebView(
                    requireActivity(),
                    pdfTechnicalRepot
                )
            }



            R.id.btnUploadTechnicalReport -> {
                pickerSwitch = 0
                imgOrFile()
            }
            binding.rlUserStatus.id -> {
                showPopup(view)
            }
            binding.btnUserStatusRelatedImage.id -> {
                pickerSwitch = 1
                if (!isCompany) {
                    showPickerDialog()
                } else {
                    imagePick()
                }
            }
            binding.btnUploadVehicleRegistrationCard.id -> {
                pickerSwitch = 2
                showPickerDialog()
            }
            binding.btnVatRegistration.id -> {
                pickerSwitch = 3
                imagePick()
            }
            R.id.addCarInfo -> {
                if (carInfoModel.makeName.isNotEmpty() && carInfoModel.modelName.isNotEmpty()) {
                    val bundle = Bundle()
                    bundle.putSerializable(MODEL, carInfoModel)
                    bundle.putSerializable(CARINFODATAMODEL, carListDataModel)
                    bundle.putString(
                        Constants.DataParsing.NAME,
                        "${carInfoModel.makeName} ${carInfoModel.modelName}"
                    )
                    uiHelper.openActivityForResult(
                        requireActivity(),
                        CarFeaturesActivity::class.java,
                        true,
                        ACTIVITY_CAR_FEATURE, bundle
                    )
                } else {
                    uiHelper.showLongToastInCenter(
                        requireActivity(), "please select car make model information first"
                    )
                }
            }
            R.id.next -> {
                //changing button transition
                sendDataToAPi()
            }

            dropDownViewCompanySelectionBinding.company.id -> {
                isCompany = true
                needToShowError = false
                popupWindow.dismiss()
                carInfoModel.hostType = Constants.HostType.COMPANY
                setTitles()
                setVisibilities()
            }

            dropDownViewCompanySelectionBinding.individual.id -> {
                popupWindow.dismiss()
                isCompany = false
                needToShowError = false
                carInfoModel.hostType = Constants.HostType.INDIVIDUAL
                setTitles()
                setVisibilities()
            }

            binding.tvSeeMore.id -> {
                binding.tvSeeMore.isVisible = false
                binding.tvViewLess.isVisible = true
                (binding.carFeatureList.adapter as CarFeatureAdapterVertical).setList(
                    carFeature
                )
            }
            binding.tvViewLess.id -> {
                binding.tvSeeMore.isVisible = true
                binding.tvViewLess.isVisible = false
                (binding.carFeatureList.adapter as CarFeatureAdapterVertical).setList(
                    ArrayList(
                        carFeature.take(4)
                    )
                )
            }
        }
    }

    private fun showPickerDialog() {
        requireActivity().showChooseAppDialog {
            when (it) {
                MyImagePickerEnum.CAMERA -> {
                    val intent =
                        Intent(requireActivity(), TakePhotoActivity::class.java)
                    intent.putExtra(Constants.DataParsing.MOVE_TO_DRIVER_INFO, MoveFrom.OTHER_ACTIVITY.value)
                    launcherTakePhoto.launch(intent)
                }
                MyImagePickerEnum.GALLERY -> {
                    ImagePicker.with(
                        requireActivity()
                    )
                        .galleryOnly()
                        .crop()
                        .compress(1024) //Final image size will be less than 1 MB(Optional)
                        .maxResultSize(
                            1080,
                            1080
                        )    //Final image resolution will be less than 1080 x 1080(Optional)
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
        count = 0
        when (pickerSwitch) {
            1 -> {
                natIDPath = url.path.toString()
                binding.btnUserStatusRelatedImage.text =
                    url.lastPathSegment.toString()
                if (!isRequestSent) {
                    isRequestSent = true
                    carListViewModel.uploadImage(
                        true,
                        Constants.API.SNAP_OF_NATIONAL_ID,
                        this,
                        imagesToUpload
                    )
                }
            }
            2 -> {
                drivingLicensePath = url.path.toString()
             //   binding.btnUploadVehicleRegistrationCard.text = url.lastPathSegment.toString()
                if (!isRequestSent) {
                    isRequestSent = true
                    carListViewModel.uploadImage(
                        true,
                        Constants.API.SNAP_OF_DRIVING_LICENSE,
                        this,
                        imagesToUpload
                    )
                }
            }
        }
    }


    private fun setTitles() {
        binding.edtUserStatusRelatedNumber.setText("")
        binding.spUserStatus.text =
            if (isCompany) getString(R.string.company) else getString(R.string.individual)
        binding.edtUserStatusRelatedNumber.setHint(
            if (isCompany) R.string.cr_number else R.string.id_number
        )
        if ((carListDataModel.hostType == Constants.HostType.COMPANY && !isCompany) || (carListDataModel.hostType == Constants.HostType.INDIVIDUAL && isCompany)) {
            binding.btnVatRegistration.text = getString(R.string.upload_your_photo)
            binding.btnUserStatusRelatedImage.text = getString(R.string.upload_your_photo)
        }
        binding.tvUserStatusRelatedTitle.setText(
            if (isCompany) R.string.upload_image_of_commercial_registration else R.string.upload_image_of_your_national_id
        )
        binding.majorAccidentCheck.text = if (isCompany) getString(
            R.string.car_has_never_been_in_any_major_accident,
            getString(R.string.this_)
        ) else getString(R.string.car_has_never_been_in_any_major_accident, getString(R.string.my))
        binding.listPersonalVehicleCheck.text =
            if (isCompany) getString(R.string.this_car_is_owned_by_the_company) else getString(R.string.i_have_listed_my_personal_vehicle)

    }

    private fun setVisibilities() {
        binding.clCompanyVatRegistration.isVisible = isCompany
        binding.clUserFields.isVisible = true
        binding.edtUserStatusRelatedNumber.isVisible = true
    }

    /**
     * Function for sending data to server
     * */
    private fun sendDataToAPi() {
        if ((binding.edtUserStatusRelatedNumber.text.toString()
                .isEmpty() || binding.edtUserStatusRelatedNumber.text.toString().length < 10) && carListDataModel.carExist == Constants.NOT_EXIST
        ) {
            uiHelper.showLongToastInCenter(
                requireContext(),
                if (isCompany) getString(
                    R.string.user_status_related_number_err_msg,
                    getString(R.string.the_company_registration_number)
                ) else getString(
                    R.string.user_status_related_number_err_msg,
                    getString(R.string.the_national_id)
                )
            )
        } else if (carInfoModel.nationalIDImagePath.isEmpty() && binding.clUserFields.isVisible) {
            uiHelper.showLongToastInCenter(
                requireContext(),
                getString(R.string.add_snap_national_id_err_msg)
            )
        } else if (carInfoModel.street_address.isEmpty()) {
            uiHelper.showLongToastInCenter(requireContext(), getString(R.string.enter_location))
        } else if (carInfoModel.year_id == 0) {
            uiHelper.showLongToastInCenter(
                requireContext(),
                getString(R.string.enter_car_info_err_msg)
            )
        } else if (carInfoModel.istamarImagePath.isEmpty()) {

            uiHelper.showLongToastInCenter(
                requireContext(),
                getString(R.string.enter_snap_of_istamara_err_msg)
            )
        } else if (binding.vinChassisEdt.text.toString().isEmpty()) {
            uiHelper.showLongToastInCenter(
                requireContext(),
                getString(R.string.enter_vin_chassis_err_msg)
            )
        } else if (carInfoModel.technicalReportPath.isEmpty()) {
            uiHelper.showLongToastInCenter(
                requireContext(),
                getString(R.string.upload_latest_technical_report)
            )
        } else if (!binding.majorAccidentCheck.isChecked) {
            uiHelper.showLongToastInCenter(
                requireContext(),
                getString(R.string.check_major_accident_err_msg)
            )
        } else if (!binding.listPersonalVehicleCheck.isChecked) {
            uiHelper.showLongToastInCenter(
                requireContext(),
                getString(R.string.check_personal_vehicle_accident_err_msg)
            )
        } else if (binding.licencePlateEdt.text.toString().isEmpty()) {
            uiHelper.showLongToastInCenter(
                requireContext(),
                getString(R.string.enter_licence_plate_err_msg)
            )
        } else if (citiesList.isEmpty()) {
            uiHelper.showLongToastInCenter(
                requireContext(),
                getString(R.string.select_registration_err_msg)
            )
        } else if (carInfoModel.car_features.isEmpty()) {

            uiHelper.showLongToastInCenter(
                requireContext(),
                getString(R.string.add_car_feature_err_msg)
            )
        } else if (binding.descriptionEdt.text.toString().isEmpty()) {
            uiHelper.showLongToastInCenter(
                requireContext(),
                getString(R.string.enter_description_car_err_msg)
            )
        } else {
            val userModel = preferenceHelper.getObject(
                Constants.USER_MODEL,
                UserModel::class.java
            ) as? UserModel
            carInfoModel.user_id = userModel!!.id
            carInfoModel.national_id =
                if (!isCompany && binding.clUserFields.isVisible) binding.edtUserStatusRelatedNumber.text.toString() else ""
            carInfoModel.crNumber =
                if (isCompany && binding.clUserFields.isVisible) binding.edtUserStatusRelatedNumber.text.toString() else ""
            carInfoModel.vin_chasis_num = binding.vinChassisEdt.text.toString()
            carInfoModel.car_registration_city_id =
                citiesList[binding.citiesList.selectedItemPosition].id
            carInfoModel.description = binding.descriptionEdt.text.toString()
            carInfoModel.licence_plate_num = binding.licencePlateEdt.text.toString()
            if (binding.automatic.isChecked) {
                carInfoModel.transmission = "Automatic"
            } else {
                carInfoModel.transmission = "Manual"
            }
            carInfoModel.list_personal_car = 1
            carInfoModel.never_major_accident = 1

            carInfoModel.step = Constants.CarStep.STEP_1_COMPLETED
            carInfoModel.pin_address = carInfoModel.street_address
            carInfoModel.isSelectionMade = binding.clUserFields.isVisible
            carListViewModel.sendCarInfo(
                carInfoModel,
                true,
                Constants.API.SEND_CAR_DATA,
                this
            )

        }

    }


    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.SEND_CAR_DATA -> {
                stepOneDataModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    StepOneDataModel::class.java
                )
                carListDataModel.hostType = carInfoModel.hostType
                carInfoModel.nationalIDImagePath =
                    stepOneDataModel?.carInfoModel?.snap_of_national_id
                        ?: stepOneDataModel?.company_registration_snap
                                ?: getString(R.string.upload_your_photo)
                carInfoModel.technicalReportPath =
                    stepOneDataModel?.carInfoModel?.latest_techical_report ?: ""
                carInfoModel.vat_registration_snap =
                    stepOneDataModel?.vat_registration_snap ?: getString(R.string.upload_your_photo)
                carInfoModel.istamarImagePath =
                    stepOneDataModel?.carInfoModel?.snap_of_istimara ?: ""
                loadImages()
                carListDataModel.isStepOneApiRequired = true
                isApiNeeded = true
                preferenceHelper.saveObject(
                    carListDataModel,
                    Constants.DataParsing.CAR_LIST_DATA_MODEL
                )
                val navController = requireActivity().findNavController(R.id.navHostFragmentCarList)
                navController.navigate(
                    R.id.action_carInformationFragment_to_carAvailabilityFragment
                )
            }
            Constants.API.CITIES_LIST -> {
                val cityModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    CitiesListModel::class.java
                )
                citiesList = cityModel.cityList
                setSpinnerData()
            }
            Constants.API.GET_CAR_LIST -> {

                stepOneDataModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    StepOneDataModel::class.java
                )

                populateStepOneData()
            }
            Constants.API.SNAP_OF_NATIONAL_ID -> {
                val imagesResponseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    ImagesResponseModel::class.java
                )
                isRequestSent = false
                carInfoModel.nationalIDImagePath = imagesResponseModel.imagesList[0]


                binding.cvUserStatusRelatedImage.isVisible = true
                binding.btnUserStatusRelatedImage.text = getString(R.string.Update_image)

                commercialRegisterUrl = BuildConfig.IMAGE_BASE_URL + carInfoModel.nationalIDImagePath
                uiHelper.glideLoadImage(
                    requireActivity(),
                    BuildConfig.IMAGE_BASE_URL + carInfoModel.nationalIDImagePath,
                    binding.ivCommercialRegister
                )


            }
            Constants.API.SNAP_OF_DRIVING_LICENSE -> {
                val imagesResponseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    ImagesResponseModel::class.java
                )
                carInfoModel.istamarImagePath = imagesResponseModel.imagesList[0]
                isRequestSent = false


                binding.cvVehicleRegistrationCard.isVisible = true
                binding.btnUploadVehicleRegistrationCard.text = getString(R.string.Update_image)
                vehicleRegistrationCardUrl = BuildConfig.IMAGE_BASE_URL +  carInfoModel.istamarImagePath
                uiHelper.glideLoadImage(
                    requireActivity(),
                    BuildConfig.IMAGE_BASE_URL +  carInfoModel.istamarImagePath,
                    binding.ivVehicleRegistrationCard
                )

             //
            }
            Constants.API.TECHNICAL_REPORT -> {
                val imagesResponseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    ImagesResponseModel::class.java
                )
                isRequestSent = false
                carInfoModel.technicalReportPath = imagesResponseModel.imagesList[0]

                if (carInfoModel.technicalReportPath.contains(".pdf")){
                    pdfTechnicalRepot = carInfoModel.technicalReportPath
                    binding.cvTechnicalReport.isVisible = true
                    binding.btnUploadTechnicalReport.text = getString(R.string.Update_document)
                    binding.wvPdfLoader.isVisible = true
                    binding.viewPdf.isVisible = true
                    binding.ivTechnicalReport.isVisible = false
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.wvPdfLoader.webViewClient = WebViewClient()
                        binding.wvPdfLoader.settings.setSupportZoom(false)
                        binding.wvPdfLoader.settings.javaScriptEnabled = true
                        binding.wvPdfLoader.settings.builtInZoomControls = false

                      //  showLoaderDialog()
                        Log.e("onCreate: ", BuildConfig.IMAGE_BASE_URL + carInfoModel.technicalReportPath)
                        binding.wvPdfLoader.loadUrl("https://docs.google.com/gview?embedded=true&url=${BuildConfig.IMAGE_BASE_URL + carInfoModel.technicalReportPath}")
                        binding.wvPdfLoader.webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView, url: String) {
                            //    hideLoaderDialog()
                            }
                        }
                    }, 500)

                }else{
                    binding.cvTechnicalReport.isVisible = true
                    binding.btnUploadTechnicalReport.text = getString(R.string.Update_image)
                    binding.wvPdfLoader.isVisible = false
                    binding.viewPdf.isVisible = false
                    binding.ivTechnicalReport.isVisible = true
                    technicalReportUrl = BuildConfig.IMAGE_BASE_URL +  carInfoModel.technicalReportPath
                    uiHelper.glideLoadImage(
                        requireActivity(),
                        BuildConfig.IMAGE_BASE_URL +  carInfoModel.technicalReportPath,
                        binding.ivTechnicalReport
                    )
                }

              //  binding.btnUploadTechnicalReport.text = carInfoModel.technicalReportPath.toUri().lastPathSegment


            }

            Constants.API.VAT_REGISTRATION -> {
                val imagesResponseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    ImagesResponseModel::class.java
                )
                carInfoModel.vat_registration_snap = imagesResponseModel.imagesList[0]
                isRequestSent = false

                vatCertificateUrl = BuildConfig.IMAGE_BASE_URL + carInfoModel.vat_registration_snap
                binding.cvVatCertificate.isVisible = true
                binding.btnVatRegistration.text = getString(R.string.Update_image)
                uiHelper.glideLoadImage(
                    requireActivity(),
                    BuildConfig.IMAGE_BASE_URL + carInfoModel.vat_registration_snap,
                    binding.ivVatCertificate
                )
              //  binding.btnVatRegistration.text = carInfoModel.vat_registration_snap
            }
        }
    }

    private fun setUserStatus() {
        binding.rlUserStatus.isEnabled = carListDataModel.carExist == Constants.NOT_EXIST
        val userStatus = if (carInfoModel.isSelectionMade)
            if (carInfoModel.hostType == Constants.HostType.COMPANY) R.string.company else R.string.individual
        else
            if (carListDataModel.carExist == Constants.EXIST || carListDataModel.isStepOneApiRequired)
                if (carInfoModel.hostType == Constants.HostType.COMPANY) R.string.company else R.string.individual
            else
                R.string.select_user_status
        binding.spUserStatus.text = getString(userStatus)
        binding.clUserFields.isVisible =
            carInfoModel.isSelectionMade || carListDataModel.carExist == Constants.NOT_EXIST
        binding.edtUserStatusRelatedNumber.isVisible = binding.clUserFields.isVisible
        binding.majorAccidentCheck.text =
            if (carInfoModel.hostType == Constants.HostType.COMPANY) getString(
                R.string.car_has_never_been_in_any_major_accident,
                getString(R.string.this_)
            ) else getString(
                R.string.car_has_never_been_in_any_major_accident,
                getString(R.string.my)
            )
        binding.listPersonalVehicleCheck.text =
            if (carInfoModel.hostType == Constants.HostType.COMPANY) getString(R.string.this_car_is_owned_by_the_company) else getString(
                R.string.i_have_listed_my_personal_vehicle
            )

    }

    private fun loadImages() {

        binding.cvVehicleRegistrationCard.isVisible = true
        binding.btnUploadVehicleRegistrationCard.text = getString(R.string.Update_image)
        vehicleRegistrationCardUrl = BuildConfig.IMAGE_BASE_URL +  carInfoModel.istamarImagePath
        uiHelper.glideLoadImage(
            requireActivity(),
            BuildConfig.IMAGE_BASE_URL +  carInfoModel.istamarImagePath,
            binding.ivVehicleRegistrationCard
        )
        if (carListDataModel.hostType == Constants.HostType.COMPANY)
            binding.edtUserStatusRelatedNumber.setText(carInfoModel.crNumber)
        else
            binding.edtUserStatusRelatedNumber.setText(carInfoModel.national_id)


        binding.cvUserStatusRelatedImage.isVisible = true
        binding.btnUserStatusRelatedImage.text = getString(R.string.Update_image)
        commercialRegisterUrl = BuildConfig.IMAGE_BASE_URL + carInfoModel.nationalIDImagePath
        uiHelper.glideLoadImage(
            requireActivity(),
            BuildConfig.IMAGE_BASE_URL +  carInfoModel.nationalIDImagePath,
            binding.ivCommercialRegister
        )


        vatCertificateUrl = BuildConfig.IMAGE_BASE_URL + carInfoModel.vat_registration_snap
        binding.cvVatCertificate.isVisible = true
        binding.btnVatRegistration.text = getString(R.string.Update_image)
        uiHelper.glideLoadImage(
            requireActivity(),
            BuildConfig.IMAGE_BASE_URL +  carInfoModel.vat_registration_snap,
            binding.ivVatCertificate
        )

        if (carInfoModel.technicalReportPath.contains(".pdf")){
            binding.cvTechnicalReport.isVisible = true
            binding.btnUploadTechnicalReport.text = getString(R.string.Update_document)
            pdfTechnicalRepot = carInfoModel.technicalReportPath
            binding.wvPdfLoader.isVisible = true
            binding.viewPdf.isVisible = true
            binding.ivTechnicalReport.isVisible = false
            Handler(Looper.getMainLooper()).postDelayed({
                binding.wvPdfLoader.webViewClient = WebViewClient()
                binding.wvPdfLoader.settings.setSupportZoom(false)
                binding.wvPdfLoader.settings.javaScriptEnabled = true
                binding.wvPdfLoader.settings.builtInZoomControls = false

                Log.e("onCreate: ", BuildConfig.IMAGE_BASE_URL + carInfoModel.technicalReportPath)
                binding.wvPdfLoader.loadUrl("https://docs.google.com/gview?embedded=true&url=${BuildConfig.IMAGE_BASE_URL + carInfoModel.technicalReportPath}")
                binding.wvPdfLoader.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        //    hideLoaderDialog()
                    }
                }
            }, 500)

        }else {
            technicalReportUrl = BuildConfig.IMAGE_BASE_URL + carInfoModel.technicalReportPath
            binding.cvTechnicalReport.isVisible = true
            binding.btnUploadTechnicalReport.text = getString(R.string.Update_image)
            binding.wvPdfLoader.isVisible = false
            binding.viewPdf.isVisible = false
            binding.ivTechnicalReport.isVisible = true
            uiHelper.glideLoadImage(
                requireActivity(),
                BuildConfig.IMAGE_BASE_URL + carInfoModel.technicalReportPath,
                binding.ivTechnicalReport
            )
        }
    }

    private fun populateStepOneData() {
        stepOneDataModel?.let {
            carInfoModel.nationalIDImagePath =
                if (carInfoModel.hostType == Constants.HostType.COMPANY) it.company_registration_snap.toString() else it.snap_of_national_id.toString()
            carInfoModel.car_registration_city_id =
                it.carInfoModel.car_registration_city.id
            carInfoModel.national_id = it.national_id
            carInfoModel.vin_chasis_num =
                it.carInfoModel.vin_chasis_num
            carInfoModel.street_address =
                it.user_address.street_address
            carInfoModel.zip_code = it.user_address.zip_code
            carInfoModel.pin_lat = it.user_address.pin_lat
            carInfoModel.pin_long = it.user_address.pin_long
            carInfoModel.pin_address =
                it.user_address.pin_address
            carInfoModel.country_name =
                it.user_address.country_name
            carInfoModel.state_name = it.user_address.state_name
            carInfoModel.city_name = it.user_address.city_name
            carInfoModel.description =
                it.carInfoModel.description
            carInfoModel.step = it.step

            carInfoModel.year_id = it.year.id
            carInfoModel.make_id = it.make.id
            carInfoModel.odometer = it.odometer.id
            carInfoModel.model_id = it.model.id
            carInfoModel.crNumber = it.crNumber
            carInfoModel.company_registration_snap = it.company_registration_snap.toString()

            carInfoModel.licence_plate_num =
                it.carInfoModel.licence_plate_num

            binding.clUserFields.isVisible =
                carListDataModel.isStepOneApiRequired && carListDataModel.carExist == Constants.NOT_EXIST
            binding.edtUserStatusRelatedNumber.isVisible = binding.clUserFields.isVisible
            binding.edtUserStatusRelatedNumber.setText(
                if (isCompany) carInfoModel.crNumber else carInfoModel.national_id
            )
            carInfoModel.vat_registration_snap =
                it.vat_registration_snap ?: getString(R.string.upload_your_photo)
            isCompany = carInfoModel.hostType == Constants.HostType.COMPANY
            setUserStatus()
            setVisibilities()
            setTitles()
            binding.vinChassisEdt.setText(carInfoModel.vin_chasis_num)
            binding.descriptionEdt.setText(carInfoModel.description)
            binding.licencePlateEdt.setText(carInfoModel.licence_plate_num)
            binding.locationBtn.setText(carInfoModel.street_address)
            carInfoModel.modelName = it.model.name
            carInfoModel.makeName = it.make.name
            carInfoModel.year = it.year.year
            binding.carInfoBtn.setText(it.make.name + " " + it.model.name + " " + it.year.year)
            carInfoModel.istamarImagePath =
                it.carInfoModel.snap_of_istimara
            carInfoModel.technicalReportPath =
                it.carInfoModel.latest_techical_report

            if (it.carInfoModel.list_personal_car) {
                binding.listPersonalVehicleCheck.isChecked = true
            }

            if (it.carInfoModel.never_major_accident) {
                binding.majorAccidentCheck.isChecked = true
            }

            if (it.carInfoModel.transmission == getString(R.string.automatic)) {
                binding.automatic.isChecked = true
            } else {
                binding.manual.isChecked = true
            }
            binding.editInfoCar.text = getString(R.string.edit_)
            binding.editInfoLocation.text = getString(R.string.edit_)
            for (i in it.car_feature.indices) {
                carInfoModel.car_features.add(it.car_feature[i].id)
            }
            carFeature = it.car_feature
            binding.carFeatureList.layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL,
                false
            )
            val adapter = CarFeatureAdapterVertical(
                requireContext(),
                uiHelper
            )
            binding.viewCarFeatures.isVisible = it.car_feature.isEmpty()
            binding.carFeatureList.adapter = adapter

            adapter.setList(
                ArrayList(carFeature.take(4))
            )
            binding.tvSeeMore.isVisible = carFeature.size > 4
            binding.addCarInfo.text = getString(R.string.edit_)
            setSpinnerData()
            loadImages()
        }

    }

    private fun setSpinnerData() {

        if (citiesList.isNotEmpty()) {

            for (i in citiesList.indices) {
                carRegistrationCityList.add(citiesList[i].city)
                if (carInfoModel.car_registration_city_id == citiesList[i].id) {
                    selectedIndex = i
                }
            }
            val adapter: ArrayAdapter<String> = ArrayAdapter(
                requireActivity(),
                R.layout.spinner_simple_item, carRegistrationCityList
            )
            adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
            binding.citiesList.adapter = adapter
            binding.citiesList.setSelection(selectedIndex)
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(requireActivity(), response?.message)
        if (count < 3) {
            when (tag) {
                Constants.API.SNAP_OF_DRIVING_LICENSE -> {
                    carListViewModel.uploadImage(
                        true,
                        Constants.API.SNAP_OF_DRIVING_LICENSE,
                        this,
                        imagesToUpload
                    )
                    count++
                }

                Constants.API.SNAP_OF_NATIONAL_ID -> {
                    carListViewModel.uploadImage(
                        true,
                        Constants.API.SNAP_OF_NATIONAL_ID,
                        this,
                        imagesToUpload
                    )
                    count++
                }

                Constants.API.TECHNICAL_REPORT -> {
                    carListViewModel.uploadImage(
                        true,
                        Constants.API.TECHNICAL_REPORT,
                        this,
                        imagesToUpload
                    )
                    count++
                }
            }
        }

    }

    override fun onOtherButtonClick(actionSheet: ActionSheet?, index: Int) {
        when (index) {
            0 -> {
                ImagePicker.with(requireActivity())
                    .crop()
                    .compress(1024).cameraOnly() //Final image size will be less than 1 MB(Optional)
                    .maxResultSize(
                        1080,
                        1080
                    )  //Final image resolution will be less than 1080 x 1080(Optional)
                    .start { resultCode, data ->
                        when (resultCode) {
                            RESULT_OK -> {
                                val fileUri = data?.data
                                if (fileUri != null) {
                                    techReportPath = fileUri.path!!
                                  //  binding.btnUploadTechnicalReport.text = techReportPath.toUri().lastPathSegment
                                    imagesToUpload =
                                        uiHelper.multiPart(
                                            fileUri.path.toString().toUri(),
                                            "files"
                                        )
                                    count = 0
                                    carListViewModel.uploadImage(
                                        true,
                                        Constants.API.TECHNICAL_REPORT,
                                        this,
                                        imagesToUpload
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
                        }
                    }
            }
            1 -> {
                ImagePicker.with(requireActivity())
                    .crop()
                    .compress(1024)
                    .galleryOnly() //Final image size will be less than 1 MB(Optional)
                    .maxResultSize(
                        1080,
                        1080
                    )  //Final image resolution will be less than 1080 x 1080(Optional)
                    .start { resultCode, data ->
                        when (resultCode) {
                            RESULT_OK -> {
                                val fileUri = data?.data
                                if (fileUri != null) {
                                    techReportPath = fileUri.path!!
                                   // binding.btnUploadTechnicalReport.text = techReportPath.toUri().lastPathSegment
                                    imagesToUpload =
                                        uiHelper.multiPart(
                                            fileUri.path.toString().toUri(),
                                            "files"
                                        )
                                    count = 0
                                    carListViewModel.uploadImage(
                                        true,
                                        Constants.API.TECHNICAL_REPORT,
                                        this,
                                        imagesToUpload
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
                        }
                    }
            }
            else -> {
                browseDocuments(
                    launcher
                )
            }
        }
        actionSheet?.dismiss()
    }

    override fun onDismiss(actionSheet: ActionSheet?, isCancel: Boolean) {
    }

    private fun showPopup(v: View?) {
        popupWindow = PopupWindow(
            dropDownViewCompanySelectionBinding.root,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        popupWindow.isOutsideTouchable = true
        popupWindow.showAsDropDown(v)
        dropDownViewCompanySelectionBinding.company.setOnClickListener(this::onClick)
        dropDownViewCompanySelectionBinding.individual.setOnClickListener(this::onClick)
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

            Toast.makeText(requireActivity(), "Failed copying file: $errorCode", Toast.LENGTH_SHORT)
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
              //  binding.btnUploadTechnicalReport.text = path.toString().toUri().lastPathSegment
                count = 0
                if (!isRequestSent) {
                    carListViewModel.uploadImage(
                        true,
                        Constants.API.TECHNICAL_REPORT,
                        this@CarInformationFragment,
                        imagesToUpload
                    )
                }
            }

        }
    }

}