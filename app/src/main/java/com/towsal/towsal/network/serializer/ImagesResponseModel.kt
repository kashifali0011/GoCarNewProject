package com.towsal.towsal.network.serializer

import com.google.gson.annotations.SerializedName

class ImagesResponseModel {
    @SerializedName("images_name")
    val imagesList: List<String> = null ?: emptyList()
}