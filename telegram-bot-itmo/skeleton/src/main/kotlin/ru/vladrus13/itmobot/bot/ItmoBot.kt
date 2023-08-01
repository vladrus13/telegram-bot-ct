package ru.vladrus13.itmobot.bot

import org.apache.logging.log4j.kotlin.Logging
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.database.DataBase
import ru.vladrus13.itmobot.properties.InitialProperties
import ru.vladrus13.itmobot.tables.PointTablesReloader
import ru.vladrus13.itmobot.utils.Messager

class ItmoBot(
    // Reloader нужен здесь только для того, чтобы на момент создания ItmoBot таблицы баллов уже были загружены
    @Suppress("UNUSED_PARAMETER") pointTablesReloader: PointTablesReloader
) : TelegramLongPollingBot(), Logging {

    private var token = ""

    private fun onUser(update: Update) {
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
        logger.info("Receive message from user: ${user.username}: ${update.message.text}")
        user.path.last().onUpdate(update, this, user)
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
                    logger.warn(
                        "Chats are not supported: received update from chat with id [${update.message.chatId}]," +
                                " update [${update.message}]"
                    )
                } else {
                    if (update.message.isUserMessage) {
                        onUser(update)
                    }
                }
            } catch (e: Exception) {
                logger.error("Something went wrong executing command", e)
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
                logger.error("Bot token not found. Please, check \"BOT_TOKEN\" on main.properties")
            }
        }
        return token
    }

    override fun getBotUsername(): String {
        return "ParseScheduleBot"
    }
}