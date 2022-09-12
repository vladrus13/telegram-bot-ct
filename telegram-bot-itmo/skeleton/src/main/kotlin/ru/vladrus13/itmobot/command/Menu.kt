package ru.vladrus13.itmobot.command

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.vladrus13.itmobot.bean.Chat
import ru.vladrus13.itmobot.bean.Chatted
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.bean.UserPlugins
import ru.vladrus13.itmobot.exceptions.ItmoBotException
import ru.vladrus13.itmobot.plugins.Plugin
import ru.vladrus13.itmobot.plugins.PluginsHolder
import ru.vladrus13.itmobot.utils.PathsUtils
import java.util.*

abstract class Menu(
    override val parent: Menu?
) : Foldable {

    protected abstract val childes: Array<Foldable>
    protected val pluginChildes: ArrayList<Pair<Plugin, Foldable>> = PluginsHolder.getFoldable(this)

    open fun folder(list: LinkedList<String>, userPlugins: UserPlugins): Foldable {
        if (list.isEmpty()) {
            throw IllegalStateException("Empty path in ${this::class.simpleName}")
        }
        if (list[0] == this.systemName) {
            list.pollFirst()
            if (list.isEmpty()) {
                return this
            } else {
                val first = PathsUtils.indexFromFolderSplit(list.peekFirst()!!).first()
                for (it in childes) {
                    if (it.systemName == first) {
                        return if (it is Menu) {
                            it.folder(list, userPlugins)
                        } else {
                            if (it is Command && list.size == 1) {
                                it
                            } else {
                                throw IllegalStateException("Path on command: $list in ${this::class.simpleName}")
                            }
                        }
                    }
                }
                for (it in pluginChildes) {
                    if (it.second.systemName == first) {
                        if (userPlugins.plugins.contains(it.first.systemName)) {
                            return if (it.second is Menu) {
                                (it.second as Menu).folder(list, userPlugins)
                            } else {
                                if (it.second is Command && list.size == 1) {
                                    it.second
                                } else {
                                    throw IllegalStateException("Path on command: $list in ${this::class.simpleName}")
                                }
                            }
                        } else {
                            throw ItmoBotException("Not unlock plugin")
                        }
                    }
                }
                throw IllegalStateException("Unknown path: $list in ${this::class.simpleName}")
            }
        } else {
            throw IllegalStateException("Unknown path: $list in ${this::class.simpleName}")
        }
    }

    override fun help(): String {
        val sb = StringBuilder()
        sb.append(menuHelp() + "\nКоманды:\n\n")
        for (it in childes) {
            sb.append(it.name + ": " + (if (it is Menu) it.menuHelp() else it.help()) + "\n\n")
        }
        return sb.toString()
    }

    fun groupHelp(tab: Int, userPlugins: UserPlugins): String {
        val sb = StringBuilder()
        sb.appendLine("${"=".repeat(tab)} /$path - ${menuHelp()}")
        for (fold in childes) {
            when (fold) {
                is Menu -> {
                    sb.appendLine(fold.groupHelp(tab + 1, userPlugins))
                }
                is Command -> {
                    sb.appendLine("${"=".repeat(tab + 1)}C /${fold.path} - ${fold.help()}")
                }
            }
        }
        for (plugin in pluginChildes) {
            if (userPlugins.plugins.contains(plugin.first.systemName)) {
                when (val fold = plugin.second) {
                    is Menu -> {
                        sb.appendLine(fold.groupHelp(tab + 1, userPlugins))
                    }
                    is Command -> {
                        sb.appendLine("${"=".repeat(tab + 1)}C /${fold.path}- ${fold.help()}")
                    }
                }
            }
        }
        return sb.toString()
    }

    fun standardChatHelp(update: Update, bot: TelegramLongPollingBot, chatted: Chatted, user: User): Boolean {
        return if (update.message.text.endsWith("help")) {
            chatted.send(
                bot = bot,
                text = groupHelp(0, user.getPlugins())
            )
            true
        } else {
            false
        }
    }

    fun standardCommand(update: Update, bot: TelegramLongPollingBot, user: User): Boolean {
        when (update.message.text!!) {
            "<< Назад" -> {
                if (parent != null) {
                    user.send(
                        bot = bot,
                        text = "Возвращаемся...",
                        replyKeyboard = parent!!.getReplyKeyboard(user)
                    )
                    user.path.setPath(parent!!.path)
                } else {
                    user.send(
                        bot = bot,
                        text = "Возвращаться некуда."
                    )
                }
                return true
            }
            "Помощь", "Help", "/help" -> {
                user.send(
                    bot = bot,
                    text = help()
                )
                return true
            }
        }
        return false
    }

    fun mapping(update: Update, bot: TelegramLongPollingBot, user: User): Boolean {
        for (it in childes) {
            if (it.isAccept(update)) {
                if (it is Menu) {
                    user.send(
                        bot = bot,
                        text = "Переходим в раздел...",
                        replyKeyboard = it.getReplyKeyboard(user)
                    )
                    if (it.onEnter != null) {
                        user.send(
                            bot = bot,
                            text = it.onEnter!!
                        )
                    }
                    user.path.setPath(it.path)
                }
                if (it is Command) {
                    it.get(update, bot, user)
                }
                return true
            }
        }
        val plugins = user.getPlugins()
        for (it in pluginChildes) {
            if (it.second.isAccept(update) && plugins.plugins.contains(it.first.systemName)) {
                if (it.second is Menu) {
                    user.send(
                        bot = bot,
                        text = "Переходим в раздел...",
                        replyKeyboard = (it.second as Menu).getReplyKeyboard(user)
                    )
                    user.path.setPath(it.second.path)
                }
                if (it.second is Command) {
                    it.second.get(update, bot, user)
                }
                return true
            }
        }
        return false
    }

    fun unknownCommand(bot: TelegramLongPollingBot, user: User) {
        user.send(
            bot = bot,
            text = "Неизвестная команда!"
        )
    }

    fun classicUpdate(update: Update, bot: TelegramLongPollingBot, user: User): Boolean {
        if (standardCommand(update, bot, user)) return true
        if (mapping(update, bot, user)) return true
        return false
    }

    abstract fun menuHelp(): String

    open val onEnter: String? = null

    open fun getReplyKeyboard(user: User): ReplyKeyboard {
        val replyKeyboardMarkup = ReplyKeyboardMarkup()
        val list = ArrayList<KeyboardRow>()
        for (i in childes.indices step 2) {
            val row = KeyboardRow()
            row.add(childes[i].name)
            if (i + 1 < childes.size) {
                row.add(childes[i + 1].name)
            }
            list.add(row)
        }
        val plugins = user.getPlugins()
        val pluginsNames =
            pluginChildes.filter { plugins.plugins.contains(it.first.systemName) }.map { it.second.name }
        for (i in pluginsNames.indices step 2) {
            val row = KeyboardRow()
            row.add(pluginsNames[i])
            if (i + 1 < pluginsNames.size) {
                row.add(pluginsNames[i + 1])
            }
            list.add(row)
        }
        val backRow = KeyboardRow()
        backRow.add("Помощь")
        backRow.add("<< Назад")
        list.add(backRow)
        replyKeyboardMarkup.keyboard = list
        return replyKeyboardMarkup
    }

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User, chat: Chat) {
        if (standardChatHelp(update, bot, chat, user)) return
        chat.send(
            bot = bot,
            text = "Вы пытаетесь выполнить папку("
        )
    }
}