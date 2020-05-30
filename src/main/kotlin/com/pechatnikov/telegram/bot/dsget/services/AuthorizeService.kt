package com.pechatnikov.telegram.bot.dsget.services

import com.pechatnikov.telegram.bot.dsget.db.models.User
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update

@Service
class AuthorizeService(
    val chatService: ChatService,
    val userService: UserService,
    val downloadStationService: DownloadStationService
) {
    fun checkUser(userId: Long, chatId: Long): User {
        val user: User? = userService.findByUserId(userId)

        return if (user == null) {
            val newUser = userService.create(userId)
            newUser.chat = chatService.createChat(chatId)
            userService.update(newUser)
            newUser
        } else {
            user
        }
    }

    fun authorize(user: User, update: Update): SendMessage? {
        when (user.chat?.stage) {
            "message" -> {
                chatService.setStage(update.message.chatId, "authorization")
                val responseMessage = SendMessage().setChatId(update.message.chatId)
                responseMessage.text =
                    "Введите логин и пароль от сетевого хранилища. Например: serverlogin password"
                return responseMessage
            }
            "authorization" -> {
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
            }
            else -> {
                return null
            }
        }
    }
}