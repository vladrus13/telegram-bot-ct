package ru.vladrus13.itmobot.command

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.Chat
import ru.vladrus13.itmobot.bean.User
import java.util.logging.Logger

interface Foldable {
    val logger: Logger
        get() = Logger.getLogger(Foldable::name.toString())

    fun help(): String
    val name: String
    val systemName: String
    val parent: Menu?
    val path: String
        get() = if (parent == null) systemName else (parent!!.path + "/" + systemName)

    fun isAccept(update: Update): Boolean
    fun get(update: Update, bot: TelegramLongPollingBot, user: User)
    fun get(update: Update, bot: TelegramLongPollingBot, user: User, chat: Chat)
}