package com.pechatnikov.telegram.bot.dsget.db.repositories

import com.pechatnikov.telegram.bot.dsget.db.models.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UsersRepository: JpaRepository<User, Long> {
    fun findByUserId(userId: Long): User?
}