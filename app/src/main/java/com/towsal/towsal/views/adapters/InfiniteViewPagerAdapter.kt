package com.towsal.towsal.views.adapters

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.asksira.loopingviewpager.LoopingPagerAdapter
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.views.activities.FullScreenImageActivity
/**
 * Adapter class for car photos
 * */
class InfiniteViewPagerAdapter(
    private val photosList: ArrayList<String>,
    isInfinite: Boolean,
    var context: Activity,
    val uiHelper: UIHelper,
    val blockClick: Boolean
) : LoopingPagerAdapter<String>(photosList, isInfinite) {


    override fun getItemViewType(listPosition: Int): Int {
        return listPosition
    }

    override fun bindView(
        convertView: View,
        listPosition: Int,
        viewType: Int
    ) {
        val imageView = convertView.findViewById<ImageView>(R.id.image)

        if (!blockClick) {
            imageView.setOnClickListener {
                it!!.preventDoubleClick()
                val bundle = Bundle()
                bundle.putSerializable(Constants.DataParsing.IMAGES, photosList)
                bundle.putInt("position", listPosition)
                uiHelper.openActivity(context, FullScreenImageActivity::class.java, bundle)
            }
        }
        uiHelper.glideLoadImage(
            context,
            BuildConfig.IMAGE_BASE_URL + photosList[listPosition],
            imageView
        )

        convertView.findViewById<TextView>(R.id.tvCount).text =
            (listPosition + 1).toString() + " of " + photosList.size
    }

    override fun inflateView(
        viewType: Int,
        container: ViewGroup,
        listPosition: Int
    ): View {
        return LayoutInflater.from(container.context)
            .inflate(R.layout.item_photo_slide_stack, container, false)
    }

}
