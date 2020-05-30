package com.pechatnikov.telegram.bot.dsget.services

import com.pechatnikov.telegram.bot.dsget.db.models.Chat
import com.pechatnikov.telegram.bot.dsget.db.models.Message
import com.pechatnikov.telegram.bot.dsget.db.repositories.ChatsRepository
import com.pechatnikov.telegram.bot.dsget.db.repositories.MessagesRepository
import org.springframework.stereotype.Service

@Service
class ChatService(val chatsRepository: ChatsRepository, val messagesRepository: MessagesRepository) {
    fun createChat(chatId: Long): Chat {
        val chat = Chat(chatId = chatId, stage = "message")
        chatsRepository.save(chat)

        return chat
    }

    fun saveMessage(chatId: Long, messageId: Long, readText: String) {
        val message = Message(chatId = chatId, messageId = messageId, content = readText, messageType = "magnet")
        messagesRepository.save(message)
    }

    fun getLastMagnet(chatId: Long): Message? {
        return messagesRepository.findFirstByChatIdAndMessageTypeOrderByIdDesc(chatId, "magnet")
    }

    fun setStage(chatId: Long, stage: String) {
        val chat = chatsRepository.findByChatId(chatId)
        chat.stage = stage
        chatsRepository.save(chat)
    }
}