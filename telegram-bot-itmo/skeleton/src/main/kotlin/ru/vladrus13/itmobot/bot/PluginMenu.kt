package ru.vladrus13.itmobot.bot

import com.google.inject.Inject
import com.google.inject.name.Named
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.database.DataBase
import ru.vladrus13.itmobot.plugins.Plugin

class PluginMenu @Inject constructor(@Named("plugins") private val plugins: Map<String, Plugin>) :
    Menu(arrayOf()) {
    override val menuHelp = "Меню плагинов"
    override val name = listOf("Плагины")

    override fun getAdditionalButtonsForReply(user: User): List<String> {
        return plugins.values.flatMap { name }
    }

    override fun onCustomUpdate(update: Update, bot: TelegramLongPollingBot, user: User): Boolean {
        val pluginName = update.message.text!!
        val userPlugins = user.getPlugins()

        val plugin = plugins.get(pluginName)
        if (plugin != null) {
            if (userPlugins.plugins.contains(pluginName)) {
                userPlugins.plugins.remove(pluginName)
                user.send(
                    bot = bot,
                    text = "Успешно выключен плагин: $pluginName"
                )
                plugin.onDisable(user)
            } else {
                userPlugins.plugins.add(pluginName)
                user.send(
                    bot = bot,
                    text = "Успешно включен плагин: $pluginName"
                )
                plugin.onEnable(user)
            }
            DataBase.put(user.chatId, userPlugins)
        } else {
            user.send(
                bot = bot,
                text = "Плагин не найден: $pluginName"
            )
        }
        return true
    }
}