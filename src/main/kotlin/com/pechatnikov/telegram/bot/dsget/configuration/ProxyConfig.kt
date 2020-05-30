package com.pechatnikov.telegram.bot.dsget.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.bots.DefaultBotOptions

@Configuration
@ConfigurationProperties("proxy")
class ProxyConfig {
    lateinit var host: String
    var type: DefaultBotOptions.ProxyType = DefaultBotOptions.ProxyType.HTTP
    var port: Int = 0
}
