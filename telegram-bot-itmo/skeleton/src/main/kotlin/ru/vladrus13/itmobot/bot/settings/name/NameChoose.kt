package ru.vladrus13.itmobot.bot.settings.name

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu

class NameChoose(override val parent: Menu) : Menu(parent) {
    override val childes: Array<Foldable>
        get() = arrayOf()

    override fun menuHelp(): String {
        return "Пункт выбора имени"
    }

    override val name: String
        get() = "Выбор имени"
    override val systemName: String
        get() = "chooseName"

    override fun isAccept(update: Update): Boolean {
        return update.message.text!! == name
    }

    override val onEnter: String = "Напишите ваше имя. Оно будет использовано в качестве опознавателя для табличек"

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return
        user.name = update.message.text!!
        user.send(
            bot = bot,
            text = "Ваше имя успешно изменено на ${user.name}"
        )
    }
}