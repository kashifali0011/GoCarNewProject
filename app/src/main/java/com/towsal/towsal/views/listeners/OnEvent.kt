package com.towsal.towsal.views.listeners

/**
 * Interface for making car as favourite and opening car details screen
 * */
interface OnEvent {
    fun openDetailsActivity(id:Int, position: Int)
    fun markFavourite(id:Int, position: Int)
}
