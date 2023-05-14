package com.towsal.towsal.views.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityLocationSelectionBinding
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.extensions.setGradientTextColor
import com.towsal.towsal.network.serializer.carlist.CarInformationModel
import com.towsal.towsal.network.serializer.settings.CarLocationDeliveryModel
import com.towsal.towsal.utils.Constants

/**
 * Activity class for selecting location
 * */
class LocationSelectionActivity : BaseActivity(), OnMapReadyCallback {

    lateinit var binding: ActivityLocationSelectionBinding
    var mMap: GoogleMap? = null
    var locationModel = CarLocationDeliveryModel()
    var carInfoModel = CarInformationModel()
    var hideStatus = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_location_selection)
        binding.activity = this
        binding.layoutToolBar.setAsGuestToolBar(
            R.string.pickup_return,
            supportActionBar
        )

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setData()
    }

    /**
     * Function for setting up data in views
     * */
    @SuppressLint("SetTextI18n")
    private fun setData() {
        if (intent.extras != null) {

            locationModel =
                intent.extras!!.getSerializable(Constants.DataParsing.MODEL) as CarLocationDeliveryModel
            carInfoModel =
                intent.extras!!.getSerializable(Constants.DataParsing.CARINFODATAMODEL) as CarInformationModel
            binding.tvPickupLocation.text = locationModel.street_address
//            binding.carName.text = carInfoModel.na
             hideStatus = carInfoModel.isHideChnageLocation
            var lang = carInfoModel.pin_long
            var lat = carInfoModel.pin_lat
            if (hideStatus){
                hideChangeLocation()
            }else{

            }
            if (carInfoModel.locationSelected == Constants.DeliveryFeeStatus.NOT_APPLIED) {
                pickUpOrDelivery(false)
                if (hideStatus){
                    setCarModelDataWithLocationModels()
                }else{
                    setCarModelDataWithLocationModel()
                }
            }
            else {
                Log.d("testing","testing")
            }

            binding.tvDeliveryFee.text =
                "${locationModel.delivery_price} ${getString(R.string.sar)}"
        }

        val colors = intArrayOf(Color.parseColor("#1420E4"), Color.parseColor("#0F0F7D"))
        binding.tvDelivery.setGradientTextColor(colors)


    }

    /**
     * Activity class for setting up car model with location data
     * */
    private fun setCarModelDataWithLocationModel() {
        carInfoModel.city_name = locationModel.city_name
        carInfoModel.street_address = locationModel.street_address
        carInfoModel.country_name = locationModel.country_name
        carInfoModel.state_name = locationModel.state_name
        carInfoModel.pin_long = locationModel.pin_long
        carInfoModel.pin_lat = locationModel.pin_lat
        carInfoModel.zip_code = locationModel.zip_code
        carInfoModel.pin_address = locationModel.pin_address
        carInfoModel.radius = locationModel.delivery_radius.toInt()
        carInfoModel.locationSelected = Constants.DeliveryFeeStatus.NOT_APPLIED
        binding.tvDeliveryAddress.text = getString(R.string.enter_address)
    }

    /**
     * Activity class for setting up car model with location data
     * */
    private fun setCarModelDataWithLocationModels() {
        carInfoModel.city_name = locationModel.city_name
        carInfoModel.street_address = locationModel.street_address
        carInfoModel.country_name = locationModel.country_name
        carInfoModel.state_name = locationModel.state_name
        if (locationModel.pin_lat == 0.0){
            locationModel.pin_lat = carInfoModel.pin_lat
            locationModel.pin_long = carInfoModel.pin_long
        }else{
            carInfoModel.pin_lat = locationModel.pin_lat
            carInfoModel.pin_long = locationModel.pin_long
        }
        carInfoModel.pin_address = locationModel.pin_address
        carInfoModel.locationSelected = Constants.DeliveryFeeStatus.NOT_APPLIED
        binding.tvDeliveryAddress.text = getString(R.string.enter_address)

    }
    /**
     * Function for adding marker on map
     * */
    private fun addMarker(coordinate: LatLng, animateCamera: Boolean) {
        var lat = coordinate.latitude
        var lang = coordinate.longitude
        mMap?.clear()
        val cameraPosition: CameraPosition =
            CameraPosition.Builder().target(coordinate).zoom(18f).build()
        if (animateCamera)
            mMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        mMap?.addMarker(
            MarkerOptions().position(
                LatLng(
                    coordinate.latitude,
                    coordinate.longitude
                )
            )
        )
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        when (view) {
            binding.tvDeliveryAddress -> {
                val bundle = Bundle()
                bundle.putSerializable(Constants.DataParsing.MODEL, carInfoModel)
                bundle.putInt(Constants.DataParsing.FLOW_COMING_FROM, 1)
                uiHelper.openActivityForResult(
                    this,
                    MapsActivity::class.java,
                    true,
                    Constants.RequestCodes.ACTIVTIY_LOCATION, bundle
                )
            }
//            R.id.removeLocation -> {
//                addMarker(LatLng(locationModel.pin_lat, locationModel.pin_long), true)
//                setCarModelDataWithLocationModel()
//            }
            binding.save -> {
                if (binding.tvPickupLocation.text.toString()
                        .equals(binding.tvDeliveryAddress.text.toString(), true)
                ) {
                    uiHelper.showLongToastInCenter(
                        this,
                        getString(R.string.car_location_address)
                    )
                } else {

                    val intent = Intent()
                    val bundle = Bundle()
                    bundle.putSerializable(Constants.DataParsing.MODEL, carInfoModel)
                    intent.putExtra(Constants.DataParsing.MODEL, bundle)
                    setResult(RESULT_OK, intent)
                    finish()
                }

            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                Constants.RequestCodes.ACTIVTIY_LOCATION -> {
                    val bundle = data?.getBundleExtra(Constants.DataParsing.MODEL)
                    carInfoModel =
                        bundle?.getSerializable(Constants.DataParsing.MODEL) as CarInformationModel
//                    binding.tvDeliveryAddress.text = carInfoModel.street_address
                    addMarker(LatLng(carInfoModel.pin_lat, carInfoModel.pin_long), true)
                    pickUpOrDelivery(true)
//                        binding.removeLocation.visibility = View.VISIBLE
                    carInfoModel.locationSelected = Constants.DeliveryFeeStatus.APPLIED
                }
            }
        }
    }

    /**
     * Function used for adding markers on map
     * */
    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        mMap?.uiSettings?.isScrollGesturesEnabled = false
        if (carInfoModel.locationSelected == Constants.DeliveryFeeStatus.NOT_APPLIED){
            addMarker(LatLng(locationModel.pin_lat, locationModel.pin_long), true)
            pickUpOrDelivery(false)
            }
        else {
            addMarker(LatLng(carInfoModel.pin_lat, carInfoModel.pin_long), true)
            pickUpOrDelivery(true)
        }
    }
    private fun pickUpOrDelivery(checkPickOrDelivery: Boolean){
        if (checkPickOrDelivery){
            binding.clDelivery.setBackgroundResource(R.drawable.bg_blue)
            binding.ivDelivery.setColorFilter(resources.getColor(R.color.colorWhite))
            binding.ivMoveToNext.setColorFilter(resources.getColor(R.color.colorWhite))
            binding.tvDeliveryAddress.setGradientTextColor(
                intArrayOf(
                    Color.parseColor("#FFFFFF"),
                    Color.parseColor("#FFFFFF")
                )
            )
            binding.tvDelivery.setGradientTextColor(
                intArrayOf(
                    Color.parseColor("#FFFFFF"),
                    Color.parseColor("#FFFFFF")
                )
            )
            binding.tvDeliveryFee.setGradientTextColor(
                intArrayOf(
                    Color.parseColor("#FFFFFF"),
                    Color.parseColor("#FFFFFF")
                )
            )
         binding.clPickupLocation.setBackgroundResource(R.drawable.ic_select_location)

           binding.tvPickupTitle.setGradientTextColor(
                intArrayOf(
                    Color.parseColor("#000000"),
                    Color.parseColor("#000000")
                )
            )
           binding.tvPickupLocation.setGradientTextColor(
                intArrayOf(
                    Color.parseColor("#000000"),
                    Color.parseColor("#000000")
                )
            )
            binding.tvPickupFree.setGradientTextColor(
                intArrayOf(
                    Color.parseColor("#000000"),
                    Color.parseColor("#000000")
                )
            )
            binding.ivPickup.setColorFilter(resources.getColor(R.color.black))

        }else{
            binding.clDelivery.setBackgroundResource(R.drawable.ic_select_location)
            binding.ivDelivery.setColorFilter(resources.getColor(R.color.black))
            binding.ivMoveToNext.setColorFilter(resources.getColor(R.color.newColorPrimary))
            binding.tvDeliveryAddress.setGradientTextColor(
                intArrayOf(
                    Color.parseColor("#000000"),
                    Color.parseColor("#000000")
                )
            )
            binding.tvDelivery.setGradientTextColor(
                intArrayOf(
                    Color.parseColor("#000000"),
                    Color.parseColor("#000000")
                )
            )
            binding.tvDeliveryFee.setGradientTextColor(
                intArrayOf(
                    Color.parseColor("#000000"),
                    Color.parseColor("#000000")
                )
            )
            binding.clPickupLocation.setBackgroundResource(R.drawable.bg_blue)
            binding.tvPickupTitle.setGradientTextColor(
                intArrayOf(
                    Color.parseColor("#FFFFFF"),
                    Color.parseColor("#FFFFFF")
                )
            )
            binding.tvPickupLocation.setGradientTextColor(
                intArrayOf(
                    Color.parseColor("#FFFFFF"),
                    Color.parseColor("#FFFFFF")
                )
            )
            binding.tvPickupFree.setGradientTextColor(
                intArrayOf(
                    Color.parseColor("#FFFFFF"),
                    Color.parseColor("#FFFFFF")
                )
            )

            binding.ivPickup.setColorFilter(resources.getColor(R.color.colorWhite))


        }

    }
    private fun hideChangeLocation(){
        binding.cvPickupLocation.isVisible = false
        binding.viewPickUp.isVisible = false
        binding.mcvDelivery.isVisible = false
        binding.viewDelivery.isVisible = false
        binding.save.isVisible = false
    }

}