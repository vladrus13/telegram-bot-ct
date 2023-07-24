package ru.vladrus13.itmobot.plugin.shipper

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Command
import ru.vladrus13.itmobot.command.Menu

class ShipperShipperCommand(override val parent: Menu) : Command() {
    override fun help(): String = "Вызов команды шипа"

    val table = ShipperPairParser()

    override val name: String = "Шиппер!"
    override val systemName: String = "ship"

    override fun isAccept(update: Update): Boolean = update.message.text!! == name

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        user.send(
            bot = bot,
            text = "Шипперинг работает только в группах!"
        )
    }

}