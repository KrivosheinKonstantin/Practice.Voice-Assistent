package com.example.voiceassistent.parsing

import android.content.Context
import android.util.Log
import com.example.voiceassistent.AI
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.*
import java.util.*

object ParsingHtmlServiceHolidays {
    //private static String URL = "http://mirkosmosa.ru/holiday/";
    private const val ERROR_MESSAGE = "Ошибка получения данных с сайта. Попробуйте позже."
    private const val NULL_ANSWER_MESSAGE = "Не удалось найти дату."
    private const val MESSAGE_NO_HOLIDAY = "Нет праздника"
    //@kotlin.jvm.JvmStatic
    fun getHoliday(date: String, ctx: Context): List<String> {
        var URL: String? = "http://mirkosmosa.ru/holiday/"
        val resultListHolidays: MutableList<String> = ArrayList()
        val year = getYear(date)
        val month = getMonth(date)
        var document = readDocument(ctx, year)
        URL += year
        if (document == null) {
            document = try {
                Log.d("Jsoup", "connect")
                Jsoup.connect(URL).get()
                //saveDocument(ctx, year, document); //если документ не сохранен - записываем его
            } catch (ex: Exception) {
                resultListHolidays.add(ERROR_MESSAGE)
                return resultListHolidays //не удалось скачать документ
            }
        }
        val body = document!!.body()
        val elements = body.getElementsByClass("holiday_month")
        var i = 0
        while (i < elements.size) {
            //ищем нужный месяц
            val element = elements[i]
            val holidaysOnMonth = element.select("h3.div_center").text()
            val dateString = "Праздники в $month $year года"
            if (holidaysOnMonth == dateString) { //если месяц найден
                val el2 = element.getElementsByClass("next_phase")
                for (j in el2) { //идем по дням месяца
                    val element1 = j
                    val dateOnMonth = element1.selectFirst("div").selectFirst("span").text()
                    if (dateOnMonth == date) { //сравниваем имеющие даты
                        val el3 = element1.select("a")
                        if (el3.size == 0) resultListHolidays.add(MESSAGE_NO_HOLIDAY) else {
                            for (k in el3) {
                                resultListHolidays.add(k.text())
                            }
                        }
                        break
                    }
                }
                break
            }
            i += 2
        }
        if (resultListHolidays.size == 0) resultListHolidays.add(NULL_ANSWER_MESSAGE)
        return resultListHolidays
    }

    private fun getMonth(date: String): String { //преобразуем
        val words: Array<String> = date.split(" ").toTypedArray()
        words[1] = AI.deleteLastSymbol(words[1]) + "е"
        return words[1]
    }

    private fun getYear(date: String): String {
        val words: Array<String> = date.split(" ").toTypedArray()
        return words[2]
    }

    private fun saveDocument(ctx: Context, year: String, document: Document): Boolean {
        try {
            val file = File(year)
            if (!file.exists()) {
                val bw = BufferedWriter(OutputStreamWriter(
                        ctx.openFileOutput(year, Context.MODE_PRIVATE)))
                bw.write(document.toString())
                bw.close()
                Log.i("Parsing", "Document saved")
                return true
            } else {
                Log.i("Parsing", "Document exists")
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("Parsing saveDoc", "Error=$e")
        }
        return false
    }

    private fun readDocument(ctx: Context, year: String): Document? {
        try {
            val br = BufferedReader(InputStreamReader(
                    ctx.openFileInput(year)))
            val document: Document
            val documentAsString = StringBuilder()
            for (line in br.lines()){
                documentAsString.append(line);
            }
            document = Jsoup.parseBodyFragment(documentAsString.toString())
            Log.i("Parsing readDoc", "Document read")
            return document
        } catch (e: IOException) {
            Log.e("Parsing readDoc", "Error=$e")
        }
        return null
    }
}

