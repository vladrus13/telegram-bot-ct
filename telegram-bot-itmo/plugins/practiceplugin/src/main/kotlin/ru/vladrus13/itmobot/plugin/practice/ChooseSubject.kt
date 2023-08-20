package ru.vladrus13.itmobot.plugin.practice

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu
import java.util.logging.Logger

class ChooseSubject(override val parent: Menu) : Menu(parent) {
    override val logger: Logger = super.logger
    override val childes: Array<Foldable> = arrayOf(
        NeercTransit(this)
    )

    override fun menuHelp() = "Здесь происходит выбор, откуда будут выкачиваться данные"

    override val name: String
        get() = "Выберите предмет, на который вы хотите завести таблицу"
    override val systemName: String
        get() = "chooseSubject"

    override fun isAccept(update: Update): Boolean = update.message.text == name

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (classicUpdate(update, bot, user)) return
        unknownCommand(bot, user)
    }
}