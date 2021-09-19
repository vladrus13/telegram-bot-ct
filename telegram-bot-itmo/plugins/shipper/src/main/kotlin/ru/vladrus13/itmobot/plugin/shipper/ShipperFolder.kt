package ru.vladrus13.itmobot.plugin.shipper

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu

class ShipperFolder(override val parent: Menu) : Menu(parent) {
    override val childes: Array<Foldable>
        get() = arrayOf(RegisterShipperCommand(this), DeregisterShipperCommand(this), ShipperShipperCommand(this))

    override fun menuHelp(): String = "Меню шиппера"

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return
        if (mapping(update, bot, user)) return
        unknownCommand(bot, user)
    }

    override val name: String
        get() = "Шиппер"
    override val systemName: String
        get() = "shipper"

    override fun isAccept(update: Update): Boolean {
        return update.message.text!! == name
    }
}