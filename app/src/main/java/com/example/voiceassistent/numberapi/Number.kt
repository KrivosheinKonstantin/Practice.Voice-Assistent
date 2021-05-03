package com.example.voiceassistent.numberapi

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Number : Serializable {
    //@kotlin.jvm.JvmField
    @SerializedName("str")
    @Expose
    var current: String? = null
}