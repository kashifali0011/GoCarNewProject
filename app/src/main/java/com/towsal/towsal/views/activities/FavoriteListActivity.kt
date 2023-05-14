package com.towsal.towsal.views.activities

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityFavoriteListBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.interfaces.RecyclerViewItemClick
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.UserModel
import com.towsal.towsal.network.serializer.cardetails.CalendarDateTimeModel
import com.towsal.towsal.network.serializer.home.CarSearchModel
import com.towsal.towsal.network.serializer.profile.FavouriteResponseModel
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.viewmodel.CarDetailViewModel
import com.towsal.towsal.views.adapters.SearchCarAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Activity class for favourite list
 * */
class FavoriteListActivity : BaseActivity(), OnNetworkResponse,
    RecyclerViewItemClick, SearchCarAdapter.EmptyItem {

    lateinit var binding: ActivityFavoriteListBinding
    val carDetailViewModel: CarDetailViewModel by viewModel()
    var responseModel = FavouriteResponseModel()
    var calendarDateTimeModel = CalendarDateTimeModel()
    lateinit var adapter: SearchCarAdapter
    var name: String = ""

    companion object {
        var carSearchModel = CarSearchModel()
        var filterFragmentShown = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_favorite_list)
        binding.activity = this
        uiHelper.hideActionBar(supportActionBar)
        setData()
        filterFragmentShown = false
    }

    override fun onResume() {
        super.onResume()

        setData()
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        binding.toolBar.setAsGuestToolBar(
            R.string.favorites,
            supportActionBar
        )
        var lat = 0.0
        var lng = 0.0
        var cityId: Int
        val userId =
            intent?.extras?.getInt(Constants.DataParsing.USER_ID, 0) ?: (preferenceHelper.getObject(
                Constants.USER_MODEL,
                UserModel::class.java
            ) as? UserModel)?.id ?: 0
        cityId = preferenceHelper.getIntOther(Constants.DataParsing.CITY_SELECTED_ID, 0)
        if (uiHelper.isPermissionAllowed(this, PERMISSION)) {
            if (!gpsTracker!!.checkGPsEnabled()) {
                uiHelper.showEnableLocationSetting(this)
            }
            lat = gpsTracker?.getLatitude()!!
            lng = gpsTracker?.getLongitude()!!
            cityId = 0
        }
        carDetailViewModel.getFavList(
            userId,
            cityId,
            lat,
            lng,
            true,
            Constants.API.GET_FAV_LIST,
            this
        )
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_FAV_LIST -> {
                responseModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    FavouriteResponseModel::class.java
                )
                populateData()
            }
        }
    }
    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
        binding.mainLayout.visibility = View.VISIBLE
        binding.noDataLayout.isVisible = !binding.tvName.isVisible
    }

    /**
     * Function for populate data
     * */
    private fun populateData() {
        name = responseModel.firstName
        binding.tvName.text = name + "'" + "S" + " " + getString(R.string.favorite_car)
        val adapter =
            SearchCarAdapter(
                this,
                uiHelper,
                responseModel.fav_car_list,
                this,
                carDetailViewModel,
                true,
                this
            )
        binding.recyclerView.adapter = adapter
        binding.mainLayout.visibility = View.VISIBLE
        binding.noDataLayout.isVisible = responseModel.fav_car_list.isEmpty()
        binding.tvName.isVisible = responseModel.fav_car_list.isNotEmpty()
    }



    /**
     * Function for click listeners
     * */
    fun onClick(view: View) {
        view.preventDoubleClick()
        when (view.id) {
            R.id.iv_arrowBack -> {
                finish()
            }

        }
    }

    override fun onItemClick(position: Int) {
        val bundle = Bundle()
        val cityId = preferenceHelper.getIntOther(Constants.DataParsing.CITY_SELECTED_ID, 0)
        carSearchModel.lat = gpsTracker?.getLatitude()!!
        carSearchModel.lng = gpsTracker?.getLongitude()!!
        bundle.putDouble(Constants.LAT, carSearchModel.lat)
        bundle.putDouble(Constants.LNG, carSearchModel.lng)
        bundle.putInt(Constants.CITY_ID, cityId)
        bundle.putInt(Constants.CAR_ID, responseModel.fav_car_list[position].id)
        bundle.putSerializable(Constants.DataParsing.DATE_TIME_MODEL, calendarDateTimeModel)
        uiHelper.openActivity(this, CarDetailsActivity::class.java, true, bundle)
    }

    override fun onEmptyList() {
        populateData()
    }


}