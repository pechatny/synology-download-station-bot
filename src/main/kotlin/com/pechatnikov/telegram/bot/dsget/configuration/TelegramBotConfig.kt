package com.pechatnikov.telegram.bot.dsget.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("telegram-bot")
class TelegramBotConfig {
    lateinit var name: String
    lateinit var token: String
}