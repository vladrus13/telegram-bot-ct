package ru.vladrus13.itmobot.tables

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.google.inject.multibindings.MapBinder
import ru.vladrus13.itmobot.tables.parsers.GoogleTwoColumnTable.GoogleTwoColumnTableFactory
import ru.vladrus13.itmobot.tables.parsers.GoshaHtmlTable.GoshaHtmlTableConstructor
import ru.vladrus13.itmobot.tables.schedule.ScheduleRegistry

class TableModule : AbstractModule() {
    override fun configure() {
        val mapBinder =
            MapBinder.newMapBinder(binder(), String::class.java, TableConstructor::class.java)
        mapBinder.addBinding("gosha-html").to(GoshaHtmlTableConstructor::class.java)
        mapBinder.addBinding("google-sheets").to(GoogleTwoColumnTableFactory::class.java)
    }

    @Provides
    @Singleton
    fun mainTableReloader(
        pointTablesRegistry: PointTablesRegistry,
        tableConstructorsMap: Map<String, TableConstructor>
    ): MainTableReloader {
        return MainTableReloader(pointTablesRegistry, tableConstructorsMap)
    }

    @Provides
    @Singleton
    fun pointTablesReloader(pointTablesRegistry: PointTablesRegistry) : PointTablesReloader {
        return PointTablesReloader(pointTablesRegistry)
    }

    @Provides
    @Singleton
    fun pointTablesRegistry(): PointTablesRegistry {
        return PointTablesRegistry()
    }

    @Provides
    @Singleton
    fun scheduleRegistry(): ScheduleRegistry {
        return ScheduleRegistry()
    }

    interface TableConstructor {
        fun construct(list: ArrayList<String>, coolDown: Long): Table
    }
}