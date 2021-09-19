package ru.vladrus13.itmobot.database.entities

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ru.vladrus13.itmobot.bean.Chat
import ru.vladrus13.itmobot.database.DataBaseEntity

class ChatParser : DataBaseEntity<Chat>() {

    companion object {
        object Chats : Table() {
            val id = long("chat_id").uniqueIndex()
            val group = varchar("group", 25).nullable()
            val name = varchar("name", 255).nullable()
        }
    }

    override val name: String = "Chats"
    override val table: Table = Chats
    override val columnId: Column<Long> = Chats.id

    override fun getNew(chatId: Long): Chat = Chat(chatId)

    override fun set(o: Chat, it: UpdateBuilder<Number>) {
        it[Chats.group] = o.group
        it[Chats.name] = o.name
    }

    override fun get(result: ResultRow): Chat = Chat(
        result[Chats.id],
        result[Chats.group],
        result[Chats.name]
    )
}