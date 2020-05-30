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

    fun saveMessage(chatId: Long, messageId: Long, readText: String) {
        val message =
            Message(chatId = chatId, messageId = messageId, content = readText, messageType = MessageType.MAGNET)
        messagesRepository.save(message)
    }

    fun getLastMagnet(chatId: Long): Message? {
        return messagesRepository.findFirstByChatIdAndMessageTypeOrderByIdDesc(chatId, MessageType.MAGNET)
    }

    fun setStage(chatId: Long, stage: Stage) {
        val chat = chatsRepository.findByChatId(chatId)
        chat.stage = stage
        chatsRepository.save(chat)
    }
}