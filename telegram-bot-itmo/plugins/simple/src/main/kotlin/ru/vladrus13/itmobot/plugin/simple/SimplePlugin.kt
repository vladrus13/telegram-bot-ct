package ru.vladrus13.itmobot.plugin.simple

import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.database.DataBaseEntity
import ru.vladrus13.itmobot.plugins.Plugin
import kotlin.reflect.KClass

class SimplePlugin(private val pingCommand: PingCommand) : Plugin() {
    override val name = "Простой плагин"

    override fun getDataBases(): List<Pair<KClass<*>, DataBaseEntity<*>>> = emptyList()
    override fun getMainFoldable(): Foldable = pingCommand
}