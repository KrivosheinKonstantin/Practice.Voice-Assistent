package com.example.voiceassistent

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.util.Log
import androidx.core.util.Consumer
import com.example.voiceassistent.numberapi.NumberToString.getNumber
import com.example.voiceassistent.parsing.ParsingHtmlServiceHolidays.getHoliday
import com.example.voiceassistent.parsing.ParsingHtmlServiceWiki.getDescription
import com.example.voiceassistent.weatherapi.ForecastToString.getForecast
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.*
import java.util.regex.Pattern
import kotlin.text.*
import java.lang.*

object AI {
    /**
     * da6f447934a5da1b5cb4ca2d4313cda1
     * http://api.weatherstack.com/current?access_key=da6f447934a5da1b5cb4ca2d4313cda1&query=%D0%9A%D0%B8%D1%80%D0%BE%D0%B2
     * 0966eb0edb7571b52b40233d6f471892
     * Задачи:
     * расчитать кол-во дней до даты (есть 2 даты - текущая и нужная(посчитать не была ли она уже))
     * лабораторная работа 6 - не сохраняет ласт сообщение
     */
    const val STRING_EMPTY = ""
    const val DEFAULT_ANSWER = "Ничего не нашлось. Совсем ничего. Попробуйте немного изменить вопрос."
    const val WEATHER_SEARCH = "Хм... Изучаю погоду."
    const val WEATHER_ERROR = "Не могу узнать погоду. Попробуйте ввести названиие города в именительном падеже."
    const val NUMBER_GETTING_ERROR = "Ошибка получения."
    const val NUMBER_ERROR = "Не удалось получить число. Попробуйте снова."
    const val TRANSLATE_ERROR = "Ошибка перевода."
    const val MAX_LENGTH_MESSAGE = 150
    var dictionaryCode: MutableMap<String?, Int>? = null
    var helloPhrase = Arrays.asList("привет", "хеллоу", "бонжур", "только вас вспоминал", "я тут", "я здесь")
    var howAreYouPhrase = Arrays.asList("не плохо", "отлично", "отлично, приятно, что интересуетесь")
    var WYDphrases = Arrays.asList("отвечаю на вопросы", "перечитываю статьи в Википедии")

    /**
     * Получить ответ на вопрос
     * @param question вопрос
     * @param callback функциональный ответ
     * @param ctx контекст, передавать mainActivity для работы с файлами
     */

