package com.towsal.towsal.views.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.app.MainApplication
import com.towsal.towsal.databinding.ActivityMapsBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setAsHostToolBar
import com.towsal.towsal.extensions.setGradientTextColor
import com.towsal.towsal.network.serializer.carlist.CarInformationModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.Constants.RequestCodes.PLACE_AUTOCOMPLETE_REQUEST_CODE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * Activity class for maps activity
 * */
class MapsActivity : BaseActivity(), OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

    lateinit var binding: ActivityMapsBinding
    var mMap: GoogleMap? = null
    var city = ""
    var state = ""
    var country = ""
    var addressValue = ""
    var latLng = LatLng(0.0, 0.0)
    var carInfoModel = CarInformationModel()
    var boundLatLng = LatLng(0.0, 0.0)
    var circle: Circle? = null
    var comingFrom = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_maps)
        binding.activity = this
        setData()

        val colors = intArrayOf(Color.parseColor("#1420E4"), Color.parseColor("#0F0F7D"))
        binding.tvDelivery.setGradientTextColor(colors)
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        //var label = getString(R.string.list_your_car)
        var label = R.string.enter_location
        if (intent.extras != null) {
            carInfoModel = intent?.extras?.getSerializable(Constants.DataParsing.MODEL) as CarInformationModel
            if (carInfoModel.hideData) {
                label = R.string.pickup_location
                disableFields()
            }
            comingFrom = intent?.extras?.getInt(Constants.DataParsing.FLOW_COMING_FROM, 0) ?: 0

        }
        if (comingFrom == 0) {
            actionBarSetting(label)
            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
            if (!Places.isInitialized()) {
                Places.initialize(
                    applicationContext, resources.getString(R.string.custom_google_api_key)
                )
            }
            binding.locationBtn.movementMethod = ScrollingMovementMethod()
            binding.clOldDesign.visibility = View.VISIBLE
            binding.clNewDesign.visibility = View.GONE

        } else {
            label = R.string.delivery_address
            binding.clNewDesign.visibility = View.VISIBLE
            uiHelper.hideActionBar(supportActionBar)
            binding.clOldDesign.visibility = View.GONE
            actionBarSetting(label)
            val mapFragment = supportFragmentManager.findFragmentById(R.id.newMap) as SupportMapFragment
            mapFragment.getMapAsync(this)
            if (!Places.isInitialized()) {
                Places.initialize(
                    applicationContext, resources.getString(R.string.custom_google_api_key)
                )
            }
            binding.tvLocationAddress.movementMethod = ScrollingMovementMethod()
        }
    }

    /**
     * Function for setting up action bar
     * */
    private fun actionBarSetting(titleId: Int) {

        binding.layoutToolBar.setAsHostToolBar(
            titleId, supportActionBar
        )
    }

    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view.id) {

            R.id.confirm, R.id.save -> {
                if (carInfoModel.pin_lat == 0.0 || carInfoModel.pin_long == 0.0) {
                    uiHelper.showLongToastInCenter(
                        this, getString(R.string.select_location_err_msg)
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
            R.id.locationBtn, binding.tvLocationAddress.id -> {
                if (carInfoModel != null && carInfoModel.hideData) {

                } else {
                    val fields: List<Place.Field> = listOf(Place.Field.ADDRESS, Place.Field.LAT_LNG)
                    val autocompleteIntent: Intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this)
                    startActivityForResult(
                        autocompleteIntent, PLACE_AUTOCOMPLETE_REQUEST_CODE
                    )
                }
            }
        }
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int, data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val place = Autocomplete.getPlaceFromIntent(data!!)
                addressValue = place.address.toString()
                this.latLng = LatLng(place.latLng!!.latitude, place.latLng!!.longitude)
                addMarker(latLng)
            }
        } else {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    val bundle = data.getBundleExtra(Constants.DataParsing.MODEL)
                    carInfoModel = bundle?.getSerializable(Constants.DataParsing.MODEL) as CarInformationModel
                    val intent = Intent()
                    bundle.putSerializable(Constants.DataParsing.MODEL, carInfoModel)
                    intent.putExtra(Constants.DataParsing.MODEL, bundle)
                    setResult(RESULT_OK, intent)
                    finish()
                } else {
                    uiHelper.showLongToastInCenter(this, "Data is Null")
                }
            }
        }
    }

    /**
     * Function for adding markers to the map
     * */
    private fun addMarker(coordinate: LatLng, animateCamera: Boolean = true) {
        showLocation(coordinate)
        if (animateCamera) {
            val zoom = 18f
            val cameraPosition: CameraPosition = CameraPosition.Builder().target(coordinate).zoom(zoom).build()
            mMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }
    }

    /**
     * Function for showing location on map
     * */
    private fun showLocation(coordinate: LatLng) {
        MainApplication.coroutineScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(this@MapsActivity, Locale.getDefault())
                val sb = StringBuilder()
                val result = geocoder.getFromLocation(
                    coordinate.latitude, coordinate.longitude, 5
                )
                result?.let {
                    if (it.isNotEmpty()) {
                        val address = it[0].getAddressLine(0)
                        if (address != null) sb.append(address).append(" ")
                        city = it[0].locality
                        sb.append(city).append(" ")
                        state = it[0].adminArea
                        sb.append(state).append(" ")
                        country = it[0].countryName
                        sb.append(country).append(" ")
                        val postalCode = it[0].postalCode
                        if (postalCode != null) sb.append(postalCode).append(" ")
                        addressValue = sb.toString()
                        if (comingFrom == 0) {
                            Handler(Looper.getMainLooper()).post {
                                binding.locationBtn.text = address

                            }
                        } else {
                            Handler(Looper.getMainLooper()).post {
                                binding.tvLocationAddress.text = address
                            }
                        }
                        carInfoModel.street_address = address
                        carInfoModel.pin_lat = coordinate.latitude
                        carInfoModel.pin_long = coordinate.longitude
                        carInfoModel.city_name = city

                        carInfoModel.zip_code = "00000"
                      //  carInfoModel.zip_code = it[0].postalCode.ifEmpty { "00000" }

                        if (city.isEmpty()) {
                            carInfoModel.city_name = ""
                        }
                        carInfoModel.country_name = country
                        if (country.isEmpty()) {
                            carInfoModel.country_name = ""
                        }
                        carInfoModel.state_name = state
                        if (state.isEmpty()) {
                            carInfoModel.state_name = ""
                        }

                    }
                }


            } catch (e: Exception) {
                Log.e("Map Activity", "Error : " + e.localizedMessage)
            }
        }

    }

    /**
     * Function for disable fields
     * */
    private fun disableFields() {
        if (carInfoModel.hideData) {
            binding.confirm.visibility = View.GONE
            binding.locationBtn.isClickable = false
            binding.locationBtn.isFocusable = false
            mMap?.uiSettings?.isScrollGesturesEnabled = false

        }
    }

    /**
     * Function used for performing different things
     * permissions checking
     * adding markers
     * adding circles
     * disable fields
     * */
    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            uiHelper.requestPermission(PERMISSION, this)
        } else {
            mMap?.isMyLocationEnabled = true
            if (!carInfoModel.hideData && carInfoModel.pin_lat == 0.0) {
                addMarker(LatLng(gpsTracker!!.getLatitude(), gpsTracker!!.getLongitude()))
            }
        }
        mMap?.setOnCameraIdleListener(this@MapsActivity)
        if (carInfoModel.pin_lat != 0.0 && carInfoModel.pin_long != 0.0) {
            addMarker(LatLng(carInfoModel.pin_lat, carInfoModel.pin_long))
        }
        if (carInfoModel.radius != 0) {
            boundLatLng = LatLng(carInfoModel.pin_lat, carInfoModel.pin_long)
            circle = mMap!!.addCircle(
                CircleOptions().center(boundLatLng).radius(carInfoModel.radius.toDouble() * 1000).strokeColor(getColor(R.color.btn_color)).fillColor(Color.TRANSPARENT)
            )
            mMap!!.addCircle(
                CircleOptions().center(boundLatLng).radius(carInfoModel.radius.toDouble() * 1000).strokeColor(getColor(R.color.btn_color)).fillColor(Color.TRANSPARENT)
            )
        }
        disableFields()
    }

    override fun onCameraIdle() {
        mMap?.let {
            it.isMyLocationEnabled = ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            this.latLng = LatLng(it.cameraPosition.target.latitude, it.cameraPosition.target.longitude)
            addMarker(latLng, false)
            if (circle != null && carInfoModel.radius != 0) {
                val distance = FloatArray(2)
                Location.distanceBetween(
                    it.cameraPosition.target.latitude, it.cameraPosition.target.longitude, circle?.center!!.latitude, circle!!.center.longitude, distance
                )

                if (distance[0] >= circle!!.radius) {
                    uiHelper.showLongToastInCenter(this, getString(R.string.outside_radius_err_msg))
                    binding.confirm.visibility = View.GONE
                } else {
                    binding.confirm.visibility = View.VISIBLE
                }
                disableFields()
            }
        }

    }
}