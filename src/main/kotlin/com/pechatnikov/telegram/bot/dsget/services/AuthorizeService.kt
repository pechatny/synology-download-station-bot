package com.pechatnikov.telegram.bot.dsget.services

import com.pechatnikov.telegram.bot.dsget.db.models.User
import com.pechatnikov.telegram.bot.dsget.db.repositories.UsersRepository
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Service
class AuthorizeService(
    val usersRepository: UsersRepository,
    val chatService: ChatService,
    val downloadStationService: DownloadStationService,
    val userService: UserService
) {
    fun checkUser(userId: Long, chatId: Long): User {
        val user: User? = usersRepository.findByUserId(userId)

        return if (user == null) {
            val newUser = createUser(userId)
            val chat = chatService.createChat(chatId)
            newUser.chat = chat
            usersRepository.save(newUser)
            newUser
        } else {
            user
        }
    }

    fun checkAuthorization(user: User) {
    }

    fun authorize(user: User, update: Update): SendMessage? {
        if (user.chat?.stage == "message") {
            chatService.setStage(update.message.chatId, "authorization")
            val responseMessage = SendMessage().setChatId(update.message.chatId)
            responseMessage.text =
                "Введите логин и пароль от сетевого хранилища. Например: serverlogin password"
            return responseMessage
        } else if (user.chat?.stage == "authorization") {
            val credentials = update.message.text.split(" ")
            val authResult = downloadStationService.auth(credentials[0], credentials[1])
            return if (authResult) {
                chatService.setStage(update.message.chatId, "message")
                userService.setAuthorized(user)
                val responseMessage = SendMessage().setChatId(update.message.chatId)
                responseMessage.text = "Вы успешно авторизованы!"
                responseMessage
            } else {
                val responseMessage = SendMessage().setChatId(update.message.chatId)
                responseMessage.text = "Ошбика авторизации. Попробуйте другие логин и пароль!"
                responseMessage
            }
        } else {
            return null
        }
    }

    fun createUser(userId: Long): User {
        val newUser = User(userId = userId, authorized = false)
        usersRepository.save(newUser)

        return newUser
    }
}