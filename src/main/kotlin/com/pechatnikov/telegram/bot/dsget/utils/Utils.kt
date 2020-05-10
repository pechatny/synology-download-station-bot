package com.pechatnikov.telegram.bot.dsget.utils

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

class Utils {
    companion object {
        fun sendMessage(message: String, update: Update): SendMessage {
            return SendMessage().setChatId(update.message.chatId).setText(message)
        }
    }
}