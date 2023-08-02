package ru.vladrus13.itmobot.bot.schedule

import com.google.inject.Inject
import ru.vladrus13.itmobot.command.Menu

class ScheduleMenu @Inject constructor(
    relative: RelativeScheduleCommand,
    all: AllScheduleCommand,
    allWeek: AllWeekScheduleCommand
) : Menu(
    arrayOf(
        relative,
        all,
        allWeek
    )
) {
    override val menuHelp = "Меню расписания"
    override val name = listOf("Меню расписания")
}