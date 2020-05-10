package com.pechatnikov.telegram.bot.dsget.db.models

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,
    var userId: Long,
    var username: String? = null,
    var surname: String? = null,
    var authorized: Boolean = false,

    @OneToOne
    var chat: Chat? = null
)
