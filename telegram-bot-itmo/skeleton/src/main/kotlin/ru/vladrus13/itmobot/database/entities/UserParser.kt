package ru.vladrus13.itmobot.database.entities

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ru.vladrus13.itmobot.bean.Settings
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.database.DataBaseEntity

class UserParser : DataBaseEntity<User>() {

    companion object {
        object Users : Table() {
            val id = long("chat_id").uniqueIndex()
            val role = integer("role")
            val group = varchar("group", 25).nullable()
            val name = varchar("name", 255).nullable()
            val username = varchar("username", 255).nullable()
            val settings = integer("settings")
            val path = varchar("path", 1023)
            val notification = bool("notification")
        }
    }

    override val table: Table
        get() = Users

    override val columnId: Column<Long>
        get() = Users.id

    override fun getNew(chatId: Long): User = User(chatId)

    override fun set(o: User, it: UpdateBuilder<Number>) {
        it[Users.role] = o.role
        it[Users.group] = o.group
        it[Users.name] = o.name
        it[Users.username] = o.username
        it[Users.settings] = o.settings.getByte()
        it[Users.path] = o.path
        it[Users.notification] = o.notification
    }

    override fun get(result: ResultRow): User = User(
        result[Users.id],
        result[Users.role],
        result[Users.group],
        result[Users.name],
        result[Users.username],
        Settings(result[Users.settings]),
        result[Users.path],
        result[Users.notification]
    )

    override val name: String = "Users"
}