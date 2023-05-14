package com.towsal.towsal.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import com.jsibbold.zoomage.ZoomageView
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.interfaces.RecyclerViewItemClick
import com.towsal.towsal.network.serializer.trips.CarPhotosDetail
import com.towsal.towsal.views.activities.ImageViewPagerActivity
import java.util.*

/**
 * Adapter class for car photos
 * */
class ImagesViewPagerAdapter(// Context object
    var context: Context, // Array of images
    var images: ArrayList<CarPhotosDetail>,
    var uiHelper: UIHelper
) : PagerAdapter(), RecyclerViewItemClick {
    var mCurrRotation = 0f

    // Layout Inflater
    var mLayoutInflater: LayoutInflater
    lateinit var imageView: ImageView
    override fun getCount(): Int {
        // return the number of images
        return images.size - 1
    }

    override fun isViewFromObject(
        view: View,
        `object`: Any
    ): Boolean {
        return view === `object` as LinearLayout
    }

    override fun instantiateItem(
        container: ViewGroup,
        position: Int
    ): Any {
        // inflating the item.xml
        val itemView = mLayoutInflater.inflate(
            R.layout.item_image_viewpager,
            container,
            false
        )

        // referencing the image view from the item.xml file
        imageView = itemView.findViewById<View>(R.id.imageViewMain) as ZoomageView

        // setting the image in the imageView
        var image = ""
        image =
            if (!images[position].counter_photo.isNullOrEmpty() && !images[position].counter_photo.contains(
                    "null")
            ) {
                images[position].counter_photo
            } else {
                images[position].file_name
            }
        uiHelper.glideLoadImage(
            context,
            BuildConfig.IMAGE_BASE_URL + image,
            imageView
        )
//        imageView.setImageResource(images[position].file_name)
        itemView.tag = "imageView$position"
        // Adding the View
        Objects.requireNonNull(container).addView(itemView)
        return itemView
    }

    override fun destroyItem(
        container: ViewGroup,
        position: Int,
        `object`: Any
    ) {
        container.removeView(`object` as LinearLayout)
    }

    // Viewpager Constructor
    init {
        mLayoutInflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        ImageViewPagerActivity.callback = this
    }

    override fun getItemPosition(object_: Any): Int {
        return POSITION_NONE
    }

    override fun onItemClick(position: Int) {
        mCurrRotation %= 360
        val fromRotation: Float = mCurrRotation
        val toRotation: Float = 90.let { mCurrRotation += it; mCurrRotation }

        val rotateAnim = RotateAnimation(
            fromRotation,
            toRotation,
            (imageView.width / 2).toFloat(),
            (imageView.height / 2).toFloat()
        )

        rotateAnim.duration = 0 // Use 0 ms to rotate instantly

        rotateAnim.fillAfter = true // Must be true or the animation will reset


        imageView.startAnimation(rotateAnim)
        notifyDataSetChanged()
//        imageView.rotation = imageView.rotation + 90
    }

    fun getCurrentView(): ImageView {
        return imageView
    }
}