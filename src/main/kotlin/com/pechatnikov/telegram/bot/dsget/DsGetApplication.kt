package com.pechatnikov.telegram.bot.dsget

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.telegram.telegrambots.ApiContextInitializer

@SpringBootApplication
class DsGetApplication

fun main(args: Array<String>) {
    ApiContextInitializer.init()
    runApplication<DsGetApplication>(*args)
}
