package com.example.voiceassistent.messageview

import java.util.*

class Message {
    //@kotlin.jvm.JvmField
    var text: String?
    //@kotlin.jvm.JvmField
    var date: Date? = null
    //@kotlin.jvm.JvmField
    var isSend: Boolean?

    constructor(text: String?, isSend: Boolean) {
        this.text = text
        date = Date()
        this.isSend = isSend
    }

    constructor(entity: MessageEntity) {
        text = entity.text
        date = if (entity.date == null) null else Date(entity.date)
        isSend = entity.isSend != 0
    }
}