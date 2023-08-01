package ru.vladrus13.itmobot.bot.settings

import com.google.inject.Inject
import ru.vladrus13.itmobot.command.Menu

class SettingsMenu @Inject constructor(
    groupChooseMenu: GroupChooseMenu,
    nameChooseMenu: NameChooseMenu,
    scheduleSettingsMenu: ScheduleSettingsMenu,
    subjectSettingsMenu: SubjectSettingsMenu
) : Menu(arrayOf(groupChooseMenu, nameChooseMenu, scheduleSettingsMenu, subjectSettingsMenu)) {
    override val menuHelp: String
        get() = "Меню настроек"
    override val name: String
        get() = "Настройки"
}