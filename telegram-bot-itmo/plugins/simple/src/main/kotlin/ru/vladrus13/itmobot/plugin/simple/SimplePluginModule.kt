package ru.vladrus13.itmobot.plugin.simple

import com.google.inject.AbstractModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.google.inject.multibindings.ProvidesIntoMap
import com.google.inject.multibindings.StringMapKey
import com.google.inject.name.Named

class SimplePluginModule : AbstractModule() {
    @Provides
    @Singleton
    fun pingCommand(): PingCommand {
        return PingCommand()
    }

    @ProvidesIntoMap
    @StringMapKey("Простой плагин")
    @Named("plugins")
    @Singleton
    fun simplePlugin(pingCommand: PingCommand): SimplePlugin {
        return SimplePlugin(pingCommand)
    }
}