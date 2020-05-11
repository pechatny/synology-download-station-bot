package com.pechatnikov.telegram.bot.dsget.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.pechatnikov.telegram.bot.dsget.configuration.DownloadStationConfig
import com.pechatnikov.telegram.bot.dsget.services.models.DsResponse
import com.pechatnikov.telegram.bot.dsget.services.models.LoginRequest
import com.pechatnikov.telegram.bot.dsget.services.models.Task
import com.pechatnikov.telegram.bot.dsget.services.models.TaskStatus
import com.pechatnikov.telegram.bot.dsget.services.models.TasksResponse
import khttp.structures.files.FileLike
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.nio.charset.Charset

@Service
class DownloadStationService(val downloadStationConfig: DownloadStationConfig, val objectMapper: ObjectMapper) {
    private val logger = LoggerFactory.getLogger(DownloadStationService::class.java)
    private val BASE_AUTH_URL = "${downloadStationConfig.url}/webapi/auth.cgi"
    private val BASE_TASK_URL = "${downloadStationConfig.url}/webapi/DownloadStation/task.cgi"
    private lateinit var smid: String
    private lateinit var id: String

    init {
        login()
    }

    fun auth(login: String, password: String): Boolean {
        val request = LoginRequest(login, password).toMap()
        val response = khttp.get(
            url = """$BASE_AUTH_URL?api=${request["api"]}&version=${request["version"]}&method=${request["method"]}&account=${request["account"]}&passwd=${request["passwd"]}&session=${request["session"]}&format=${request["format"]}"""
        )

        response.encoding = Charset.defaultCharset()
        val dsResponse = objectMapper.readValue(response.text, DsResponse::class.java)

        return dsResponse.success
    }

    private fun login() {
        val request = LoginRequest(downloadStationConfig.login, downloadStationConfig.password).toMap()
        val response = khttp.get(
            url = """$BASE_AUTH_URL?api=${request["api"]}&version=${request["version"]}&method=${request["method"]}&account=${request["account"]}&passwd=${request["passwd"]}&session=${request["session"]}&format=${request["format"]}"""
        )

        smid = response.cookies["smid"].toString()
        id = response.cookies["id"].toString()
    }

    private fun getTasks(): TasksResponse {
        val response = khttp.get(
            url = BASE_TASK_URL,
            params = mapOf(
                "api" to "SYNO.DownloadStation.Task",
                "version" to "3",
                "method" to "list",
                "additional" to "transfer"
            ),
            cookies = mapOf("smid" to smid, "id" to id)
        )
        response.encoding = Charset.defaultCharset()

        return objectMapper.readValue(response.text, TasksResponse::class.java)
    }

    fun getDownloadingTasks(): List<Task> {
        return getTasks().data.tasks.filter { it.status == TaskStatus.DOWNLOADING }
    }

//    private fun <E> List<E>.toText(): String {
//    }
    private fun post(url: String, destination: String, fileContent: ByteArray, fileName: String): DsResponse {
        val response = khttp.post(
            url = BASE_TASK_URL,
            data = mapOf(
                "api" to "SYNO.DownloadStation.Task",
                "version" to "3",
                "method" to "create",
                "destination" to destination
            ),
            files = listOf(
                FileLike("file", fileName, fileContent)
            ),
            cookies = mapOf("smid" to smid, "id" to id)
        )
        response.encoding = Charset.defaultCharset()

        return objectMapper.readValue(response.text, DsResponse::class.java)
    }

    fun createTask(fileString: ByteArray, fileName: String, destination: String): Boolean {
        logger.info("task creation started")
        val response =
            post(url = BASE_TASK_URL, destination = destination, fileName = fileName, fileContent = fileString)

        return if (response.success) {
            logger.info("task created; filename: $fileName")
            true;
        } else {
            logger.info("task creation failed")
            logger.info("error code: " + response.error?.code)
            false
        }
    }
}

fun List<Task>.toText(): String {
    if (this.isEmpty()) return "Нет активных заданий."

    var stringTasks = "Активные задания:\r\n"
    for (item in this) {
        stringTasks +=
"""
Название: ${item.title}
Размер: ${(item.size / 1073741824)} ГБ
Скачано: ${(item.additional?.transfer?.size_downloaded?: 0 / 1073741824L)} ГБ
Осталось: ${item.endTime} Мин
"""
    }
    return stringTasks
}