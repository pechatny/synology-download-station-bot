package com.pechatnikov.telegram.bot.dsget.db.repositories

import com.pechatnikov.telegram.bot.dsget.db.models.Message
import com.pechatnikov.telegram.bot.dsget.services.types.MessageType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MessagesRepository: JpaRepository<Message, Long> {
    fun findFirstByChatIdAndMessageTypeOrderByIdDesc(chatId: Long, messageType: MessageType): Message?
}