package ru.vladrus13.itmobot.plugins.simplePlugin

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.Chat
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Command
import ru.vladrus13.itmobot.command.Menu

class PingCommand(override val parent: Menu) : Command() {
    override fun help(): String = "Пинг"

    override val name: String
        get() = "Пинг"
    override val systemName: String
        get() = "ping"

    override fun isAccept(update: Update): Boolean = update.message.text!! == name

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        user.send(
            bot = bot,
            text = "Понг!"
        )
    }

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User, chat: Chat) {
        chat.send(
            bot = bot,
            text = "Понг!"
        )
    }
}