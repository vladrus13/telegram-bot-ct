package ru.vladrus13.itmobot.bot.schedule

import com.google.inject.Inject
import ru.vladrus13.itmobot.tables.schedule.ScheduleRegistry
import java.util.*

class RelativeScheduleCommand @Inject constructor(scheduleRegistry: ScheduleRegistry) :
    AbstractScheduleCommand(scheduleRegistry) {
    override val name = listOf("Сегодня", "Завтра")
    override fun getDay(text: String): Int {
        return (Calendar.getInstance()[Calendar.DAY_OF_WEEK] + 5 + name.indexOf(text)) % 7
    }
}

class AllScheduleCommand @Inject constructor(scheduleRegistry: ScheduleRegistry) :
    AbstractScheduleCommand(scheduleRegistry) {
    override val name = listOf("Все")
    override fun getDay(text: String) = null
}

class AllWeekScheduleCommand @Inject constructor(scheduleRegistry: ScheduleRegistry) :
    AbstractScheduleCommand(scheduleRegistry) {
    override val name =
        listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье")

    override fun getDay(text: String) = name.indexOf(text)
}