package com.pechatnikov.telegram.bot.dsget.bot

import org.telegram.telegrambots.bots.DefaultBotOptions

class DsGetBotOptions : DefaultBotOptions() {
    init {
        proxyHost = "155.235.2.49"
        proxyPort = 8080
        proxyType = ProxyType.SOCKS5
    }
}