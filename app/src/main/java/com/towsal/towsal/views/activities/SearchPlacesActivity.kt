package com.towsal.towsal.views.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.microsoft.appcenter.utils.storage.SharedPreferencesManager.getInt
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivitySearchPlacesBinding
import com.towsal.towsal.extensions.hideKeyboard
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.network.serializer.home.CarHomeModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.views.adapters.AutoCompletePlacesAdapter
import com.towsal.towsal.views.listeners.ClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Activity class for search places
 * */
class SearchPlacesActivity : BaseActivity(), View.OnClickListener {

    //  variables
    lateinit var binding: ActivitySearchPlacesBinding
    var lat = 0.0
    var lng = 0.0
    var carId = 0
    var list: List<CarHomeModel> = ArrayList()
    private var listAutocompletePrediction: List<AutocompletePrediction> = ArrayList()
    lateinit var placesClient: PlacesClient
    var address = ""
    lateinit var imm: InputMethodManager
    private var flow = 0

    companion object {
        const val NORMAL_FLOW = 0
        const val OTHER_FLOW = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_places)
        binding.activity = this
        uiHelper.hideActionBar(supportActionBar)
        if (intent.hasExtra(Constants.ADVANCE_SEARCH_PARAM)) {
            list =
                intent.getSerializableExtra(Constants.ADVANCE_SEARCH_PARAM) as List<CarHomeModel>
            flow =
                intent.getIntExtra(Constants.DataParsing.FLOW_COMING_FROM, NORMAL_FLOW)
        }
        carId = intent.getIntExtra(Constants.CAR_TYPE_ID, 0)
        Log.d("carIdIs", carId.toString())
        placesClient = Places.createClient(this)
        imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        )

        binding.searchPlace.requestFocus()
        binding.tvCityName.text = list[0].name
        binding.tvAirportName.text = list[1].name
        setRecyclerAdapter()
        addTextChangeListener()

    }

    /**
     * Function for setting up recycler adapter
     * */
    private fun setRecyclerAdapter() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@SearchPlacesActivity)
            adapter = AutoCompletePlacesAdapter(
                this@SearchPlacesActivity,
                listAutocompletePrediction,
                object : ClickListener {
                    override fun onClick(position: Int) {
                        searchAddress(position)
                    }
                })
        }
    }

    /**
     * Function for fetching search place
     * */
    private fun searchAddress(position: Int) {
        val placeFields: List<Place.Field> =
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        showLoaderDialog()
        CoroutineScope(Dispatchers.IO).launch {
            placesClient.fetchPlace(
                FetchPlaceRequest.newInstance(
                    listAutocompletePrediction[position].placeId,
                    placeFields
                )
            )
                .addOnSuccessListener {
                    lat = it.place.latLng?.latitude ?: 0.0
                    lng = it.place.latLng?.longitude ?: 0.0
                    this@SearchPlacesActivity.address =
                        listAutocompletePrediction[position].getFullText(null).toString()
                    openActivity(false)
                }
                .addOnFailureListener {
                    if (it is ApiException) {
                        uiHelper.showLongToastInCenter(this@SearchPlacesActivity, "Place not found")
                    }
                }
        }
    }

    /**
     * Function for add text change listeners
     * */
    private fun addTextChangeListener() {
        binding.searchPlace.addTextChangedListener {
            if (it.toString().isEmpty()) {
                binding.defaultOptions.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
                binding.noDataFound.visibility = View.GONE
            } else {
                binding.defaultOptions.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                binding.noDataFound.visibility = View.GONE

                sendSearchRequest(it.toString())
            }
        }

        binding.searchPlace.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard(activity)
                    val list = (binding.recyclerView.adapter as AutoCompletePlacesAdapter).list
                    return if (
                        list.isNotEmpty()
                        && binding.searchPlace.text.toString().isNotEmpty()
                    ) {
                        searchAddress(0)
                        hideKeyboard(this@SearchPlacesActivity)
                        true
                    } else if (list.isEmpty() && binding.searchPlace.text.toString().isNotEmpty()) {
                        uiHelper.showLongToastInCenter(
                            activity,
                            activity.getString(R.string.please_enter_valid_query)
                        )
                        false
                    } else {
                        uiHelper.showLongToastInCenter(
                            activity,
                            activity.getString(R.string.please_enter_your_query)
                        )
                        false
                    }
                }
                return false
            }
        })
    }

    /**
     * Function for sending user to search car screen
     * */
    private fun sendSearchRequest(input: String) {
        // Use the builder to create a FindAutocompletePredictionsRequest.
        val request =
            FindAutocompletePredictionsRequest.builder() // Call either setLocationBias() OR setLocationRestriction().
                .setCountry(BuildConfig.PLACES_COUNTRY) //Saudi Arabia
                .setQuery(input)
                .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener {

                val adapter = binding.recyclerView.adapter as AutoCompletePlacesAdapter
                listAutocompletePrediction = it.autocompletePredictions
                adapter.changeList(listAutocompletePrediction)
                binding.defaultOptions.isVisible = binding.searchPlace.text.toString().isEmpty()
                setVisibilities()

            }.addOnFailureListener { exception: Exception? ->
                binding.noDataFound.isVisible = true
                setVisibilities()
            }
    }

    private fun setVisibilities() {
        binding.recyclerView.isVisible =
            listAutocompletePrediction.isNotEmpty() && !binding.defaultOptions.isVisible
        binding.noDataFound.isVisible =
            listAutocompletePrediction.isEmpty() && !binding.defaultOptions.isVisible
    }

    /**
     * Function for click listeners
     * */
    override fun onClick(v: View?) {
        v?.preventDoubleClick()
        when (v?.id) {
            binding.currentLocation.id -> {
                if (uiHelper.isPermissionAllowed(this, PERMISSION)) {
                    if (!gpsTracker!!.checkGPsEnabled()) {
                        uiHelper.showEnableLocationSetting(this)
                        return
                    }
                    lat = gpsTracker?.getLatitude()!!
                    lng = gpsTracker?.getLongitude()!!
                    openActivity(true)
                } else {
                    uiHelper.requestPermission(PERMISSION, this)
                }
            }
            binding.tvCityName.id -> {
                if (uiHelper.isPermissionAllowed(this, PERMISSION)) {
                    if (!gpsTracker!!.checkGPsEnabled()) {
                        uiHelper.showEnableLocationSetting(this)
                        return
                    }
                    lat = list[0].lat.toDouble()
                    lng = list[0].long.toDouble()
                    address = list[0].name
                    openActivity(false)
                } else {
                    uiHelper.requestPermission(PERMISSION, this)
                }
            }

            binding.tvAirportName.id -> {
                if (uiHelper.isPermissionAllowed(this, PERMISSION)) {
                    if (!gpsTracker!!.checkGPsEnabled()) {
                        uiHelper.showEnableLocationSetting(this)
                        return
                    }
                    lat = list[1].lat.toDouble()
                    lng = list[1].long.toDouble()
                    address = list[1].name
                    openActivity(false)
                } else {
                    uiHelper.requestPermission(PERMISSION, this)
                }
            }

            binding.close.id -> {
                onBackPressed()
            }
        }
    }

    /**
     * Function for opening activity
     * */
    private fun openActivity(isCurrentLocation: Boolean) {

        hideLoaderDialog()
        val bundle = Bundle()
        bundle.putDouble(Constants.LAT, lat)
        bundle.putDouble(Constants.LNG, lng)
        bundle.putInt(Constants.CITY_ID, 0)
        if (carId > 0){
            bundle.putInt(Constants.CAR_TYPE_ID, carId)
        }

        if (isCurrentLocation) {
            bundle.putString(
                Constants.ADDRESS,
                resources.getString(R.string.current_location)
            )
        } else {
            bundle.putString(
                Constants.ADDRESS,
                address
            )
        }

        if (flow == OTHER_FLOW) {
            val intent = Intent()
            intent.putExtras(bundle)
            setResult(Activity.RESULT_OK, intent)
            finish()
        } else
            uiHelper.openActivity(
                this,
                SearchCarActivity::class.java,
                bundle
            )
    }

    override fun onDestroy() {
        super.onDestroy()
        imm.hideSoftInputFromWindow(binding.searchPlace.windowToken, 0)
    }
}