package ru.vladrus13.itmobot.plugin.homework

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import ru.vladrus13.itmobot.bot.MainFolder
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.database.DataBaseEntity
import ru.vladrus13.itmobot.database.DataBaseParser
import ru.vladrus13.itmobot.plugins.Plugin
import java.lang.UnsupportedOperationException
import kotlin.reflect.KClass

class HomeworkPlugin : Plugin() {
    override val name: String = "HW плагин"
    override val systemName: String = "homework"
    override val password: String? = null
    override val isAvailableUser: Boolean = true
    override val isAvailableChat: Boolean = false

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

    object TeamDatabase : DataBaseEntity<Team>() {
        override val name: String = "HomeworkTeam"
        override val table: Table = TeamTable
        override val columnId: Column<Long> = TeamTable.id

        override fun getNew(chatId: Long): Team = throw UnsupportedOperationException()

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
                val temp : Int? = null
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

        fun getAll() : List<Team> =
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
            Team(result[TeamTable.id], result[TeamTable.name], result[TeamTable.password])

        override fun set(o: Team, it: UpdateBuilder<Number>) {
            it[TeamTable.id] = o.id
            it[TeamTable.name] = o.name
            it[TeamTable.password] = o.password
        }
    }

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

    object TeamRoleDatabase : DataBaseEntity<TeamRole>() {
        override val name: String = "HomeworkTeamRole"
        override val table: Table = TeamRoleTable
        override val columnId: Column<Long>
            get() {
                throw UnsupportedOperationException()
            }

        override fun getNew(chatId: Long): TeamRole = throw UnsupportedOperationException()

        fun getByTeamId(teamId: Long) = TeamRoleDatabase.getAllByFilter { TeamRoleTable.teamId eq teamId }

        fun get(userId: Long, teamId: Long): TeamRole? =
            transaction(DataBaseParser.connection) {
                val result = table.select { (TeamRoleTable.userId eq userId) and (TeamRoleTable.teamId eq teamId) }
                when (result.count()) {
                    0 -> null
                    1 -> get(result.single())
                    else -> throw IllegalStateException("More than 1 entries with teamId: $teamId and userId: $userId")
                }
            }

        fun put(teamRole: TeamRole) {
            transaction(DataBaseParser.connection) {
                val result =
                    table.select { (TeamRoleTable.userId eq teamRole.userId) and (TeamRoleTable.teamId eq teamRole.teamId) }
                when (result.count()) {
                    0 -> table.insert { set(teamRole, it) }
                    1 -> table.update({ (TeamRoleTable.userId eq teamRole.userId) and (TeamRoleTable.teamId eq teamRole.teamId) }) { set(teamRole, it) }
                    else -> throw IllegalStateException("More than 1 entries with teamId: ${teamRole.teamId} and userId: ${teamRole.userId}")
                }
            }
        }

        fun deleteByTeamId(teamId: Long) =
            transaction(DataBaseParser.connection) {
                val temp : Int? = null
                table.deleteWhere(offset = temp) { TeamRoleTable.teamId eq teamId }
            }

        fun deleteByTeamAndUser(userId: Long, teamId: Long) =
            transaction(DataBaseParser.connection) {
                val temp : Int? = null
                table.deleteWhere(offset = temp) { (TeamRoleTable.teamId eq teamId) and (TeamRoleTable.userId eq userId) }
            }

        override fun get(result: ResultRow): TeamRole =
            TeamRole(result[TeamRoleTable.userId], result[TeamRoleTable.teamId], result[TeamRoleTable.role])

        override fun set(o: TeamRole, it: UpdateBuilder<Number>) {
            it[TeamRoleTable.userId] = o.userId
            it[TeamRoleTable.teamId] = o.teamId
            it[TeamRoleTable.role] = o.role
        }
    }

    class TeamTask(
        val taskId: Long,
        val teamId: Long,
        val date: DateTime,
        val task: String
    )

    object TeamTaskTable : Table() {
        val taskId = long("id").uniqueIndex().autoIncrement().primaryKey()
        val teamId = long("teamId")
        val date = datetime("created").defaultExpression(CurrentDateTime())
        val task = varchar("task", 2048)
    }

    object TeamTaskDatabase : DataBaseEntity<TeamTask>() {
        override val name: String = "HomeworkTeamTask"
        override val table: Table = TeamTaskTable
        override val columnId: Column<Long> = TeamTaskTable.taskId

        override fun getNew(chatId: Long): TeamTask = throw UnsupportedOperationException()

        fun getByTeamId(teamId: Long) = getAllByFilter { TeamTaskTable.teamId eq teamId }

        fun put(teamId: Long, task: String) =
            transaction(DataBaseParser.connection) {
                table.insert {
                    it[TeamTaskTable.task] = task
                    it[TeamTaskTable.teamId] = teamId
                }
            }

        fun deleteByTeamId(teamId: Long) =
            transaction(DataBaseParser.connection) {
                val temp : Int? = null
                table.deleteWhere(offset = temp) { TeamTaskTable.teamId eq teamId }
            }

        override fun get(result: ResultRow): TeamTask =
            TeamTask(result[TeamTaskTable.taskId], result[TeamTaskTable.teamId], result[TeamTaskTable.date], result[TeamTaskTable.task])

        override fun set(o: TeamTask, it: UpdateBuilder<Number>) {
            it[TeamTaskTable.taskId] = o.teamId
            it[TeamTaskTable.taskId] = o.taskId
            it[TeamTaskTable.task] = o.task
            it[TeamTaskTable.date] = o.date
        }

    }

    override fun getDataBases(): List<Pair<KClass<*>, DataBaseEntity<*>>> = listOf(
        Pair(Team::class, TeamDatabase),
        Pair(TeamRole::class, TeamRoleDatabase),
        Pair(TeamTask::class, TeamTaskDatabase)
    )

    override fun init() {
    }

    override fun addFoldable(current: Foldable): List<Pair<Plugin, Foldable>> {
        return when (current) {
            is MainFolder -> {
                listOf(Pair(this, HomeworkCommand(current)))
            }

            else -> emptyList()
        }
    }
}