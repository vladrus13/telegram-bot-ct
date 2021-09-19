package ru.vladrus13.itmobot.bot.settings

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.bot.settings.group.GroupChoose
import ru.vladrus13.itmobot.bot.settings.name.NameChoose
import ru.vladrus13.itmobot.bot.settings.schedule.ScheduleSettings
import ru.vladrus13.itmobot.bot.settings.subjects.SubjectsSettingsFolder
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu

class SettingsFolder(
    override val parent: Menu
) : Menu(parent) {
    override val childes: Array<Foldable>
        get() = arrayOf(GroupChoose(this), NameChoose(this), ScheduleSettings(this), SubjectsSettingsFolder(this))

    override fun menuHelp(): String {
        return "Меню настроек"
    }

    override val path: String
        get() = super.path

    override fun isAccept(update: Update): Boolean {
        return update.message.text!! == name
    }

    override val name: String
        get() = "Настройки"
    override val systemName: String
        get() = "settings"

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return
        if (mapping(update, bot, user)) return
        unknownCommand(bot, user)
    }
}