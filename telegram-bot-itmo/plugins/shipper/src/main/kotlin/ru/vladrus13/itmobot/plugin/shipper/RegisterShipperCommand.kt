package ru.vladrus13.itmobot.plugin.shipper

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Command
import ru.vladrus13.itmobot.command.Menu

class RegisterShipperCommand(override val parent: Menu) : Command() {
    override fun help(): String = "Регистрация в шипербота"

    override val name: String
        get() = "Регистрация"
    override val systemName: String
        get() = "register"

    override fun isAccept(update: Update): Boolean = update.message.text!! == name

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        user.send(
            bot = bot,
            text = "Регистрация работает только в группах!"
        )
    }

}