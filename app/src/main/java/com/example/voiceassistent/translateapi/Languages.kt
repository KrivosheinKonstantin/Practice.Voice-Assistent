package com.example.voiceassistent.translateapi

enum class Languages(val languageCode: String) {
    AUTO_DETECT("auto"), ENGLISH("en"), RUSSIAN("ru");

    override fun toString(): String {
        return languageCode
    }
}