package ru.vladrus13.itmobot.bot.settings.group

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.vladrus13.itmobot.bean.Chat
import ru.vladrus13.itmobot.bean.Chatted
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.tables.schedule.ScheduleHolder
import ru.vladrus13.itmobot.utils.PathsUtils
import ru.vladrus13.itmobot.utils.Utils

class GroupChoose(override val parent: Menu) : Menu(parent) {
    override val childes: Array<Foldable>
        get() = arrayOf()

    override fun menuHelp(): String {
        return "Пункт выбора группы"
    }

    override fun getReplyKeyboard(user: User): ReplyKeyboard {
        val replyKeyboardMarkup = ReplyKeyboardMarkup()
        val list = Utils.splitBy(ScheduleHolder.table.getGroups().sorted())
        val backRow = KeyboardRow()
        backRow.add("<< Назад")
        list.add(backRow)
        replyKeyboardMarkup.keyboard = list
        return replyKeyboardMarkup
    }

    override val name: String
        get() = "Выбор группы"
    override val systemName: String
        get() = "chooseGroup"

    override fun isAccept(update: Update): Boolean {
        return update.message.text!! == name
    }

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return
        get(PathsUtils.MessagePath(emptyList(), listOf(update.message.text!!)), bot, user)
    }

    private fun get(message: PathsUtils.MessagePath, bot: TelegramLongPollingBot, sender: Chatted) {
        val newGroup = message.arguments[0]
        if (ScheduleHolder.table.getGroups().contains(newGroup)) {
            sender.group = newGroup
            if (sender is User) {
                sender.path = parent.path
            }
            sender.send(
                bot = bot,
                text = "Успешно изменено на группу $newGroup!",
                replyKeyboard = if (sender is User) parent.getReplyKeyboard(sender) else null
            )
        } else {
            sender.send(
                bot = bot,
                text = "Неизвестная группа!"
            )
        }
    }

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User, chat: Chat) {
        if (standardChatHelp(update, bot, chat, user)) return
        val splitted = update.message.text.split(' ')
        if (splitted.size == 1) {
            chat.send(
                bot = bot,
                text = "Нужно написать группу"
            )
            return
        }
        get(PathsUtils.MessagePath(update.message.text), bot, chat)
    }
}