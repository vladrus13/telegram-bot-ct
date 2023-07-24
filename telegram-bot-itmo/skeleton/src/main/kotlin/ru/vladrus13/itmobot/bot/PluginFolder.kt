package ru.vladrus13.itmobot.bot

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.vladrus13.itmobot.bean.*
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.database.DataBase
import ru.vladrus13.itmobot.plugins.PluginsHolder
import ru.vladrus13.itmobot.utils.Utils

class PluginFolder(override val parent: Menu) : Menu(parent) {
    override val childes: Array<Foldable>
        get() = arrayOf()

    override fun menuHelp(): String = "Меню плагинов"

    override val name: String = "Плагины"
    override val systemName: String = "plugins"

    override fun getReplyKeyboard(user: User): ReplyKeyboard {
        val replyKeyboardMarkup = ReplyKeyboardMarkup()
        val list = Utils.splitBy(PluginsHolder.plugins.map { it.name }, 1)
        val backRow = KeyboardRow()
        backRow.add("<< Назад")
        list.add(backRow)
        replyKeyboardMarkup.keyboard = list
        return replyKeyboardMarkup
    }

    override fun isAccept(update: Update): Boolean = update.message.text!! == name

    private fun save(
        bot: TelegramLongPollingBot,
        chatted: Chatted,
        userPlugins: UserPlugins,
        namePlugin: String
    ) {
        val plugin = PluginsHolder.getPluginByName(namePlugin)
        if (plugin != null) {
            if (userPlugins.plugins.contains(plugin.systemName)) {
                userPlugins.plugins.remove(plugin.systemName)
                chatted.send(
                    bot = bot,
                    text = "Успешно выключен плагин: ${plugin.name}"
                )
                plugin.onDisable(chatted)
            } else {
                userPlugins.plugins.add(plugin.systemName)
                chatted.send(
                    bot = bot,
                    text = "Успешно включен плагин: ${plugin.name}"
                )
                plugin.onEnable(chatted)
            }
            DataBase.put(chatted.chatId, userPlugins)
        } else {
            chatted.send(
                bot = bot,
                text = "Плагин не найден: $namePlugin"
            )
        }
    }

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return
        if (mapping(update, bot, user)) return

        val text = update.message.text!!
        val userPlugins = user.getPlugins()

        save(bot, user, userPlugins, text)
    }

}