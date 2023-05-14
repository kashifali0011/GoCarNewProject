package com.towsal.towsal.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.databinding.ImageItemBinding
import com.towsal.towsal.helper.UIHelper

/**
 * Adapter class for car photos
 * */
class ImageAdapter
    (
    val activity: Context,
    private val imagesList: List<String>,
    val uiHelper: UIHelper,
    val callBack: () -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun getItemCount(): Int {
        return imagesList.size * 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ImageItemBinding.bind(
            LayoutInflater.from(activity).inflate(R.layout.image_item, parent, false)
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder

        uiHelper.glideLoadImage(
            activity,
            BuildConfig.IMAGE_BASE_URL + imagesList[position % imagesList.size],
            viewHolder.binding.image
        )

        viewHolder.binding.image.setOnClickListener {
            callBack()
        }
    }

    class ViewHolder(val binding: ImageItemBinding) : RecyclerView.ViewHolder(binding.root)
}