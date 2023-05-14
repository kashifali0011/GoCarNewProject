package com.towsal.towsal.views.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityAddTripDetailImagesBinding
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.trips.*
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.TripsViewModel
import com.towsal.towsal.views.adapters.TripDetailImagesAdapter
import okhttp3.MultipartBody
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DecimalFormat

/**
 * Activity class for adding, accepting and rejecting the trip images
 * */
class AddTripDetailImages : BaseActivity(), OnNetworkResponse, PopupCallback {

    val tripsViewModel: TripsViewModel by viewModel()
    lateinit var binding: ActivityAddTripDetailImagesBinding
    var mArrayList = ArrayList<CarPhotosDetail>()
    val nameList = ArrayList<String>()
    var photoType = 0
    val indexList = ArrayList<Int>()
    var id = 0
    var car_id = 0
    var pickUpImageStatus = 0
    var dropOffImageStatus = 0
    lateinit var adapter: TripDetailImagesAdapter
    var model = CarPhotosResponseModel()
    var fromNotification = false
    var allRejectClicked = false
    val uriList = ArrayList<Uri>()              //adding placeholders images

    var dropoffDate = ""        //date for alarm
    var isPickup = false
    var odometerReading = ""

    companion object {
        val ACCEPTALL = 1
        val REJECTALL = 2
        val ACCEPT_REJECT_1_BY_1 = 0

    }

