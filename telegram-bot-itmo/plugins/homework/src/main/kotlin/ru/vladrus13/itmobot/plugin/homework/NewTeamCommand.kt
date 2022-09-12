package ru.vladrus13.itmobot.plugin.homework

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu

class NewTeamCommand(override val parent: Menu) : Menu(parent) {
    override val childes: Array<Foldable> = arrayOf()

    override fun menuHelp(): String = "Новая команда (введите название)"

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return
        val message = update.message.text
        if (user.path.getData("name") == null) {
            if (HomeworkPlugin.TeamDatabase.getByName(message) != null) {
                user.send(
                    bot = bot,
                    text = "Группа с таким именем уже существует!"
                )
            } else {
                user.path.setData("name", message)
                user.send(
                    bot = bot,
                    text = "Теперь введите пароль для группы!"
                )
            }
        } else {
            val name = user.path.getData("name")!!
            val password = if (message.isNullOrBlank()) null else message
            if (HomeworkPlugin.TeamDatabase.put(name, password)) {
                user.path.setPath(parent.path)
                user.send(
                    bot = bot,
                    text = "Группа успешно создана!",
                    replyKeyboard = parent.getReplyKeyboard(user)
                )
                HomeworkPlugin.TeamRoleDatabase.put(HomeworkPlugin.TeamRole(
                    user.chatId,
                    HomeworkPlugin.TeamDatabase.getByName(name)!!.id,
                    3
                ))
            } else {
                user.path.setPath(user.path.getPath())
                user.send(
                    bot = bot,
                    text = "Группа с таким именем уже существует!"
                )
            }
        }
    }

    override val name: String = "Новая команда (введите название)"
    override val systemName: String = "newTeam"

    override fun isAccept(update: Update): Boolean =
        update.message.text == name
}