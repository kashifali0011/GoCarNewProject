package com.towsal.towsal.views.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.FragmentCarPhotosBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.ImagesResponseModel
import com.towsal.towsal.network.serializer.carlist.CarListDataModel
import com.towsal.towsal.network.serializer.carlist.step3.CarPhotoListModel
import com.towsal.towsal.network.serializer.carlist.step3.Step3Model
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.CarListViewModel
import com.towsal.towsal.viewmodel.TripsViewModel
import com.towsal.towsal.views.adapters.NewCarPhotosAdapter
import okhttp3.MultipartBody
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Fragment class for car photos step of car listing process
 * */
class CarPhotosFragment : BaseFragment(), OnNetworkResponse {

    private lateinit var binding: FragmentCarPhotosBinding
    private lateinit var adapter: NewCarPhotosAdapter
    private val carListViewModel: CarListViewModel by activityViewModels()
    private val tripsViewModel: TripsViewModel by viewModel()
    private var carListDataModel = CarListDataModel()
    private var step3Model: Step3Model? = null
    var imagesToUpload = ArrayList<MultipartBody.Part>()
    var retryCount = 0

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback = object : SimpleCallback(UP or DOWN or START or END, 0) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                //the position from where item has been moved
                val from = viewHolder.adapterPosition

                //the position where the item is moved
                val to = target.adapterPosition

                //telling the adapter to move the item
                adapter.moveItem(from, to)

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)


            }

            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) {
                super.clearView(recyclerView, viewHolder)


            }
        }

        ItemTouchHelper(simpleItemTouchCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_car_photos,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
        setData()

    }

    /**
     * Function for setting up data of the views
     * */
    private fun setData() {
        adapter = NewCarPhotosAdapter(
            requireActivity().parent ?: requireActivity(),
            false
        ){ index ->
            if (!binding.clImageOptions.isVisible)
                binding.clImageOptions.isVisible = true
            adapter.getList().removeAt(index)
            binding.save.isVisible = adapter.getList().size >= 5
            adapter.notifyDataSetChanged()
        }

        binding.rvCarPhotos.layoutManager = GridLayoutManager(requireActivity(), 2)
        binding.rvCarPhotos.adapter = adapter
        itemTouchHelper.attachToRecyclerView(binding.rvCarPhotos)

        carListDataModel = preferenceHelper.getObject(
            Constants.DataParsing.CAR_LIST_DATA_MODEL,
            CarListDataModel::class.java
        ) as CarListDataModel

        if (carListDataModel.isStepThreeApiRequired && step3Model == null) {
            callApi()
        } else if (carListDataModel.isStepThreeApiRequired && step3Model != null) {
            populateRecyclerData()
        }
        if (carListDataModel.finishActivity) {
            binding.save.text = requireActivity().getString(R.string.update_)
        }
    }

    private fun callApi() {
        carListViewModel.getCarList(
            showLoader = true,
            tag = Constants.API.GET_CAR_LIST,
            callback = this,
            step = Constants.CarStep.STEP_3_COMPLETED
        )
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view) {
            binding.save -> {
                if (step3Model == null) step3Model = Step3Model()
                if (adapter.getList().size >= 5)
                    (step3Model)?.let {
                        carListViewModel.sendCarPhotos(
                            ArrayList(adapter.getList().map {
                                val hashMap = HashMap<String, Any>()
                                hashMap[DataKeyValues.URL] = it.url
                                hashMap[DataKeyValues.SEQUENCE] = it.sequence ?: 0
                                hashMap[DataKeyValues.STATUS] = it.status
                                hashMap
                            }),
                            Constants.CarStep.STEP_3_COMPLETED,
                            true,
                            Constants.API.SEND_CAR_DATA,
                            this
                        )
                    }
                else gotoNextStep()
            }
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
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.SEND_CAR_DATA -> {
                step3Model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    Step3Model::class.java
                )
                uiHelper.showLongToastInCenter(
                    requireActivity(),
                    resources.getString(R.string.photos_success_msg)
                )
                carListDataModel.isStepThreeApiRequired = true
                preferenceHelper.saveObject(
                    carListDataModel,
                    Constants.DataParsing.CAR_LIST_DATA_MODEL
                )
                if (carListDataModel.finishActivity) {
                    preferenceHelper.clearKey(Constants.DataParsing.CAR_LIST_DATA_MODEL)
                    requireActivity().finish()
                } else
                    gotoNextStep()
            }

            Constants.API.GET_CAR_LIST -> {
                val step3Model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    Step3Model::class.java
                )
                this.step3Model = step3Model

                populateRecyclerData()
            }

            Constants.API.UPLOAD_PHOTO -> {
                val imagesResponseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    ImagesResponseModel::class.java
                )

                if (imagesResponseModel.imagesList.isNotEmpty()) {
                    val carPhotosModel = CarPhotoListModel()
                    carPhotosModel.url = imagesResponseModel.imagesList[0]
                    carPhotosModel.status = 0
                    carPhotosModel.sequence = adapter.getList().size
                    if (adapter.getList().size <= 20) {
                        adapter.getList().add(carPhotosModel)
                    }
                    binding.clImageOptions.isVisible = adapter.getList().size < 6
                    binding.save.isVisible = adapter.getList().size >= 5
                    adapter.notifyItemInserted(adapter.getList().size)
                }
            }
        }
    }

    /**
     * Function for going to next step of listing process
     * */
    private fun gotoNextStep() {
        val navController = requireActivity().findNavController(R.id.navHostFragmentCarList)
        preferenceHelper.saveObject(carListDataModel, Constants.DataParsing.CAR_LIST_DATA_MODEL)
        val direction =
            CarPhotosFragmentDirections.actionCarPhotosFragmentToVehicleProtectionFragment()
        navController.navigate(direction)
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
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
            else -> {
                uiHelper.showLongToastInCenter(requireContext(), response?.message)
            }
        }
    }


    private fun populateRecyclerData() {
        adapter.setList(step3Model?.car_photos?.imagesList ?: ArrayList())
        binding.save.isVisible = true
        binding.clImageOptions.isVisible = false
    }
}