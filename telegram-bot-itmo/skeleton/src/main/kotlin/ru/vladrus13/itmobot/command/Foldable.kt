package ru.vladrus13.itmobot.command

import org.apache.logging.log4j.kotlin.Logging
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User

interface Foldable : Logging {
    fun help(): String
    val name: String
    val systemName: String
    val parent: Menu?
    val path: String
        get() = if (parent == null) systemName else (parent!!.path + "/" + systemName)

    fun isAccept(update: Update): Boolean
    fun get(update: Update, bot: TelegramLongPollingBot, user: User)
}