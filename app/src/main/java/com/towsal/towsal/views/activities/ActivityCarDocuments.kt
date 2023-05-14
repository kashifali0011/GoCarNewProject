package com.towsal.towsal.views.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.documentfile.provider.DocumentFile
import com.anggrayudi.storage.callback.FileCallback
import com.anggrayudi.storage.file.copyFileTo
import com.baoyz.actionsheet.ActionSheet
import com.baoyz.actionsheet.ActionSheet.ActionSheetListener
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityCarDocumentsBinding
import com.towsal.towsal.extensions.browseDocuments
import com.towsal.towsal.extensions.intentToDocumentFiles
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.extensions.showChooseAppDialog
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.ImagesResponseModel
import com.towsal.towsal.network.serializer.carlist.step3.CarPhotoListModel
import com.towsal.towsal.network.serializer.carlist.step3.CarPhotosModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DocType
import com.towsal.towsal.utils.MoveFrom
import com.towsal.towsal.viewmodel.CarListViewModel
import com.towsal.towsal.viewmodel.TripsViewModel
import com.towsal.towsal.views.adapters.CarDocumentsAdapter
import com.towsal.towsal.views.adapters.CarDocumentsAdapter.Companion.UPDATE
import com.towsal.towsal.views.adapters.CarDocumentsAdapter.Companion.VIEW
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

/**
 * Activity class for user listed vehicle documents
 * */
class ActivityCarDocuments : BaseActivity(), OnNetworkResponse {

