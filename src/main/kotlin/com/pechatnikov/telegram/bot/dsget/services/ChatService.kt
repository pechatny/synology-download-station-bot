package com.pechatnikov.telegram.bot.dsget.services

import com.pechatnikov.telegram.bot.dsget.db.models.Chat
import com.pechatnikov.telegram.bot.dsget.db.models.Message
import com.pechatnikov.telegram.bot.dsget.db.repositories.ChatsRepository
import com.pechatnikov.telegram.bot.dsget.db.repositories.MessagesRepository
import com.pechatnikov.telegram.bot.dsget.services.types.MessageType
import com.pechatnikov.telegram.bot.dsget.services.types.Stage
import org.springframework.stereotype.Service

@Service
class ChatService(val chatsRepository: ChatsRepository, val messagesRepository: MessagesRepository) {
    fun createChat(chatId: Long): Chat {
        val chat = Chat(chatId = chatId, stage = Stage.MESSAGE)
        chatsRepository.save(chat)

        return chat
    }

    fun saveMessage(chatId: Long, messageId: Long, readText: String, messageType: MessageType) {
        val message =
            Message(chatId = chatId, messageId = messageId, content = readText, messageType = messageType)
        messagesRepository.save(message)
    }

    fun getLastMessage(chatId: Long): Message? {
        return messagesRepository.findFirstByChatIdOrderByIdDesc(chatId)
    }

    fun setStage(chatId: Long, stage: Stage) {
        val chat = chatsRepository.findByChatId(chatId)
        chat.stage = stage
        chatsRepository.save(chat)
    }
}