package com.pechatnikov.telegram.bot.dsget.services.types

enum class TaskStatus {
    WAITING,
    DOWNLOADING,
    PAUSED,
    FINISHING,
    FINISHED,
    HASH_CHECKING,
    SEEDING,
    FILEHOSTING_WAITING,
    EXTRACTING,
    ERROR
}