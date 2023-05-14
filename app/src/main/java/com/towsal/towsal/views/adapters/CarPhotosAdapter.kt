package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemCarPhotosBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.ImagesResponseModel
import com.towsal.towsal.network.serializer.carlist.step3.CarPhotoListModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.TripsViewModel
import okhttp3.MultipartBody

/**
 * Adapter class for car photos
 * */
class CarPhotosAdapter(
    var context: Activity,
    val uiHelper: UIHelper,
    val tripsViewModel: TripsViewModel,
) : BaseAdapter() {

    var disableViews = false
    var positionToChange = 0
    var count = 0
    var imagesToUpload = ArrayList(emptyList<MultipartBody.Part>())
    private fun setDisableView(b: Boolean) {
        disableViews = b
        notifyDataSetChanged()
    }

    var mArrayList = ArrayList<CarPhotoListModel>()

    /**
     *  Functions for setting up new list
     * */
    fun setList(arrayList: ArrayList<CarPhotoListModel>) {
        this.mArrayList = arrayList
        notifyDataSetChanged()
    }

    /**
     *  Functions for getting attached list
     * */
    fun getList(): ArrayList<CarPhotoListModel> {
        return mArrayList
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val binding = ItemCarPhotosBinding.bind(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_car_photos, parent, false
            )
        )
        return ViewHolder(binding)
    }


    override fun getItemCount(): Int {
        return mArrayList.size
    }

    /**
     *  Functions for click listeners
     * */
    fun onClick(position: Int, view: View) {
        view.preventDoubleClick()
        when (view.id) {
            R.id.rejectInfoOdometer -> {
                tripsViewModel.popupRejectionInfo(context, mArrayList[position].reason)
            }
        }

    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position_: Int) {
        val viewHolder: ViewHolder = holder as ViewHolder
        viewHolder.setIsRecyclable(false)
        if ((mArrayList[position_].url ?: "").isNotEmpty()) {
            uiHelper.glideLoadImage(
                context,
                BuildConfig.IMAGE_BASE_URL + mArrayList[position_].url,
                viewHolder.binding.carImage
            )
        } else {
            viewHolder.binding.carImage.setImageURI(mArrayList[position_].uri)
        }
        viewHolder.binding.rejectInfoOdometer.isVisible = false
        if (mArrayList[position_].status != -1) {
            if (mArrayList[position_].status == 2) {
                viewHolder.binding.rejectInfoOdometer.isVisible = true
            }

            viewHolder.binding.statusText.isVisible = true
            viewHolder.binding.statusText.text = getStatus(mArrayList[position_].status)
        }
        viewHolder.binding.carImage.setOnClickListener {
            if (disableViews) {
                //---------Do Noting--------
            } else {
                if (mArrayList[position_].status != 1) {
                    ImagePicker.with(context)
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
                                        positionToChange = position_
                                        imagesToUpload =
                                            uiHelper.multiPart(
                                                fileUri,
                                                "files"
                                            )
                                        count = 0
                                        tripsViewModel.uploadImage(
                                            showLoader = true,
                                            tag = Constants.API.UPLOAD_PHOTO,
                                            callback = this@CarPhotosAdapter,
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
                        }
                }
            }
        }
        viewHolder.binding.rejectInfoOdometer.setOnClickListener { onClick(position_, it) }
    }

    /**
     *  Functions for getting status
     * */
    private fun getStatus(status: Int): String {
        return when (status) {
            Constants.ImagesStatus.PENDING -> {
                "Pending"
            }
            Constants.ImagesStatus.APPROVED -> {
                "Accepted"
            }
            else -> {
                "REJECTED"
            }
        }

    }

    class ViewHolder(var binding: ItemCarPhotosBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        super.onSuccess(response, tag)

        when (tag) {
            Constants.API.UPLOAD_PHOTO -> {
                val imagesResponseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    ImagesResponseModel::class.java
                )
                mArrayList[positionToChange].url = imagesResponseModel.imagesList[0]
                mArrayList[positionToChange].status = -1
                Log.e("onSuccess: ", Gson().toJson(mArrayList))
                setList(mArrayList)
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        super.onFailure(response, tag)

        when (tag) {
            Constants.API.UPLOAD_PHOTO -> {
                if (count < 3) {
                    tripsViewModel.uploadImage(
                        showLoader = true,
                        tag = Constants.API.UPLOAD_PHOTO,
                        callback = this@CarPhotosAdapter,
                        arrayList = imagesToUpload
                    )
                    count++
                } else {
                    uiHelper.showLongToastInCenter(context, response?.message ?: "")
                    setDisableView(true)
                }
            }
        }
    }

}
