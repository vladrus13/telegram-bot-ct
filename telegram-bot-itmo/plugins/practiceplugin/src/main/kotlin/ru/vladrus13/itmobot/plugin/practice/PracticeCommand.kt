package ru.vladrus13.itmobot.plugin.practice

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.Chat
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Command
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu

class PracticeCommand(override val parent: Menu) : Menu(parent) {
    override val childes: Array<Foldable> = arrayOf(
        AddTable(this)
    )

    override fun menuHelp() = "Этот плагин позволяет создать таблицу для отметки задач на практиках"

    override val name: String
        get() = "Создать таблицу"
    override val systemName: String
        get() = "practice"

    override fun isAccept(update: Update): Boolean = update.message.text == name

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (classicUpdate(update, bot, user)) return
        unknownCommand(bot, user)
    }
}