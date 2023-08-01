package ru.vladrus13.itmobot.bot.schedule

import com.google.inject.Inject
import ru.vladrus13.itmobot.tables.schedule.ScheduleRegistry
import java.util.*

class TodayScheduleCommand @Inject constructor(scheduleRegistry: ScheduleRegistry) :
    AbstractScheduleCommand(scheduleRegistry) {
    override val name = "Сегодня"
    override fun getDay(): Int {
        return (Calendar.getInstance()[Calendar.DAY_OF_WEEK] + 5) % 7
    }
}

class TommorowScheduleCommand @Inject constructor(scheduleRegistry: ScheduleRegistry) :
    AbstractScheduleCommand(scheduleRegistry) {
    override val name = "Завтра"
    override fun getDay(): Int {
        return (Calendar.getInstance()[Calendar.DAY_OF_WEEK] + 6) % 7
    }
}

class AllScheduleCommand @Inject constructor(scheduleRegistry: ScheduleRegistry) :
    AbstractScheduleCommand(scheduleRegistry) {
    override val name = "Все"
    override fun getDay() = null
}

class MondayScheduleCommand @Inject constructor(scheduleRegistry: ScheduleRegistry) :
    AbstractScheduleCommand(scheduleRegistry) {
    override val name = "Понедельник"
    override fun getDay() = 0
}

class TuesdayScheduleCommand @Inject constructor(scheduleRegistry: ScheduleRegistry) :
    AbstractScheduleCommand(scheduleRegistry) {
    override val name = "Вторник"
    override fun getDay() = 1
}


class WednesdayScheduleCommand @Inject constructor(scheduleRegistry: ScheduleRegistry) :
    AbstractScheduleCommand(scheduleRegistry) {
    override val name = "Среда"
    override fun getDay() = 2
}

class ThursdayScheduleCommand @Inject constructor(scheduleRegistry: ScheduleRegistry) :
    AbstractScheduleCommand(scheduleRegistry) {
    override val name = "Четверг"
    override fun getDay() = 3
}

class FridayScheduleCommand @Inject constructor(scheduleRegistry: ScheduleRegistry) :
    AbstractScheduleCommand(scheduleRegistry) {
    override val name = "Пятница"
    override fun getDay() = 4
}

class SaturdayScheduleCommand @Inject constructor(scheduleRegistry: ScheduleRegistry) :
    AbstractScheduleCommand(scheduleRegistry) {
    override val name = "Суббота"
    override fun getDay() = 5
}

class SundayScheduleCommand @Inject constructor(scheduleRegistry: ScheduleRegistry) :
    AbstractScheduleCommand(scheduleRegistry) {
    override val name = "Воскресенье"
    override fun getDay() = 6
}
