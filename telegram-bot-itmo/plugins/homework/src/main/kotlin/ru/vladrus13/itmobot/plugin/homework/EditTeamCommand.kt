package ru.vladrus13.itmobot.plugin.homework

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.database.DataBase
import ru.vladrus13.itmobot.database.entities.UserParser

class EditTeamCommand : Menu(arrayOf()) {
    override val menuHelp = "Редактирование команды. Позволяет командам, где вы главный администратор, назначать модификаторов, которые смогут загружать файлы в эту группу"
    override val name = listOf("Редактирование команды")

    override fun getAdditionalButtonsForReply(user: User): List<String> {
        return if (user.path.getData("target") == null) {
            TeamRoleDatabase.getAllTeamsWhereUserIsAdmin(user.chatId)
        } else {
            val teamId = user.path.getData("target")!!.toLong()
            TeamRoleDatabase.getAllNonAdminTeamMembers(teamId)
        }
    }

    override fun onCustomUpdate(update: Update, bot: TelegramLongPollingBot, user: User): Boolean {
        if (user.path.getData("target") != null) {
            val teamId = user.path.getData("target")!!
            val team = TeamDatabase.getById(teamId.toLong())!!
            val users = DataBase.get<User> { UserParser.Users.username eq update.message.text }
            if (users.isEmpty()) {
                user.send(
                    bot = bot,
                    text = "Не удалось найти такого человека!"
                )
            } else {
                val target = users[0]
                val targetUserId = target.chatId
                val role = TeamRoleDatabase.get(targetUserId, team.id)
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
                    TeamRoleDatabase.put(TeamRoleDatabase.TeamRole(targetUserId, team.id, newRole))
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
            val team = TeamDatabase.getByName(teamName)
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
        return true
    }
}