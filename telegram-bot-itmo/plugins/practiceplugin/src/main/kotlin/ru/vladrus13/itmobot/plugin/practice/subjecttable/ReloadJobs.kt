package ru.vladrus13.itmobot.plugin.practice.subjecttable

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.plugin.practice.PracticePlugin
import java.util.logging.Logger

class ReloadJobs(override val parent: Menu) : Menu(parent) {
    override val logger: Logger = super.logger
    override val childes: Array<Foldable> = arrayOf()

    override fun menuHelp() = "Вводить ничего не требуется"

    override val name: String
        get() = "Продолжить обновление таблиц"
    override val systemName: String
        get() = "reloadInitJob"

    override fun isAccept(update: Update): Boolean = update.message.text == name

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return

        val result: String =
            if (PracticePlugin.isWorkingJob) "Бот обновляет таблицы, дайте ему немного времени"
            else {
                PracticePlugin.isWorkingJob = true
                "Бот скоро запустит обновление таблиц"
            }

        user.send(
            bot = bot,
            text = result,
            replyKeyboard = getReplyKeyboard(user)
        )
    }
}
