package ru.vladrus13.itmobot.bot

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.bot.results.ResultsFolder
import ru.vladrus13.itmobot.bot.schedule.ScheduleFolder
import ru.vladrus13.itmobot.bot.settings.SettingsFolder
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu

class MainFolder : Menu(null) {
    override val childes: Array<Foldable>
        get() = arrayOf(
            LinkCommand(this),
            ScheduleFolder(this),
            SettingsFolder(this),
            PluginFolder(this),
            StartCommand(this),
            ResultsFolder(this)
        )
    override val name: String
        get() = "main"
    override val systemName: String
        get() = "main"
    override val path: String
        get() = "main"

    override fun menuHelp(): String {
        return "Главное меню."
    }

    override val parent: Menu?
        get() = null

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (classicUpdate(update, bot, user)) return
        unknownCommand(bot, user)
    }

    override fun isAccept(update: Update): Boolean {
        return true
    }
}