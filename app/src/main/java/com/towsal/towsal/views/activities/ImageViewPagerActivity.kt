package com.towsal.towsal.views.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.View
import android.view.animation.RotateAnimation
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.jsibbold.zoomage.ZoomageView
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityImageViewPagerBinding
import com.towsal.towsal.helper.SetActionBar
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.interfaces.RecyclerViewItemClick
import com.towsal.towsal.network.ApiInterface
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.trips.AcceptRejectImageModel
import com.towsal.towsal.network.serializer.trips.CarPhotosDetail
import com.towsal.towsal.network.serializer.trips.CounterImageModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.TripsViewModel
import com.towsal.towsal.views.adapters.ImagesViewPagerAdapter
import com.towsal.towsal.views.adapters.TripDetailImagesAdapter
import kotlinx.android.synthetic.main.activity_add_trip_detail_images.*
import okhttp3.MultipartBody
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for trip images
 * */
class ImageViewPagerActivity : BaseActivity(), OnNetworkResponse, PopupCallback {

    var viewpagerAdapter: ImagesViewPagerAdapter? = null
    private lateinit var dots: Array<TextView?>
    val tripsViewModel: TripsViewModel by viewModel()
    var acceptRejectImageModel = AcceptRejectImageModel()
    var photoType = -1

    companion object {
        var callback: RecyclerViewItemClick? = null
    }

    var mCurrRotation = 0f

