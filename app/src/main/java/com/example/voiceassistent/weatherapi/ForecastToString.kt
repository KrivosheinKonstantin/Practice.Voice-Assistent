package com.example.voiceassistent.weatherapi

import android.util.Log
import androidx.core.util.Consumer
import com.example.voiceassistent.AI
import com.example.voiceassistent.translateapi.Languages
import com.example.voiceassistent.translateapi.TranslateAPI.getTranslate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object ForecastToString {
   // @kotlin.jvm.JvmStatic
    fun getForecast(city: String?, callback: Consumer<String?>) {
        val api: ForecastApi? = ForecastService.getApi()
        val call = api?.getCurrentWeather(city)
        call!!.enqueue(object : Callback<Forecast?> {
            override fun onResponse(call: Call<Forecast?>, response: Response<Forecast?>) {
                //String symbol = "!";
                val result = response.body()
                Log.i("ForecastToString", "onResponse")
                if (result!!.current != null) {
                    val region = result.region!!.region
                    val descriptions = result.current!!.weather_descriptions!![0]
                    val textToTranslate = ("In " + region
                            + " it is now about " + result.current!!.temperature + " degrees Celsius"
                            + ", " + descriptions)
                    getTranslate(Languages.ENGLISH.languageCode, Languages.RUSSIAN.languageCode, textToTranslate, Consumer<String> { s ->
                        if (s == AI.TRANSLATE_ERROR) {
                            val answer = ("В " + region
                                    + " сейчас где-то " + result.current!!.temperature + " "
                                    + AI.getWord(result.current!!.temperature!!, "градусов", "градус", " градуса")
                                    + " и " + descriptions)
                            callback.accept(answer)
                        } else {
                            //Добавить api для получения падежей
                            //https://htmlweb.ru/service/sklonjator.php

                            /*String[] listResult = s.split(symbol);
                                            String answer = "В " + listResult[0]
                                                    + " сейчас где-то " + result.current.temperature  + " "
                                                    + AI.getWord(result.current.temperature, "градусов", "градус", " градуса")
                                                    + " и " + listResult[1].substring(1).toLowerCase();*/
                            callback.accept(s)
                        }
                    })
                } else {
                    Log.e("ForecastToString", "Empty answer")
                    callback.accept(AI.WEATHER_ERROR)
                }
            }

            override fun onFailure(call: Call<Forecast?>, t: Throwable) {
                Log.e("ForecastToString", "onFailure=" + t.message)
            }
        })
    }
}