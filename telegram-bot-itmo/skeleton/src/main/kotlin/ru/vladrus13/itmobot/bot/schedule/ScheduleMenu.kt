package ru.vladrus13.itmobot.bot.schedule

import com.google.inject.Inject
import ru.vladrus13.itmobot.command.Menu

class ScheduleMenu @Inject constructor(
    today: TodayScheduleCommand,
    tommorow: TommorowScheduleCommand,
    all: AllScheduleCommand,
    monday: MondayScheduleCommand,
    tuesday: TuesdayScheduleCommand,
    wednesday: WednesdayScheduleCommand,
    thursday: ThursdayScheduleCommand,
    friday: FridayScheduleCommand,
    saturday: SaturdayScheduleCommand,
    sunday: SundayScheduleCommand
) : Menu(
    arrayOf(
        today,
        tommorow,
        all,
        monday,
        tuesday,
        wednesday,
        thursday,
        friday,
        saturday,
        sunday
    )
) {
    override val menuHelp: String = "Меню расписания"
    override val name: String = "Меню расписания"
}