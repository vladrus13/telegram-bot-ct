package ru.vladrus13.itmobot.plugin.practice.subjecttable

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.plugin.practice.CoroutineJob
import ru.vladrus13.itmobot.plugin.practice.PracticePlugin
import ru.vladrus13.itmobot.plugin.practice.subjecttable.SingleTable.Companion.getTextLines
import java.util.logging.Logger

class ReloadJobs(override val parent: Menu) : Menu(parent) {
    override val logger: Logger = super.logger
    override val childes: Array<Foldable> = arrayOf()

    override fun menuHelp() = """Введите all, если хотите перезагрузить все таблицы; 31**, если только первый курс; 32**, если только второй курс; Введите номер группы "3*[3-4]*""""

    override val name: String
        get() = "Запустить обновление таблиц"
    override val systemName: String
        get() = "reloadInitJob  "

    override fun isAccept(update: Update): Boolean = update.message.text == name

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return

        val result: String = if (PracticePlugin.isWorkingJob) {
            "Бот обновляет таблицы, дайте ему немного времени"
        } else {
            PracticePlugin.isWorkingJob = true

            user.send(
                bot = bot,
                text = "Бот начал обновление таблиц (таблицы)",
                replyKeyboard = getReplyKeyboard(user)
            )

            val text: String = update.message.text
            val listTexts = getTextLines(text)
            val name = listTexts[0]

            val result = when (name) {
                "all" -> CoroutineJob.runTasks()
                "31**", "32**" -> CoroutineJob.runTasks(name)
                else -> CoroutineJob.runTask(name)
            }

            PracticePlugin.isWorkingJob = false

            if (result) "Бот смог обновить запрошенные таблицы (таблицу)" else "Бот не смог обновить таблицы"
        }

        user.send(
            bot = bot,
            text = result,
            replyKeyboard = getReplyKeyboard(user)
        )
    }
}
