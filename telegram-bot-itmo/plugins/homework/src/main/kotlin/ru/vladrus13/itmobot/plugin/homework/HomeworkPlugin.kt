package ru.vladrus13.itmobot.plugin.homework

import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.database.DataBaseEntity
import ru.vladrus13.itmobot.plugins.Plugin
import kotlin.reflect.KClass

class HomeworkPlugin(private val homeworkMenu: HomeworkMenu) : Plugin() {
    override val name = "HW плагин"

    override fun getDataBases(): List<Pair<KClass<*>, DataBaseEntity<*>>> = listOf(
        Pair(TeamDatabase.Team::class, TeamDatabase),
        Pair(TeamRoleDatabase.TeamRole::class, TeamRoleDatabase),
        Pair(TeamTaskDatabase.TeamTask::class, TeamTaskDatabase)
    )

    override fun getMainFoldable(): Foldable {
        return homeworkMenu
    }
}