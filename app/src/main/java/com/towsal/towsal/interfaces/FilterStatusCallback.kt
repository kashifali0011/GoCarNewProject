package com.towsal.towsal.interfaces

import com.towsal.towsal.network.serializer.trips.FilterTripsModel

/**
 * Interface for filtering trips
 * */
interface FilterStatusCallback {
    fun statusSelected(filterStatusList:ArrayList<FilterTripsModel>)
}