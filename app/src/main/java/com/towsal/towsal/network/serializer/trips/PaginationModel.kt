package com.towsal.towsal.network.serializer.trips

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for pagination of data
 * */
class PaginationModel : Serializable {

    @SerializedName(DataKeyValues.Trips.TOTAL_PAGES)
    @Expose
    var totalPages: Int = null ?: 0

    @SerializedName(DataKeyValues.Trips.CURRENT_PAGE)
    @Expose
    var currentPage: Int = null ?: 0

    @SerializedName(DataKeyValues.Trips.NEXT_PAGE)
    @Expose
    var nextPage: Int = null ?: 0

    @SerializedName(DataKeyValues.Trips.NUM_OF_ITEMS)
    @Expose
    var numOfItems: Int = null ?: 0


}