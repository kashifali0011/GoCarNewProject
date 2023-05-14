package com.towsal.towsal.views.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.os.Bundle
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
import com.towsal.towsal.databinding.ItemCarPhotosTripDetailBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.ImagesResponseModel
import com.towsal.towsal.network.serializer.trips.CarPhotosDetail
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.TripsViewModel
import com.towsal.towsal.views.activities.ImageViewPagerActivity
import okhttp3.MultipartBody
import java.io.Serializable

/**
 * Adapter class for pickup and drop off images
 * */
class TripDetailImagesAdapter(
    val context: Activity,
    val uiHelper: UIHelper,
    val tripsViewModel: TripsViewModel,
    val callBack: PopupCallback
) : BaseAdapter() {

    var selectedPosition = 0

    companion object {
        const val Accept = 1
        const val Reject = 0
        const val Pending = -2

    }

    var mArrayList = ArrayList<CarPhotosDetail>()
    var photoType = -1
    var pos = 0
    var isRequestSent = false
    var countRetry = 0
    private var imagesToUpload = ArrayList(emptyList<MultipartBody.Part>())

    /**
     *  Functions for setting up new list
     * */
    fun setList(arrayList: ArrayList<CarPhotosDetail>, photoType: Int) {
        this.photoType = photoType
        this.mArrayList.clear()
        this.mArrayList = arrayList
        notifyDataSetChanged()
    }

    /**
     *  Functions for getting attached list
     * */
    fun getList(): ArrayList<CarPhotosDetail> {
        return mArrayList
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemCarPhotosTripDetailBinding.bind(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_car_photos_trip_detail, parent, false)
        )
        binding.adapter = this
        return ViewHolder(binding)
    }


    override fun getItemCount(): Int =
        mArrayList.size

    /**
     *  Functions for click listeners
     * */
    fun onClick(position: Int, view: View) {
        view.preventDoubleClick()
        when (view.id) {
            R.id.rejectedImage -> {
                var pickup = false
                Log.d("pickksks", "rejected")
                if (photoType == Constants.CarPhotos.HOST_PICKUP_PHOTOS) {
                    pickup = true
                }
                tripsViewModel.popupRejectionImage(context, callBack, position)
                pos = position

            }
            R.id.ibRejectedImage -> {
                tripsViewModel.popupRejectionInfo(context, mArrayList[position].reason)
            }
        }

    }


    @SuppressLint("SetTextI18n", "UseCompatLoadingForDrawables")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position_: Int) {
        val viewHolder: ViewHolder = holder as ViewHolder
        viewHolder.binding.position = viewHolder.adapterPosition
        viewHolder.setIsRecyclable(false)
        viewHolder.binding.clUploadAnother.isVisible =
            position_ == 5 && mArrayList[position_].file_name.isEmpty()
        viewHolder.binding.cvUploadPhoto.isVisible = !viewHolder.binding.clUploadAnother.isVisible
        if (mArrayList[viewHolder.binding.position].file_name.isNotEmpty()) {
            var image = ""
            image =
                if (mArrayList[viewHolder.binding.position].counter_photo.isNotEmpty() && !mArrayList[viewHolder.binding.position].counter_photo.contains(
                        "null"
                    )
                ) {
                    mArrayList[viewHolder.binding.position].counter_photo
                } else {
                    mArrayList[viewHolder.binding.position].file_name
                }
            uiHelper.glideLoadImage(
                context,
                BuildConfig.IMAGE_BASE_URL + image,
                viewHolder.binding.carImage
            )
        } else if (Uri.EMPTY != mArrayList[viewHolder.binding.position].uri) {
            viewHolder.binding.carImage.setImageURI(mArrayList[viewHolder.binding.position].uri)
        }
        viewHolder.binding.imageName.isVisible =
            mArrayList[viewHolder.binding.position].file_name.isEmpty()
        viewHolder.binding.tvUpload.isVisible = viewHolder.binding.imageName.isVisible
        viewHolder.binding.imageName.text = mArrayList[viewHolder.binding.position].name
        viewHolder.binding.rejectedImageName.text = mArrayList[viewHolder.binding.position].name

        viewHolder.binding.clUploadAgain.isVisible = false
        viewHolder.binding.rejectedImage.visibility = View.VISIBLE
        if (photoType == Constants.CarPhotos.HOST_DROP_OFF_ONE_REJECTED_PHOTO) {
            mArrayList[pos].status = 0

        }
        when (mArrayList[viewHolder.binding.position].status) {
            Accept -> {
                viewHolder.binding.clUploadAgain.isVisible = false
                viewHolder.binding.clUploadImage.isVisible = true
                viewHolder.binding.carImage.foreground = null
                viewHolder.binding.rejectedImage.visibility = View.GONE
                viewHolder.binding.ibRejectedImage.visibility = View.GONE
            }
            Reject -> {
                viewHolder.binding.clUploadAgain.isVisible = true
                viewHolder.binding.clUploadImage.isVisible = false
            }
            -1 -> {
                viewHolder.binding.clUploadAgain.isVisible = false
                viewHolder.binding.clUploadImage.isVisible = true
                viewHolder.binding.carImage.foreground = null
                viewHolder.binding.rejectedImage.visibility =
                    View.VISIBLE
                viewHolder.binding.ibRejectedImage.visibility = View.GONE
            }
            -2 -> {
                viewHolder.binding.clUploadAgain.isVisible = false
                viewHolder.binding.clUploadImage.isVisible = true
                viewHolder.binding.carImage.foreground = null
                viewHolder.binding.rejectedImage.visibility =
                    View.VISIBLE          //adding it for place holder status images
                viewHolder.binding.ibRejectedImage.visibility = View.GONE
            }
            else -> {
                viewHolder.binding.clUploadAgain.isVisible = false
                viewHolder.binding.clUploadImage.isVisible = true
                viewHolder.binding.carImage.foreground = null
            }
        }
        if (photoType == Constants.CarPhotos.GUEST_PICKUP_PHOTOS || photoType == Constants.CarPhotos.GUEST_DROP_OFF_PHOTOS ||
            photoType == Constants.CarPhotos.HOST_ACCIDENT_PHOTOS ||
            photoType == Constants.CarPhotos.HOST_REJECT_ALL_IMAGES || photoType == Constants.CarPhotos.HOST_ACCIDENT_PHOTOS ||
            photoType == Constants.CarPhotos.GUEST_ACCIDENT_PHOTOS || photoType == Constants.CarPhotos.FROM_NOTIFICATION
        ) {
            viewHolder.binding.rejectedImage.visibility = View.GONE          //it was gone
        }

        if ((photoType in listOf(
                Constants.CarPhotos.GUEST_PICKUP_PHOTOS,
                Constants.CarPhotos.GUEST_DROP_OFF_PHOTOS,
                Constants.CarPhotos.GUEST_ACCIDENT_PHOTOS,
            ) && mArrayList[position_].status != Accept) || photoType == Constants.CarPhotos.HOST_REJECT_ALL_IMAGES
        ) {
            viewHolder.itemView.setOnClickListener {
                requestForPickingImage(position_)
            }
        }
        if (
            photoType == Constants.CarPhotos.HOST_DROP_OFF_PHOTOS
        ) {
            viewHolder.binding.rejectedImage.visibility = View.VISIBLE          //it was gone
            //  viewHolder.binding.statusText.text = context.getString(R.string.rejected)
        }

        viewHolder.binding.executePendingBindings()

    }

    private fun requestForPickingImage(position: Int) {
        when (photoType) {
            Constants.CarPhotos.HOST_PICKUP_PHOTOS -> {
                //making large view at pickups
                Log.d("pickksks", "hostPickup")
                val bundle = Bundle()
                bundle.putSerializable(
                    Constants.DataParsing.MODEL,
                    mArrayList as Serializable
                )
                bundle.putString(Constants.DataParsing.HEADING, "At pick-up")
                bundle.putInt(Constants.DataParsing.POSITION, position)
                bundle.putString(Constants.DataParsing.IS_PICKUP, "pickup")
                bundle.putInt("photoType", Constants.CarPhotos.HOST_PICKUP_PHOTOS)

                uiHelper.openActivityForResult(
                    context,
                    ImageViewPagerActivity::class.java,
                    true,
                    Constants.RequestCodes.PICKUP_REJECTION_IMAGES, bundle
                )

            }
            Constants.CarPhotos.HOST_ACCIDENT_PHOTOS -> {
            }
            Constants.CarPhotos.FROM_NOTIFICATION -> {
            }

            Constants.CarPhotos.HOST_DROP_OFF_PHOTOS -> {
                Log.d("pickksks", "hostDropOff")
                val bundle = Bundle()
                bundle.putSerializable(
                    Constants.DataParsing.MODEL,
                    mArrayList as Serializable
                )
                bundle.putInt(Constants.DataParsing.POSITION, position)
                bundle.putString(Constants.DataParsing.HEADING, "At Dropoff")

                uiHelper.openActivityForResult(
                    context,
                    ImageViewPagerActivity::class.java,
                    true,
                    Constants.RequestCodes.ACTIVITY_IMAGES, bundle
                )
            }
            else -> {
                Log.d("pickksks", "comingInElse")
                ImagePicker.with(context)
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
                                if (fileUri != null) {
                                    imagesToUpload = uiHelper.multiPart(
                                        fileUri,
                                        "files"
                                    )
                                    selectedPosition = position
                                    isRequestSent = true
                                    countRetry = 0
                                    tripsViewModel.uploadImage(
                                        true,
                                        Constants.API.UPLOAD_PHOTO,
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
                            else -> {

                            }
                        }
                    }
            }
        }

    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        super.onSuccess(response, tag)
        when (tag) {
            Constants.API.UPLOAD_PHOTO -> {
                val imagesResponseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    ImagesResponseModel::class.java
                )
                isRequestSent = false
                countRetry = 0
                mArrayList[selectedPosition].index = selectedPosition
                mArrayList[selectedPosition].status = -1
                mArrayList[selectedPosition].file_name = imagesResponseModel.imagesList[0]
                notifyDataSetChanged()
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        super.onFailure(response, tag)
        if (countRetry < 3) {
            tripsViewModel.uploadImage(
                true,
                Constants.API.UPLOAD_PHOTO,
                this,
                imagesToUpload
            )
            countRetry++
        } else {
            uiHelper.showLongToastInCenter(
                context,
                "Server not responding or may be your internet is down"
            )
        }
    }

    class ViewHolder(var binding: ItemCarPhotosTripDetailBinding) :
        RecyclerView.ViewHolder(binding.root)
}