    fun getAnswer(question: String, callback: Consumer<String?>, ctx: Context?) {
        val retAnswer: MutableList<String> = ArrayList()
        val answerDir: MutableMap<Int, String?> = TreeMap()
        for ((key, value) in dictionaryCode!!) {
            val index: Int? = key?.let { question.indexOf(it) }
            if (index != -1) {
                var conStr = ""
                when (value) {
                    0 -> {
                        conStr += getDay(0)
                        Log.i("AI", "Today=$conStr")
                    }
                    1 -> {
                        conStr += currentTime
                        Log.i("AI", "CurrentTime=$conStr")
                    }
                    2 -> {
                        conStr += dayOfWeek
                        Log.i("AI", "DayOfWeek=$conStr")
                    }
                    3 -> {
                        conStr += getNumberOfDaysToDate(question)
                        Log.i("AI", "getNumberOfDaysToDate=$conStr")
                    }
                    4 -> {
                        conStr += randomPhrase(helloPhrase)
                        Log.i("AI", "random answer=$conStr")
                    }
                    5 -> {
                        conStr += randomPhrase(howAreYouPhrase)
                        Log.i("AI", "random answer=$conStr")
                    }
                    6 -> {
                        conStr += randomPhrase(WYDphrases)
                        Log.i("AI", "random answer=$conStr")
                    }
                    7 -> {
                        val cityPattern = Pattern.compile("погода в (городе)?(\\p{L}+[- ]*\\p{L}*[- ]*\\p{L}*)",
                                Pattern.CASE_INSENSITIVE)
                        val cityMatcher = cityPattern.matcher(question)
                        if (cityMatcher.find()) {
                            var cityName = cityMatcher.group()
                            cityName = cityName.replace("погода в ".toRegex(), "")
                            cityName = cityName.replace("городе ".toRegex(), "")
                            while (cityName[cityName.length - 1] == ' ') {
                                cityName = deleteLastSymbol(cityName)
                            }
                            if (cityName[cityName.length - 1] == 'е') {
                                cityName = deleteLastSymbol(cityName)
                            }
                            Log.i("AI", "Weather city=$cityName")
                            conStr += WEATHER_SEARCH
                            getForecast(cityName, Consumer<String?> { answer ->
                                Log.i("AI", "Weather answer=$answer")
                                callback.accept(java.lang.String.join(", ", answer))
                            })
                        }
                    }
                    8 -> {
                        val numberPattern = Pattern.compile("число -?(\\d+)",
                                Pattern.CASE_INSENSITIVE)
                        val numberMatcher = numberPattern.matcher(question)
                        if (numberMatcher.find()) {
                            val number: String = numberMatcher.group().replace("число ", "")
                            Log.i("AI", "Number=$number")
                            if (isNumeric(number)) {
                                conStr += "Число $number будет..."
                                val sing = number[0] == '-'
                                getNumber(number, Consumer<String?> { s ->
                                    var s = s
                                    if (sing && s != NUMBER_GETTING_ERROR) s = "минус $s"
                                    Log.i("AI", "Number answer=$s")
                                    if (s.length > MAX_LENGTH_MESSAGE) {
                                        val list = transformAnswer(s)
                                        list.forEach { el: String? -> callback.accept(java.lang.String.join(", ", el)) }
                                    } else callback.accept(java.lang.String.join(", ", s))
                                })
                            } else {
                                conStr += NUMBER_ERROR
                            }
                        }
                    }
                    9 -> {
                        val dateList = getDate(question, Pattern.compile("праздник ((0?[1-9]|[12][0-9]|3[01]) (янв(?:аря)?|фев(?:раля)?|мар(?:та)?|апр(?:еля)?|мая|июн(?:я)?|июл(?:я)?|авг(?:уста)?|сен(?:тября)?|окт(?:ября)?|ноя(?:бря)?|дек(?:абря)?) \\d{4})?(сегодня?|завтра?|вчера?)?",
                                Pattern.CASE_INSENSITIVE), "праздник ")
                        if (dateList.size == 1) {
                            conStr += "Ищу праздник..."
                        } else if (dateList.size > 1) {
                            conStr += "Ищу праздники..."
                        }
                        if (dateList.size > 0) {
                            Observable.fromCallable<List<String>> {
                                val answer: MutableList<String> = ArrayList()
                                var i = 0
                                while (i < dateList.size) {
                                    Log.i("AI", "dateList=" + dateList[i])
                                    val answerList = getHoliday(dateList[i], ctx!!)
                                    val answerString = StringBuilder()
                                    var j = 0
                                    while (j < answerList.size) {
                                        answerString.append(answerList[j]).append(". ")
                                        ++j
                                    }
                                    answer.add(dateList[i] + " - " + answerString.toString())
                                    ++i
                                }
                                answer
                            }.subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe { result: List<String> -> result.forEach { el: String? -> callback.accept(java.lang.String.join(", ", el)) } }
                        }
                    }
                    10 -> {
                        val pattern = Pattern.compile("(что такое)?(кто такой)? \\w+", Pattern.CASE_INSENSITIVE)
                        val matcher = pattern.matcher(question)
                        while (matcher.find()) {
                            var text = matcher.group()
                            if (text.length > 10) {
                                text = text.substring(10)
                                val finalText = text
                                conStr += "$finalText? Попробую найти."
                                val stringObservable = Observable.fromCallable {
                                    val answer = getDescription(finalText)
                                    answer
                                }
                                stringObservable.subscribeOn(Schedulers.io())
                                stringObservable.observeOn(AndroidSchedulers.mainThread())
                                stringObservable.subscribe { result: String ->
                                    Log.e(result, Integer.toString(result.length))
                                    if (result.length > MAX_LENGTH_MESSAGE) {
                                        val stringList = transformAnswer(result)
                                        var i = 0
                                        while (i < stringList.size) {
                                            callback.accept(java.lang.String.join(", ", stringList[i]))
                                            ++i
                                        }
                                    } else callback.accept(java.lang.String.join(", ", result))
                                }
                            }
                        }
                    }
                    else -> Log.e("AI", "Error. Case $value")
                }
                if (index != null) {
                    answerDir.put(index, conStr)
                }
            }
        }
        var ret = STRING_EMPTY
        for ((_, value) in answerDir) {
            if (value != null) {
                if (!value.isEmpty()) {
                    if (ret != STRING_EMPTY && (ret + value).length >= MAX_LENGTH_MESSAGE) {
                        retAnswer.add(ret)
                        ret = STRING_EMPTY
                    }
                    if (value != STRING_EMPTY) ret += "$value. "
                }
            }
        }
        retAnswer.add(ret)

        var i = 0
        while (i < retAnswer.size){
            val callStr = if (retAnswer[i].isEmpty()) DEFAULT_ANSWER else changeStringWithPunc(retAnswer[i])
            callback.accept(java.lang.String.join(", ", callStr))
            Log.e("AI", callback.toString())
            i++
        }
    }

