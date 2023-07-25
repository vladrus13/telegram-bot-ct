package ru.vladrus13.itmobot.bot

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import ru.vladrus13.itmobot.tables.PointTablesRegistry

class BotModule : AbstractModule() {
    @Provides
    @Singleton
    fun itmoBot(pointTablesRegistry: PointTablesRegistry) : ItmoBot {
        return ItmoBot(pointTablesRegistry)
    }
}