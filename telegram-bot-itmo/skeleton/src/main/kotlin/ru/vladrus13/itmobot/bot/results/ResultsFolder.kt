package ru.vladrus13.itmobot.bot.results

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu

class ResultsFolder(
    override val parent: Menu
) : Menu(parent) {
    override val childes: Array<Foldable>
        get() = arrayOf(ResultsGet(this))

    override fun menuHelp(): String = "Меню результатов"

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return
        if (mapping(update, bot, user)) return
        unknownCommand(bot, user)
    }

    override val name: String = "Результаты"
    override val systemName: String = "results"

    override fun isAccept(update: Update): Boolean = update.message.text == name
}