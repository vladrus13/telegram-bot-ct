package ru.vladrus13.itmobot.plugin.alarm

import org.jetbrains.exposed.sql.Op
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.bot.ItmoBot
import ru.vladrus13.itmobot.database.DataBase
import ru.vladrus13.itmobot.database.DataBaseParser
import ru.vladrus13.itmobot.tables.schedule.ScheduleRegistry
import ru.vladrus13.itmobot.utils.TimeUtils
import ru.vladrus13.itmobot.utils.TimeUtils.Companion.getCurrentTimeString
import java.util.*

open class ScheduleTimerTask(
    private val scheduleRegistry: ScheduleRegistry,
    private val bot: ItmoBot
) : TimerTask() {
    override fun run() {
        DataBaseParser.get<ScheduleTimeDataBaseEntity.ScheduleTimerRow> { Op.TRUE }.stream()
            .forEach(::onUser)
    }

    fun onUser(it: ScheduleTimeDataBaseEntity.ScheduleTimerRow) {
        val user = DataBase.get<User>(it.chatId)
        if (user.group == null) {
            user.send(
                bot = bot,
                text = "${getText()}\nУ вас не выбрана группа!"
            )
        } else {
            val temp = scheduleRegistry.table.toStringBuilder(
                user,
                user.getSubjects(),
                TimeUtils.getDay()
            )
            user.send(
                bot = bot,
                text = "${getText()}\n<code>${temp}</code>",
                other = {
                    it.enableHtml(true)
                }
            )
        }
    }

    fun getText(): String = """
        Автоматическая система посылки расписания!
        Сейчас: ${getCurrentTimeString()}
    """.trimIndent()
}