    /**
     * Преобразование ответа к виду "как в предложении"
     * @param str исходная строка
     * @return
     */
    fun changeStringWithPunc(str: String?): String {
        val stringBuffer = StringBuilder(str)
        var t = false
        val symbols = ".?!"
        var i = 0
        while (i < stringBuffer.length){
            val elem = stringBuffer[i]
            if (!t && Character.isAlphabetic(elem.toInt())) { //пред символ знак и элемент - буква
                stringBuffer.setCharAt(i, Character.toUpperCase(elem)) //Заглавный
                t = true
            } else if (symbols.contains(elem.toString())) //если знак, запоминаем
                t = false
            i++
        }
        return stringBuffer.toString()
    }

    /**
     * Получить нужный падеж слова
     * @param count число
     * @param str1 мн.ч Родительный падеж
     * @param str2 ед.ч Именительный падеж
     * @param str3 ед.ч Родительный падеж
     * @return
     */
    fun getWord(count: Int, str1: String, str2: String, str3: String): String { //"градусов", "градус", " градуса"
        var n = count % 100
        if (n < 11 || n > 14) {
            n = count % 10
            if (n == 1) return str2
            if (n >= 2 && n <= 4) return str3
        }
        return str1
    }

    /**
     * Удалить все ссылки [число] и (слова) из предложения
     * @param text
     * @return
     */
    fun removeLinks(text: String): String {
        var text = text
        val pattern = Pattern.compile("\\[\\d+\\]")
        val matcher = pattern.matcher(text)
        while (matcher.find()) {
            val find = matcher.group()
            text = text.replace(find, "")
        }
        val pattern2 = Pattern.compile("\\(.+\\) \\— ", Pattern.CASE_INSENSITIVE)
        val matcher2 = pattern2.matcher(text)
        while (matcher2.find()) {
            val find = matcher2.group()
            val pattern1 = Pattern.compile("\\(.+\\)", Pattern.CASE_INSENSITIVE)
            val matcher1 = pattern1.matcher(find)
            if (matcher1.find()) {
                text = text.replace(matcher1.group(), "")
            } else text = text.replace(find, "")
        }
        return text
    }

    /**
     * Усекает строку, оставляя только первое предложение
     * @param str
     * @return
     */
    fun getTruncatedString(str: String): String {
        var str = str
        val symbols = ".?!"
        var i = 0
        while (i < str.length){
            val elem = str[i]
            if (symbols.contains(elem.toString())) {
                str = str.substring(0, i + 1)
                break
            }
            i++
        }
        return str
    }

    /**
     * Проверяет, является ли строка числом типа Long
     * @param str
     * @return
     */
    private fun isNumeric(str: String): Boolean {
        var ret = false
        try {
            str.toLong()
            ret = true
        } catch (ignored: NumberFormatException) {
        }
        Log.w("isNumeric", java.lang.Boolean.toString(ret))
        return ret
    }

    /**
     * Получить лист дат из строки
     * @param str исходная строка
     * @param pattern паттерн, используемый в поиске даты
     * @param replaceStr строка, удаляемая из паттерна
     * @return список строк
     */
    private fun getDate(str: String, pattern: Pattern, replaceStr: String): List<String> {
        val result: MutableList<String> = ArrayList()
        val matcher = pattern.matcher(str)
        while (matcher.find()) {
            val dateHoliday: String = matcher.group().replace(replaceStr, "")
            var addStr = ""
            when (dateHoliday) {
                "" -> addStr += ""
                "сегодня" -> addStr += getDay(0)
                "завтра" -> addStr += getDay(1)
                "вчера" -> addStr += getDay(-1)
                else -> {
                    Log.d("AI", "getDate:dateHoliday=$dateHoliday")
                    val myMap: HashMap<String?, String?> = object : HashMap<String?, String?>() {
                        init {
                            put("янв", "аря")
                            put("фев", "раля")
                            put("мар", "та")
                            put("апр", "еля")
                            put("июн", "я")
                            put("июл", "я")
                            put("авг", "уста")
                            put("сен", "тября")
                            put("окт", "ября")
                            put("ноя", "бря")
                            put("дек", "абря")
                        }
                    }
                    val words: Array<String?> = dateHoliday.split(" ").toTypedArray()
                    if (myMap.containsKey(words[1])) {
                        words[1] += myMap[words[1]]
                        addStr += words[0].toString() + " " + words[1] + " " + words[2]
                    } else {
                        addStr += dateHoliday
                    }
                    Log.d("AI", "getDate:addStr=$addStr")
                }
            }
            Log.i("AI", "date=$addStr")
            if (addStr != STRING_EMPTY) result.add(addStr)
        }
        return result
    }

    /**
     * Получить день с +amount от текущего
     * @param amount число дней от текущего числа
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    private fun getDay(amount: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, amount)
        return SimpleDateFormat("d MMMM yyyy").format(cal.time)
    }

    /**
     * @return текущее время
     */
    private val currentTime: String
        @SuppressLint("SimpleDateFormat") get() {
            val cal = Calendar.getInstance()
            return SimpleDateFormat("HH:mm").format(cal.time)
        }

