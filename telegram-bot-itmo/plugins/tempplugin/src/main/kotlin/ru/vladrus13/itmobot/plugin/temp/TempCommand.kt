package ru.vladrus13.itmobot.plugin.temp

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.Chat
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Command
import ru.vladrus13.itmobot.command.Menu

class TempCommand(override val parent: Menu) : Command() {
    override fun help(): String = "Докажи, иначе гей"

    override val name: String
        get() = "Не пидор, а гей"
    override val systemName: String
        get() = "gay"

    override fun isAccept(update: Update): Boolean = true

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        user.send(
            bot = bot,
            text = update.message.text ?: "---"
        )
    }

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User, chat: Chat) {
        chat.send(
            bot = bot,
            text = update.message.text ?: "---"
        )
    }
}