package ru.vladrus13.itmobot.bot

import com.google.inject.Inject
import com.google.inject.name.Named
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.bot.results.ResultsMenu
import ru.vladrus13.itmobot.bot.schedule.ScheduleMenu
import ru.vladrus13.itmobot.bot.settings.SettingsMenu
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.plugins.Plugin

class MainMenu @Inject constructor(
    linkCommand: LinkCommand,
    scheduleMenu: ScheduleMenu,
    settingsMenu: SettingsMenu,
    pluginMenu: PluginMenu,
    startCommand: StartCommand,
    resultsMenu: ResultsMenu,
    @Named("plugins") private val plugins: Map<String, Plugin>
) : Menu(
    arrayOf(
        linkCommand,
        scheduleMenu,
        settingsMenu,
        pluginMenu,
        startCommand,
        resultsMenu
    )
) {
    override val name = listOf("main")
    override val menuHelp = "Главное меню."
    override fun getAdditionalButtonsForReply(user: User): List<String> {
        return getAvailablePlugins(user).toList()
    }

    private fun getAvailablePlugins(user: User): Set<String> =
        user.getPlugins().plugins
            .intersect(plugins.values
                .filter { it.getMainFoldable() != null }
                .map { it.name }.toSet()
            )

    override fun onCustomUpdate(update: Update, bot: TelegramLongPollingBot, user: User): Boolean {
        val text = update.message.text
        if (getAvailablePlugins(user).contains(text)) {
            val plugin = plugins.values.find { it.name == text }!!
            enterChild(plugin.getMainFoldable()!!, user, bot, update)
            return true
        }
        return false
    }
}