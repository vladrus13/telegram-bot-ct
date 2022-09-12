package ru.vladrus13.itmobot.plugin.homework

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu

class HomeworkCommand(override val parent : Menu) : Menu(parent) {
    override val childes: Array<Foldable> = arrayOf(
        NewTeamCommand(this),
        AddHWCommand(this),
        EditHWTeamCommand(this),
        JoinTeamHWCommand(this),
        LeaveTeamHWCommand(this),
        RemoveHWTeam(this)
    )

    override fun menuHelp(): String = "Раздел домашних заданий"

    override val name: String = "ДЗ"
    override val systemName: String = "homework"

    override fun isAccept(update: Update): Boolean =
        update.message.text == name

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (classicUpdate(update, bot, user)) return
        unknownCommand(bot, user)
    }
}