package com.example.voiceassistent.numberapi

import io.reactivex.rxjava3.core.Observable
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NumberApi {
    @GET("json/convert/num2str?dec=0")
    fun getCurrentWeather(@Query("num") num: String?): Call<Number>
}