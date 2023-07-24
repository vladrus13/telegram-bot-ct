package ru.vladrus13.itmobot.bot.schedule

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Command
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.tables.schedule.ScheduleHolder
import ru.vladrus13.itmobot.utils.TimeUtils

class GetScheduleCommand(override val parent: Menu) : Command() {
    override fun help(): String {
        return "Получение расписания"
    }

    override val name: String
        get() = "Расписание"
    override val systemName: String
        get() = "schedule"

    val names = listOf("Сегодня", "Завтра", "Все").plus(TimeUtils.days)

    override fun isAccept(update: Update): Boolean {
        return names.contains(update.message.text)
    }

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (user.group == null) {
            user.send(
                bot = bot,
                text = "У вас не выбрана группа. Пожалуйста, выберите ее в настройках"
            )
            return
        }
        val text = update.message.text!!
        val answer = if (TimeUtils.days.contains(text)) {
            val day = TimeUtils.getDayByName(text)
            ScheduleHolder.table.toStringBuilder(user, user.getSubjects(), day).toString()
        } else {
            when (text) {
                "Сегодня" -> ScheduleHolder.table.toStringBuilder(
                    user,
                    user.getSubjects(),
                    TimeUtils.getDay()
                ).toString()
                "Завтра" -> ScheduleHolder.table.toStringBuilder(
                    user,
                    user.getSubjects(),
                    TimeUtils.getDay(1)
                ).toString()
                "Все" -> ScheduleHolder.table.toStringBuilder(user, user.getSubjects()).toString()
                else -> "Неизвестный день("
            }
        }
        user.send(
            bot = bot,
            text = "<code>$answer</code>",
            other = {
                it.enableHtml(true)
            }
        )
    }

}