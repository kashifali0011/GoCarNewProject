package com.towsal.towsal.network.serializer.userdetails

import com.google.gson.annotations.Expose

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Response model for reviews given to user
 * */
class Reviews : Serializable{
    @SerializedName("rating_review_by")
    @Expose
    val ratingReviewBy: String? = null

    @SerializedName("rating_review_by_image")
    @Expose
    val ratingReviewByImage: String? = null

    @SerializedName("rating")
    @Expose
    val rating: Float? = null

    @SerializedName("review")
    @Expose
    val review: String? = null

    @SerializedName("created_at")
    @Expose
    val createdAt: String? = null

    @SerializedName("user_country")
    @Expose
    val reviewerCountry: String? = null
}