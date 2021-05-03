package com.example.voiceassistent.numberapi

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NumberService {
    //Базовая часть адреса
    //Конвертер, необходимый для преобразования JSON'а в объекты
    //Создание объекта, при помощи которого будут выполняться запросы
    //@kotlin.jvm.JvmStatic
    fun getApi(): NumberApi?
    {
        val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://htmlweb.ru") //Базовая часть адреса
                .build()
        Log.i("NumberService", "builder received")
        return retrofit.create(NumberApi::class.java)
    }
}


//https://habr.com/ru/post/336034/
//retrofit.create(GithubApiService::class.java);
