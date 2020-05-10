package com.pechatnikov.telegram.bot.dsget.services.models

data class TasksResponse (val data: TaskData, val success: Boolean) {
    data class TaskData(val offset: Int, val tasks: List<Task>, val total: Int)
}