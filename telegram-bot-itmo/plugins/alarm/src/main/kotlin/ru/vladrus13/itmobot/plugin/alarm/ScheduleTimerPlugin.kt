package ru.vladrus13.itmobot.plugin.alarm

import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.database.DataBaseEntity
import ru.vladrus13.itmobot.database.DataBaseParser
import ru.vladrus13.itmobot.plugins.Plugin
import ru.vladrus13.itmobot.properties.InitialProperties.Companion.bot
import ru.vladrus13.itmobot.tables.schedule.ScheduleRegistry
import ru.vladrus13.itmobot.utils.TimeUtils.Companion.getCurrentTime
import java.util.*
import kotlin.reflect.KClass

class ScheduleTimerPlugin(scheduleRegistry: ScheduleRegistry) : Plugin() {
    override val name = "Поставщик расписания"

    init {
        val nextDay = getCurrentTime()
        nextDay.set(Calendar.HOUR_OF_DAY, 9)
        nextDay.set(Calendar.MINUTE, 0)
        nextDay.set(Calendar.SECOND, 0)
        if (nextDay.before(GregorianCalendar(TimeZone.getTimeZone("GMT+3")))) {
            nextDay.add(Calendar.DAY_OF_YEAR, 1)
        }
        Timer().schedule(
            ScheduleTimerTask(scheduleRegistry, bot),
            nextDay.time,
            24 * 60 * 60 * 1000
        )
    }

    override fun getMainFoldable(): Foldable? = null

    override fun getDataBases(): List<Pair<KClass<*>, DataBaseEntity<*>>> =
        listOf(
            Pair(
                ScheduleTimeDataBaseEntity.ScheduleTimerRow::class,
                ScheduleTimeDataBaseEntity()
            )
        )

    override fun onEnable(user: User) {
        if (user.group == null) {
            user.send(
                bot = bot,
                text = "Для автоматической рассылки расписания нужно выбрать группу!"
            )
        }
        val row = ScheduleTimeDataBaseEntity.ScheduleTimerRow(user)
        DataBaseParser.set(row.chatId, row)
    }

    override fun onDisable(user: User) {
        val row = ScheduleTimeDataBaseEntity.ScheduleTimerRow(user)
        DataBaseParser.delete<ScheduleTimeDataBaseEntity.ScheduleTimerRow>(row.chatId)
    }
}