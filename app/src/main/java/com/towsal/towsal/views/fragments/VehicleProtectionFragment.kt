package com.towsal.towsal.views.fragments

import android.app.Activity
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.anggrayudi.storage.callback.FileCallback
import com.anggrayudi.storage.file.copyFileTo
import com.baoyz.actionsheet.ActionSheet
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.hbisoft.pickit.PickiT
import com.hbisoft.pickit.PickiTCallbacks
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentVehicleProtectionBinding
import com.towsal.towsal.extensions.browseDocuments
import com.towsal.towsal.extensions.copy
import com.towsal.towsal.extensions.formatDate
import com.towsal.towsal.extensions.intentToDocumentFiles
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.ImagesResponseModel
import com.towsal.towsal.network.serializer.carlist.CarListDataModel
import com.towsal.towsal.network.serializer.carlist.step4.Step4Model
import com.towsal.towsal.network.serializer.carlist.step4.VehicleProtectionPostModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil
import com.towsal.towsal.viewmodel.CarListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import java.io.File
import java.util.*

/**
 * Fragment class for vehicle protection
 * */
class VehicleProtectionFragment : BaseFragment(), OnNetworkResponse,
    ActionSheet.ActionSheetListener,  DatePicker.OnDateChangedListener {

    private var vehicleProtection = VehicleProtectionPostModel()
    lateinit var binding: FragmentVehicleProtectionBinding
    private var expiryDate = ""

    //    private val args: VehicleProtectionFragmentArgs by navArgs()
    val carListViewModel: CarListViewModel by activityViewModels()
    var carListDataModel = CarListDataModel()
    var step4Model: Step4Model? = null
    var imagesToUpload = ArrayList<MultipartBody.Part>()
    var count = 0

    private var insurancePath = ""
    private var insuranceReportUri = Uri.EMPTY

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
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_vehicle_protection,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
        carListDataModel = preferenceHelper.getObject(
            Constants.DataParsing.CAR_LIST_DATA_MODEL,
            CarListDataModel::class.java
        ) as CarListDataModel
        binding.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.yesInsurance) {
                binding.insuranceLayout.visibility = View.VISIBLE
            } else {
                binding.insuranceLayout.visibility = View.GONE
            }
        }
        binding.datePicker.minDate = Calendar.getInstance().time.time
        binding.datePicker.formatDate("dmy")
        setData()
        if (carListDataModel.finishActivity) {
            binding.save.text = requireActivity().getString(R.string.update_)
        }

    }


    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        if (carListDataModel.disableViews) {
            binding.yesInsurance.isEnabled = false
            binding.noInsurance.isEnabled = false
            binding.insurancePolicyEdt.isEnabled = false
            binding.yesInsuranceRC.isEnabled = false
            binding.insuranceCoverageEdt.isEnabled = false
            binding.datePicker.isEnabled = false
            binding.noInsuranceRC.isEnabled = false
            binding.noInsuranceRC.isEnabled = false
            binding.noInsuranceRC.isEnabled = false
            binding.btnInsuranceImg.isEnabled = false
        }

        if (carListDataModel.isStepFourApiRequired && step4Model == null)
            getVehicleProtection()
        else {
            val calendar = Calendar.getInstance()
            calendar[Calendar.DAY_OF_YEAR] += 1
            binding.datePicker.init(
                calendar[Calendar.YEAR],
                calendar[Calendar.MONTH],
                calendar[Calendar.DAY_OF_MONTH],
                this
            )
        }

    }

    private fun getVehicleProtection() {
        carListViewModel.getCarList(
            true,
            Constants.API.GET_CAR_LIST,
            this,
            step = Constants.CarStep.STEP_4_COMPLETED
        )
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view) {
            binding.save -> {
                vehicleProtection.step = Constants.CarStep.STEP_4_COMPLETED
                if (binding.noInsurance.isChecked) {
                    vehicleProtection.have_insurance = false
                    callApi()
                } else {
                    checkValidation()
                }
            }
            binding.btnInsuranceImg -> {
                ActionSheet.createBuilder(requireActivity(), childFragmentManager)
                    .setCancelButtonTitle(getString(R.string.cancel_))
                    .setOtherButtonTitles(
                        getString(R.string.camera),
                        getString(R.string.gallery),
                        getString(R.string.file)
                    )
                    .setCancelableOnTouchOutside(true).setListener(this).show()
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
                    requireContext(),
                    getString(R.string.enter_insurance_policy_number)
                )
            }
            insurancePath.isEmpty() && vehicleProtection.snap_insurance.isEmpty() -> {
                uiHelper.showLongToastInCenter(
                    requireContext(),
                    getString(R.string.enter_copy_of_insurance_policy)
                )
            }
            binding.insuranceCoverageEdt.text.toString().isEmpty() -> {
                uiHelper.showLongToastInCenter(
                    requireContext(),
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
                if (insurancePath.isEmpty()) {
                    vehicleProtection.insuranceImage = null
                } else {
                    if (insuranceReportUri == Uri.EMPTY) {
                        vehicleProtection.insuranceImage =
                            uiHelper.multipartImage(insurancePath, "snap_insurance")
                    } else {
                        vehicleProtection.insuranceImage =
                            uiHelper.uriToMultipart(insuranceReportUri, "snap_insurance")

                    }
                }

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

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.SEND_CAR_DATA -> {
                if (carListDataModel.finishActivity) {
                    preferenceHelper.clearKey(Constants.DataParsing.CAR_LIST_DATA_MODEL)
                    requireActivity().finish()
                } else {
                    step4Model = Step4Model()
                    step4Model?.let {
                        it.vehicleProtection = Gson().fromJson(
                            uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                            VehicleProtectionPostModel::class.java
                        )
                        vehicleProtection = it.vehicleProtection

                        val navController =
                            requireActivity().findNavController(R.id.navHostFragmentCarList)
                        carListDataModel.isStepFourApiRequired = true
                        preferenceHelper.saveObject(
                            carListDataModel,
                            Constants.DataParsing.CAR_LIST_DATA_MODEL
                        )

                        navController.navigate(
                            R.id.action_vehicleProtectionFragment_to_safteyQualityStandardFragment
                        )
                    }

                }
            }
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
                binding.btnInsuranceImg.text =
                    vehicleProtection.snap_insurance.toUri().lastPathSegment
            }
        }
    }


    private fun showProtectionData() {
        step4Model?.let {
            if (it.vehicleProtection.have_insurance) {
                binding.yesInsurance.isChecked = true
            } else {
                binding.noInsurance.isChecked = true
            }
            vehicleProtection = it.vehicleProtection

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
                binding.datePicker.init(
                    calendar[Calendar.YEAR],
                    calendar[Calendar.MONTH],
                    calendar[Calendar.DAY_OF_MONTH],
                    this
                )
            }
            binding.insuranceCoverageEdt.setText(it.vehicleProtection.liability_covered)
            if (it.vehicleProtection.insurance_covered_cost) {
                binding.yesInsuranceRC.isChecked = true
            } else {
                binding.noInsuranceRC.isChecked = true
            }

            binding.btnInsuranceImg.text =
                it.vehicleProtection.snap_insurance.toUri().lastPathSegment

        }

    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(requireContext(), response?.message)
        when (tag) {
            Constants.API.UPLOAD_PHOTO -> {
                if (count < 3) {
                    callApiToUploadImage(
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
                            Activity.RESULT_OK -> {
                                val fileUri = data?.data
                                if (fileUri != null) {
                                    insurancePath = fileUri.path!!
                                    binding.btnInsuranceImg.text =
                                        insurancePath.toUri().lastPathSegment
                                    callApiToUploadImage(
                                        uiHelper.multiPart(
                                            fileUri,
                                            "files"
                                        )
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
                            Activity.RESULT_OK -> {
                                data?.let {
                                    val fileUri = data.data
                                    fileUri?.let {
                                        insurancePath = fileUri.path!!
                                        binding.btnInsuranceImg.text =
                                            insurancePath.toUri().lastPathSegment
                                        callApiToUploadImage(
                                            uiHelper.multiPart(
                                                fileUri,
                                                "files"
                                            )
                                        )
                                    }

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
                browseDocuments(launcher)
            }
        }
        actionSheet?.dismiss()
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

    override fun onDismiss(actionSheet: ActionSheet?, isCancel: Boolean) {
    }

    override fun onDateChanged(view: DatePicker?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        expiryDate =
            "$year-${if ((monthOfYear + 1).toString().length == 1) 0.toString() + (monthOfYear + 1).toString() else monthOfYear + 1}-${if ((dayOfMonth).toString().length == 1) "".toString() + (dayOfMonth).toString() else monthOfYear}"
        Log.e("onDateChanged: ", expiryDate)
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

            Toast.makeText(requireActivity(), "Failed copying file: $errorCode", Toast.LENGTH_SHORT).show()
        }

        override fun onCompleted(result: Any) {
            val path  = (result as DocumentFile).uri.path
            if (path.toString().isNotEmpty()) {
                imagesToUpload =
                    uiHelper.multiPartFile(
                        path.toString().toUri(),
                        "files"
                    )
                binding.btnInsuranceImg.text = path.toString().toUri().lastPathSegment
                count = 0
                callApiToUploadImage(
                    imagesToUpload
                )
            }

        }
    }



}