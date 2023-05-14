package com.towsal.towsal.network.serializer.userdetails

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.home.CarHomeModel
import com.towsal.towsal.utils.Constants
import java.io.Serializable

/**
 * Response model for user details
 * */
class UserDetails : Serializable {
    @SerializedName(Constants.DataParsing.IS_HOST)
    val isHost: Int = null ?: 0

    @SerializedName("user_name")
    @Expose
    val userName: String? = null

    @SerializedName("user_join_date")
    @Expose
    val userJoinDate: String? = null

    @SerializedName("user_country")
    @Expose
    val userCountry: String? = null

    @SerializedName("user_profile_image")
    @Expose
    val userProfileImage: String? = null

    @SerializedName("user_trips")
    @Expose
    val userTrips: Int? = null

    @SerializedName("user_average_rating")
    @Expose
    val userAverageRating: String? = null?: ""

    @SerializedName("host_average_rating")
    @Expose
    val hostAverageRating: Int? = null

    @SerializedName("guest_average_rating")
    @Expose
    val guestAverageRating: String? = null?: ""

    @SerializedName("guests_reviews")
    @Expose
    val guestsReviews: List<Reviews> = null ?: ArrayList()

    @SerializedName("host_reviews")
    @Expose
    val hostReviews: List<Reviews> = null ?: ArrayList()

    @SerializedName("host_avg_response_time")
    @Expose
    val avgResponseTime: String = null ?: ""

    @SerializedName("has_all_stars")
    @Expose
    val hasAllStars: Int? = null ?: 0

    @SerializedName("active_cars")
    @Expose
    val activeCarsCount: Int = null ?: 0
    @SerializedName("top_cars")
    val topCars: List<CarHomeModel> = null ?: emptyList()

}