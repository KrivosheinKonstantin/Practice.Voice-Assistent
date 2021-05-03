package com.example.voiceassistent.parsing

import com.example.voiceassistent.AI
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object ParsingHtmlServiceWiki {
    private const val ERROR_MESSAGE = "Ошибка получения данных с сайта. Попробуйте позже."
    private const val NULL_ANSWER = "Не получилось найти ответ"

    /**
     * Получить описание текста
     * @param text слово для поиска
     * @return строка с пояснением сходного слова
     */
    //@kotlin.jvm.JvmStatic
    fun getDescription(text: String): String {
        val URL = "https://ru.wikipedia.org/wiki/$text"
        val result = AI.STRING_EMPTY
        var document: Document? = null
        document = try {
            Jsoup.connect(URL).get()
        } catch (ex: Exception) {
            return ERROR_MESSAGE
        }
        val body = document?.body()
        val elements = body?.getElementsByTag("p")
        if (elements != null) {
            for (i in elements) {
                val element = i
                val elements1 = element.select("b")
                if (elements1.size != 0) {
                    return AI.getTruncatedString(AI.removeLinks(element.text()))
                }
            }
        }
        return NULL_ANSWER
    }
}