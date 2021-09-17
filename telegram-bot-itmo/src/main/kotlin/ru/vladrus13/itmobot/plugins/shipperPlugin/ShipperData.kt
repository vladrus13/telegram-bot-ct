package ru.vladrus13.itmobot.plugins.shipperPlugin

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import ru.vladrus13.itmobot.database.DataBaseEntity
import ru.vladrus13.itmobot.database.DataBaseParser

class ShipperGroup(
    val chatId: Long,
    val users: MutableList<Long>,
    val last: Long
) {
    constructor(chatId: Long, users: String, last: Long) : this(
        chatId,
        if (users.isBlank()) mutableListOf<Long>() else users.split("~~").map { it.toLong() }.toMutableList(),
        last
    )

    fun getUsers() = users.joinToString(separator = "~~") { it.toString() }
}

class ShipperPair(
    val chatIdGroup: Long,
    var chatIdFirst: Long = 0L,
    var chatIdSecond: Long = 0L
)

object ShipperGroupTable : Table() {
    val id = long("chat_id").uniqueIndex()
    val users = text("users")
    val last = long("last")
}

object ShipperPairTable : Table() {
    val idGroup = long("chat_id_group")
    val idFirst = long("chat_id_first")
    val idSecond = long("chat_id_second")
}

class ShipperGroupParser : DataBaseEntity<ShipperGroup>() {
    override val name: String
        get() = "ShipperGroups"
    override val table: Table
        get() = ShipperGroupTable
    override val columnId: Column<Long>
        get() = ShipperGroupTable.id

    override fun getNew(chatId: Long): ShipperGroup = ShipperGroup(chatId, "", 0)

    override fun set(o: ShipperGroup, it: UpdateBuilder<Number>) {
        it[ShipperGroupTable.users] = o.getUsers()
        it[ShipperGroupTable.last] = o.last
    }

    override fun get(result: ResultRow): ShipperGroup = ShipperGroup(
        result[ShipperGroupTable.id],
        result[ShipperGroupTable.users],
        result[ShipperGroupTable.last]
    )
}

class ShipperPairParser : DataBaseEntity<ShipperPair>() {
    override val name: String
        get() = "ShipperPair"
    override val table: Table
        get() = ShipperPairTable
    override val columnId: Column<Long>
        get() = ShipperPairTable.idGroup

    override fun getNew(chatId: Long): ShipperPair = ShipperPair(chatId)

    override fun set(o: ShipperPair, it: UpdateBuilder<Number>) {
        it[ShipperPairTable.idFirst] = o.chatIdFirst
        it[ShipperPairTable.idSecond] = o.chatIdSecond
    }

    override fun get(result: ResultRow): ShipperPair = ShipperPair(
        result[ShipperPairTable.idGroup],
        result[ShipperPairTable.idFirst],
        result[ShipperPairTable.idSecond]
    )

    fun put(pair: ShipperPair) {
        transaction(DataBaseParser.connection) {
            table.insert {
                it[ShipperPairTable.idGroup] = pair.chatIdGroup
                it[ShipperPairTable.idFirst] = pair.chatIdFirst
                it[ShipperPairTable.idSecond] = pair.chatIdSecond
            }
        }
    }

    fun getByGroup(chatId: Long): List<ShipperPair> {
        return getAllByFilter { ShipperPairTable.idGroup eq chatId }
    }

    fun getByUser(chatId: Long): List<ShipperPair> {
        return getAllByFilter { (ShipperPairTable.idFirst eq chatId) or (ShipperPairTable.idSecond eq chatId) }
    }

    fun getByGroupAndUser(chatIdGroup: Long, chatIdFirst: Long): List<ShipperPair> {
        return getAllByFilter { (ShipperPairTable.idGroup eq chatIdGroup) and ((ShipperPairTable.idFirst eq chatIdFirst) or (ShipperPairTable.idSecond eq chatIdFirst)) }
    }

}