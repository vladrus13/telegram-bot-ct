package ru.vladrus13.itmobot.bot.schedule

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Command
import ru.vladrus13.itmobot.tables.schedule.ScheduleRegistry

abstract class AbstractScheduleCommand constructor(private val scheduleRegistry: ScheduleRegistry) :
    Command() {
    override val help: String = "Получение расписания"

    override fun onUpdate(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (user.group == null) {
            user.send(
                bot = bot,
                text = "У вас не выбрана группа. Пожалуйста, выберите ее в настройках"
            )
            return
        }
        val answer = scheduleRegistry.table.toStringBuilder(
            user,
            user.getSubjects(),
            getDay()
        ).toString()
        user.send(
            bot = bot,
            text = "<code>$answer</code>",
            other = {
                it.enableHtml(true)
            }
        )
    }

    abstract fun getDay(): Int?
}