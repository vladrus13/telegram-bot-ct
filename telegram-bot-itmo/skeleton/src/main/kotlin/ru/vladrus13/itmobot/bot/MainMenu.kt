package ru.vladrus13.itmobot.bot

import com.google.inject.Inject
import com.google.inject.name.Named
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
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
    override val name: String = "main"
    override val menuHelp: String = "Главное меню."

    override fun getReplyKeyboard(user: User): ReplyKeyboard {
        val rows = ArrayList<KeyboardRow>()
        for (childPair in children.asList().chunked(2)) {
            val row = KeyboardRow()
            row.addAll(childPair.map { name })
            rows.add(row)
        }
        val availablePlugins = getAvailablePlugins(user)
        for (childPair in availablePlugins.toList().chunked(2)) {
            val row = KeyboardRow()
            row.addAll(childPair.map { name })
            rows.add(row)
        }
        val backRow = KeyboardRow()
        backRow.add("Помощь")
        backRow.add("<< Назад")
        rows.add(backRow)
        return ReplyKeyboardMarkup(rows)
    }

    private fun getAvailablePlugins(user: User) =
        user.getPlugins().plugins.intersect(plugins.values.filter { it.getMainFoldable() != null }.map { name }.toSet())

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