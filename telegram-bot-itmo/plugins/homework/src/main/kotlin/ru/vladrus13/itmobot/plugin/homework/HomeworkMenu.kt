package ru.vladrus13.itmobot.plugin.homework

import com.google.inject.Inject
import ru.vladrus13.itmobot.command.Menu

class HomeworkMenu @Inject constructor(
    joinTeamCommand: JoinTeamCommand,
    leaveTeamCommand: LeaveTeamCommand,
    newTeamCommand: NewTeamCommand,
    removeTeamCommand: RemoveTeamCommand,
    editTeamCommand: EditTeamCommand,
    addHWCommand: AddHWCommand
) : Menu(
    arrayOf(
        joinTeamCommand,
        leaveTeamCommand,
        newTeamCommand,
        removeTeamCommand,
        editTeamCommand,
        addHWCommand
    )
) {
    override val menuHelp = "Раздел домашних заданий"
    override val name = "ДЗ"
}