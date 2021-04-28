package com.pechatnikov.telegram.bot.dsget.services.models

data class DsResponse(val data: Data?, val success: Boolean, val error: Error?)
data class DownloadTaskResponse(val data: DataV2, val success: Boolean)
data class DataV2(val listId: List<String>?, val taskId: List<String>?)
