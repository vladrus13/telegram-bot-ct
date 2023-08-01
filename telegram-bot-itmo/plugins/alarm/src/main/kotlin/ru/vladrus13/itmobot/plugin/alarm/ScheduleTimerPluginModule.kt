package ru.vladrus13.itmobot.plugin.alarm

import com.google.inject.AbstractModule
import com.google.inject.multibindings.ProvidesIntoMap
import com.google.inject.multibindings.StringMapKey
import com.google.inject.name.Named
import ru.vladrus13.itmobot.tables.schedule.ScheduleRegistry

class ScheduleTimerPluginModule : AbstractModule() {
    @ProvidesIntoMap
    @Named("plugins")
    @StringMapKey("Поставщик расписания")
    fun scheduleTimerPlugin(scheduleRegistry: ScheduleRegistry): ScheduleTimerPlugin {
        return ScheduleTimerPlugin(scheduleRegistry)
    }
}