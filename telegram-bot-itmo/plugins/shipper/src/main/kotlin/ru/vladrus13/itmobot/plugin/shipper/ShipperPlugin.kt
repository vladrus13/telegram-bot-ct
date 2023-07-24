package ru.vladrus13.itmobot.plugin.shipper

import ru.vladrus13.itmobot.bot.MainFolder
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.database.DataBaseEntity
import ru.vladrus13.itmobot.plugins.Plugin
import kotlin.reflect.KClass

class ShipperPlugin : Plugin() {
    override val name: String = "Шиппер"
    override val systemName: String = "shipperPlugin"
    override val password: String? = null

    override fun getDataBases(): List<Pair<KClass<*>, DataBaseEntity<*>>> {
        return listOf(
            Pair(ShipperGroup::class, ShipperGroupParser()),
            Pair(ShipperPair::class, ShipperPairParser())
        )
    }

    override fun init() {}

    override fun addFoldable(current: Foldable): List<Pair<Plugin, Foldable>> {
        return when (current) {
            is MainFolder -> {
                arrayListOf(Pair(this, ShipperFolder(current)))
            }
            else -> arrayListOf()
        }
    }
}