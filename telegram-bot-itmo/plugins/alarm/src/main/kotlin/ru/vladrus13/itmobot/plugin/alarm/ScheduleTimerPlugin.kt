package ru.vladrus13.itmobot.plugin.alarm

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ru.vladrus13.itmobot.bean.Chatted
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.database.DataBase
import ru.vladrus13.itmobot.database.DataBaseEntity
import ru.vladrus13.itmobot.database.DataBaseParser
import ru.vladrus13.itmobot.plugins.Plugin
import ru.vladrus13.itmobot.properties.InitialProperties.Companion.bot
import ru.vladrus13.itmobot.tables.schedule.ScheduleRegistry
import ru.vladrus13.itmobot.utils.TimeUtils
import ru.vladrus13.itmobot.utils.TimeUtils.Companion.getCurrentTime
import ru.vladrus13.itmobot.utils.TimeUtils.Companion.getCurrentTimeString
import java.util.*
import kotlin.reflect.KClass

class ScheduleTimerPlugin : Plugin() {
    override val name: String = "Поставщик расписания"
    override val systemName: String = "scheduleTimer"
    override val password: String? = null

    object ScheduleTimePluginTable : Table() {
        val id = long("chat_id").uniqueIndex()
    }

    class ScheduleTimerRow(
        val chatId: Long
    ) {
        constructor(user: User) : this(user.chatId)
    }

    class OnPluginChatId : DataBaseEntity<ScheduleTimerRow>() {
        override val table: Table = ScheduleTimePluginTable
        override val columnId: Column<Long> = ScheduleTimePluginTable.id

        override fun getNew(chatId: Long): ScheduleTimerRow = ScheduleTimerRow(chatId)

        override fun set(o: ScheduleTimerRow, it: UpdateBuilder<Number>) = Unit

        override fun get(result: ResultRow): ScheduleTimerRow = ScheduleTimerRow(
            result[ScheduleTimePluginTable.id],
        )

        override val name: String = "ScheduleTimerPluginChatId"
    }

    override fun getDataBases(): List<Pair<KClass<*>, DataBaseEntity<*>>> =
        listOf(Pair(ScheduleTimerRow::class, OnPluginChatId()))

    override fun init() {
        val nextDay = getCurrentTime()
        nextDay.set(Calendar.HOUR_OF_DAY, 9)
        nextDay.set(Calendar.MINUTE, 0)
        nextDay.set(Calendar.SECOND, 0)
        if (nextDay.before(GregorianCalendar(TimeZone.getTimeZone("GMT+3")))) {
            nextDay.add(Calendar.DAY_OF_YEAR, 1)
        }
        Timer().schedule(object : TimerTask() {

            fun getText(): String = """
                Автоматическая система посылки расписания!
                Сейчас: ${getCurrentTimeString()}
            """.trimIndent()

            fun onUser(it: ScheduleTimerRow) {
                val user = DataBase.get<User>(it.chatId)
                if (user.group == null) {
                    user.send(
                        bot = bot,
                        text = "${getText()}\nУ вас не выбрана группа!"
                    )
                } else {
                    // TODO прикрутить DI
                    val temp = ScheduleRegistry().table.toStringBuilder(user, user.getSubjects(), TimeUtils.getDay())
                    user.send(
                        bot = bot,
                        text = "${getText()}\n<code>${temp}</code>",
                        other = {
                            it.enableHtml(true)
                        }
                    )
                }
            }

            override fun run() {
                DataBaseParser.get<ScheduleTimerRow> { Op.TRUE }.stream()
                    .forEach(::onUser)
            }
        }, nextDay.time, 86400000)
    }

    override fun addFoldable(current: Foldable): List<Pair<Plugin, Foldable>> = arrayListOf()

    override fun onEnable(chatted: Chatted) {
        val row = when (chatted) {
            is User -> ScheduleTimerRow(chatted)
            else -> null
        }
        if (row != null) {
            DataBaseParser.set(row.chatId, row)
        }
    }

    override fun onDisable(chatted: Chatted) {
        val row = when (chatted) {
            is User -> ScheduleTimerRow(chatted)
            else -> null
        }
        if (row != null) {
            DataBaseParser.delete<ScheduleTimerRow>(row.chatId)
        }
    }
}