    var images = ArrayList<CarPhotosDetail>()
    var position = 0
    var pickup = "dropoff"
    var heading = ""
    lateinit var binding: ActivityImageViewPagerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_view_pager)
        binding.activity = this
        images =
            intent.extras?.getSerializable(Constants.DataParsing.MODEL) as ArrayList<CarPhotosDetail>
        if (intent.extras?.getInt(Constants.DataParsing.POSITION) != null)
            position = intent.extras?.getInt(Constants.DataParsing.POSITION, 0)!!
        if (intent.extras?.getString(Constants.DataParsing.HEADING) != null)
            heading = intent.extras?.getString(Constants.DataParsing.HEADING, "")!!
        if (intent.extras?.getString(Constants.DataParsing.IS_PICKUP) != null)
            pickup = intent.extras?.getString(Constants.DataParsing.IS_PICKUP, "")!!
        if (intent.extras?.getInt("photoType") != null)
            photoType = intent.extras?.getInt("photoType", 0)!!

        actionBarSetting()
        setData()
    }

    /**
     * Function for setting up action bar
     * */
    private fun actionBarSetting() {
        setActionBar = SetActionBar(supportActionBar, this)
        setActionBar?.setActionBarHeaderText(heading)
        setActionBar?.setBackButton()
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        acceptRejectImageModel.status.clear()
        acceptRejectImageModel.images.clear()
        acceptRejectImageModel.reason.clear()
        acceptRejectImageModel.cardId = images[0].car_id
        acceptRejectImageModel.car_checkout_id = images[0].car_checkout_id
        dots = arrayOfNulls(images.size - 1)
        viewpagerAdapter = ImagesViewPagerAdapter(this, images, uiHelper)
        binding.imagesViewPager.adapter = viewpagerAdapter
        binding.imagesViewPager.addOnPageChangeListener(viewPagerPageChangeListener)
        addBottomDots(0)
        binding.imagesViewPager.currentItem = position
    }

    private var viewPagerPageChangeListener: ViewPager.OnPageChangeListener = object :
        ViewPager.OnPageChangeListener {

        override fun onPageSelected(position: Int) {
            addBottomDots(position)
        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {}
        override fun onPageScrollStateChanged(arg0: Int) {}
    }

    /**
     * Function for adding dot views
     * */
    private fun addBottomDots(currentPage: Int) {
        val colorsActive = resources.getIntArray(R.array.array_dot_active)
        val colorsInactive = resources.getIntArray(R.array.array_dot_inactive)
        binding.layoutDots.removeAllViews()
        try {
            for (i in dots.indices) {
                dots[i] = TextView(this)
                dots[i]?.text = Html.fromHtml("&#8226;")
                dots[i]?.textSize = 60f
                dots[i]?.setTextColor(colorsInactive[currentPage])
                binding.layoutDots.addView(dots[i])
            }
            if (dots.isNotEmpty()) {
                dots[currentPage]?.setTextColor(colorsActive[currentPage])
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.rotate -> {
                val adapterView =
                    binding.imagesViewPager.findViewWithTag("imageView" + binding.imagesViewPager.currentItem) as View
                val imageView = adapterView.findViewById<View>(R.id.imageViewMain) as ZoomageView
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
//                if (callback != null)
//                    callback?.onItemClick(binding.imagesViewPager.currentItem)
            }
            R.id.rejectImage -> {
                if (pickup == "dropoff") {
                    tripsViewModel.popupRejectionImage(
                        this,
                        this,
                        binding.imagesViewPager.currentItem
                    )
                } else if (pickup == "pickup") {
                    tripsViewModel.popupRejectionImage(
                        this,
                        this,
                        binding.imagesViewPager.currentItem
                    )
                }

            }
        }
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.ACCEPT_REJECT_DROP_OFF_IMAGES -> {
                val intent = Intent()
                val bundle = Bundle()
                intent.putExtra(Constants.DataParsing.MODEL, bundle)
                setResult(RESULT_OK, intent)
                uiHelper.showLongToastInCenter(this, response?.message)
                finish()
            }
            Constants.API.ACCEPT_REJECT_PICKUP_IMAGES -> {
                val intent = Intent()
                val bundle = Bundle()
                intent.putExtra(Constants.DataParsing.MODEL, bundle)
                setResult(RESULT_OK, intent)
                uiHelper.showLongToastInCenter(this, response?.message)
                finish()
            }
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }

    override fun popupButtonClick(value: Int) {

    }

    override fun popupButtonClick(value: Int, text_id_model: Any) {
        //making large view at pickup images too
        if (text_id_model is CounterImageModel) {
            acceptRejectImageModel.reason.clear()
            val model = text_id_model
            val indexList = ArrayList<Int>()
            val listUri = ArrayList<Uri>()
            listUri.add(model.uri)
            indexList.add(model.position)
            var listMultiPart: java.util.ArrayList<MultipartBody.Part> =
                uiHelper.multiPartArrayList(
                    listUri,
                    ApiInterface.Companion.ParamNames.COUNTER_PHOTO
                )
            if (!indexList.isNullOrEmpty()) {
                listMultiPart.clear()
                listMultiPart = uiHelper.multiPartArrayList(
                    listUri,
                    ApiInterface.Companion.ParamNames.COUNTER_PHOTO, indexList
                )
            }
            acceptRejectImageModel.status.add(TripDetailImagesAdapter.Reject)
            acceptRejectImageModel.images = listMultiPart
            acceptRejectImageModel.reason.add(model.reason)
            acceptRejectImageModel.position = value
            acceptRejectImageModel.accept_reject_flag = AddTripDetailImages.ACCEPT_REJECT_1_BY_1
            tripsViewModel.acceptRejectDropOffImages(
                acceptRejectImageModel, "",
                true,
                Constants.API.ACCEPT_REJECT_DROP_OFF_IMAGES,
                this
            )
        } else {
            if (value == -1) {
                Log.d("submitButton", "comingInValue2 in 1")
                for (i in acceptRejectImageModel.status.indices) {
                    acceptRejectImageModel.status[i] = TripDetailImagesAdapter.Reject
                    acceptRejectImageModel.reason[i] = text_id_model.toString()
                }
            } else {
                Log.d("submitButton", "comingInValue2 in 2")
                for (i in acceptRejectImageModel.status.indices) {
                    if (acceptRejectImageModel.status[i] != TripDetailImagesAdapter.Reject
                        && acceptRejectImageModel.status[i] != TripDetailImagesAdapter.Accept
                    ) {
                        acceptRejectImageModel.status[i] = TripDetailImagesAdapter.Accept
                    }
                }

                if (value == 5) {
                    Log.d("submitButton", "comingInValue came here in 5 true lower")
                    acceptRejectImageModel.odometer = odometerEdt.text.toString()
                    Log.d(
                        "submitButton",
                        "comingInValue came here in 5 true lower valuee =" + acceptRejectImageModel.odometer
                    )
                    acceptRejectImageModel.status[value] = TripDetailImagesAdapter.Reject
                    acceptRejectImageModel.reason[value] = text_id_model.toString()
                    acceptRejectImageModel.accept_reject_flag =
                        AddTripDetailImages.ACCEPT_REJECT_1_BY_1
                    acceptRejectImageModel.position = value

//                    tripsViewModel.acceptRejectImagesWithOdMeter(
//                        acceptRejectImageModel,
//                        acceptRejectImageModel.odometer,
//                        true,
//                        Constants.API.ACCEPT_REJECT_PICKUP_IMAGES,
//                        this
//                    )
                } else {

                    Log.d("submitButton", "comingInValue came here in 5 else part")
                    acceptRejectImageModel.reason.add("")
                    acceptRejectImageModel.reason.add("")
                    acceptRejectImageModel.reason.add("")
                    acceptRejectImageModel.reason.add("")
                    acceptRejectImageModel.reason.add("")
                    acceptRejectImageModel.reason.add("")
                    acceptRejectImageModel.status.add(1)
                    acceptRejectImageModel.status.add(1)
                    acceptRejectImageModel.status.add(1)
                    acceptRejectImageModel.status.add(1)
                    acceptRejectImageModel.status.add(1)
                    acceptRejectImageModel.status.add(1)


                    acceptRejectImageModel.status[value] = TripDetailImagesAdapter.Reject
                    acceptRejectImageModel.reason[value] = text_id_model.toString()
                    acceptRejectImageModel.accept_reject_flag =
                        AddTripDetailImages.ACCEPT_REJECT_1_BY_1
                    acceptRejectImageModel.position = value

                    tripsViewModel.acceptRejectImages(
                        acceptRejectImageModel,
                        true,
                        Constants.API.ACCEPT_REJECT_PICKUP_IMAGES,
                        this
                    )
                }
            }
        }
        if (photoType == Constants.CarPhotos.HOST_PICKUP_PHOTOS) {
            Log.d("submitButton", "comingInValue2 in 3")

            tripsViewModel.acceptRejectImages(
                acceptRejectImageModel,
                true,
                Constants.API.ACCEPT_REJECT_PICKUP_IMAGES,
                this
            )
        }

    }

}