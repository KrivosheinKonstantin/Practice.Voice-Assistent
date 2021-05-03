package com.example.voiceassistent.messageview

import android.annotation.SuppressLint
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.voiceassistent.R

class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    protected var messageText: TextView
    protected var messageDate: TextView

    /**
     * положить данные из модели в соответствующие текстовые поля
     */
    @SuppressLint("SimpleDateFormat")
    fun bind(message: Message) {
        messageText.text = message.text
        val fmt: DateFormat = SimpleDateFormat("")
        messageDate.text = fmt.format(message.date)
    }

    init {
        messageText = itemView.findViewById(R.id.messageTextView)
        messageDate = itemView.findViewById(R.id.messageDateView)
    }
}