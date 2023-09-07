package ru.vladrus13.itmobot.plugin.practice

import kotlinx.coroutines.delay
import ru.vladrus13.itmobot.bot.MainFolder
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.database.DataBaseEntity
import ru.vladrus13.itmobot.plugins.Plugin
import ru.vladrus13.itmobot.properties.InitialProperties.Companion.timeToReloadJobs
import kotlin.coroutines.cancellation.CancellationException
import kotlin.reflect.KClass

class PracticePlugin : Plugin() {
    override val name = "Таблица для практик"
    override val systemName = "practicePlugin"
    override val password: String? = null
    override val isAvailableUser = true
    override val isAvailableChat = false

    override fun getDataBases(): List<Pair<KClass<*>, DataBaseEntity<*>>> =
        listOf(Pair(SheetJob::class, SheetJobParser()))

    override suspend fun init() {
        try {
            while (true) {
                CoroutineJob.runTasks()
                delay(timeToReloadJobs)
            }

        } catch (_: CancellationException) {
        }
    }

    override fun addFoldable(current: Foldable): List<Pair<Plugin, Foldable>> {
        return when (current) {
            is MainFolder -> {
                arrayListOf(Pair(this, PracticeCommand(current)))
            }

            else -> arrayListOf()
        }
    }
}