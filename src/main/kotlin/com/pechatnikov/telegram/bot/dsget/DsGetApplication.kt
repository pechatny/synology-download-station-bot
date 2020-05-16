package com.pechatnikov.telegram.bot.dsget

import org.glassfish.jersey.client.ClientProperties.PROXY_PASSWORD
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.telegram.telegrambots.ApiContextInitializer
import java.net.Authenticator
import java.net.PasswordAuthentication

@SpringBootApplication
@EnableCaching
class DsGetApplication

fun main(args: Array<String>) {
//    val PROXY_USER = "telegram"
//    val PROXY_PASSWORD = "telegram"
    // Create the Authenticator that will return auth's parameters for proxy authentication
//    Authenticator.setDefault(object : Authenticator() {
//        override fun getPasswordAuthentication(): PasswordAuthentication {
//            return PasswordAuthentication(PROXY_USER, PROXY_PASSWORD.toCharArray())
//        }
//    })
    ApiContextInitializer.init()
    runApplication<DsGetApplication>(*args)
}

