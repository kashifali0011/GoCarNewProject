package com.towsal.towsal.views.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.towsal.towsal.BuildConfig
import com.towsal.towsal.R
import com.towsal.towsal.app.BaseActivity
import com.towsal.towsal.databinding.ActivityUserProfileBinding
import com.towsal.towsal.extensions.preventDoubleClick
import com.towsal.towsal.extensions.setAsGuestToolBar
import com.towsal.towsal.extensions.showEnableLocationSetting
import com.towsal.towsal.helper.PreferenceHelper
import com.towsal.towsal.interfaces.PopupCallback
import com.towsal.towsal.network.OnNetworkResponse
import com.towsal.towsal.network.serializer.BaseResponse
import com.towsal.towsal.network.serializer.cities.CitiesListModel
import com.towsal.towsal.network.serializer.userdetails.Reviews
import com.towsal.towsal.network.serializer.userdetails.UserDetails
import com.towsal.towsal.utils.Constants
import com.towsal.towsal.utils.DateUtil
import com.towsal.towsal.viewmodel.CarDetailViewModel
import com.towsal.towsal.viewmodel.ProfileViewModel
import com.towsal.towsal.views.adapters.CarTypesAdapter
import com.towsal.towsal.views.adapters.ReviewsAdapter
import com.towsal.towsal.views.adapters.SearchCarAdapter
import com.towsal.towsal.views.fragments.SearchHomeFragment
import com.towsal.towsal.views.listeners.OnEvent
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.Serializable

/**
 * Activity class for user profile screen
 * */
class UserProfileActivity : BaseActivity(), View.OnClickListener, OnNetworkResponse {

    lateinit var binding: ActivityUserProfileBinding
    private val profileViewModel: ProfileViewModel by viewModel()
    private val carDetailViewModel: CarDetailViewModel by viewModel()
    var userId: Int = -1
    var model = UserDetails()
    var review = ""
    var rating = 0f
    private var lat = 0.0
    private var lng = 0.0
    private var cityModel = CitiesListModel()
    private var position: Int = -1


