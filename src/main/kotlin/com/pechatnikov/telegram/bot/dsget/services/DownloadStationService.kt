package com.pechatnikov.telegram.bot.dsget.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.pechatnikov.telegram.bot.dsget.configuration.DownloadStationConfig
import com.pechatnikov.telegram.bot.dsget.services.models.AuthCookies
import com.pechatnikov.telegram.bot.dsget.services.models.Authentication
import com.pechatnikov.telegram.bot.dsget.services.models.DownloadTaskResponse
import com.pechatnikov.telegram.bot.dsget.services.models.DsResponse
import com.pechatnikov.telegram.bot.dsget.services.models.LoginRequest
import com.pechatnikov.telegram.bot.dsget.services.models.Task
import com.pechatnikov.telegram.bot.dsget.services.models.TasksResponse
import com.pechatnikov.telegram.bot.dsget.services.types.TaskStatus
import khttp.structures.files.FileLike
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import java.io.File
import java.nio.charset.Charset

@Service
class DownloadStationService(private val downloadStationConfig: DownloadStationConfig, val objectMapper: ObjectMapper) {
    private val logger = LoggerFactory.getLogger(DownloadStationService::class.java)
    private val BASE_AUTH_URL = "${downloadStationConfig.url}/webapi/auth.cgi"
    private val BASE_TASK_URL = "${downloadStationConfig.url}/webapi/DownloadStation/task.cgi"
    private val DOWNLOAD_TASK_URL = "${downloadStationConfig.url}/webapi/entry.cgi"
    private lateinit var authCookies: AuthCookies

    init {
        login()
    }

    @Cacheable("auth")
    fun authenticate(login: String, password: String): Authentication {
        val request = LoginRequest(login, password).toMap()
        val response = khttp.get(
            url = """$BASE_AUTH_URL?api=${request["api"]}&version=${request["version"]}&method=${request["method"]}&account=${request["account"]}&passwd=${request["passwd"]}&session=${request["session"]}&format=${request["format"]}"""
        ).apply { encoding = Charset.defaultCharset() }
        val dsResponse = objectMapper.readValue(response.text, DsResponse::class.java)
        val authCookies = AuthCookies(response.cookies["id"].toString(), response.cookies["smid"].toString())
        return Authentication(dsResponse, authCookies)
    }

    fun auth(login: String, password: String): Boolean {
        return authenticate(login, password).dsResponse.success
    }

    private fun login() {
        authCookies = authenticate(downloadStationConfig.login, downloadStationConfig.password).authCookies
    }

    private fun getTasks(): TasksResponse {
        return khttp.get(
            url = BASE_TASK_URL,
            params = mapOf(
                "api" to "SYNO.DownloadStation.Task",
                "version" to "3",
                "method" to "list",
                "additional" to "transfer"
            ),
            cookies = mapOf("smid" to authCookies.smid, "id" to authCookies.id)
        )
            .apply { encoding = Charset.defaultCharset() }
            .let { objectMapper.readValue(it.text, TasksResponse::class.java) }
    }

    fun getDownloadingTasks(): List<Task> {
        return getTasks().data.tasks.filter { it.status == TaskStatus.DOWNLOADING }
    }

    fun createMagnetTask(magnetLink: String, destination: String): Boolean {
        logger.info("task creation started")
        val response = khttp.get(
            url = BASE_TASK_URL,
            data = mapOf(
                "api" to "SYNO.DownloadStation.Task",
                "version" to "3",
                "method" to "create",
                "destination" to destination,
                "uri" to magnetLink
            ),
            cookies = mapOf("smid" to authCookies.smid, "id" to authCookies.id)
        )
            .apply { encoding = Charset.defaultCharset() }
            .let { objectMapper.readValue(it.text, DsResponse::class.java) }

        return if (response.success) {
            logger.info("task created")
            true
        } else {
            logger.info("task creation failed")
            logger.info("error code: " + response.error?.code)
            false
        }
    }

    fun createTorrentTask(torrentFile: File, destination: String): Boolean {
        logger.info("task creation started")
        logger.info("torrent file path is " + torrentFile.path)
        val response = khttp.post(
            url = DOWNLOAD_TASK_URL,
            data = mapOf(
                "api" to "SYNO.DownloadStation2.Task",
                "version" to "2",
                "method" to "create",
                "file" to "[\"torrent\"]",
                "type" to "\"file\"",
                "create_list" to "false",
                "destination" to "\"$destination\""
            ),
            files = listOf(
                FileLike("torrent", torrentFile.name, torrentFile.readBytes())
            ),
            cookies = mapOf("smid" to authCookies.smid, "id" to authCookies.id)
        )
            .apply { encoding = Charset.defaultCharset() }
            .let { objectMapper.readValue(it.text, DownloadTaskResponse::class.java) }

        torrentFile.delete()
        return if (response.success) {
            logger.info("task created")
            true
        } else {
            logger.info("task creation failed")
//            logger.info("error code: " + response.error?.code)
            false
        }
    }
}

fun List<Task>.toText(): String {
    if (this.isEmpty()) return "Нет активных заданий."
    var stringTasks = "Активные задания:\r\n"
    this.forEach { stringTasks += it }

    return stringTasks
}