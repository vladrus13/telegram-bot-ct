package ru.vladrus13.itmobot.plugin.homework

import org.jetbrains.exposed.sql.and
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.database.DataBase
import ru.vladrus13.itmobot.database.entities.UserParser
import ru.vladrus13.itmobot.utils.Utils

class EditHWTeamCommand(override val parent: Menu?) : Menu(parent) {
    override val childes: Array<Foldable> = arrayOf()

    override fun menuHelp(): String = "Редактирование команды. Позволяет командам, где вы главный администратор, назначать модификаторов, которые смогут загружать файлы в эту группу"

    override fun getReplyKeyboard(user: User): ReplyKeyboard {
        val replyKeyboardMarkup = ReplyKeyboardMarkup()
        val list = if (user.path.getData("target") == null) {
            Utils.splitBy(
                HomeworkPlugin.TeamRoleDatabase.getAllByFilter {
                    (HomeworkPlugin.TeamRoleTable.userId eq user.chatId) and
                            (HomeworkPlugin.TeamRoleTable.role eq 3)
                }
                    .mapNotNull { HomeworkPlugin.TeamDatabase.getById(it.teamId)?.name }
            )
        } else {
            val teamId = user.path.getData("target")!!.toLong()
            Utils.splitBy(
                HomeworkPlugin.TeamRoleDatabase.getAllByFilter {
                    (HomeworkPlugin.TeamRoleTable.teamId eq teamId) and
                            (HomeworkPlugin.TeamRoleTable.role neq 3)
                }
                    .mapNotNull { DataBase.get<User>(it.userId).username }
            )
        }
        val backRow = KeyboardRow()
        backRow.add("<< Назад")
        list.add(backRow)
        replyKeyboardMarkup.keyboard = list
        return replyKeyboardMarkup
    }

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return
        if (user.path.getData("target") != null) {
            val teamId = user.path.getData("target")!!
            val team = HomeworkPlugin.TeamDatabase.getById(teamId.toLong())!!
            val users = DataBase.get<User> { UserParser.Companion.Users.username eq update.message.text }
            if (users.isEmpty()) {
                user.send(
                    bot = bot,
                    text = "Не удалось найти такого человека!"
                )
            } else {
                val target = users[0]
                val targetUserId = target.chatId
                val role = HomeworkPlugin.TeamRoleDatabase.get(targetUserId, team.id)
                if (role == null) {
                    user.send(
                        bot = bot,
                        text = "Человек не состоит в вашей команде"
                    )
                } else {
                    val newRole = 3 - role.role
                    val roleText = when (newRole) {
                        1 -> "Участник"
                        2 -> "Модификатор"
                        else -> "Кто?"
                    }
                    HomeworkPlugin.TeamRoleDatabase.put(HomeworkPlugin.TeamRole(targetUserId, team.id, newRole))
                    user.send(
                        bot = bot,
                        text = "Пользователь назначен на роль \"$roleText\""
                    )
                    DataBase.get<User>(targetUserId).send(
                        bot = bot,
                        text = "Вы назначены на роль \"$roleText\" в команде ${team.name}"
                    )
                }
            }
        } else {
            val teamName = update.message.text
            val team = HomeworkPlugin.TeamDatabase.getByName(teamName)
            if (team != null) {
                user.path.setData("target", team.id.toString())
                user.send(
                    bot = bot,
                    text = "Теперь выберите участника",
                    replyKeyboard = getReplyKeyboard(user)
                )
            } else {
                user.send(
                    bot = bot,
                    text = "НЕКОрректное имя группы"
                )
            }
        }
    }

    override val name: String = "Редактирование команды"
    override val systemName: String = "editTeam"

    override fun isAccept(update: Update): Boolean = update.message.text == name
}