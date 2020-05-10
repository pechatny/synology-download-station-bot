package com.pechatnikov.telegram.bot.dsget.services.models

class LoginRequest(
    private val account: String,
    private val passwd: String,
    private val version: Int = 6,
    private val api: String = "SYNO.API.Auth",
    private val session: String = "DownloadStation",
    private val format: String = "cookie",
    private val method: String = "login"
) {
    fun toMap(): Map<String, String> {

        return mapOf(
            "api" to api,
            "version" to version.toString(),
            "method" to method,
            "account" to account,
            "passwd" to passwd,
            "session" to session,
            "format" to format
        )
    }
}


