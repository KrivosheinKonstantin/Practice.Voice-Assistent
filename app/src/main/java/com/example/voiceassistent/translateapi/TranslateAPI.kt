package com.example.voiceassistent.translateapi

import android.content.ContentValues
import android.util.Log
import androidx.core.util.Consumer
import com.example.voiceassistent.AI
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object TranslateAPI {
    const val ERROR = AI.TRANSLATE_ERROR
    //@kotlin.jvm.JvmStatic
    fun getTranslate(langFrom: String, langTo: String, text: String?, callback: Consumer<String>) {
        Observable.fromCallable {
            var resp: String? = null
            try {
                val url = "https://translate.googleapis.com/translate_a/single?" + "client=gtx&" + "sl=" +
                        langFrom + "&tl=" + langTo + "&dt=t&q=" + URLEncoder.encode(text, "UTF-8")
                val obj = URL(url)
                val con = obj.openConnection() as HttpURLConnection
                con.setRequestProperty("User-Agent", "Mozilla/5.0")
                val `in` = BufferedReader(InputStreamReader(con.inputStream))
                var inputLine: String?
                val respons = StringBuffer()
                for (line in `in`.lines()){
                    respons.append(line);
                }
                `in`.close()
                resp = respons.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            var temp = ""
            if (resp == null) {
                return@fromCallable ERROR
            } else {
                try {
                    val main = JSONArray(resp)
                    val total = main[0] as JSONArray

                    var j = 0;
                    while (j < total.length()){
                        val currentLine = total[j] as JSONArray
                        temp += currentLine[0].toString()
                        j++;
                    }
                    Log.d(ContentValues.TAG, "onPostExecute: $temp")
                    if (temp.length > 2) {
                        return@fromCallable temp
                    } else {
                        return@fromCallable ERROR
                    }
                } catch (e: JSONException) {
                    return@fromCallable ERROR
                }
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { listResult: String? -> callback.accept(java.lang.String.join(", ", listResult)) }
    }
}