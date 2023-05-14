package com.towsal.towsal.views.activities

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.anggrayudi.storage.callback.FileCallback
import com.anggrayudi.storage.file.copyFileTo
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityAddTripPhotosBinding
import com.towsal.towsal.databinding.ItemTripPhotosBinding
import com.towsal.towsal.extensions.browseDocuments
import com.towsal.towsal.extensions.intentToDocumentFiles
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.ImagesResponseModel
import com.towsal.towsal.network.serializer.carlist.step3.CarPhotoListModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.TripsViewModel
import com.towsal.towsal.views.adapters.TripPhotosAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddTripPhotosActivity : BaseActivity(), View.OnClickListener, OnNetworkResponse {

    val tripsViewModel: TripsViewModel by viewModel()
    lateinit var binding: ActivityAddTripPhotosBinding
    val arrayList: ArrayList<CarPhotoListModel> = ArrayList()
    var imagesToUpload = java.util.ArrayList<MultipartBody.Part>()
    var carId = ""
    var checkOutId = ""
    var flow = 1
    private var reasonId = 1
    private var fileTypeToAllow = Constants.FileTypes.IMAGES

    companion object {
        const val FLOW_FOR_PRE_TRIP_IMAGES = 1
        const val FLOW_FOR_POST_TRIP_IMAGES = 2
        const val FLOW_FROM_CHECKOUT_SCREEN = 3
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        if (it.resultCode == RESULT_OK) {
            val files = intentToDocumentFiles(it.data)

            if (files.isNotEmpty()) {
                val destFile = cacheDir

                lifecycleScope.launch(Dispatchers.IO) {
                    (files.first()).copyFileTo(
                        this@AddTripPhotosActivity,
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
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_trip_photos)
        binding.activity = this
        if (intent.extras != null) {
            carId = intent.extras?.getString(Constants.CAR_ID, "") ?: ""
            checkOutId = intent.extras?.getString(Constants.DataParsing.CHECKOUT_ID, "") ?: ""
            flow = intent.extras?.getInt(Constants.DataParsing.FLOW_COMING_FROM, 1) ?: 1
            reasonId = intent.extras?.getInt(Constants.DataParsing.REASON_ID, 1) ?: 1
            fileTypeToAllow =
                intent.extras?.getInt(Constants.DataParsing.FILE_TYPE, fileTypeToAllow)
                    ?: fileTypeToAllow
            tripsViewModel.flowComingFrom = flow
            tripsViewModel.carId = carId
            tripsViewModel.checkOutId = checkOutId
            binding.clAddTripPhotos.isVisible =
                flow in FLOW_FOR_PRE_TRIP_IMAGES..FLOW_FOR_POST_TRIP_IMAGES
            binding.clTripPhotos.isVisible = flow == FLOW_FROM_CHECKOUT_SCREEN

            binding.layoutToolBar.setAsGuestToolBar(
                if (flow in FLOW_FOR_PRE_TRIP_IMAGES..FLOW_FOR_POST_TRIP_IMAGES)
                    if (flow == FLOW_FOR_PRE_TRIP_IMAGES)
                        "At Pick up"
                    else "At Drop Off"
                else {
                    if(fileTypeToAllow == Constants.FileTypes.PDF)
                        getString(R.string.upload_documents)
                    else
                        getString(
                            R.string.upload_images
                        )
                },
                supportActionBar
            )

        }
        setRecyclerView()
    }

    private fun setRecyclerView() {
        binding.rvTripPhotos.apply {
            adapter = TripPhotosAdapter(
                uiHelper = uiHelper,
                fileType = fileTypeToAllow,
                callBackSelectImage = object : (Int, ItemTripPhotosBinding, View) -> Unit {
                    override fun invoke(position: Int, binding: ItemTripPhotosBinding, view: View) {
                        when (view) {
                            binding.cvTripPhoto -> {
                                if (fileTypeToAllow == Constants.FileTypes.PDF) {
                                    browseDocuments(launcher)
                                } else {
                                    ImagePicker.with(activity)
                                        .crop()
                                        .compress(
                                            1024
                                        )
                                        .maxResultSize(
                                            1080,
                                            1080
                                        )
                                        .start { resultCode, data ->
                                            when (resultCode) {
                                                Activity.RESULT_OK -> {
                                                    val fileUri = data?.data
                                                    Log.d("ImageSelected", "onClick: ${data?.data}")
                                                    if (fileUri != null) {
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
                                                else -> {

                                                }
                                            }
                                        }
                                }
                            }
                            binding.ivRemoveImage -> {
                                val addPlaceHolderExits = arrayList.any {
                                    it.url.isEmpty()
                                }
                                if (addPlaceHolderExits)
                                    arrayList.removeAt(position)
                                else {
                                    arrayList.add(0, CarPhotoListModel())
                                    arrayList.removeAt(position + 1)
                                }
                                setSaveButtonVisibility()
                                adapter?.notifyDataSetChanged()
                            }
                        }
                    }
                }
            )
            arrayList.add(CarPhotoListModel())
            (adapter as TripPhotosAdapter).setList(arrayList)
        }
    }

    private fun setSaveButtonVisibility() {
        binding.btnSave.isVisible = arrayList.size == 2
    }

    override fun onClick(p0: View) {
        p0.preventDoubleClick()
        when (p0) {
            binding.btnAddPhotos -> {
                binding.clAddTripPhotos.isVisible = false
                binding.navHostFragment.isVisible = true
                val navController = findNavController(binding.navHostFragment.id)
                navController.setGraph(
                    R.navigation.nav_graph_trip_photos
                )
            }
            binding.btnSave -> {
                tripsViewModel.addCancellationImages(
                    list = arrayList.filter { it.url.isNotEmpty() },
                    carId = carId,
                    tripId = checkOutId,
                    reasonId = reasonId,
                    onNetworkResponse = this,
                    tag = Constants.API.CANCELLATION_IMAGES
                )
            }
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.CANCELLATION_IMAGES -> {
                setResult(Activity.RESULT_OK)
                finish()
            }
            Constants.API.UPLOAD_PHOTO -> {
                val imagesResponseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    ImagesResponseModel::class.java
                )
                val carPhotoListModel = CarPhotoListModel()
                carPhotoListModel.url = imagesResponseModel.imagesList[0]
                arrayList.add(carPhotoListModel)
                if (arrayList.size == 7 && fileTypeToAllow == Constants.FileTypes.IMAGES) {
                    arrayList.removeAt(0)
                }
                if (arrayList.size == 3 && fileTypeToAllow == Constants.FileTypes.PDF) {
                    arrayList.removeAt(0)
                }

                (binding.rvTripPhotos.adapter as TripPhotosAdapter).notifyDataSetChanged()
                setSaveButtonVisibility()
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(
            this, response?.message ?: ""
        )
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
                this@AddTripPhotosActivity,
                "Failed copying file: $errorCode",
                Toast.LENGTH_SHORT
            ).show()
        }

        override fun onCompleted(result: Any) {
            val path = (result as DocumentFile).uri.path
            if (path.toString().isNotEmpty()) {
                imagesToUpload =
                    uiHelper.multiPartFile(
                        path.toString().toUri(),
                        "files"
                    )
                count = 0
                callApiToUploadImage(
                    imagesToUpload
                )
            }

        }
    }

    private fun callApiToUploadImage(list: java.util.ArrayList<MultipartBody.Part>) {
        imagesToUpload = list
        tripsViewModel.uploadImage(
            arrayList = imagesToUpload,
            showLoader = true,
            tag = Constants.API.UPLOAD_PHOTO,
            callback = this
        )
    }

}