    private var locationActivityLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            (activity as BaseActivity).showLoaderDialog()
            Handler(Looper.getMainLooper()).postDelayed({
                hideLoaderDialog()
                prepareApiRequest()
            }, 5000)
        }

    private fun prepareApiRequest() {
        val cityId = preferenceHelper.getIntOther(Constants.DataParsing.CITY_SELECTED_ID, 0)
        lat = 0.0
        lng = 0.0

        try {
            if (uiHelper.isPermissionAllowed(
                    this,
                    PERMISSION
                ) && cityId == 0
            ) {

                if (gpsTracker!!.checkGPsEnabled()) {
                    lat = gpsTracker?.getLatitude() ?: 0.0
                    lng = gpsTracker?.getLongitude() ?: 0.0
                    callUserDetailsApi(0)
                } else {
                    showEnableLocationSetting(locationActivityLauncher)
                }
            } else {
                if (cityId == 0)
                    profileViewModel.getCitiesList(
                        true,
                        Constants.API.CITIES_LIST,
                        this
                    )
                else {
                    callUserDetailsApi(cityId)
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            e.printStackTrace()
            Log.e("setData: ", e.message ?: "")
        }
    }

    private fun callUserDetailsApi(cityId: Int) {
        profileViewModel.getUserDetails(
            userId,
            cityId,
            lat,
            lng,
            true,
            Constants.API.GET_USER_DETAILS,
            this
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_profile)
        binding.clMainRoot.isVisible = false
        userId = intent.getIntExtra(Constants.DataParsing.USER_ID, -1)
        uiHelper.hideActionBar(supportActionBar)

        binding.activity = this

        prepareApiRequest()
    }

    /**
     * Function for click listeners
     * */
    override fun onClick(v: View?) {
        v?.preventDoubleClick()
        when (v) {
            binding.viewAllGuestReviews -> {
                openActivity(model.guestsReviews, resources.getString(R.string.reviews_from_guests))
            }
            binding.viewAllHostReviews -> {
                openActivity(model.hostReviews, resources.getString(R.string.reviews_from_hosts))
            }

            binding.llFavourites -> {
                val bundle = Bundle()
                bundle.putInt(Constants.DataParsing.USER_ID, userId)
                uiHelper.openActivity(this, FavoriteListActivity::class.java, bundle)

            }
        }
    }

    /**
     * Function for opening activity
     * */
    private fun openActivity(reviewsList: List<Reviews>, heading: String) {
        val bundle = Bundle()
        bundle.putSerializable(Constants.DataParsing.REVIEWS_LIST, reviewsList as Serializable)
        bundle.putString(Constants.DataParsing.HEADING, heading)
        bundle.putString(Constants.DataParsing.REVIEW, review)
        bundle.putFloat(Constants.DataParsing.RATING, rating)
        uiHelper.openActivity(this, RatingAndReviewsActivity::class.java, bundle)
    }

    override fun onSuccess(response: BaseResponse?, tag: Any?) {
        when (tag) {
            Constants.API.GET_USER_DETAILS -> {
                model = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    UserDetails::class.java
                )
                binding.clMainRoot.isVisible = true
                setData()
            }

            Constants.API.CITIES_LIST -> {
                cityModel = Gson().fromJson(
                    uiHelper.jsonConverterObject(response?.dataObject as? LinkedTreeMap<*, *>),
                    CitiesListModel::class.java
                )
                if (cityModel.cityList.isNotEmpty()) {
                    profileViewModel.showCitySelectionPopup(
                        this,
                        object : PopupCallback {
                            override fun popupButtonClick(value: Int) {
                                super.popupButtonClick(value)
                                if (cityModel.cityList.isNotEmpty()) {
                                    preferenceHelper.setIntOther(
                                        Constants.DataParsing.CITY_SELECTED_ID,
                                        cityModel.cityList[value].id
                                    )
                                    preferenceHelper.setString(
                                        Constants.DataParsing.CITY_SELECTED,
                                        cityModel.cityList[value].city
                                    )
                                    preferenceHelper.setLong(
                                        Constants.DataParsing.CITY_LAT,
                                        cityModel.cityList[value].lat.toLong()
                                    )
                                    preferenceHelper.setLong(
                                        Constants.DataParsing.CITY_LNG,
                                        cityModel.cityList[value].lng.toLong()
                                    )
                                    lat = cityModel.cityList[value].lat
                                    lng = cityModel.cityList[value].lng
                                    callUserDetailsApi(cityModel.cityList[value].id)
                                }
                                profileViewModel.dismissCitySelectionDialog()
                            }
                        },
                        ArrayList(cityModel.cityList.map {
                            it.city
                        })
                    )
                }
            }


            Constants.API.DELETE_FAV_CAR -> {
                model.topCars[position].carDetailInfo.isFavourite = 0
                (binding.rvTopVehicles.adapter as CarTypesAdapter).notifyItemChanged(position)
            }
            Constants.API.ADD_FAV_CAR -> {
                model.topCars[position].carDetailInfo.isFavourite = 1
                (binding.rvTopVehicles.adapter as CarTypesAdapter).notifyItemChanged(position)
            }
        }
    }

    /**
     * Function for setting up data in views
     * */
    private fun setData() {
        binding.tvResponseTime.isVisible = model.isHost == 1
        binding.tvResponseTimeTitle.isVisible = model.isHost == 1
        binding.toolBar.setAsGuestToolBar(
            R.string.rating_and_review,
            supportActionBar
        )
        uiHelper.glideLoadImage(
            this,
            BuildConfig.IMAGE_BASE_URL + model.userProfileImage,
            binding.profileImage
        )
        if (model.userAverageRating == "0.0"){
            binding.tvNoRating.isVisible = true
            binding.tvRating.isVisible = false
        }else{
            binding.tvNoRating.isVisible = false
            binding.tvRating.text = model.userAverageRating.toString()
        }

        binding.userName.text = model.userName /*(model.userName?: "").split(" ")[0]*/
        binding.clTopVehilces.isVisible = model.activeCarsCount != 0
        binding.tvViewAllVehicles.isVisible = model.activeCarsCount > 2
        binding.tvTopVehicles.text = getString(R.string.top_vehicles, (model.userName?: "").split(" ")[0])
        binding.rvTopVehicles.apply {
            layoutManager = LinearLayoutManager(this@UserProfileActivity, LinearLayoutManager.VERTICAL, false)
            adapter = CarTypesAdapter(
                ArrayList(model.topCars),
                this@UserProfileActivity,
                uiHelper,
                object : OnEvent {
                    override fun openDetailsActivity(id: Int, position: Int) {
//                        val bundle = Bundle()
//                        this@UserProfileActivity.position = position
//                        val cityId =
//                            PreferenceHelper().getIntOther(Constants.DataParsing.CITY_SELECTED_ID, 0)
//                        if (uiHelper.isPermissionAllowed(
//                                this@UserProfileActivity,
//                                PERMISSION
//                            ) && cityId == 0
//                        ) {
//                            if (!gpsTracker!!.checkGPsEnabled()) {
//                                showEnableLocationSetting(locationActivityLauncher)
//                            }
//                            lat = gpsTracker!!.getLatitude()
//                            lng = gpsTracker!!.getLongitude()
//                        }
//                        bundle.putString(
//                            Constants.DataParsing.ACTIVITY_NAME,
//                            SearchHomeFragment::javaClass.name
//                        )
//                        bundle.putDouble(Constants.LAT, lat)
//                        bundle.putDouble(Constants.LNG, lng)
//                        bundle.putInt(Constants.CITY_ID, cityId)
//                        bundle.putInt(Constants.CAR_ID, id)
//                        uiHelper.openActivityForResult(
//                            this@UserProfileActivity,
//                            CarDetailsActivity::class.java,
//                            bundle,
//                            Constants.RequestCodes.ACTIVITY_CAR_DETAIL
//                        )
                    }

                    override fun markFavourite(id: Int, position: Int) {
                        this@UserProfileActivity.position = position
                        if (model.topCars[position].carDetailInfo.isFavourite == 1) {
                            carDetailViewModel.deleteFavorite(
                                id,
                                true,
                                Constants.API.DELETE_FAV_CAR,
                                this@UserProfileActivity
                            )
                        } else {
                            carDetailViewModel.addFavorite(
                                id,
                                true,
                                Constants.API.ADD_FAV_CAR,
                                this@UserProfileActivity
                            )
                        }
                    }

                },
                1
            )
        }
        val joinDate = DateUtil.changeDateFormat(
            Constants.ServerDateFormat,
            Constants.MonthYearFormat,
            model.userJoinDate
        )
        binding.tvFavorities.text = model.userName!!.split(" ")[0] + "'" + "S" + " " + "favourites"
        binding.tvJoined.text = joinDate
        binding.tvResponseTime.text =
            if (model.avgResponseTime.isNotEmpty()) "~ ${model.avgResponseTime}" else ""

        binding.tvTotalTrips.text = model.userTrips.toString()
        binding.containerGuestReviews.isVisible = model.guestsReviews.isNotEmpty()
        if (model.guestsReviews.isNotEmpty()) {
            rating = model.guestAverageRating?.toFloat() ?: 0f
            review = " ${model.guestsReviews.size} ${this.resources.getString(R.string.ratings)}"
        } else {
            rating = model.hostAverageRating?.toFloat() ?: 0f
            review = " ${model.hostReviews.size} ${this.resources.getString(R.string.ratings)}"
        }
        binding.rbGuestRatings.rating = model.guestAverageRating?.toFloat() ?: 0f
        binding.tvGuestRatings.text =
            " ${model.guestsReviews.size} ${this.resources.getString(R.string.ratings)}"

        binding.viewAllHostReviews
        binding.viewAllGuestReviews.isVisible =
            binding.containerGuestReviews.isVisible

        Log.d("guestsReviews" , model.guestsReviews.size.toString())
        binding.viewAllGuestReviews.isVisible = model.guestsReviews.size > 1

        binding.guestReviewsRecyclerview.apply {
            adapter = ReviewsAdapter(
                this@UserProfileActivity,
                model.guestsReviews.take(2),
                uiHelper
            )
        }
        binding.containerHostReviews.isVisible = model.hostReviews.isNotEmpty()
        binding.rbHostRatings.rating = model.hostAverageRating?.toFloat() ?: 0f
        binding.tvHostRatings.text =
            " ${model.hostReviews.size} ${this.resources.getString(R.string.ratings)}"

        binding.viewAllHostReviews.isVisible =
            binding.containerHostReviews.isVisible
        binding.ivAllStar.isInvisible = model.hasAllStars == Constants.HostAllStarStatus.NO
        Log.d("guestsReviews" , model.hostReviews.size.toString())

        binding.viewAllHostReviews.isVisible = model.hostReviews.size > 1
        binding.hostReviewsRecyclerview.apply {
            adapter = ReviewsAdapter(
                this@UserProfileActivity,
                model.hostReviews.take(2),
                uiHelper
            )
        }
    }

    override fun onFailure(response: BaseResponse?, tag: Any?) {
        uiHelper.showLongToastInCenter(this, response?.message)
    }

}