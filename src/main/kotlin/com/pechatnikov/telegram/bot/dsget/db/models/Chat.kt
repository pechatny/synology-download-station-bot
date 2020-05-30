package com.pechatnikov.telegram.bot.dsget.db.models

import com.pechatnikov.telegram.bot.dsget.services.types.Stage
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "chats")
data class Chat(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    var chatId: Long,
    @Enumerated(EnumType.STRING)
    var stage: Stage = Stage.MESSAGE
)
