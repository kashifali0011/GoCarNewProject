package com.towsal.towsal.views.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentTripPhotosTypeSelectionBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.ImagesResponseModel
import com.towsal.towsal.network.serializer.carlist.step3.CarPhotoListModel
import com.towsal.towsal.network.serializer.fuellevels.FuelLevels
import com.towsal.towsal.network.serializer.fuellevels.FuelLevelsResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.TripsViewModel
import com.towsal.towsal.views.adapters.TripPhotosAdapter
import okhttp3.MultipartBody


class TripPhotosTypeSelectionFragment : BaseFragment(), View.OnClickListener, OnNetworkResponse {


    val tripsViewModel: TripsViewModel by activityViewModels()
    var imagesToUpload = ArrayList<MultipartBody.Part>()
    lateinit var binding: FragmentTripPhotosTypeSelectionBinding
    private var mArrayList = ArrayList<CarPhotoListModel>()
    private val fuelLevelsList = ArrayList<FuelLevels>()
    var retryCount = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTripPhotosTypeSelectionBinding.bind(
            inflater.inflate(R.layout.fragment_trip_photos_type_selection, container, false)
        )
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(p0: View, p1: Bundle?) {
        super.onViewCreated(p0, p1)

        tripsViewModel.getFuelLevels(
            true,
            Constants.API.FUEL_LEVEL,
            this
        )
    }

    override fun onClick(v: View?) {
        v?.preventDoubleClick()
        when (v) {
            binding.cvOpenGallery -> {
                ImagePicker.with(this)
                    .galleryOnly()
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
                                Log.d("ImageSelected", "onClick: ${data?.data}")
                                if (fileUri != null) {
                                    imagesToUpload =
                                        uiHelper.multiPart(
                                            fileUri,
                                            "files"
                                        )
                                    retryCount = 0
                                    tripsViewModel.uploadImage(
                                        showLoader = true,
                                        tag = Constants.API.UPLOAD_PHOTO,
                                        callback = this,
                                        arrayList = imagesToUpload
                                    )
                                }
                            }
                            ImagePicker.RESULT_ERROR -> {
                                Toast.makeText(
                                    requireActivity(),
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
            binding.cvOpenCamera -> {
                ImagePicker.with(this)
                    .cameraOnly()
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
                                Log.d("ImageSelected", "onClick: ${data?.data}")
                                if (fileUri != null) {
                                    imagesToUpload =
                                        uiHelper.multiPart(
                                            fileUri,
                                            "files"
                                        )
                                    retryCount = 0
                                    tripsViewModel.uploadImage(
                                        showLoader = true,
                                        tag = Constants.API.UPLOAD_PHOTO,
                                        callback = this,
                                        arrayList = imagesToUpload
                                    )
                                }
                            }
                            ImagePicker.RESULT_ERROR -> {
                                Toast.makeText(
                                    requireActivity(),
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
            binding.save -> {
                when {
                    binding.edtOdometerReading.text.toString().isEmpty() -> {
                        uiHelper.showLongToastInCenter(
                            requireActivity(),
                            getString(R.string.odometer_err_message)
                        )
                    }
                    mArrayList.isEmpty() -> {
                        uiHelper.showLongToastInCenter(requireActivity(), "Upload photos first")
                    }
                    else -> {
                        tripsViewModel.addTripPhotos(
                            list = mArrayList,
                            carId = tripsViewModel.carId,
                            tripId = tripsViewModel.checkOutId,
                            imageType = tripsViewModel.flowComingFrom,
                            odometerReading = binding.edtOdometerReading.text.toString(),
                            fuelLevelId = fuelLevelsList[binding.spFuelLevel.selectedItemPosition].id,
                            onNetworkResponse = this,
                            tag = Constants.API.PRE_HOST_TRIP_PHOTOS
                        )
                    }
                }
            }
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.FUEL_LEVEL -> {
                val fuelLevelsResponseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    FuelLevelsResponseModel::class.java
                )

                fuelLevelsList.addAll(fuelLevelsResponseModel.list)
                uiHelper.setSpinnerWithNewText(
                    requireActivity(),
                    ArrayList(
                        fuelLevelsList.map {
                            it.name
                        }
                    ),
                    binding.spFuelLevel
                )
                binding.spFuelLevel.setSelection(fuelLevelsList.size - 1)
                binding.rvCarPhotos.apply {
                    adapter = TripPhotosAdapter(
                        uiHelper,
                        false
                    )
                    (adapter as TripPhotosAdapter).setList(mArrayList)
                }
            }
            Constants.API.UPLOAD_PHOTO -> {
                val imagesResponseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    ImagesResponseModel::class.java
                )

                if (imagesResponseModel.imagesList.isNotEmpty()) {
                    val carPhotosModel = CarPhotoListModel()
                    carPhotosModel.url = imagesResponseModel.imagesList[0]
                    if (mArrayList.size <= 20) {
                        mArrayList.add(carPhotosModel)
                    }
                    binding.clImageOptions.isVisible = mArrayList.size <= 20
                    (binding.rvCarPhotos.adapter as TripPhotosAdapter).setList(mArrayList)
                }
            }

            Constants.API.PRE_HOST_TRIP_PHOTOS -> {
                requireActivity().finish()
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(requireActivity(), response?.message ?: "")
        when (tag) {
            Constants.API.UPLOAD_PHOTO -> {
                if (retryCount < 3) {
                    tripsViewModel.uploadImage(
                        showLoader = true,
                        tag = Constants.API.UPLOAD_PHOTO,
                        callback = this,
                        arrayList = imagesToUpload
                    )
                    retryCount++
                } else {
                    uiHelper.showLongToastInCenter(requireActivity(), response?.message ?: "")
                }
            }
        }
    }
}