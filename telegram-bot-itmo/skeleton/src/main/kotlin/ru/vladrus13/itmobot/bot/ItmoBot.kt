package ru.vladrus13.itmobot.bot

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.database.DataBase
import ru.vladrus13.itmobot.properties.InitialProperties
import ru.vladrus13.itmobot.tables.TableGroupsHolder
import ru.vladrus13.itmobot.utils.Messager
import ru.vladrus13.itmobot.utils.PathsUtils
import ru.vladrus13.itmobot.utils.Writer
import java.util.*
import java.util.logging.Logger

object ItmoBot : TelegramLongPollingBot() {

    private var token = ""
    private val logger: Logger = InitialProperties.logger
    private val mainFolder = MainFolder()

    private fun onUser(update: Update) {
        if (!TableGroupsHolder.isReady) {
            logger.info("Receive ignored message from user with chatId: ${update.message.chatId}: ${update.message.text}")
            execute(
                Messager.getMessage(
                    chatId = update.message.chatId,
                    text = "Просим прощения, в данный момент бот только начал свою работу, и поэтому он не успел прогрузить все таблицы. Пожалуйста, повторите команду чуть позднее"
                )
            )
            return
        }
        val user = DataBase.get<User>(update.message.chatId!!)
        if (update.message.chat.userName != null) {
            user.username = update.message.chat.userName
        }
        if (InitialProperties.mainProperties.getProperty("DEBUG_MODE") == "true" && user.username != "vladrus13") {
            logger.info("Receive ignored message from user: ${user.username}: ${update.message.text}")
            execute(
                Messager.getMessage(
                    chatId = update.message.chatId,
                    text = "Просим прощения, в данный момент бот находится в режиме разработки. Пожалуйста, повторите команду чуть позднее"
                )
            )
            return
        }
        if (update.message.chatId != null) {
            logger.info("Receive message from user: ${user.username}: ${update.message.text}")
        }
        val current = mainFolder.folder(LinkedList(PathsUtils.foldersSplit(user.path.getPath())), user.getPlugins())
        current.get(update, this, user)
        DataBase.put(user.chatId, user)
    }

    override fun onUpdateReceived(update: Update?) {
        if (update == null || update.message == null || update.message.chatId == null) {
            return
        }
        val chatId = update.message.chatId
        if (update.hasMessage()) {
            if (update.message.text == null) update.message.text = ""
            try {
                if (update.message.chat.isGroupChat || update.message.chat.isSuperGroupChat) {
                    logger.warning("Chats are not supported: received update from chat with id [${update.message.chatId}]," +
                            " update [${update.message}]")
                } else {
                    if (update.message.isUserMessage) {
                        onUser(update)
                    }
                }
            } catch (e: Exception) {
                Writer.printStackTrace(logger, e)
                execute(
                    Messager.getMessage(
                        chatId = chatId,
                        text = "Произошла ошибка во время исполнения команды!"
                    )
                )
            }
        } else {
            execute(
                Messager.getMessage(
                    chatId = chatId,
                    text = "Простите, бот не понимает данного вида сообщений"
                )
            )
        }
    }

    override fun getBotToken(): String {
        if (token.isEmpty()) {
            token = InitialProperties.mainProperties.getProperty("BOT_TOKEN")
            if (token.isEmpty()) {
                logger.severe("Bot token not found. Please, check \"BOT_TOKEN\" on main.properties")
            }
        }
        return token
    }

    override fun getBotUsername(): String {
        return "ParseScheduleBot"
    }
}