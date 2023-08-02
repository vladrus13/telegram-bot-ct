package ru.vladrus13.itmobot.plugin.homework

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.database.DataBase
import ru.vladrus13.itmobot.database.DataBaseEntity
import ru.vladrus13.itmobot.database.DataBaseParser

open class TeamRoleDatabase {
    class TeamRole(
        val userId: Long,
        val teamId: Long,
        // 1 - member, 2 - mod, 3 - admin
        val role: Int
    )

    object TeamRoleTable : Table() {
        val userId = long("userId")
        val teamId = long("teamId")
        val role = integer("role")
    }

    companion object : DataBaseEntity<TeamRole>() {
        override val name: String = "HomeworkTeamRole"
        override val table: Table = TeamRoleTable
        override val columnId: Column<Long>
            get() {
                throw UnsupportedOperationException()
            }

        override fun getNew(chatId: Long): TeamRole =
            throw UnsupportedOperationException()

        fun getByTeamId(teamId: Long) =
            getAllByFilter { TeamRoleTable.teamId eq teamId }

        fun get(userId: Long, teamId: Long): TeamRole? =
            transaction(DataBaseParser.connection) {
                val result =
                    table.select { (TeamRoleTable.userId eq userId) and (TeamRoleTable.teamId eq teamId) }
                when (result.count()) {
                    0 -> null
                    1 -> get(result.single())
                    else -> throw IllegalStateException("More than 1 entries with teamId: $teamId and userId: $userId")
                }
            }

        fun get(userId: Long): TeamRole? =
            transaction(DataBaseParser.connection) {
                val result =
                    table.select { (TeamRoleTable.userId eq userId) }
                when (result.count()) {
                    0 -> null
                    1 -> get(result.single())
                    else -> throw IllegalStateException("More than 1 entries with userId: $userId")
                }
            }

        fun put(teamRole: TeamRole) {
            transaction(DataBaseParser.connection) {
                val result =
                    table.select { (TeamRoleTable.userId eq teamRole.userId) and (TeamRoleTable.teamId eq teamRole.teamId) }
                when (result.count()) {
                    0 -> table.insert { set(teamRole, it) }
                    1 -> table.update({ (TeamRoleTable.userId eq teamRole.userId) and (TeamRoleTable.teamId eq teamRole.teamId) }) {
                        set(
                            teamRole,
                            it
                        )
                    }

                    else -> throw IllegalStateException("More than 1 entries with teamId: ${teamRole.teamId} and userId: ${teamRole.userId}")
                }
            }
        }

        fun deleteByTeamId(teamId: Long) =
            transaction(DataBaseParser.connection) {
                val temp: Int? = null
                table.deleteWhere(offset = temp) { TeamRoleTable.teamId eq teamId }
            }

        fun deleteByTeamAndUser(userId: Long, teamId: Long) =
            transaction(DataBaseParser.connection) {
                val temp: Int? = null
                table.deleteWhere(offset = temp) { (TeamRoleTable.teamId eq teamId) and (TeamRoleTable.userId eq userId) }
            }


        fun getAllTeamsWhereUserIsAdmin(userId: Long): List<String> {
            return getAllByFilter {
                (TeamRoleTable.userId eq userId) and
                        (TeamRoleTable.role eq 3)
            }.mapNotNull { TeamDatabase.getById(it.teamId)?.name }
        }

        fun getAllNonAdminTeamMembers(teamId: Long): List<String> {
            return getAllByFilter {
                (TeamRoleTable.teamId eq teamId) and (TeamRoleTable.role neq 3)
            }.mapNotNull { DataBase.get<User>(it.userId).username }
        }

        fun getAllTeamsForUser(userId: Long): List<Long> {
            return getAllByFilter { TeamRoleTable.userId eq userId }
                .map { it.teamId }
        }

        fun getAllTeamsWhereUserCanAddHomework(userId: Long): List<String> {
            return getAllByFilter {
                (TeamRoleTable.userId eq userId) and
                        (TeamRoleTable.role neq 1)
            }
                .mapNotNull { TeamDatabase.getById(it.teamId)?.name }
        }

        override fun get(result: ResultRow): TeamRole =
            TeamRole(
                result[TeamRoleTable.userId],
                result[TeamRoleTable.teamId],
                result[TeamRoleTable.role]
            )

        override fun set(o: TeamRole, it: UpdateBuilder<Number>) {
            it[TeamRoleTable.userId] = o.userId
            it[TeamRoleTable.teamId] = o.teamId
            it[TeamRoleTable.role] = o.role
        }
    }
}