package ru.vladrus13.itmobot.plugins

import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.database.DataBaseEntity
import kotlin.reflect.KClass

abstract class Plugin {
    abstract val name: String

    abstract fun getDataBases(): List<Pair<KClass<*>, DataBaseEntity<*>>>

    abstract fun getMainFoldable(): Foldable?

    open fun onEnable(user: User) {}

    open fun onDisable(user: User) {}
}