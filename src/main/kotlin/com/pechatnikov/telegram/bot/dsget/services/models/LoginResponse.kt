package com.pechatnikov.telegram.bot.dsget.services.models

data class LoginResponse(val data: Data?, val success: Boolean, val error: Error?)
data class Data(val sid: String)
data class Error(val code: String)