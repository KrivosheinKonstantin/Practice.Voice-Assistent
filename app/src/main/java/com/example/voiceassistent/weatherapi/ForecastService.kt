package com.example.voiceassistent.weatherapi

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ForecastService {
    //Базовая часть адреса
    //Конвертер, необходимый для преобразования JSON'а в объекты
    //Создание объекта, при помощи которого будут выполняться запросы
    //@kotlin.jvm.JvmStatic
    fun getApi(): ForecastApi?
    {
        val retrofit = Retrofit.Builder()
                .baseUrl("http://api.weatherstack.com") //Базовая часть адреса
                .addConverterFactory(GsonConverterFactory.create()) //Конвертер, необходимый для преобразования JSON'а в объекты
                .build()
        return retrofit.create(ForecastApi::class.java) //Создание объекта, при помощи которого будут выполняться запросы
    }
}