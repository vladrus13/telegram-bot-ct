package ru.vladrus13.itmobot.database.entities

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ru.vladrus13.itmobot.bean.Settings
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.bean.UserPath
import ru.vladrus13.itmobot.bot.MainMenu
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.database.DataBaseEntity

class UserParser @Inject constructor(private val mainMenu: MainMenu) : DataBaseEntity<User>() {
    override val name: String = "Users"

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

    override val table: Table = Users

    override val columnId: Column<Long> = Users.id

    override fun getNew(chatId: Long): User =
        User(chatId, path = UserPath(mutableListOf(mainMenu)))

    override fun set(o: User, it: UpdateBuilder<Number>) {
        it[Users.role] = o.role
        it[Users.group] = o.group
        it[Users.name] = o.name
        it[Users.username] = o.username
        it[Users.settings] = o.settings.getByte()
        it[Users.path] = o.path.toString()
        it[Users.notification] = o.notification
    }


    override fun get(result: ResultRow): User = User(
        result[Users.id],
        result[Users.role],
        result[Users.group],
        result[Users.name],
        result[Users.username],
        Settings(result[Users.settings]),
        getPath(result[Users.path]),
        result[Users.notification]
    )


    fun getPath(s: String): UserPath {
        return try {
            val objectMapper = ObjectMapper()
            val node = objectMapper.readTree(s)
            val objectPathText = node["objectPath"].asText()
            var current: Menu = mainMenu
            val objectPath = mutableListOf(current)
            if (objectPathText != null) {
                val pathParts = objectPathText.split("/")
                // first is always main menu
                for (part in pathParts.drop(1)) {
                    current = current.children.find { part in it.name } as Menu
                    objectPath.add(current)
                }
            }

            UserPath(objectPath)
        } catch (e: JsonParseException) {
            UserPath(mutableListOf(mainMenu))
        }
    }
}