    var acceptRejectImageModel = AcceptRejectImageModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_trip_detail_images)
        binding.activity = this
        setData()
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        setPhotosNames()
        Log.e("ImageBaseUrl", BuildConfig.IMAGE_BASE_URL)
        val bundle = intent.extras
        if (bundle != null) {
            id = bundle.getInt(Constants.DataParsing.ID, 0)
            car_id = bundle.getInt(Constants.DataParsing.CAR_ID, 0)
            photoType = bundle.getInt(Constants.DataParsing.GOTO_PHOTOS, 0)
            val heading = bundle.getString(Constants.DataParsing.HEADING, "")
            binding.layoutToolBar.setAsGuestToolBar(
                heading,
                supportActionBar
            )
            fromNotification = bundle.getBoolean(Constants.DataParsing.FROM_NOTIFICATION, false)
            pickUpImageStatus = bundle.getInt(Constants.DataParsing.CAR_IMAGE_STATUS_PICKUP, 0)
            dropOffImageStatus =
                bundle.getInt(Constants.DataParsing.CAR_IMAGE_STATUS_DROP_OFF, 0)
            adapter = TripDetailImagesAdapter(this, uiHelper, tripsViewModel, this)
            binding.photosRecyclerView.layoutManager = GridLayoutManager(this, 2)
            binding.photosRecyclerView.adapter = adapter
            adapter.setList(mArrayList, photoType)
            setPhotoTypeData(photoType)
            acceptRejectImageModel.cardId = car_id
            acceptRejectImageModel.car_checkout_id = id

            if (intent.extras!!.getString(Constants.DataParsing.DROP_OFF_DATE_TIME) != null) {
                dropoffDate = bundle.getString(Constants.DataParsing.DROP_OFF_DATE_TIME, "")
            }
        }

        binding.odometerEdt.addTextChangedListener {
            if (it.toString().isNotEmpty()) {
                if (odometerReading != binding.odometerEdt.text.toString()) {
                    val result =
                        DecimalFormat("###,###").format(it.toString().replace(",", "").toDouble())
                    odometerReading = result
                    binding.odometerEdt.setText(result)
                    binding.odometerEdt.setSelection(result.length)
                }
            } else {
                odometerReading = it.toString()
            }
        }
    }

    /**
     * Function for setting up photos according to types of photos
     * */
    private fun setPhotoTypeData(photoType: Int) {
        when (photoType) {
            Constants.CarPhotos.GUEST_ACCIDENT_PHOTOS -> {
                binding.odometerEdt.visibility = View.GONE
                binding.odometerLabel.text = getString(R.string.upload_photos)
                binding.btnSubmit.visibility = View.VISIBLE
                binding.mainLayout.visibility = View.VISIBLE
                binding.rejectOdometerReading.visibility = View.GONE
            }
            Constants.CarPhotos.HOST_ACCIDENT_PHOTOS -> {
                binding.odometerLabel.visibility = View.GONE
                binding.odometerEdt.visibility = View.GONE
                binding.rejectOdometerReading.visibility = View.GONE

                binding.btnSubmit.visibility = View.GONE
                tripsViewModel.viewAccidentImages(
                    car_id,
                    id,
                    true,
                    Constants.API.VIEW_ACCIDENT_IMAGES,
                    this
                )

            }
            Constants.CarPhotos.GUEST_PICKUP_PHOTOS -> {
                Log.d("carPhotos", "GuestPickUpImages")
                binding.rejectOdometerReading.visibility = View.GONE
                if (pickUpImageStatus == Constants.ImagesStatus.REJECTED) {
                    tripsViewModel.getPickupImages(
                        car_id,
                        id,
                        true,
                        Constants.API.VIEW_PICKUP_IMAGES,
                        this
                    )
                } else {
                    binding.btnSubmit.visibility = View.VISIBLE
                    binding.mainLayout.visibility = View.VISIBLE
                }
            }
            Constants.CarPhotos.GUEST_DROP_OFF_PHOTOS -> {
                binding.btnSubmit.visibility = View.VISIBLE
                binding.mainLayout.visibility = View.VISIBLE
                binding.rejectOdometerReading.visibility = View.GONE
            }
            Constants.CarPhotos.HOST_PICKUP_PHOTOS -> {
                binding.acceptRejectBtnLL.visibility = View.VISIBLE
                binding.btnSubmit.isVisible = false
                isPickup = true
                tripsViewModel.getPickupImages(
                    car_id,
                    id,
                    true,
                    Constants.API.VIEW_PICKUP_IMAGES,
                    this
                )
            }
            Constants.CarPhotos.HOST_DROP_OFF_PHOTOS -> {
                binding.acceptRejectBtnLL.visibility = View.VISIBLE
                binding.btnSubmit.isVisible = false
                tripsViewModel.getDropOffImages(
                    car_id,
                    id,
                    true,
                    Constants.API.VIEW_DROP_OFF_IMAGES,
                    this
                )
            }
            Constants.CarPhotos.HOST_REJECT_ALL_IMAGES -> {
                binding.btnSubmit.visibility = View.VISIBLE
                binding.rejectionLabel.visibility = View.VISIBLE
                binding.rejectReasonEdt.visibility = View.VISIBLE
                binding.rejectOdometerReading.visibility = View.GONE
                binding.mainLayout.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Function for setting up photos names
     * */
    private fun setPhotosNames() {
        Log.d("carPhotos", "setNames")
        nameList.clear()
        nameList.add(getString(R.string.front))
        nameList.add(getString(R.string.back))
        nameList.add(getString(R.string.right))
        nameList.add(getString(R.string.left))
        nameList.add(getString(R.string.odometer))
        nameList.add("")

        //adding place holder images
        uriList.add(Uri.parse("android.resource://" + "com.towsal.towsal" + "/"))
        uriList.add(Uri.parse("android.resource://" + "com.towsal.towsal" + "/"))
        uriList.add(Uri.parse("android.resource://" + "com.towsal.towsal" + "/"))
        uriList.add(Uri.parse("android.resource://" + "com.towsal.towsal" + "/"))
        uriList.add(Uri.parse("android.resource://" + "com.towsal.towsal" + "/"))

        for (i in 0..5) {
            val model = CarPhotosDetail()
            model.name = nameList[i]
            if (i <= uriList.size - 1)
                model.uri = uriList[i]          //adding place holder images
            mArrayList.add(model)
        }
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view.id) {
            R.id.btnSubmit -> {
                var check = true
                when (photoType) {
                    Constants.CarPhotos.GUEST_PICKUP_PHOTOS -> {
                        if (binding.odometerEdt.text.toString()
                                .isEmpty()
                        ) {
                            uiHelper.showLongToastInCenter(
                                this,
                                getString(R.string.enter_odometer_reading_err_msg)
                            )
                        } else {
                            for (i in 0 until adapter.getList().size) {
                                if (pickUpImageStatus == Constants.ImagesStatus.PENDING) {
                                    if (adapter.getList()[i].file_name.isEmpty() && adapter.getList()[i].name.isNotEmpty()) {
                                        uiHelper.showLongToastInCenter(
                                            this,
                                            getString(R.string.select) + " " + adapter.getList()[i].name + " " + getString(
                                                R.string.image
                                            )
                                        )
                                        check = false
                                        break
                                    }
                                } else if (pickUpImageStatus == Constants.ImagesStatus.REJECTED) {
                                    if (adapter.getList()[i].status in listOf(
                                            0
                                        ) && adapter.getList()[i].index == -1
                                    ) {
                                        uiHelper.showLongToastInCenter(
                                            this,
                                            getString(R.string.select) + " " + adapter.getList()[i].name + " " + getString(
                                                R.string.image
                                            )
                                        )
                                        check = false
                                        break
                                    }
                                }
                            }
                            if (check) {
                                tripsViewModel.uploadPickupImages(
                                    adapter.getList(),
                                    binding.odometerEdt.text.toString().replace(",", ""),
                                    id,
                                    car_id,
                                    true,
                                    Constants.API.ADD_PICKUP_IMAGES,
                                    (model.imageReading
                                        ?: OdoMeterReadingResponseModel()).status != TripDetailImagesAdapter.Accept,
                                    this
                                )
                            }
                        }
                    }
                    Constants.CarPhotos.GUEST_ACCIDENT_PHOTOS -> {
                        for (i in 0 until adapter.getList().size) {
                            if (adapter.getList()[i].index == -1 && adapter.getList()[i].name.isNotEmpty()) {
                                uiHelper.showLongToastInCenter(
                                    this,
                                    getString(R.string.select) + " " + adapter.getList()[i].name + " " + getString(
                                        R.string.image
                                    )
                                )
                                check = false
                                break
                            }
                        }

                        if (check)
                            tripsViewModel.addAccidentImages(
                                adapter.getList(), id, car_id,
                                true,
                                Constants.API.ADD_ACCIDENT_PHOTOS,
                                this
                            )
                    }
                    Constants.CarPhotos.GUEST_DROP_OFF_PHOTOS -> {
                        if (binding.odometerEdt.text.toString().isEmpty()) {
                            uiHelper.showLongToastInCenter(
                                this,
                                getString(R.string.enter_odometer_reading_err_msg)
                            )
                        } else {
                            for (i in 0 until adapter.getList().size) {
                                if (adapter.getList()[i].index == -1 && adapter.getList()[i].name.isNotEmpty()) {
                                    uiHelper.showLongToastInCenter(
                                        this,
                                        getString(R.string.select) + " " + adapter.getList()[i].name + " " + getString(
                                            R.string.image
                                        )
                                    )
                                    check = false
                                    break
                                }
                            }
                            if (check) {
                                tripsViewModel.uploadDropOffImages(
                                    adapter.getList(),
                                    binding.odometerEdt.text.toString().replace(",", ""),
                                    id,
                                    car_id,
                                    true,
                                    Constants.API.ADD_PICKUP_IMAGES,
                                    this
                                )

                            }
                        }
                    }
                    Constants.CarPhotos.HOST_REJECT_ALL_IMAGES -> {
                        if (binding.rejectReasonEdt.text.toString().isEmpty()) {
                            uiHelper.showLongToastInCenter(
                                this,
                                getString(R.string.enter_reason_for_rejection)
                            )
                        } else if (binding.odometerEdt.text.toString().isEmpty()) {
                            uiHelper.showLongToastInCenter(
                                this,
                                getString(R.string.enter_odometer_reading_err_msg)
                            )
                        } else {
                            for (i in 0 until adapter.getList().size) {
                                if (adapter.getList()[i].file_name.isEmpty()) {
                                    uiHelper.showLongToastInCenter(
                                        this,
                                        getString(R.string.select) + " " + adapter.getList()[i].name + " " + getString(
                                            R.string.image
                                        )
                                    )
                                    check = false
                                    break
                                } else if (adapter.getList()[i].status == -1)            //adding place holders for dropoff reject images
                                {
                                    uiHelper.showLongToastInCenter(
                                        this,
                                        getString(R.string.select) + " " + adapter.getList()[i].name + " " + getString(
                                            R.string.image
                                        )
                                    )
                                    check = false
                                    break
                                }
                            }
                            if (check) {
                                val listMultiPart: ArrayList<MultipartBody.Part> = ArrayList()
                                acceptRejectImageModel.images.clear()
                                acceptRejectImageModel.images = listMultiPart
                                acceptRejectImageModel.reason.clear()
                                acceptRejectImageModel.status.clear()
                                acceptRejectImageModel.accept_reject_flag = REJECTALL
                                for (i in acceptRejectImageModel.images.indices + 1) {
                                    acceptRejectImageModel.reason.add(binding.rejectReasonEdt.text.toString())
                                    acceptRejectImageModel.status.add(TripDetailImagesAdapter.Reject)
                                }

                                tripsViewModel.acceptRejectDropOffImages(
                                    acceptRejectImageModel, binding.odometerEdt.text.toString(),
                                    true,
                                    Constants.API.ACCEPT_REJECT_DROP_OFF_IMAGES,
                                    this
                                )

                            }
                        }
                    }
                }
            }

            R.id.rejectAllBtn -> {
                when (photoType) {
                    Constants.CarPhotos.HOST_PICKUP_PHOTOS -> {
                        tripsViewModel.popupRejectionImage(this, this, -1)
                    }
                    else -> {
                        val bundle = Bundle()
                        bundle.putInt(
                            Constants.DataParsing.GOTO_PHOTOS,
                            Constants.CarPhotos.HOST_REJECT_ALL_IMAGES
                        )
                        bundle.putInt(
                            Constants.DataParsing.ID,
                            id
                        )
                        bundle.putInt(
                            Constants.DataParsing.CAR_ID,
                            car_id
                        )
                        bundle.putString(
                            Constants.DataParsing.HEADING,
                            getString(R.string.at_dropoff)
                        )


                        uiHelper.openActivityForResult(
                            this,
                            AddTripDetailImages::class.java,
                            true,
                            Constants.RequestCodes.ACTIVITY_IMAGES, bundle
                        )

                    }
                }

            }
            R.id.odometerEdt -> {
            }
            R.id.rejectOdometerReading -> {
                tripsViewModel.popupRejectionImage(
                    this,
                    this,
                    -2
                )
            }
            R.id.acceptAllBtn -> {
                Log.d("rejectButtonClicked", "reject click = " + allRejectClicked)
                for (i in adapter.getList().indices) {
                    adapter.getList()[i].status = TripDetailImagesAdapter.Accept
                }
                model.imageReading?.status = TripDetailImagesAdapter.Accept
                when (photoType) {
                    Constants.CarPhotos.HOST_PICKUP_PHOTOS -> {
                        tripsViewModel.acceptRejectImagesWithOdMeter(
                            listPhotos = adapter.getList(),
                            odometerModel = model.imageReading
                                ?: OdoMeterReadingResponseModel(),
                            carCheckoutId = id,
                            carId = car_id,
                            acceptRejectFlag = ACCEPTALL,
                            showLoader = true,
                            tag = Constants.API.ACCEPT_REJECT_PICKUP_IMAGES,
                            callback = this
                        )
                    }
                    Constants.CarPhotos.HOST_DROP_OFF_PHOTOS -> {

                        acceptRejectImageModel.accept_reject_flag = ACCEPTALL
                        tripsViewModel.acceptRejectDropOffImages(
                            acceptRejectImageModel,
                            "",
                            true,
                            Constants.API.ACCEPT_REJECT_DROP_OFF_IMAGES,
                            this
                        )
                    }

                }
            }
            R.id.rejectInfoOdometer -> {
                tripsViewModel.popupRejectionInfo(
                    this,
                    model.imageReading?.reason ?: ""
                )
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.ADD_PICKUP_IMAGES -> {
                val intent = Intent()
                val bundle = Bundle()
                bundle.putInt(Constants.DataParsing.ID, id)
                intent.putExtra(Constants.DataParsing.MODEL, bundle)
                setResult(RESULT_OK, intent)
                finish()
            }
            Constants.API.VIEW_PICKUP_IMAGES -> {
                model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    CarPhotosResponseModel::class.java
                )
                isPickup = true
                dropoffDate = model.dropOffTime
                if (nameList.size != model.image_info.size)
                    nameList.removeLast()
                for (i in nameList.indices) {
                    model.image_info[i].name = nameList[i]
                }
                //binding.odometerEdt.setText(model.pick_up_image_info[model.pick_up_image_info.size - 1].odometer_reading)
                binding.odometerEdt.setText(model.imageReading?.odometer_reading)
                if (photoType != Constants.CarPhotos.GUEST_PICKUP_PHOTOS) {
                    binding.odometerEdt.isFocusable = false
                    binding.rejectOdometerReading.visibility = View.VISIBLE
                } else {
                    binding.btnSubmit.visibility = View.VISIBLE
                }
                binding.rejectOdometerReading.isVisible =
                    photoType == Constants.CarPhotos.HOST_PICKUP_PHOTOS
                binding.rejectInfoOdometer.isVisible =
                    model.imageReading?.status == TripDetailImagesAdapter.Reject && photoType == Constants.CarPhotos.GUEST_PICKUP_PHOTOS
                binding.statusText.isVisible = model.imageReading?.status in listOf(
                    TripDetailImagesAdapter.Reject,
                    TripDetailImagesAdapter.Accept
                )
                binding.statusText.text =
                    if (model.imageReading?.status == TripDetailImagesAdapter.Reject) getString(
                        R.string.not_accepted
                    ) else getString(R.string.accepted)
                binding.statusText.setTextColor(
                    if (model.imageReading?.status == TripDetailImagesAdapter.Reject) getColor(
                        R.color.new_text_color_orange
                    )
                    else getColor(
                        R.color.send_msg_bg
                    )
                )
                val status: ArrayList<Int> = ArrayList()
                val reason: ArrayList<String> = ArrayList()
                val images: ArrayList<MultipartBody.Part?> = ArrayList()
                for (i in model.image_info.indices) {
                    status.add(model.image_info[i].status)
                    reason.add(model.image_info[i].reason)
                    images.add(null)
                }
                acceptRejectImageModel.status = status
                acceptRejectImageModel.reason = reason
                acceptRejectImageModel.status = status
                if (pickUpImageStatus == Constants.ImagesStatus.REJECTED) {
                    binding.rejectOdometerReading.visibility = View.GONE
                }
                var selectionType = photoType
                if (fromNotification) {
                    var count = 0
                    for (i in model.image_info.indices) {
                        if (model.image_info[i].status == 1) {
                            count += 1
                        }
                    }
                    if (count == model.image_info.size) {
                        binding.btnSubmit.visibility = View.GONE
                        selectionType = Constants.CarPhotos.FROM_NOTIFICATION
                    }
                }
                checkBookingStatus()

            }
            Constants.API.ACCEPT_REJECT_PICKUP_IMAGES -> {
                val intent = Intent()
                val bundle = Bundle()
                bundle.putInt(Constants.DataParsing.ID, car_id)
                intent.putExtra(Constants.DataParsing.MODEL, bundle)
                setResult(RESULT_OK, intent)
                this.finish()
            }
            Constants.API.VIEW_DROP_OFF_IMAGES -> {
                model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    CarPhotosResponseModel::class.java
                )
                if (nameList.size != model.image_info.size)
                    nameList.removeLast()
                for (i in nameList.indices) {
                    model.image_info[i].name = nameList[i]
                }
                isPickup = false
                checkBookingStatus()
                Log.d("photoType", "phtoType = " + photoType)
                //binding.odometerEdt.setText(model.pickUpImageReading?.odometer_reading)
                binding.odometerEdt.setText(model.imageReading?.odometer_reading)
                if (photoType != Constants.CarPhotos.GUEST_PICKUP_PHOTOS) {
                    binding.odometerEdt.isFocusable = false
                } else {
                    binding.btnSubmit.visibility = View.VISIBLE
                }
                binding.rejectOdometerReading.visibility = View.VISIBLE
                if (model.imageReading?.status == TripDetailImagesAdapter.Reject) {
                    binding.rejectOdometerReading.visibility = View.GONE
                    binding.rejectInfoOdometer.visibility = View.VISIBLE
                    binding.statusText.text = getString(R.string.not_accepted)
                    binding.statusText.setTextColor(getColor(R.color.new_text_color_orange))
                } else if (model.imageReading?.status == TripDetailImagesAdapter.Accept) {
                    binding.rejectOdometerReading.visibility = View.VISIBLE
                    binding.rejectInfoOdometer.visibility = View.GONE
                    binding.statusText.text = getString(R.string.accepted)
                    binding.odometerEdt.foreground = null
                }
                val status: ArrayList<Int> = ArrayList()
                val reason: ArrayList<String> = ArrayList()
                val images: ArrayList<MultipartBody.Part?> = ArrayList()
                for (i in model.image_info.indices) {
                    status.add(model.image_info[i].status)
                    reason.add(model.image_info[i].reason)
                    images.add(null)
                }
                status.add(model.imageReading?.status ?: -1)
                reason.add(model.imageReading?.reason ?: "")
                acceptRejectImageModel.status = status
                acceptRejectImageModel.reason = reason
            }
            Constants.API.ADD_ACCIDENT_PHOTOS -> {
                val intent = Intent()
                val bundle = Bundle()
                bundle.putSerializable(Constants.DataParsing.MODEL, model)
                bundle.putInt(Constants.DataParsing.ID, car_id)
                intent.putExtra(Constants.DataParsing.MODEL, bundle)
                setResult(RESULT_OK, intent)
                finish()
            }

            Constants.API.ACCEPT_REJECT_DROP_OFF_IMAGES -> {
                this.finish()
            }
            Constants.API.VIEW_ACCIDENT_IMAGES -> {
                model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    CarPhotosResponseModel::class.java
                )

                /*for (i in nameList.indices) {
                    model.accident_image_info[i].name = nameList[i]

                }*/
                adapter.setList(model.accident_image_info, photoType)
            }
        }
        binding.mainLayout.visibility = View.VISIBLE
    }

    /**
     * Function for checking booking status
     * */
    private fun checkBookingStatus() {
        adapter.setList(model.image_info, photoType)
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }

    override fun popupButtonClick(value: Int) {

    }

    override fun popupButtonClick(value: Int, text_id_model: Any) {
        if (value in listOf(-1, -2)) {
            adapter.getList().forEach {
                it.status =
                    if (value == -1) TripDetailImagesAdapter.Reject else TripDetailImagesAdapter.Accept
                it.reason =
                    if (text_id_model is CounterImageModel)
                        if (value == -1)
                            text_id_model.reason
                        else ""
                    else
                        if (value == -1)
                            text_id_model.toString()
                        else
                            ""
            }
            model.imageReading?.status = TripDetailImagesAdapter.Reject
            model.imageReading?.reason =
                if (text_id_model is CounterImageModel) text_id_model.reason else text_id_model.toString()
        } else {
            adapter.getList()[value].status = TripDetailImagesAdapter.Reject
            adapter.getList()[value].reason =
                if (text_id_model is CounterImageModel) text_id_model.reason else text_id_model.toString()
            model.imageReading?.status = TripDetailImagesAdapter.Accept
            model.imageReading?.reason = null
        }

        if (photoType == Constants.CarPhotos.HOST_PICKUP_PHOTOS)
            tripsViewModel.acceptRejectImagesWithOdMeter(
                listPhotos = adapter.getList(),
                odometerModel = model.imageReading ?: OdoMeterReadingResponseModel(),
                carCheckoutId = id,
                carId = car_id,
                acceptRejectFlag = if (value == -1) REJECTALL else ACCEPT_REJECT_1_BY_1,
                showLoader = true,
                tag = Constants.API.ACCEPT_REJECT_PICKUP_IMAGES,
                callback = this
            )
        else if (photoType == Constants.CarPhotos.HOST_DROP_OFF_PHOTOS) {
            acceptRejectImageModel.accept_reject_flag =
                if (value == -1) REJECTALL else ACCEPT_REJECT_1_BY_1
            tripsViewModel.acceptRejectDropOffImages(
                acceptRejectImageModel, acceptRejectImageModel.odometer,
                true,
                Constants.API.ACCEPT_REJECT_DROP_OFF_IMAGES,
                this
            )
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && (
                    requestCode == Constants.RequestCodes.ACTIVITY_IMAGES
                            || requestCode == Constants.RequestCodes.PICKUP_REJECTION_IMAGES
                    )
        ) {
            setResult(RESULT_OK, intent)
            finish()
        }
    }


    override fun onBackPressed() {
        if (photoType == Constants.CarPhotos.HOST_DROP_OFF_PHOTOS || photoType == Constants.CarPhotos.HOST_PICKUP_PHOTOS) {

            val bundle = Bundle()
            bundle.putInt(Constants.DataParsing.ID, id)
            bundle.putInt(Constants.DataParsing.VIEW_TYPE, Constants.TripUserType.HOST_TYPE)
            uiHelper.openActivity(
                this,
                TripDetailsActivity::class.java,
                bundle
            )
            finish()
        } else {
            super.onBackPressed()
        }

    }

}

