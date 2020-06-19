package com.pechatnikov.telegram.bot.dsget.db.repositories

import com.pechatnikov.telegram.bot.dsget.db.models.Message
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MessagesRepository: JpaRepository<Message, Long> {
    fun findFirstByChatIdOrderByIdDesc(chatId: Long): Message?
}