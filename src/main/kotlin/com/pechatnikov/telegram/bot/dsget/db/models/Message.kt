package com.pechatnikov.telegram.bot.dsget.db.models

import com.pechatnikov.telegram.bot.dsget.services.types.MessageType
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "messages")
data class Message(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    var messageId: Long,
    var chatId: Long,
    @Enumerated(EnumType.STRING)
    var messageType: MessageType,
    var content: String,
    @Column(name = "created_at", columnDefinition = "TIMESTAMP")
    var createdAt: LocalDateTime = LocalDateTime.now()
)
