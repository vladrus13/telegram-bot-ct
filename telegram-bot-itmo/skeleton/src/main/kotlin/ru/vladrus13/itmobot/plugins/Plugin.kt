package ru.vladrus13.itmobot.plugins

import ru.vladrus13.itmobot.bean.Chatted
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.database.DataBaseEntity
import kotlin.reflect.KClass

abstract class Plugin {
    abstract val name: String
    abstract val systemName: String
    abstract val password: String?

    abstract val isAvailableUser: Boolean
    abstract val isAvailableChat: Boolean

    abstract fun getDataBases(): List<Pair<KClass<*>, DataBaseEntity<*>>>

    abstract suspend fun init()

    abstract fun addFoldable(current: Foldable): List<Pair<Plugin, Foldable>>

    open fun onEnable(chatted: Chatted) {}

    open fun onDisable(chatted: Chatted) {}
}