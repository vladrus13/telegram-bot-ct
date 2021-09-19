package ru.vladrus13.itmobot.plugin.shikimori

import ru.vladrus13.itmobot.bot.MainFolder
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.database.DataBaseEntity
import ru.vladrus13.itmobot.plugins.Plugin
import kotlin.reflect.KClass

class ShikimoriPlugin : Plugin() {
    override val name: String
        get() = "Shikimori"
    override val systemName: String
        get() = "shikimori"
    override val password: String?
        get() = null
    override val isAvailableUser: Boolean
        get() = true
    override val isAvailableChat: Boolean
        get() = false

    override fun getDataBases(): List<Pair<KClass<*>, DataBaseEntity<*>>> = listOf()

    override fun init() {}

    override fun addFoldable(current: Foldable): List<Pair<Plugin, Foldable>> {
        return when (current) {
            is MainFolder -> {
                arrayListOf(Pair(this, GetCommand(current)))
            }
            else -> arrayListOf()
        }
    }
}