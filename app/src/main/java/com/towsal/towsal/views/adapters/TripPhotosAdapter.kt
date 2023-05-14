package com.towsal.towsal.views.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ItemTripPhotosBinding
import com.towsal.towsal.extensions.loadImage
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.carlist.step3.CarPhotoListModel
import com.towsal.towsal.utils.Constants

class TripPhotosAdapter(
    val uiHelper: UIHelper,
    private val isClickAble: Boolean = true,
    private val fileType: Int = Constants.FileTypes.IMAGES,
    val callBackSelectImage: (position: Int, binding: ItemTripPhotosBinding, view: View) -> Unit = { _, _, _ -> },
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var mArrayList = ArrayList<CarPhotoListModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemTripPhotosBinding.bind(
            LayoutInflater.from(parent.context).inflate(R.layout.item_trip_photos, parent, false)
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        val binding = viewHolder.binding
        binding.adapter = this
        binding.holder = viewHolder
        binding.position = position
        binding.cvTripPhoto.isEnabled = isClickAble

        // visibilities
        binding.ivTripPhoto.isVisible =
            mArrayList[position].url.isNotEmpty() && fileType == Constants.FileTypes.IMAGES
        binding.ivTripPdf.isVisible =
            mArrayList[position].url.isNotEmpty() && fileType == Constants.FileTypes.PDF
        binding.ivRemoveImage.isVisible = mArrayList[position].url.isNotEmpty()
        binding.clUploadTripPhoto.isVisible =
            mArrayList[position].url.isEmpty() && fileType == Constants.FileTypes.IMAGES
        binding.clUploadTripPdf.isVisible =
            mArrayList[position].url.isEmpty() && fileType == Constants.FileTypes.PDF
        binding.tvFileName.isVisible = binding.ivTripPdf.isVisible || binding.clUploadTripPdf.isVisible

        //        setting up card view background color
        if (fileType == Constants.FileTypes.PDF) {
            if (mArrayList[position].url.isEmpty()) {
                binding.cvTripPhoto.setCardBackgroundColor(binding.cvTripPhoto.context.getColor(R.color.seprator_color))
                binding.tvFileName.text = binding.tvFileName.context.getString(R.string.upload_new_pdf)
            }
            else {
                binding.cvTripPhoto.setCardBackgroundColor(binding.cvTripPhoto.context.getColor(R.color.black_text_black_variation_18))
                binding.tvFileName.text = mArrayList[position].url.toUri().lastPathSegment
            }
        }


        if (mArrayList[position].url.isNotEmpty())
            if (fileType == Constants.FileTypes.IMAGES)
                binding.ivTripPhoto.loadImage(
                    BuildConfig.IMAGE_BASE_URL + mArrayList[position].url
                )
            else {
                binding.ivTripPdf.setImageResource(R.drawable.ic_pdf_file)
            }
    }

    override fun getItemCount() = mArrayList.size


    fun onClick(position: Int, view: View, holder: ViewHolder) {
        view.preventDoubleClick()
        when (view) {
            holder.binding.cvTripPhoto -> {
                if (isClickAble && mArrayList[position].url.isEmpty())
                    callBackSelectImage(position, holder.binding, view)
            }
            holder.binding.ivRemoveImage -> {
                callBackSelectImage(position, holder.binding, view)
            }
        }
    }

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


    class ViewHolder(var binding: ItemTripPhotosBinding) :
        RecyclerView.ViewHolder(binding.root)

}