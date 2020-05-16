package com.pechatnikov.telegram.bot.dsget.services

import com.pechatnikov.telegram.bot.dsget.db.models.User
import com.pechatnikov.telegram.bot.dsget.db.repositories.UsersRepository
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class UserService(val usersRepository: UsersRepository) {

    @CacheEvict(value = ["user"], key = "#user.userId")
    fun setAuthorized(user: User) {
        user.authorized = true
        usersRepository.save(user)
    }

    @Cacheable("user")
    fun findByUserId(userId: Long): User? = usersRepository.findByUserId(userId)

    @CachePut("user")
    fun create(userId: Long): User {
        val newUser = User(userId = userId, authorized = false)
        usersRepository.save(newUser)

        return newUser
    }

    @CacheEvict(value = ["user"], key = "#user.userId")
    fun update(user: User){
        usersRepository.save(user)
    }
}