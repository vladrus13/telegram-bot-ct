package ru.vladrus13.itmobot.plugin.alarm

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.database.DataBaseEntity

class ScheduleTimeDataBaseEntity : DataBaseEntity<ScheduleTimeDataBaseEntity.ScheduleTimerRow>() {
    override val table: Table = ScheduleTimePluginTable
    override val columnId: Column<Long> = ScheduleTimePluginTable.id

    override fun getNew(chatId: Long): ScheduleTimerRow = ScheduleTimerRow(chatId)

    override fun set(o: ScheduleTimerRow, it: UpdateBuilder<Number>) = Unit

    override fun get(result: ResultRow): ScheduleTimerRow =
        ScheduleTimerRow(result[ScheduleTimePluginTable.id])

    override val name: String = "ScheduleTimerPluginChatId"

    object ScheduleTimePluginTable : Table() {
        val id = long("chat_id").uniqueIndex()
    }

    class ScheduleTimerRow(
        val chatId: Long
    ) {
        constructor(user: User) : this(user.chatId)
    }
}