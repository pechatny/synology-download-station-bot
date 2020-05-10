package com.pechatnikov.telegram.bot.dsget.db

import com.pechatnikov.telegram.bot.dsget.db.models.Chat
import com.pechatnikov.telegram.bot.dsget.db.models.User
import com.pechatnikov.telegram.bot.dsget.db.repositories.ChatsRepository
import com.pechatnikov.telegram.bot.dsget.db.repositories.UsersRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

@DataJpaTest
class GetUserTest@Autowired constructor(
    val usersRepository: UsersRepository,
    val chatsRepository: ChatsRepository
) {
    @Test
    fun insertUser() {
        usersRepository.save(User(userId = 123))
        val foundUser = usersRepository.findByUserId(123)
        val wrongUser:User? = usersRepository.findByUserId(124)
        assertEquals(123, foundUser?.userId)
        assertNull(wrongUser)
    }

    @Test
    fun createUserWithChat(){
        val chat = Chat(chatId = 22)
        chatsRepository.save(chat)
        usersRepository.save(User(userId = 11, chat = chat))

        val user = usersRepository.findByUserId(11)
        assertEquals(22, user?.chat?.chatId)
    }

}
