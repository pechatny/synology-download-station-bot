package com.pechatnikov.telegram.bot.dsget.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("path")
class PathConfig {
    lateinit var torrents: String
}