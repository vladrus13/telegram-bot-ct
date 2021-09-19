package ru.vladrus13.itmobot.database.entities

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ru.vladrus13.itmobot.bean.UpdateBotUser
import ru.vladrus13.itmobot.database.DataBaseEntity
import ru.vladrus13.itmobot.updates.Updates

class UpdateUserParser : DataBaseEntity<UpdateBotUser>() {

    companion object {
        object UpdateUserTable : Table() {
            val id = long("chat_id").uniqueIndex()
            val version = varchar("name", 255).nullable()
        }
    }

    override val name: String = "UpdatesUser"
    override val table: Table = UpdateUserTable
    override val columnId: Column<Long> = UpdateUserTable.id

    override fun getNew(chatId: Long): UpdateBotUser {
        return UpdateBotUser(chatId)
    }

    override fun set(o: UpdateBotUser, it: UpdateBuilder<Number>) {
        it[UpdateUserTable.version] = o.version
    }

    override fun get(result: ResultRow): UpdateBotUser {
        return UpdateBotUser(
            chatId = result[UpdateUserTable.id],
            version = result[UpdateUserTable.version] ?: Updates.getLast()
        )
    }
}