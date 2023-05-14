package com.towsal.towsal.network.serializer.carlist

import java.io.Serializable

/**
 * Model class using for view for steps
 * */
class StepViewModel : Serializable {
    var number: Int = null ?: 0
    var selected: Boolean = null ?: false
}