package ru.vladrus13.itmobot.bean

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import ru.vladrus13.itmobot.database.DataBase
import ru.vladrus13.itmobot.updates.Updates
import ru.vladrus13.itmobot.utils.Messager
import java.nio.file.Path

open class Chatted(
    val chatId: Long,
    var group: String? = null,
    var settings: Settings = Settings(3),
    private var updateBotUser: UpdateBotUser? = null
) {

    fun send(
        bot: TelegramLongPollingBot,
        text: String,
        replyKeyboard: ReplyKeyboard? = null,
        other: ((SendMessage) -> Unit)? = null
    ) {
        Messager.sendMessage(
            bot, chatId, text, replyKeyboard, other
        )
        val texts = getUpdateTexts()
        for (pair in texts) {
            Messager.sendMessage(
                bot, chatId, "", replyKeyboard, pair.second
            )
        }
        updateBotUser = UpdateBotUser(chatId, Updates.getLast())
    }

    fun send(
        bot: TelegramLongPollingBot,
        image: Path,
        text: String? = null,
        replyKeyboard: ReplyKeyboard? = null,
        other: ((SendPhoto) -> Unit)? = null
    ) {
        Messager.sendPhoto(
            bot, chatId, image, text, replyKeyboard, other
        )
        val texts = getUpdateTexts()
        for (pair in texts) {
            Messager.sendMessage(
                bot, chatId, "", replyKeyboard, pair.second
            )
        }
        updateBotUser = UpdateBotUser(chatId, Updates.getLast())
    }

    fun getUpdateBotUser(forced: Boolean = false): UpdateBotUser {
        if (forced || updateBotUser == null) {
            updateBotUser = DataBase.get(chatId)
        }
        return updateBotUser!!
    }

    fun getUpdateTexts(forced: Boolean = false): List<Pair<String, SendMessage.() -> Unit>> {
        val updateBotUser = getUpdateBotUser(forced)
        return Updates.getUpdates(updateBotUser.version)
    }
}