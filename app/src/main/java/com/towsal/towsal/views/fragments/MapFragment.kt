package com.towsal.towsal.views.fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.material.shape.CornerFamily
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.collections.MarkerManager
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseFragment
import com.towsal.towsal.databinding.AdapterInfoWindowMapBinding
import com.towsal.towsal.databinding.FragmentMapBinding
import com.towsal.towsal.extensions.getValueInPx
import com.towsal.towsal.extensions.glideLoadCornerImage
import com.towsal.towsal.helper.UIHelper
import com.towsal.towsal.network.serializer.cardetails.CalendarDateTimeModel
import com.towsal.towsal.network.serializer.home.CarSearchResponseModel
import com.towsal.towsal.network.serializer.home.SearchCarDetailModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.MainScreenViewModel
import com.towsal.towsal.views.activities.CarDetailsActivity
import com.towsal.towsal.views.activities.SearchCarActivity


private var clickedCluster: Cluster<SearchCarDetailModel>? = null
private var clickedClusterItem: SearchCarDetailModel? = null
var responseModel = CarSearchResponseModel()
val uiHelper: UIHelper = UIHelper()
private lateinit var clusterManager: ClusterManager<SearchCarDetailModel>
private var markerClick: Marker? = null
private var adapterCluster: MapFragment.MyCustomAdapterForItems? = null

class MapFragment : BaseFragment(),
    ClusterManager.OnClusterItemInfoWindowClickListener<SearchCarDetailModel> {
    lateinit var binding: FragmentMapBinding
    var mMap: GoogleMap? = null

    private var calendarDateTimeModel = CalendarDateTimeModel()
    private val homeViewModel: MainScreenViewModel by activityViewModels()
    private lateinit var markerManager: MarkerManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragment = this
        setObserver()
    }

    private fun setObserver() {
        homeViewModel.carSearchResponseModel.observe(
            viewLifecycleOwner
        ) {
            responseModel = it
            val mapFragment = childFragmentManager
                .findFragmentById(binding.map.id) as SupportMapFragment

            mapFragment.getMapAsync { googleMap ->
                mMap = googleMap
                setData()
            }
        }

        homeViewModel.calendarDateTimeModel.observe(
            viewLifecycleOwner
        ) {
            calendarDateTimeModel = it
        }
    }

    private fun setData() {
        if (mMap != null) {
            setUpClusterer(mMap!!, responseModel)
        }
        if (!Places.isInitialized()) {
            Places.initialize(
                requireActivity(),
                getString(R.string.custom_google_api_key)
            )
        }
    }

    private fun setUpClusterer(
        map: GoogleMap,
        responseModel: CarSearchResponseModel
    ) {
        map.uiSettings.isMyLocationButtonEnabled = false
        //map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(responseModel.advance_search_param.user_lat, responseModel.advance_search_param.user_long), 12.0f))
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                LatLng(
                    responseModel.advance_search_param.userLat,
                    responseModel.advance_search_param.userLong
                ), 12.0f
            )
        )
        clusterManager = ClusterManager(requireActivity(), map)
        map.setOnCameraIdleListener(clusterManager)
        map.setOnMarkerClickListener(clusterManager)
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            uiHelper.requestPermission(PERMISSION, requireActivity())
        } else {
            map.isMyLocationEnabled = true
        }

        clusterManager.setOnClusterItemInfoWindowClickListener(this)
        clusterManager
            .setOnClusterClickListener { cluster ->
                clickedCluster = cluster
                false
            }

        clusterManager
            .setOnClusterItemClickListener { item ->
                clickedClusterItem = item
                false
            }

        mMap!!.setInfoWindowAdapter(clusterManager.markerManager)
        adapterCluster = MyCustomAdapterForItems(requireActivity())
        clusterManager.markerCollection.setInfoWindowAdapter(
            adapterCluster
        )
        addItems()
    }

    private fun addItems() {

        for (carDetail in responseModel.car_details) {
            val offsetItem = SearchCarDetailModel(
                lat_ = carDetail.pin_lat,
                lng_ = carDetail.pin_long,
                title_ = carDetail.name + " " + carDetail.model,
                snippet_ = this.getString(R.string.sar) + " " + carDetail.price + "/" + this.getString(
                    R.string.day
                ),
                icon_ = carDetail.url,
                rating = carDetail.rating,
                distance = carDetail.distance,
                id = carDetail.id
            )
            clusterManager.addItem(offsetItem)
            clusterManager.setAnimation(true)
            clusterManager.markerManager.Collection().markers.parallelStream().forEach {
                it.showInfoWindow()
                it.hideInfoWindow()
            }

        }

        clusterManager.setAnimation(true)
    }

    override fun onClusterItemInfoWindowClick(item: SearchCarDetailModel?) {
        val bundle = Bundle()
        val cityId = preferenceHelper.getIntOther(Constants.DataParsing.CITY_SELECTED_ID, 0)
        bundle.putDouble(Constants.LAT, SearchCarActivity.carSearchModel.lat)
        bundle.putDouble(Constants.LNG, SearchCarActivity.carSearchModel.lng)
        bundle.putInt(Constants.CITY_ID, cityId)
        bundle.putInt(Constants.CAR_ID, item!!.markerId)
        bundle.putSerializable(Constants.DataParsing.DATE_TIME_MODEL, calendarDateTimeModel)
        uiHelper.openActivity(requireActivity(), CarDetailsActivity::class.java, true, bundle)

    }


    open class MyCustomAdapterForItems internal constructor(var context: Context) :
        InfoWindowAdapter {
        private val myContentsView: AdapterInfoWindowMapBinding = AdapterInfoWindowMapBinding.bind(
            LayoutInflater.from(context).inflate(
                R.layout.adapter_info_window_map, null
            )
        )

        override fun getInfoContents(marker: Marker): View? {
            Handler(Looper.myLooper()!!).postDelayed({

                if (markerClick != null && markerClick!!.isInfoWindowShown) {
                    markerClick!!.hideInfoWindow();
                    markerClick!!.showInfoWindow();
                    markerClick = null
                }
            }, 200)

            return null
        }

        override fun getInfoWindow(marker: Marker): View? {
            markerClick = marker
            myContentsView.tvCarModel.text = clickedClusterItem!!.title_
            myContentsView.ratings.text = clickedClusterItem!!.rating.toString()
            myContentsView.imageCar.glideLoadCornerImage(
                BuildConfig.IMAGE_BASE_URL + clickedClusterItem!!.icon
            )
            myContentsView.tvPrice.text = clickedClusterItem!!.snippet_
            myContentsView.tvdistance.text = clickedClusterItem?.distance
            adapterCluster!!.getInfoContents(marker)
            return myContentsView.root
        }

    }

}