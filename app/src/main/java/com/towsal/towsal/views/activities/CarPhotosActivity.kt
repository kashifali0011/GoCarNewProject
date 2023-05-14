package com.towsal.towsal.views.activities

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityCarPhotosBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.DataKeyValues
import com.towsal.towsal.network.serializer.ImagesResponseModel
import com.towsal.towsal.network.serializer.carlist.step3.CarPhotoListModel
import com.towsal.towsal.network.serializer.carlist.step3.Step3Model
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.CarListViewModel
import com.towsal.towsal.views.adapters.NewCarPhotosAdapter
import okhttp3.MultipartBody
import org.koin.androidx.viewmodel.ext.android.viewModel

class CarPhotosActivity : BaseActivity(), View.OnClickListener, OnNetworkResponse {
    lateinit var binding: ActivityCarPhotosBinding
    val carListViewModel: CarListViewModel by viewModel()
    var imagesToUpload = ArrayList<MultipartBody.Part>()
    private var step3Model: Step3Model? = null
    private lateinit var adapter: NewCarPhotosAdapter
    var retryCount = 0

    private val itemTouchHelper by lazy {
        val simpleItemTouchCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
            0
        ) {
            var from: Int = -1
            var to: Int = -1
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                //the position from where item has been moved
                from = viewHolder.adapterPosition

                //the position where the item is moved
                to = target.adapterPosition

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_car_photos)
        binding.activity = this
        carListViewModel.carId = intent?.extras?.getInt(Constants.DataParsing.CAR_ID, -1) ?: -1
        binding.layoutToolBar.setAsHostToolBar(
            R.string.car_photos,
            supportActionBar
        )
        adapter = NewCarPhotosAdapter(
            context = this,
        ) { index ->
            if (!binding.clImageOptions.isVisible)
                binding.clImageOptions.isVisible = true
            adapter.getList().removeAt(index)
            binding.save.isVisible = adapter.getList().size >= 5
            adapter.notifyDataSetChanged()
        }

        binding.rvCarPhotos.adapter = adapter
        itemTouchHelper.attachToRecyclerView(binding.rvCarPhotos)
        carListViewModel.getCarList(
            true,
            Constants.API.GET_CAR_LIST,
            this,
            Constants.CarStep.STEP_3_COMPLETED
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
                                    carListViewModel.uploadImage(
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
                                    carListViewModel.uploadImage(
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
                    }
            }
            binding.save -> {
                carListViewModel.sendCarPhotos(
                    ArrayList(
                        adapter.getList().map {
                            val hashMap = HashMap<String, Any>()
                            hashMap[DataKeyValues.URL] = it.url
                            hashMap[DataKeyValues.SEQUENCE] = it.sequence ?: 0
                            hashMap[DataKeyValues.STATUS] = it.status
                            hashMap
                        }
                    ),
                    Constants.CarStep.STEP_3_COMPLETED,
                    true,
                    Constants.API.SEND_CAR_DATA,
                    this
                )

            }
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_CAR_LIST -> {
                step3Model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    Step3Model::class.java
                )

                populateRecyclerData()
            }

            Constants.API.UPLOAD_PHOTO -> {
                val imagesResponseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    ImagesResponseModel::class.java
                )

                val carPhotoListModel = CarPhotoListModel()
                carPhotoListModel.url = imagesResponseModel.imagesList[0]
                carPhotoListModel.status = 0
                carPhotoListModel.sequence = adapter.getList().size
                adapter.getList().add(carPhotoListModel)
                adapter.notifyItemInserted(adapter.getList().size)
                binding.clImageOptions.isVisible = adapter.getList().size < 6
                binding.save.isVisible = adapter.getList().size >= 5
            }

            Constants.API.SEND_CAR_DATA -> {
                finish()
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(activity, response?.message)
        when (tag) {
            Constants.API.UPLOAD_PHOTO -> {
                if (retryCount < 3) {
                    carListViewModel.uploadImage(
                        showLoader = true,
                        tag = Constants.API.UPLOAD_PHOTO,
                        callback = this,
                        arrayList = imagesToUpload
                    )
                    retryCount++
                } else {
                    uiHelper.showLongToastInCenter(this, response?.message ?: "")
                }
            }
        }
    }

    private fun populateRecyclerData() {
        binding.clImageOptions.isVisible = (step3Model?.car_photos?.imagesList ?: ArrayList()).any {
            it.status == 2
        }

        initRecyclerView()
    }

    private fun initRecyclerView() {
        adapter.setList(step3Model?.car_photos?.imagesList ?: ArrayList())
    }

}