    /**
     * @return текущий день недели
     */

    private val dayOfWeek: String
        @SuppressLint("SimpleDateFormat") get() {
            val cal = Calendar.getInstance()
            return SimpleDateFormat("EEEE").format(cal.time)
        }

    /**
     * Получить кол-во дней до даты
     * @param que
     * @return число дней
     */
    private fun getNumberOfDaysToDate(que: String): String {
        val stringList = calculateNumberOfDays(que)
        val resultString = StringBuilder()
        if (stringList.isNotEmpty()) {
            var i = 0
            while (i < stringList.size){
                val num = stringList[i]
                if (num < 0) resultString.append("Не могу посчитать.") else if (num == 0) resultString.append("Сегодня этот день.") else resultString.append(num.toString() + " " + getWord(stringList[i], "дней", "день", "дня") + ".")
                i++
            }
        }

        return if (resultString.isNotEmpty() && resultString[resultString.length - 1] == '.') deleteLastSymbol(resultString.toString()) else resultString.toString()
    }

    /**
     * Рассчитать кол-во дней
     * @param str
     * @return список кол-ва дней до дат
     */
    private fun calculateNumberOfDays(str: String): List<Int> {
        val result: MutableList<Int> = ArrayList()
        val date = getDate(str, Pattern.compile("дней до ((0?[1-9]|[12][0-9]|3[01]) (янв(?:аря)?|фев(?:раля)?|мар(?:та)?|апр(?:еля)?|мая|июн(?:я)?|июл(?:я)?|авг(?:уста)?|сен(?:тября)?|окт(?:ября)?|ноя(?:бря)?|дек(?:абря)?) \\d{4})",
                Pattern.CASE_INSENSITIVE), "дней до")
        val today = getStringAsDate(getDay(0))
        var i = 0
        while (i < date.size){
            var count = -1
            val dateSearch = getStringAsDate(date[i])
            if (dateSearch != null) {
                count = daysBetween(today, dateSearch)
            }
            result.add(count)
            i++
        }
        return result
    }

    @SuppressLint("SimpleDateFormat")
    private fun getStringAsDate(str: String): Date? {
        try {
            val inputFormat = SimpleDateFormat("d MMMM yyyy")
            val cal = Calendar.getInstance()
            cal.time = inputFormat.parse(str)
            return cal.time
        } catch (ex: Exception) {
            Log.e("getSAD", ex.toString())
        }
        return null
    }

    private fun daysBetween(d1: Date?, d2: Date): Int {
        return ((d2.time - d1!!.time) / (1000 * 60 * 60 * 24)).toInt()
    }

    /**
     * Случайная фраза из списка
     * @param list список фраз
     * @return
     */
    private fun randomPhrase(list: List<String>): String {
        return list[(Math.random() * list.size).toInt()]
    }

    /**
     * Удалить последний символ
     * @param str
     * @return
     */
    fun deleteLastSymbol(str: String): String {
        return str.replaceFirst(".$".toRegex(), "")
    }

    /**
     * Преобразовать длинный ответ на несколько небольших
     * @param str
     * @return список фраз
     */
    private fun transformAnswer(str: String): List<String> {
        var str = str
        val list: MutableList<String> = ArrayList()
        val ch = str.length / MAX_LENGTH_MESSAGE + 1
        Log.e("ch = ", str.length.toString() + " " + MAX_LENGTH_MESSAGE)
        val sizeCh = str.length / ch
        var i = 0
        while (i < ch - 1){
            var j: Int = if (i == 0) (i + 1) * sizeCh else i * sizeCh
            while (str[j] != ' ') {
                j--
            }
            list.add(str.substring(0, j))
            str = str.substring(j + 1)
            i++
        }
        list.add(str)
        return list
    }

    init {
        dictionaryCode = HashMap()
        (dictionaryCode as HashMap<String?, Int>).put("какой сегодня день", 0)
        (dictionaryCode as HashMap<String?, Int>).put("который час", 1)
        (dictionaryCode as HashMap<String?, Int>).put("какой день недели", 2)
        (dictionaryCode as HashMap<String?, Int>).put("дней до", 3)
        (dictionaryCode as HashMap<String?, Int>).put("привет", 4)
        (dictionaryCode as HashMap<String?, Int>).put("как дела", 5)
        (dictionaryCode as HashMap<String?, Int>).put("чем занимаешься", 6)
        (dictionaryCode as HashMap<String?, Int>).put("погода", 7)
        (dictionaryCode as HashMap<String?, Int>).put("число", 8)
        (dictionaryCode as HashMap<String?, Int>).put("праздник", 9)
        (dictionaryCode as HashMap<String?, Int>).put("что такое", 10)
        (dictionaryCode as HashMap<String?, Int>).put("кто такой", 10)
    }
}