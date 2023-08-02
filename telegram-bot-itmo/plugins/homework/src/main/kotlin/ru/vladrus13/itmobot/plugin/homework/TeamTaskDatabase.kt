package ru.vladrus13.itmobot.plugin.homework

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import ru.vladrus13.itmobot.database.DataBaseEntity
import ru.vladrus13.itmobot.database.DataBaseParser

class TeamTaskDatabase {
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

    companion object : DataBaseEntity<TeamTask>() {
        override val name: String = "HomeworkTeamTask"
        override val table: Table = TeamTaskTable
        override val columnId: Column<Long> = TeamTaskTable.taskId

        override fun getNew(chatId: Long): TeamTask = throw UnsupportedOperationException()

        fun put(teamId: Long, task: String) =
            transaction(DataBaseParser.connection) {
                table.insert {
                    it[TeamTaskTable.task] = task
                    it[TeamTaskTable.teamId] = teamId
                }
            }

        fun deleteByTeamId(teamId: Long) =
            transaction(DataBaseParser.connection) {
                val temp: Int? = null
                table.deleteWhere(offset = temp) { TeamTaskTable.teamId eq teamId }
            }

        override fun get(result: ResultRow): TeamTask =
            TeamTask(
                result[TeamTaskTable.taskId],
                result[TeamTaskTable.teamId],
                result[TeamTaskTable.date],
                result[TeamTaskTable.task]
            )

        override fun set(o: TeamTask, it: UpdateBuilder<Number>) {
            it[TeamTaskTable.taskId] = o.teamId
            it[TeamTaskTable.taskId] = o.taskId
            it[TeamTaskTable.task] = o.task
            it[TeamTaskTable.date] = o.date
        }

    }
}