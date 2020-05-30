package com.pechatnikov.telegram.bot.dsget

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.telegram.telegrambots.ApiContextInitializer

@SpringBootApplication
@EnableCaching
class DsGetApplication

fun main(args: Array<String>) {
    ApiContextInitializer.init()
    runApplication<DsGetApplication>(*args)
}