    lateinit var binding: ActivityCarDocumentsBinding
    val carListViewModel: CarListViewModel by viewModel()
    val tripsViewModel: TripsViewModel by viewModel()
    var carPhotosModel: CarPhotosModel = CarPhotosModel()
    var mArrayList: ArrayList<CarPhotoListModel> = ArrayList()
    var imagesToUpload = ArrayList(emptyList<MultipartBody.Part>())
    var positionToChange = 0
    var countRetry = 0
    var selectPosition = 0
    private val job = Job()
    private val ioScope = CoroutineScope(Dispatchers.IO + job)
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        if (it.resultCode == RESULT_OK) {
            val files = intentToDocumentFiles(it.data)

            if (files.isNotEmpty()) {
                val destFile = cacheDir

                ioScope.launch {
                    (files.first()).copyFileTo(
                        this@ActivityCarDocuments,
                        destFile.path.toString(),
                        null,
                        callback = createFileCallback()
                    )
                }

            }
        }
    }


    val adapter: CarDocumentsAdapter by lazy {
        CarDocumentsAdapter(this) { flow, position ->
            when (flow) {
                VIEW -> {
                    val bundle = Bundle()
                    bundle.putSerializable(Constants.DataParsing.IMAGE, mArrayList[position].url)
                    uiHelper.openActivity(this, FullScreenImageActivity::class.java, bundle)
                }
                UPDATE -> {
                    if (mArrayList[position].type == DocType.TECHNICAL_LATEST_REPORT.value) {
                        ActionSheet.createBuilder(this, supportFragmentManager)
                            .setCancelButtonTitle(getString(R.string.cancel_))
                            .setOtherButtonTitles(
                                getString(R.string.camera),
                                getString(R.string.gallery),
                                getString(R.string.file)
                            )
                            .setCancelableOnTouchOutside(true).setListener(
                                object : ActionSheetListener {
                                    override fun onDismiss(
                                        actionSheet: ActionSheet?,
                                        isCancel: Boolean
                                    ) {
                                    }

                                    override fun onOtherButtonClick(
                                        actionSheet: ActionSheet?,
                                        index: Int
                                    ) {
                                        when (index) {
                                            0 -> {
                                                ImagePicker.with(this@ActivityCarDocuments)
                                                    .crop()
                                                    .compress(1024)
                                                    .cameraOnly() //Final image size will be less than 1 MB(Optional)
                                                    .maxResultSize(
                                                        1080,
                                                        1080
                                                    )  //Final image resolution will be less than 1080 x 1080(Optional)
                                                    .start { resultCode, data ->
                                                        when (resultCode) {
                                                            RESULT_OK -> {
                                                                uploadImage(position, data) //2
                                                            }
                                                            ImagePicker.RESULT_ERROR -> {
                                                                Toast.makeText(
                                                                    this@ActivityCarDocuments,
                                                                    ImagePicker.getError(data),
                                                                    Toast.LENGTH_SHORT
                                                                )
                                                                    .show()
                                                            }
                                                        }
                                                    }
                                            }
                                            1 -> {
                                                ImagePicker.with(this@ActivityCarDocuments)
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
                                                                uploadImage(position, data) //3
                                                            }
                                                            ImagePicker.RESULT_ERROR -> {
                                                                Toast.makeText(
                                                                    this@ActivityCarDocuments,
                                                                    ImagePicker.getError(data),
                                                                    Toast.LENGTH_SHORT
                                                                )
                                                                    .show()
                                                            }
                                                        }
                                                    }
                                            }
                                            else -> {
                                                positionToChange = position
                                                browseDocuments(launcher)
                                            }
                                        }

                                    }

                                }
                            ).show()

                    } else if (mArrayList[position].type in listOf(
                            DocType.VEHICLE_REGISTRATION_CARD.value,
                            DocType.NATIONAL_ID.value
                        )
                    ) {

                        selectPosition = position
                        showPickerDialog()


                    } else {
                        ImagePicker.with(this)
                            .crop()
                            .compress(1024) //Final image size will be less than 1 MB(Optional)
                            .maxResultSize(
                                1080,
                                1080
                            )  //Final image resolution will be less than 1080 x 1080(Optional)
                            .start { resultCode, data ->
                                when (resultCode) {
                                    Activity.RESULT_OK -> {
                                        uploadImage(position, data) //1
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
                            }
                    }
                }
            }
        }
    }

    private fun uploadImage(position: Int, data: Intent?) {
        val fileUri = data?.data
        if (fileUri != null) {
            imagesToUpload = uiHelper.multiPart(
                fileUri,
                "files"
            )
            positionToChange = position
            countRetry = 0
            tripsViewModel.uploadImage(
                true,
                Constants.API.UPLOAD_PHOTO,
                this@ActivityCarDocuments,
                imagesToUpload
            )
        }
    }

    var carId = 0

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
                    imagesToUpload =
                        uiHelper.multiPart(
                            file.path.toUri(),
                            "files"
                        )

                    positionToChange = selectPosition
                    countRetry = 0
                    tripsViewModel.uploadImage(
                        true,
                        Constants.API.UPLOAD_PHOTO,
                        this@ActivityCarDocuments,
                        imagesToUpload
                    )

                }

            }}}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_car_documents)
        carId = intent?.extras?.getInt(Constants.DataParsing.CAR_ID, 0) ?: 0
        setData()
        actionBarSetting()

    }

    private fun createFileCallback() = object : FileCallback(uiScope) {


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

            Toast.makeText(baseContext, "Failed copying file: $errorCode", Toast.LENGTH_SHORT)
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
                count = 0
                carListViewModel.uploadImage(
                    true,
                    Constants.API.UPLOAD_PHOTO,
                    this@ActivityCarDocuments,
                    imagesToUpload
                )
            }

        }
    }

    /**
     * Function for calling api
     * */
    private fun setData() {
        carListViewModel.getCarDocuments(carId, this, Constants.API.CAR_DOCUMENTS)
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.CAR_DOCUMENTS -> {
                carPhotosModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    CarPhotosModel::class.java
                )
                mArrayList.clear()
                mArrayList.addAll(carPhotosModel.imagesList)
                adapter.setList(mArrayList)
                binding.photosRecyclerView.adapter = adapter
            }

            Constants.API.UPDATE_CAR_DOCUMENTS -> {
                uiHelper.showLongToastInCenter(
                    this,
                    resources.getString(R.string.documents_success_msg)
                )
                setData()
            }

            Constants.API.UPLOAD_PHOTO -> {
                val imagesResponseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    ImagesResponseModel::class.java
                )
                count = 0
                mArrayList[positionToChange].url = imagesResponseModel.imagesList[0]
                mArrayList[positionToChange].status = -1
                carListViewModel.updateCarDocs(
                    carId,
                    mArrayList,
                    this,
                    Constants.API.UPDATE_CAR_DOCUMENTS
                )
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }

    /**
     * Function for setting the action bar
     * */
    private fun actionBarSetting() {
        binding.layoutToolBar.setAsHostToolBar(
            R.string.documents,
            supportActionBar
        )
    }

    private fun showPickerDialog() {
        this@ActivityCarDocuments.showChooseAppDialog {
            when (it) {
                com.towsal.towsal.utils.ImagePicker.CAMERA -> {
                    val intent =
                        Intent(this@ActivityCarDocuments, TakePhotoActivity::class.java)
                    intent.putExtra(Constants.DataParsing.MOVE_TO_DRIVER_INFO, MoveFrom.OTHER_ACTIVITY.value)
                    launcherTakePhoto.launch(intent)
                }
                com.towsal.towsal.utils.ImagePicker.GALLERY -> {
                    ImagePicker.with(
                        this@ActivityCarDocuments
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
        positionToChange = selectPosition
        countRetry = 0
        tripsViewModel.uploadImage(
            true,
            Constants.API.UPLOAD_PHOTO,
            this@ActivityCarDocuments,
            imagesToUpload
        )
    }

}