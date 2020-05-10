package com.pechatnikov.telegram.bot.dsget.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("download-station")
class DownloadStationConfig {
    lateinit var url: String
    lateinit var login: String
    lateinit var password: String
}