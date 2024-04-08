package ru.vladrus13.itmobot.plugin.practice

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.plugin.practice.subjecttable.AddUserAdmins
import ru.vladrus13.itmobot.plugin.practice.subjecttable.ReloadJobs
import ru.vladrus13.itmobot.plugin.practice.subjecttable.SingleTable
import ru.vladrus13.itmobot.plugin.practice.subjecttable.TablesForAllGroups
import java.util.logging.Logger

class NeercTransit(override val parent: Menu) : Menu(parent) {
    override val logger: Logger = super.logger
    override val childes: Array<Foldable> = arrayOf(
        SingleTable(this),
        TablesForAllGroups(this),
        ReloadJobs(this),
        AddUserAdmins(this)
    )

    override fun menuHelp() = "NeercTransit page"

    override val name: String
        get() = "ДМ"
    override val systemName: String
        get() = "neercSubject"

    override fun isAccept(update: Update): Boolean = update.message.text == name

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (classicUpdate(update, bot, user)) return
        unknownCommand(bot, user)
    }
}