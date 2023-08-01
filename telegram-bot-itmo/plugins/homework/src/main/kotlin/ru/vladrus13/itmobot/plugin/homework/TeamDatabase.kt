package ru.vladrus13.itmobot.plugin.homework

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import ru.vladrus13.itmobot.database.DataBaseEntity
import ru.vladrus13.itmobot.database.DataBaseParser

class TeamDatabase {
    class Team(
        val id: Long,
        val name: String,
        val password: String?
    )

    object TeamTable : Table() {
        val id = long("id").uniqueIndex().autoIncrement().primaryKey()
        val name = varchar("name", 255).uniqueIndex()
        val password = varchar("password", 255).nullable()
    }

    companion object : DataBaseEntity<Team>() {
        override val name: String = "HomeworkTeam"
        override val table: Table = TeamTable
        override val columnId: Column<Long> = TeamTable.id

        override fun getNew(chatId: Long): Team =
            throw UnsupportedOperationException()

        fun getById(id: Long): Team? {
            return transaction(DataBaseParser.connection) {
                val entity = table.select { columnId eq id }
                when (entity.count()) {
                    0 -> null
                    1 -> get(entity.single())
                    else -> throw IllegalStateException("More than 1 entries with id: $id")
                }
            }
        }

        fun deleteByTeamId(teamId: Long) =
            transaction(DataBaseParser.connection) {
                val temp: Int? = null
                table.deleteWhere(offset = temp) { TeamTable.id eq teamId }
            }

        fun getByName(name: String): Team? {
            return transaction(DataBaseParser.connection) {
                val entity = table.select { TeamTable.name eq name }
                when (entity.count()) {
                    0 -> null
                    1 -> get(entity.single())
                    else -> throw IllegalStateException("More than 1 entries with name: $name")
                }
            }
        }

        fun getAllTeamsExcept(teams: List<Long>): List<String> {
            return getAll().filter { !teams.contains(it.id) }.map { it.name }
        }

        fun getAllTeamsWithIdFrom(teams: List<Long>): List<String> {
            return getAll().filter { teams.contains(it.id) }.map { it.name }
        }


        fun getAll(): List<Team> =
            getAllByFilter { TeamTable.id eq TeamTable.id }

        fun put(teamName: String, password: String?): Boolean {
            return transaction(DataBaseParser.connection) {
                if (table.select { TeamTable.name eq name }.count() > 0) false
                else {
                    table.insert {
                        it[TeamTable.name] = teamName
                        it[TeamTable.password] = password
                    }
                    true
                }

            }
        }

        override fun get(result: ResultRow): Team =
            Team(
                result[TeamTable.id],
                result[TeamTable.name],
                result[TeamTable.password]
            )

        override fun set(o: Team, it: UpdateBuilder<Number>) {
            it[TeamTable.id] = o.id
            it[TeamTable.name] = o.name
            it[TeamTable.password] = o.password
        }
    }
}