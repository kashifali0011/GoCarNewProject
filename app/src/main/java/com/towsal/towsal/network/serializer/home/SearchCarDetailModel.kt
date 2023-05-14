package com.towsal.towsal.network.serializer.home

import android.media.Image
import android.widget.ImageView
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.google.maps.android.clustering.ClusterItem
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for searched car details
 * */
class SearchCarDetailModel() : Serializable, ClusterItem {


    @SerializedName(DataKeyValues.Home.IMAGE_URL)
    @Expose
    var url: String = null ?: ""

    @SerializedName(DataKeyValues.NAME)
    @Expose
    var name: String = null ?: ""

    @SerializedName(DataKeyValues.Home.MODEL)
    @Expose
    var model: String = null ?: ""

    @SerializedName(DataKeyValues.Home.YEAR)
    @Expose
    var year: String = null ?: ""

    @SerializedName(DataKeyValues.Home.RATING)
    @Expose
    var rating: Double = null ?: 0.0

    @SerializedName(DataKeyValues.Home.TRIPS)
    @Expose
    var trips: Int = null ?: 0

    @SerializedName(DataKeyValues.Home.PRICE)
    @Expose
    var price: String = null ?: ""

    @SerializedName(DataKeyValues.Home.DISTANCE)
    @Expose
    var distance: String = null ?: "0"

    @SerializedName(DataKeyValues.CarDetails.FAV_FLAG)
    @Expose
    var fav_flag: Int = null ?: 0

    @SerializedName(DataKeyValues.CarDetails.IS_DELIVERY)
    @Expose
    var is_delivery: Int = null ?: 0

    @SerializedName(DataKeyValues.CarDetails.IS_PICKUP_RETURN)
    @Expose
    var is_pickup_return: Int = null ?: 0

    @SerializedName(DataKeyValues.CarDetails.DELIVERY_STATUS)
    @Expose
    var delivery_status: Int = null ?: 0

    @SerializedName(DataKeyValues.Home.ID)                  //it was DataKeyValues.ID
    @Expose
    var id: Int = null ?: 0

    @SerializedName(DataKeyValues.CarDetails.PIN_LONG)
    @Expose
    var pin_long: Double = null ?: 0.0

    @SerializedName(DataKeyValues.CarDetails.PIN_LAT)
    @Expose
    var pin_lat: Double = null ?: 0.0

    @SerializedName(DataKeyValues.Home.PIN_ADDRESS)
    @Expose
    var pin_address: String = null ?: ""


    @SerializedName(DataKeyValues.CarDetails.IS_BOOK_IMMEDIATELY)
    @Expose
    var is_book_immediately: Int = null ?: 0
    var lat = 0.0
    var lng = 0.0
    var title_ = ""
    var snippet_ = ""
    var latLng = LatLng(0.0, 0.0)
    var markerId = 0
    var icon = ""

    constructor(
        lat_: Double,
        lng_: Double,
        title_: String,
        snippet_: String,
        icon_: String,
        rating: Double,
        distance:String,
        id: Int
    ) : this(
    ) {

        this.lat = lat_
        this.lng = lng_
        this.snippet_ = snippet_
        this.icon = icon_
        this.title_ = title_
        this.latLng = LatLng(this.lat, this.lng)
        this.rating = rating
        this.distance = distance
        this.markerId = id

    }

    override fun getPosition(): LatLng = latLng


    override fun getTitle(): String = title_

    override fun getSnippet(): String = snippet_

}