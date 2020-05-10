package com.pechatnikov.telegram.bot.dsget.services

import com.pechatnikov.telegram.bot.dsget.db.models.User
import com.pechatnikov.telegram.bot.dsget.db.repositories.UsersRepository
import org.springframework.stereotype.Service

@Service
class UserService(val usersRepository: UsersRepository) {
    fun setAuthorized(user: User) {
        user.authorized = true
        usersRepository.save(user)
    }
}