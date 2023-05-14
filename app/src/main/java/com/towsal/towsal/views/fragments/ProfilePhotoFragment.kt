package com.towsal.towsal.views.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentProfilePhotoBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.ImagesResponseModel
import com.towsal.towsal.network.serializer.cardetails.UserInfoResponseModel
import com.towsal.towsal.network.serializer.cardetails.UserInfoStepTwoModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.UserInformationViewModel
import com.towsal.towsal.views.activities.UserInformationActivity
import okhttp3.MultipartBody

/**
 * Fragment class for profile photo
 * */
class ProfilePhotoFragment : BaseFragment(), OnNetworkResponse {

    private var imageUri = ""
    var model = UserInfoResponseModel()
    lateinit var binding: FragmentProfilePhotoBinding
    private val args: ProfilePhotoFragmentArgs by navArgs()
    private val userInformationViewModel: UserInformationViewModel by activityViewModels()
    var count = 0
    var imagesToUpload = ArrayList(emptyList<MultipartBody.Part>())
    var isRequestSent = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_profile_photo,
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
            binding.uploadBtn.isVisible = model.stepTwo.profile_image.isNotEmpty()
            if (model.stepTwo.profile_image.isNotEmpty())
                uiHelper.glideLoadImage(
                    requireContext(),
                    BuildConfig.IMAGE_BASE_URL + model.stepTwo.profile_image,
                    binding.image
                )
        }
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view.id) {
            R.id.image -> {
                ImagePicker.with(requireActivity())
                    .crop(1f, 1f)
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
                                    binding.image.setImageURI(fileUri)
                                    imagesToUpload =
                                        uiHelper.multiPart(
                                            fileUri,
                                            "files"
                                        )
                                    isRequestSent = true
                                    count = 0
                                    userInformationViewModel.uploadImage(
                                        showLoader = true,
                                        tag = Constants.API.UPLOAD_PHOTO,
                                        callback = this,
                                        arrayList = imagesToUpload
                                    )
                                }
                                binding.uploadBtn.isVisible = fileUri != null
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
                    }
            }
            R.id.uploadBtn -> {
                if (model.stepTwo.profile_image.isEmpty() && imageUri.isEmpty()) {
                    uiHelper.showLongToastInCenter(
                        requireContext(),
                        getString(R.string.add_profile_photo_err_msg)
                    )
                } else {
                    if (imageUri.isNotEmpty() && !isRequestSent) {
                        userInformationViewModel.saveProfilePhoto(
                            imageUri, true, Constants.API.SAVE_USER_INFO, this
                        )
                        return
                    } else if (isRequestSent) {
                        uiHelper.showLongToastInCenter(requireActivity(), "wait server is busy")
                    }
                    if (model.stepTwo.profile_image.isNotEmpty()) {
                        userInformationViewModel.setUserResponseModelValue(model)
                        moveToNextFragment()
                    }
                }
            }
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.SAVE_USER_INFO -> {
                model.stepTwo = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    UserInfoStepTwoModel::class.java
                )
                userInformationViewModel.setUserResponseModelValue(model)
                moveToNextFragment()
            }

            Constants.API.UPLOAD_PHOTO -> {
                val imagesResponseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    ImagesResponseModel::class.java
                )
                isRequestSent = false
                count = 0
                imageUri = imagesResponseModel.imagesList[0]
                uiHelper.loadSimpleImage(
                    binding.image,
                    BuildConfig.IMAGE_BASE_URL + imageUri
                )
            }
        }
    }


    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(requireActivity(), response?.message)
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
        }
    }

    private fun moveToNextFragment() {
        val navController =
            requireActivity().findNavController(R.id.navGraphUserInformation)
        val bundle = Bundle()
        bundle.putSerializable(Constants.DataParsing.MODEL, model)
        navController.navigate(
            R.id.action_profilePhotFragment_to_userLicenseInformationFragment,
            bundle
        )
        if (UserInformationActivity.stepInfoCallback != null) {
            UserInformationActivity.stepInfoCallback?.stepNumber(Constants.CarStep.STEP_3_COMPLETED)
        }
    }
}