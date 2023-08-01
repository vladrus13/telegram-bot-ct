package ru.vladrus13.itmobot.plugin.homework

import com.google.inject.AbstractModule
import com.google.inject.Singleton
import com.google.inject.multibindings.ProvidesIntoMap
import com.google.inject.multibindings.StringMapKey
import com.google.inject.name.Named

class HomeworkPluginModule : AbstractModule() {
    @ProvidesIntoMap
    @StringMapKey("ДЗ")
    @Named("plugins")
    @Singleton
    fun homeworkPlugin(homeworkMenu: HomeworkMenu): HomeworkPlugin {
        return HomeworkPlugin(homeworkMenu)
    }
}