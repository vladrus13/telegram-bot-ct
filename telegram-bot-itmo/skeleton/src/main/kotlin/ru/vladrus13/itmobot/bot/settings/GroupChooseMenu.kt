package ru.vladrus13.itmobot.bot.settings

import com.google.inject.Inject
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.tables.schedule.ScheduleRegistry

class GroupChooseMenu @Inject constructor(private val scheduleRegistry: ScheduleRegistry) :
    Menu(arrayOf()) {
    override val menuHelp: String
        get() = "Пункт выбора группы"
    override val name: String
        get() = "Выбор группы"

    override fun getReplyKeyboard(user: User): ReplyKeyboard {
        val rows = ArrayList<KeyboardRow>()
        for (part in scheduleRegistry.table.getGroups().sorted().chunked(2)) {
            val row = KeyboardRow()
            row.addAll(part.map { name })
            rows.add(row)
        }
        val backRow = KeyboardRow()
        backRow.add("<< Назад")
        rows.add(backRow)
        return ReplyKeyboardMarkup(rows)
    }

    override fun onUpdate(update: Update, bot: TelegramLongPollingBot, user: User) {
        val newGroup = update.message.text
        if (scheduleRegistry.table.getGroups().contains(newGroup)) {
            user.group = newGroup
            user.path.myRemoveFromPath()
            user.send(
                bot = bot,
                text = "Успешно изменено на группу $newGroup!",
                replyKeyboard = user.path.myLast().getReplyKeyboard(user)
            )
        } else {
            user.send(
                bot = bot,
                text = "Неизвестная группа!"
            )
        }
    }

}