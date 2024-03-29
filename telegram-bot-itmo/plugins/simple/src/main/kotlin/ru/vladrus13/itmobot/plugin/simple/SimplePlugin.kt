package ru.vladrus13.itmobot.plugin.simple

import ru.vladrus13.itmobot.bot.MainFolder
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.database.DataBaseEntity
import ru.vladrus13.itmobot.plugins.Plugin
import kotlin.reflect.KClass

class SimplePlugin : Plugin() {
    override val name: String
        get() = "Простой плагин"
    override val systemName: String
        get() = "simplePlugin"
    override val password: String?
        get() = null
    override val isAvailableUser: Boolean
        get() = true
    override val isAvailableChat: Boolean
        get() = true

    override fun getDataBases(): List<Pair<KClass<*>, DataBaseEntity<*>>> = emptyList()

    override suspend fun init() {}

    override fun addFoldable(current: Foldable): List<Pair<Plugin, Foldable>> {
        return when (current) {
            is MainFolder -> {
                arrayListOf(Pair(this, PingCommand(current)))
            }
            else -> arrayListOf()
        }
    }
}