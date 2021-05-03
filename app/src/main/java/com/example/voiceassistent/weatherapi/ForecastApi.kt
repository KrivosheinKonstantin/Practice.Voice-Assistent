package com.example.voiceassistent.weatherapi

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ForecastApi {
    @GET("/current?access_key=32622875b412da159ef6b62847955664")
    fun getCurrentWeather(@Query("query") city: String?): Call<Forecast?>?
}