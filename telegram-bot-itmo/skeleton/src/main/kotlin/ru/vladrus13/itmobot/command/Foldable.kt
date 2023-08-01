package ru.vladrus13.itmobot.command

import org.apache.logging.log4j.kotlin.Logging
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User

interface Foldable : Logging {
    val name: String
    val help: String

    fun onUpdate(update: Update, bot: TelegramLongPollingBot, user: User)
}