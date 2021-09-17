package ru.vladrus13.itmobot.database.entities

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ru.vladrus13.itmobot.bean.UserPlugins
import ru.vladrus13.itmobot.database.DataBaseEntity

class PluginsParser : DataBaseEntity<UserPlugins>() {
    object Plugins : Table() {
        val id = long("chat_id").uniqueIndex()
        val plugins = varchar("plugins", 1023)
    }

    override val table: Table
        get() = Plugins
    override val columnId: Column<Long>
        get() = Plugins.id

    override fun getNew(chatId: Long): UserPlugins = UserPlugins(chatId)

    override fun set(o: UserPlugins, it: UpdateBuilder<Number>) {
        it[Plugins.plugins] = o.getPlugins()
    }

    override fun get(result: ResultRow): UserPlugins = UserPlugins(
        result[Plugins.id],
        result[Plugins.plugins],
    )

    override val name: String = "Plugins"
}