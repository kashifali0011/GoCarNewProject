package com.towsal.towsal.network.serializer.profile

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.towsal.towsal.network.serializer.DataKeyValues
import java.io.Serializable

/**
 * Response model for holding user account information
 * */
class GetAccountResponseModel : Serializable {
    @SerializedName(DataKeyValues.Profile.ACCOUNT)
    @Expose
    var account: GetAccountModel = null ?: GetAccountModel()
}