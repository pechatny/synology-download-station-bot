package com.pechatnikov.telegram.bot.dsget.bot

import com.pechatnikov.telegram.bot.dsget.configuration.TelegramBotConfig
import com.pechatnikov.telegram.bot.dsget.db.models.Message
import com.pechatnikov.telegram.bot.dsget.db.models.User
import com.pechatnikov.telegram.bot.dsget.models.DownloadType
import com.pechatnikov.telegram.bot.dsget.services.AuthorizeService
import com.pechatnikov.telegram.bot.dsget.services.ChatService
import com.pechatnikov.telegram.bot.dsget.services.DownloadStationService
import com.pechatnikov.telegram.bot.dsget.services.toText
import com.pechatnikov.telegram.bot.dsget.utils.Utils.Companion.sendMessage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.bytebuddy.utility.RandomString
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.File
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

@Component
class DsGetBot(
    val botConfig: TelegramBotConfig,
    val downloadStationService: DownloadStationService,
    val authorizeService: AuthorizeService,
    val chatService: ChatService
) : TelegramLongPollingBot() {
    private val logger = LoggerFactory.getLogger(DsGetBot::class.java)
    val START_COMMAND = "/start"
    val LIST_COMMAND = "/list"
    val HELP_COMMAND = "/help"
    val INSTRUCTION = "Для того чтобы скачать фильм или сериал, отправьте .torrent файл в этот чат."
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
                logger.info("User not authrized, starting authorization")
                authorizeService.authorize(user, update)?.let { execute(it) }
            } else {
                val messageText = update.message.text
                if (messageText != null && (messageText.startsWith(START_COMMAND) || messageText.startsWith(HELP_COMMAND))) {
                    logger.info("/start or /help command executing")
                    execute(sendMessage(INSTRUCTION, update))
                } else if (messageText != null && (messageText.startsWith(LIST_COMMAND))) {
                    listCommandHandler(update)
                } else if (update.message.hasDocument()) {
                    saveDocumentHandler(update)
                } else if (messageText != null && DownloadType.containsText(update.message.text)) {
                    createDownloadTaskHandler(update)
                    GlobalScope.launch {
                        delay(5_000)
                        listCommandHandler(update)
                    }
                    GlobalScope.launch {
                        delay(31_000)
                        listCommandHandler(update)
                    }
                } else {
                    execute(sendMessage("Отправь торрент файл в этот чат для скачивания.", update))
                }
            }
        }
    }

    private fun listCommandHandler(update: Update) {
        logger.info("/list command executing")
        val tasks = downloadStationService.getDownloadingTasks()
        execute(sendMessage(tasks.toText(), update))
    }

    private fun createDownloadTaskHandler(update: Update) {
        logger.info("Starting download task creation")
        val savedMessage: Message? = chatService.getLastDocument(update.message.chatId)
        if (savedMessage == null) {
            logger.error("Torrent file not found in the database!")
            execute(sendMessage("Торрент файл не найден", update))
            return
        }
        val fileName = RandomString(9).nextString() + ".torrent"
        val destination = DownloadType.getByText(update.message.text).path
        val message = if (downloadStationService.createTask(
                fileName = fileName,
                fileString = savedMessage.content,
                destination = destination
            )
        ) {
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
            logger.info("File downloaded from Telegram Chat")
            chatService.saveMessage(
                update.message.chatId,
                update.message.messageId.toLong(),
                downloadedFile.readBytes()
            )
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
        }

        execute(message)
    }
}