package com.pechatnikov.telegram.bot.dsget.models

enum class DownloadType(
    val text: String, val path: String
) {
    MOVIE("Фильм", "video/movies"),
    SEATCOM("Сериал", "video/seatcoms"),
    DOWNLOAD("Другое", "downloads");

    companion object {
        fun getByText(text: String): DownloadType {
            for (value in values()) {
                if (value.text == text) return value
            }
            throw Exception("Not found DownloadType")
        }

        fun containsText(text: String): Boolean {
            for (value in values()) {
                if (value.text == text) return true
            }
            return false
        }
    }
}