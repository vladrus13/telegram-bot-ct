package ru.vladrus13.itmobot.bot.settings

import com.google.inject.Inject
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.tables.schedule.ScheduleRegistry

class GroupChooseMenu @Inject constructor(private val scheduleRegistry: ScheduleRegistry) :
    Menu(arrayOf()) {
    override val menuHelp = "Пункт выбора группы"
    override val name = listOf("Выбор группы")

    override fun getAdditionalButtonsForReply(user: User): List<String> {
        return scheduleRegistry.table.getGroups()
    }

    override fun onUpdate(update: Update, bot: TelegramLongPollingBot, user: User) {
        val newGroup = update.message.text
        if (scheduleRegistry.table.getGroups().contains(newGroup)) {
            user.group = newGroup
            user.path.returnBack()
            user.send(
                bot = bot,
                text = "Успешно изменено на группу $newGroup!",
                replyKeyboard = user.path.last().getReplyKeyboard(user)
            )
        } else {
            user.send(
                bot = bot,
                text = "Неизвестная группа!"
            )
        }
    }
}