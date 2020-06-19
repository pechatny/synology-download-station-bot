package com.pechatnikov.telegram.bot.dsget.bot

import com.pechatnikov.telegram.bot.dsget.configuration.PathConfig
import com.pechatnikov.telegram.bot.dsget.configuration.TelegramBotConfig
import com.pechatnikov.telegram.bot.dsget.db.models.Message
import com.pechatnikov.telegram.bot.dsget.db.models.User
import com.pechatnikov.telegram.bot.dsget.models.DownloadType
import com.pechatnikov.telegram.bot.dsget.services.AuthorizeService
import com.pechatnikov.telegram.bot.dsget.services.ChatService
import com.pechatnikov.telegram.bot.dsget.services.DownloadStationService
import com.pechatnikov.telegram.bot.dsget.services.toText
import com.pechatnikov.telegram.bot.dsget.services.types.MessageType
import com.pechatnikov.telegram.bot.dsget.utils.Utils.Companion.sendMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.File
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import java.util.UUID

@Component
class DsGetBot(
    val botConfig: TelegramBotConfig,
    val downloadStationService: DownloadStationService,
    val authorizeService: AuthorizeService,
    val chatService: ChatService,
    val pathConfig: PathConfig,
    botOptions: DefaultBotOptions
) : TelegramLongPollingBot(botOptions) {
    private val logger = LoggerFactory.getLogger(DsGetBot::class.java)
    private val MAGNET_LINK_PREFIX = "magnet"
    private val START_COMMAND = "/start"
    private val LIST_COMMAND = "/list"
    private val POLLING_COMMAND = "/poll"
    private val HELP_COMMAND = "/help"
    private val INSTRUCTION = "Для того чтобы скачать фильм или сериал, отправьте Magnet link в этот чат."
    override fun getBotUsername(): String {
        return botConfig.name
    }

    override fun getBotToken(): String {
        return botConfig.token
    }

    override fun onUpdateReceived(update: Update) {
        if (update.hasMessage()) {
            logger.info("Message from User: id:${update.message.from.id}; firstName:${update.message.from.firstName}; lastName:${update.message.from.lastName}")
            val user: User = authorizeService.checkUser(update.message.from.id.toLong(), update.message.chatId)
            if (!user.authorized) {
                logger.info("User not authorized, starting authorization")
                authorizeService.authorize(user, update)?.let { execute(it) }
            } else {
                val messageText = update.message.text
                if (messageText != null && (messageText.startsWith(START_COMMAND) || messageText.startsWith(HELP_COMMAND))) {
                    logger.info("/start or /help command executing")
                    execute(sendMessage(INSTRUCTION, update))
                } else if (messageText != null && (messageText.startsWith(LIST_COMMAND))) {
                    listCommandHandler(update)
                } else if (messageText != null && (messageText.startsWith(POLLING_COMMAND))) {
                    GlobalScope.launch {
                        downloadingTasksPollingHandler(update)
                    }
                } else if (messageText != null && messageText.startsWith(MAGNET_LINK_PREFIX)) {
                    saveMagnetHandler(update)
                } else if (update.message.hasDocument()) {
                    saveDocumentHandler(update)
                } else if (messageText != null && DownloadType.containsText(update.message.text)) {
                    createDownloadTaskHandler(update)
                    GlobalScope.launch {
                        delay(10_000)
                        listCommandHandler(update)
                        downloadingTasksPollingHandler(update)
                    }
                } else {
                    execute(sendMessage("Отправь Magnet link в этот чат для скачивания.", update))
                }
            }
        }
    }

    private suspend fun downloadingTasksPollingHandler(update: Update) {
        logger.info("/downloading tasks polling")
        val result = poll {
            val tasks = downloadStationService.getDownloadingTasks()
            if (tasks.isEmpty()) {
                true
            } else {
                tasks.last().let { it.additional?.transfer?.speed_download ?: 0 > 0 }
            }
        }
        val tasks = downloadStationService.getDownloadingTasks()
        val message = when {
            tasks.isEmpty() -> {
                "Нет заданий на скачивание"
            }
            result -> {
                tasks.toText()
            }
            else -> {
                "Скорость скачивания последнего задания пока еще нулевая"
            }
        }

        execute(sendMessage(message, update))
    }

    private fun listCommandHandler(update: Update) {
        logger.info("/list command executing")
        val tasks = downloadStationService.getDownloadingTasks()
        execute(sendMessage(tasks.toText(), update))
    }

    private fun createDownloadTaskHandler(update: Update) {
        logger.info("Starting download task creation")
        val savedMessage: Message? = chatService.getLastMessage(update.message.chatId)
        if (savedMessage == null) {
            logger.error("Magnet link not found in the database!")
            execute(sendMessage("Торрент не найден", update))
            return
        }
        val destination = DownloadType.getByText(update.message.text).path
        var taskCreationResult = false
        if (savedMessage.messageType == MessageType.TORRENT) {
            val file = java.io.File(savedMessage.content)

            if (file.exists()) {
                taskCreationResult = downloadStationService.createTorrentTask(
                    torrentFile = file,
                    destination = destination
                )
            } else {
                taskCreationResult = false
                logger.error("File not exists in path" + file.path)
            }
        } else if (savedMessage.messageType == MessageType.MAGNET) {
            taskCreationResult = downloadStationService.createMagnetTask(
                magnetLink = savedMessage.content,
                destination = destination
            )
        }
        val message = if (taskCreationResult) {
            logger.info("Download task successfully created!")
            "Файл добавлен на закачку"
        } else {
            logger.error("Download task creation failed!")
            "Ошибка при добавлении задания, обратитесть к повелителю торрентов"
        }
        val replyKeyboardMarkup = ReplyKeyboardRemove()
        val responseMessage = SendMessage().setChatId(update.message.chatId)
        responseMessage.replyMarkup = replyKeyboardMarkup
        responseMessage.text = message
        execute(responseMessage)
    }

    private fun saveMagnetHandler(update: Update) {
        logger.info("Saving magnet link to the database")
        val message = SendMessage().setChatId(update.message.chatId)

        chatService.saveMessage(
            update.message.chatId,
            update.message.messageId.toLong(),
            update.message.text,
            MessageType.MAGNET
        )
        logger.info("Magnet link saved to database")

        showDownloadKeyboard(message)
    }

    private fun showDownloadKeyboard(message: SendMessage) {
        val replyKeyboardMarkup = ReplyKeyboardMarkup()
        val commands = arrayListOf<KeyboardRow>()
        for (destination in DownloadType.values()) {
            val commandRow = KeyboardRow()
            commandRow.add(destination.text)
            commands.add(commandRow)
        }
        replyKeyboardMarkup.resizeKeyboard = true
        replyKeyboardMarkup.oneTimeKeyboard = true
        replyKeyboardMarkup.keyboard = commands
        replyKeyboardMarkup.selective = true

        message.replyMarkup = replyKeyboardMarkup
        message.text = "Куда скачивать?"
        execute(message)
    }

    private fun saveDocumentHandler(update: Update) {
        logger.info("Saving torrent file to the database")
        val document = update.message.document
        val message = SendMessage().setChatId(update.message.chatId)
        if (document.mimeType == "application/x-bittorrent") {
            logger.info("File type is application/x-bittorrent")
            val getFileMethod = GetFile()
            getFileMethod.fileId = document.fileId
            val file: File = execute(getFileMethod)
            val downloadedFile = downloadFile(file)
            val targetFile = java.io.File(pathConfig.torrents + UUID.randomUUID() + ".torrent")
            downloadedFile.renameTo(targetFile)

            logger.info("File downloaded from Telegram Chat")
            logger.info("File file saved to ${targetFile.path}")
            chatService.saveMessage(
                update.message.chatId,
                update.message.messageId.toLong(),
                targetFile.path,
                messageType = MessageType.TORRENT
            )

            showDownloadKeyboard(message)
        }
    }

    suspend fun poll(action: () -> Boolean): Boolean {
        var initialDelay = 30_000L
        var result = false
        for (i in 1..5) {
            delay(initialDelay)
            initialDelay *= 2L
            logger.info("Attempt $i")
            result = action()
            if (result) break
        }

        logger.info("Result $result")
        return result
    }
}