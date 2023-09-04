package ru.vladrus13.itmobot.plugin.practice

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.plugin.practice.subjecttable.NeercTable
import java.util.logging.Logger

class NeercTransit(override val parent: Menu) : Menu(parent) {
    override val logger: Logger = super.logger
    override val childes: Array<Foldable> = arrayOf(
        NeercTable(this)
    )

    override fun menuHelp() = "NeercTransit page"

    override val name: String
        get() = "NeercTransit"
    override val systemName: String
        get() = "neercSubject"

    override fun isAccept(update: Update): Boolean = update.message.text == name

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (classicUpdate(update, bot, user)) return
        unknownCommand(bot, user)
    }
}