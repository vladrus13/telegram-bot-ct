package ru.vladrus13.itmobot.plugin.homework

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Menu
import java.util.concurrent.ConcurrentHashMap

class NewTeamCommand : Menu(arrayOf()) {
    override val menuHelp = "Создание новой команды"
    override val name = "Новая команда"

    private val enteredTeamNamesByUser = ConcurrentHashMap<Long, String>()

    override fun onEnter(bot: TelegramLongPollingBot, user: User) {
        user.send(
            bot = bot,
            text = if (!enteredTeamNamesByUser.contains(user.chatId)) {
                "Введите название вашей команды"
            } else {
                "Введите пароль для вашей команды"
            }
        )
    }

    override fun onCustomUpdate(update: Update, bot: TelegramLongPollingBot, user: User): Boolean {
        val message = update.message.text
        if (!enteredTeamNamesByUser.contains(user.chatId)) {
            if (TeamDatabase.getByName(message) != null) {
                user.send(
                    bot = bot,
                    text = "Команда с таким именем уже существует!"
                )
            } else {
                enteredTeamNamesByUser.put(user.chatId, message)
                user.send(
                    bot = bot,
                    text = "Теперь введите пароль для команды!"
                )
            }
        } else {
            val name = enteredTeamNamesByUser[user.chatId]!!
            val password = if (message.isNullOrBlank()) null else message
            if (TeamDatabase.put(name, password)) {
                user.path.returnBack()
                user.send(
                    bot = bot,
                    text = "Команда успешно создана!",
                    replyKeyboard = user.path.last().getReplyKeyboard(user)
                )
                TeamRoleDatabase.put(
                    TeamRoleDatabase.TeamRole(
                        user.chatId,
                        TeamDatabase.getByName(name)!!.id,
                        3
                    )
                )
            } else {
                user.send(
                    bot = bot,
                    text = "Команда с таким именем уже существует!"
                )
            }
        }
        return true
    }
}