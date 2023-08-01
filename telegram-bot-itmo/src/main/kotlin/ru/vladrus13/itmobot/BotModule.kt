package ru.vladrus13.itmobot

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import ru.vladrus13.itmobot.bot.ItmoBot
import ru.vladrus13.itmobot.plugin.alarm.ScheduleTimerPluginModule
import ru.vladrus13.itmobot.plugin.homework.HomeworkPluginModule
import ru.vladrus13.itmobot.plugin.simple.SimplePluginModule
import ru.vladrus13.itmobot.tables.PointTablesReloader

class BotModule : AbstractModule() {
    override fun configure() {
        // TODO autodiscovery via reflection?
        install(SimplePluginModule())
        install(ScheduleTimerPluginModule())
        install(HomeworkPluginModule())
    }

    @Provides
    @Singleton
    fun itmoBot(pointTablesReloader: PointTablesReloader): ItmoBot {
        return ItmoBot(pointTablesReloader)
    }
}