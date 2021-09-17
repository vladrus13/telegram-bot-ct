package ru.vladrus13.itmobot.bot

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.Launcher
import ru.vladrus13.itmobot.bean.Chat
import ru.vladrus13.itmobot.database.DataBase
import ru.vladrus13.itmobot.properties.InitialProperties
import ru.vladrus13.itmobot.service.UserService
import ru.vladrus13.itmobot.tables.TableGroupsHolder
import ru.vladrus13.itmobot.utils.Messager
import ru.vladrus13.itmobot.utils.PathsUtils
import ru.vladrus13.itmobot.utils.Writer
import java.util.*
import java.util.logging.Logger

class ItmoBot : TelegramLongPollingBot() {

    private var token = ""
    private val logger: Logger = Launcher.logger
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
        val user = UserService.get(update.message.chatId!!)
        if (update.message.chat.userName != null) {
            user.username = update.message.chat.userName
        }
        if (InitialProperties.mainProperties.getProperty("DEBUG_MODE") == "true" && user.username == "@vladrus13") {
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
        val current = mainFolder.folder(LinkedList(PathsUtils.foldersSplit(user.path)), user.getPlugins())
        current.get(update, this, user)
        UserService.set(user)
    }

    private fun onChat(update: Update) {
        if (!TableGroupsHolder.isReady) {
            return
        }
        if (!update.message.text.startsWith("/")) {
            return
        }
        val user = UserService.get(update.message.from.id)
        user.username = update.message.from.userName
        val chat = DataBase.get<Chat>(update.message.chatId)
        if (update.message.chat.title != null) {
            logger.info("Receive message from chat ${update.message.chat.title}: ${update.message.text}")
            chat.name = update.message.chat.title
        } else {
            logger.info("Receive message from chat with chatId: ${update.message.chatId}: ${update.message.text}")
        }
        try {
            val current = mainFolder.folder(
                LinkedList(PathsUtils.foldersChatSplit(update.message.text!!)),
                user.getPlugins()
            )
            current.get(update, this, user, chat)
            UserService.set(user)
            DataBase.put(chat.chatId, chat)
        } catch (e: Exception) {
            Writer.printStackTrace(logger, e)
            execute(
                Messager.getMessage(
                    chatId = chat.chatId,
                    text = "Произошла ошибка во время исполнения команды!"
                )
            )
        }
    }

    override fun onUpdateReceived(update: Update?) {
        if (update == null || update.message == null || update.message.chatId == null) {
            return
        }
        val chatId = update.message.chatId
        if (update.hasMessage()) {
            if (update.message.text == null) return
            try {
                if (update.message.chat.isGroupChat || update.message.chat.isSuperGroupChat) {
                    onChat(update)
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