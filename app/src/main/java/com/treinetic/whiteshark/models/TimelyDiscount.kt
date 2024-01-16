package com.treinetic.whiteshark.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
@Keep
class TimelyDiscount {
    @SerializedName("promotions_id")
    var promotions_id:String?=null
    @SerializedName("discount")
    var discount:Double?=null
    @SerializedName("from")
    var from:String?=null
    @SerializedName("to")
    var to:String?=null
    @SerializedName("status")
    var status:String?=null

}
