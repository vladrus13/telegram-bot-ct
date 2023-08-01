package ru.vladrus13.itmobot.plugin.simple

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Command

class PingCommand : Command() {
    override val name: String = "Пинг"
    override val help: String = "Пинг"

    override fun onUpdate(update: Update, bot: TelegramLongPollingBot, user: User) {
        user.send(
            bot = bot,
            text = "Понг!"
        )
    }
}