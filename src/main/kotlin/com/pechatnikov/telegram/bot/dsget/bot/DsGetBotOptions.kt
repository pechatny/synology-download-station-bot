package com.pechatnikov.telegram.bot.dsget.bot

import com.pechatnikov.telegram.bot.dsget.configuration.ProxyConfig
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.DefaultBotOptions

@Component
class DsGetBotOptions(proxyConfig: ProxyConfig) : DefaultBotOptions() {
    init {
        if (proxyConfig.enabled) {
            proxyHost = proxyConfig.host
            proxyType = proxyConfig.type
            proxyPort = proxyConfig.port
        }
    }
}